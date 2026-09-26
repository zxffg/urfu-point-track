// Подключение апи и форматирование вода в методе export()

package storage;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

public class SheetsExporter implements ResultExporter {
    // Поля
    private final Sheets service;
    private final String spreadsheetId;

    // Конструктор
    public SheetsExporter(String credentialsPath, String spreadsheetId) throws IOException, GeneralSecurityException {
        this.spreadsheetId = spreadsheetId;
        GoogleCredentials credentials = GoogleCredentials
                .fromStream(new FileInputStream(credentialsPath))
                .createScoped(List.of("https://www.googleapis.com/auth/spreadsheets"));
        this.service = new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("urfu-point-track")
                .build();
    }

    // Функция export(), которая вставляет данные в таблицу.
    @Override
    public void export(Submission submission) {

        // Кусок 1: собирает фамилии из метода .getSurnames()
        // Если фамилий нет, то она (функ.) заполняет пропуск пробелом.
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

        // Кусок 2: отправляет все в Google Sheets в блоке try-catch, для
        // перехвата ошибок
        try {
            service.spreadsheets().values()
                    .append(spreadsheetId, "Sheet1!A1", new ValueRange().setValues(List.of(row)))
                    .setValueInputOption("USER_ENTERED")
                    .execute();
        } catch (IOException e) {
            System.out.println("Ошибка при отправке в Google Sheets: " + e.getMessage());
        }
    }
}
