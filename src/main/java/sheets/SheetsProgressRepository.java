package sheets;

import bot.Progress;
import bot.StudentScore;
import bot.TelegramAccount;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import storage.Exercise;
import storage.Team;
import submission.Submission;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static sheets.SheetLayout.SUMMARY_SCORES;
import static sheets.SheetLayout.SUMMARY_SHEET;
import static sheets.SheetLayout.SUMMARY_USERS;
import static sheets.SheetLayout.range;

// читает сводку, пользователей и записи сдач
public class SheetsProgressRepository implements Progress {
    private final Sheets service;
    private final String spreadsheetId;
    private final String sheetName;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public SheetsProgressRepository(String credentialsPath, String spreadsheetId, String sheetName)
            throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        this.sheetName = sheetName;
        GoogleCredentials credentials;
        try (FileInputStream input = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(input)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly"));
        }
        service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("urfu-point-track").build();
    }

    // read возвращает List<List<Object>> потому что читает таблицу именно в таком формате
    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, range)
                .execute().getValues();
        return values == null ? List.of() : values;
    }

    // читаем фамлии и общий результат ↓↓↓↓ после парсим телеграм аккаунты
    public List<StudentScore> readProgress() throws IOException {
        List<List<Object>> rows = read(range(sheetName, SUMMARY_SCORES));
        List<StudentScore> scores = new ArrayList<>();
        // читаем строки и создаем запись
        for (List<Object> row : rows) {
            if (row.size() >= 2 && !row.get(0).toString().isBlank()) {
                String name = row.get(0).toString().strip();
                scores.add(new StudentScore(name, score(row.get(1))));
            }
        }
        return scores;
    }

    public List<TelegramAccount> readUsers() throws IOException {
        List<List<Object>> rows = read(range(sheetName, SUMMARY_USERS));
        List<TelegramAccount> accounts = new ArrayList<>();

        for (List<Object> row : rows) {
            if (row.size() >= 2) {
                String surname = row.get(0).toString();
                String username = row.get(1).toString();

                accounts.add(new TelegramAccount(surname, username));
            }
        }
        return accounts;
    }

    public List<Submission> loadAllSubmissions() throws IOException {
        // получает данные
        String range = SheetLayout.range(SheetLayout.SUBMISSION_SHEET, SheetLayout.SUBMISSIONS);
        List<List<Object>> rows = read(range);

        // единый submission создаваемый при запуске
        List<Submission> submissions = new ArrayList<>();
        int idCounter = 1;
        for (List<Object> row : rows) {
            // проверка для записей: должна быть минимум одна фамилия и кол-во столбцов =6
            if (row.size() < 6 || row.get(0).toString().isBlank()) {
                continue;
            }

            // собираем фамилии
            List<String> surnames = new ArrayList<>();
            for (int i = 0; i < SheetLayout.TEAM_SIZE; ++i) {
                if (i < row.size()) {
                    String surname = row.get(i).toString().strip();
                    if (!surname.isBlank()) {
                        surnames.add(surname);
                    }
                }
            }
            if (surnames.isEmpty()) continue;

            // собираем задачу
            String exerciseName = row.get(3).toString().strip();

            // дедлайн
            LocalDate deadline;
            try {
                deadline = LocalDate.parse(row.get(4).toString().strip(), formatter);
            } catch (Exception e) {
                deadline = LocalDate.now(); // если дата повреждена
            }

            // баллы
            Double score = score(row.get(5));

            submissions.add(new Submission(
                    idCounter++,
                    new Team(surnames),
                    new Exercise(exerciseName, deadline),
                    score
            ));
        }
        return submissions;
    }

    private double score(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString().strip().replace(',', '.'));
    }
}