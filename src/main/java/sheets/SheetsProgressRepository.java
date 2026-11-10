package sheets;

import bot.ProgressRepository;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

// Читает сводку и пользователей со второго листа.
public class SheetsProgressRepository implements ProgressRepository {
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

    private List<List<Object>> read(String range) throws IOException {
        List<List<Object>> values = service.spreadsheets().values().get(spreadsheetId, sheetName + range)
                .execute().getValues();
        return values == null ? List.of() : values;
    }

    public List<List<Object>> readProgress() throws IOException { return read("!A2:B"); }
    public List<List<Object>> readUsers() throws IOException { return read("!D2:E"); }
}