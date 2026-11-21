// Подключение апи и форматирование ввода в методе export()

package sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import submission.Submission;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SheetsExporter implements ResultExporter {
    private final Sheets service;
    private final String spreadsheetId;
    private final SheetsProgressRepository repository;
    private int cachedSheetId = -1;

    public SheetsExporter(String credentialsPath, String spreadsheetId) throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        GoogleCredentials credentials;
        try (FileInputStream input = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(input)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));
        }
        this.service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("urfu-point-track")
                .build();
        this.repository = new SheetsProgressRepository(credentialsPath, spreadsheetId, SheetLayout.SUMMARY_SHEET);
    }

    // функция export(), которая вставляет сдачу и заново записывает итоги.
    @Override
    public void export(Submission submission) throws IOException {
        // кусок 1: три фамилии, название задачи и балл. Пустые места в команде остаются пустыми ячейками.
        List<Object> row = new ArrayList<>();
        List<String> surnames = submission.team().surnames();
        for (int i = 0; i < SheetLayout.TEAM_SIZE; i++) {
            if (i < surnames.size()) {
                row.add(surnames.get(i));
            } else {
                row.add("");
            }
        }
        row.add(submission.exercise().name());
        LocalDate deadline = submission.exercise().deadline();
        if (deadline != null) {
            String formattedDate = deadline.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            row.add(formattedDate);
        } else {
            row.add(""); // на случай, если дедлайна почему-то нет
        }
        row.add(submission.score());

        // кусок 2: сначала журнал сдач на Sheet1, затем пересчёт A:B на втором листе.
        // колонки D:E второго листа — это ручной список Telegram, его код не трогает.
        // деление ошибок при успешном добавлении записи и проваленом обновлении итогов и наоборот
        String appendRange = SheetLayout.range(
                SheetLayout.SUBMISSION_SHEET,
                SheetLayout.SUBMISSIONS_APPEND
        );

        try {
            service.spreadsheets().values()
                    .append(spreadsheetId, appendRange, new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException e) {
            throw new IOException(
                    "Не удалось подтвердить добавление записи. "
                            + "Проверьте Sheet1 перед повтором.",
                    e
            );
        }

        try {
            rewriteSummary();
        } catch (IOException e) {
            throw new IOException(
                    "Запись добавлена в Sheet1, но итоги не обновлены. "
                            + "Запустите пересчёт итогов, не добавляя запись повторно.",
                    e
            );
        }
    }

    // удаляет строку из журнала сдач. Итоги пересчитывает SubmissionService.
    @Override
    public void deleteSubmission(Submission submission) throws IOException {
        List<List<Object>> rows = read(
                SheetLayout.range(SheetLayout.SUBMISSION_SHEET, SheetLayout.SUBMISSIONS_READ));
        int row = findRow(rows, submission);
        if (row < 0) {
            throw new IOException("Запись не найдена в Sheet1. Обновите список и повторите.");
        }

        DeleteDimensionRequest delete = new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                        .setSheetId(submissionSheetId())
                        .setDimension("ROWS")
                        .setStartIndex(row)
                        .setEndIndex(row + 1));

        try {
            service.spreadsheets().batchUpdate(spreadsheetId,
                            new BatchUpdateSpreadsheetRequest()
                                    .setRequests(List.of(new Request().setDeleteDimension(delete))))
                    .execute();
        } catch (IOException e) {
            throw new IOException("Не удалось удалить запись из Sheet1. Проверьте таблицу.", e);
        }
    }

    @Override
    public void rewriteSummary() throws IOException {
        String rosterRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_ROSTER);

        List<List<Object>> rosterRows = read(rosterRange);
        List<String> students = rosterRows.stream()
                .filter(row -> !row.isEmpty())
                .map(row -> row.get(0).toString().strip())
                .filter(name -> !name.isBlank())
                .toList();
        List<Submission> submissions = repository.loadAllSubmissions();

        List<StudentScore> summary = ScoreCalculator.build(submissions, students);
        List<List<Object>> sheetRows = new ArrayList<>();
        sheetRows.add(List.of("Студент", "Баллы"));
        for (StudentScore item : summary) {
            Object formatted = (item.score() % 1 == 0) ? (long) item.score().doubleValue() : item.score();
            sheetRows.add(List.of(item.surname(), formatted));
        }

        String clearRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_CLEAR);
        service.spreadsheets().values()
                .clear(spreadsheetId, clearRange, new ClearValuesRequest())
                .execute();

        String writeRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_WRITE);
        service.spreadsheets().values()
                .update(spreadsheetId, writeRange, new ValueRange().setValues(sheetRows))
                .setValueInputOption("RAW")
                .execute();
    }

    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
        return values == null ? List.of() : values;
    }

    // ищем снизу: последние записи лежат в конце листа, заголовок пропускаем
    private int findRow(List<List<Object>> rows, Submission target) {
        Set<String> team = surnames(target.team().surnames());
        for (int i = rows.size() - 1; i >= 1; i--) {
            List<Object> row = rows.get(i);
            if (row.size() < 6) {
                continue;
            }
            if (cell(row, 3).equalsIgnoreCase(target.exercise().name())
                    && surnames(row).equals(team)) {
                return i;
            }
        }
        return -1;
    }

    private Set<String> surnames(List<?> values) {
        Set<String> result = new HashSet<>();
        for (int i = 0; i < Math.min(SheetLayout.TEAM_SIZE, values.size()); i++) {
            Object value = values.get(i);
            if (value == null) {
                continue;
            }
            String surname = value.toString().strip();
            if (!surname.isBlank()) {
                result.add(surname.toLowerCase(Locale.ROOT));
            }
        }
        return result;
    }

    private String cell(List<?> row, int index) {
        if (index >= row.size() || row.get(index) == null) {
            return "";
        }
        return row.get(index).toString().strip();
    }

    private int submissionSheetId() throws IOException {
        if (cachedSheetId >= 0) {
            return cachedSheetId;
        }
        Spreadsheet spreadsheet = service.spreadsheets().get(spreadsheetId).execute();
        for (Sheet sheet : spreadsheet.getSheets()) {
            if (SheetLayout.SUBMISSION_SHEET.equals(sheet.getProperties().getTitle())) {
                cachedSheetId = sheet.getProperties().getSheetId();
                return cachedSheetId;
            }
        }
        throw new IOException("Лист " + SheetLayout.SUBMISSION_SHEET + " не найден.");
    }
}
