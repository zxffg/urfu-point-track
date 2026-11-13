// Подключение апи и форматирование ввода в методе export()

package sheets;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
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
import java.util.List;

public class SheetsExporter implements ResultExporter {
    private final Sheets service;
    private final String spreadsheetId;

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
    }

    // функция export(), которая вставляет сдачу и заново записывает итоги.
    @Override
    public void export(Submission submission) throws IOException {
        // кусок 1: три фамилии, название задачи и балл. Пустые места в команде остаются пустыми ячейками.
        List<Object> row = new ArrayList<>();
        List<String> surnames = submission.team().getSurnames();
        for (int i = 0; i < SheetLayout.TEAM_SIZE; i++) {
            if (i < surnames.size()) {
                row.add(surnames.get(i));
            } else {
                row.add("");
            }
        }
        row.add(submission.exercise().getName());
        LocalDate deadline = submission.exercise().getDeadline();
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

    @Override
    public void rewriteSummary() throws IOException {
        String submissionsRange = SheetLayout.range(SheetLayout.SUBMISSION_SHEET, SheetLayout.SUBMISSIONS_READ);
        String rosterRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_ROSTER);
        List<List<Object>> summary = ScoreSummary.build(read(submissionsRange), read(rosterRange));

        String clearRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_CLEAR);
        service.spreadsheets().values()
                .clear(spreadsheetId, clearRange, new ClearValuesRequest())
                .execute();

        String writeRange = SheetLayout.range(SheetLayout.SUMMARY_SHEET, SheetLayout.SUMMARY_WRITE);
        service.spreadsheets().values()
                .update(spreadsheetId, writeRange, new ValueRange().setValues(summary))
                .setValueInputOption("RAW")
                .execute();
    }

    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
        return values == null ? List.of() : values;
    }
}
