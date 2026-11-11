package sheets;

import bot.Progress;
import bot.StudentScore;
import bot.TelegramAccount;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

// Читает сводку и пользователей со второго листа.
public class SheetsProgressRepository implements Progress {
    private final Sheets service;
    private final String spreadsheetId;
    private final String sheetName;

    public SheetsProgressRepository(String credentialsPath, String spreadsheetId, String sheetName)
            throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        this.sheetName = "'" + sheetName.replace("'", "''") + "'";
        GoogleCredentials credentials;
        try (FileInputStream input = new FileInputStream(credentialsPath)) {
            credentials = GoogleCredentials.fromStream(input)
                    .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets.readonly"));
        }
        service = new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(), new HttpCredentialsAdapter(credentials))
                .setApplicationName("urfu-point-track").build();
    }

    // функция read возвращает List<List<Object>> потому что читает таблицу именно в таком формате
    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, sheetName + range)
                .execute().getValues();
        return values == null ? List.of() : values;
    }

    // читаем фамлии и общий результат ↓↓↓↓ после парсим телеграм аккаунты
    public List<StudentScore> readProgress() throws IOException {
        List<List<Object>> rows = read("!A2:B");
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
        List<List<Object>> rows = read("!D2:E");
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

    private double score(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return Double.parseDouble(value.toString().strip().replace(',', '.'));
    }
}