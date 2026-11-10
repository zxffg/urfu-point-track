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
import java.util.ArrayList;
import java.util.List;

public class SheetsExporter implements ResultExporter {
    private final Sheets service;
    private final String spreadsheetId;
    private final String summarySheet;

    public SheetsExporter(String credentialsPath, String spreadsheetId) throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        this.summarySheet = System.getenv().getOrDefault("SHEETS_SUMMARY_NAME", "Sheet2");
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

    // Функция export(), которая вставляет сдачу и заново записывает итоги.
    @Override
    public void export(Submission submission) {
        // Кусок 1: три фамилии, название задачи и балл. Пустые места в команде остаются пустыми ячейками.
        List<Object> row = new ArrayList<>();
        List<String> surnames = submission.team().getSurnames();
        for (int i = 0; i < 3; i++) {
            if (i < surnames.size()) {
                row.add(surnames.get(i));
            } else {
                row.add("");
            }
        }
        row.add(submission.exercise().getName());
        row.add(submission.score());

        // Кусок 2: сначала журнал сдач на Sheet1, затем пересчёт A:B на втором листе.
        // Колонки D:E второго листа — это ручной список Telegram, его код не трогает.
        try {
            service.spreadsheets().values()
                    .append(spreadsheetId, "Sheet1!A1", new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
            rewriteSummary();
        } catch (IOException e) {
            System.out.println("Ошибка при отправке в Google Sheets: " + e.getMessage());
        }
    }

    private void rewriteSummary() throws IOException {
        List<List<Object>> summary = ScoreSummary.build(read("Sheet1!A:E"), read(quoted(summarySheet) + "!D2:D"));
        service.spreadsheets().values()
                .clear(spreadsheetId, quoted(summarySheet) + "!A:B", new ClearValuesRequest())
                .execute();
        service.spreadsheets().values()
                .update(spreadsheetId, quoted(summarySheet) + "!A1", new ValueRange().setValues(summary))
                .setValueInputOption("RAW")
                .execute();
    }

    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
        return values == null ? List.of() : values;
    }

    private static String quoted(String sheetName) {
        return "'" + sheetName.replace("'", "''") + "'";
    }
}
