// точка входа, собирает всё вместе

import console.ConsoleHandler;
import sheets.ResultExporter;
import sheets.SheetsExporter;
import storage.Storage;
import submission.SubmissionService;

public static void main(String[] args) throws Exception {
    String credentialsPath = System.getenv("SHEETS_CREDENTIALS_PATH");
    String spreadsheetId = System.getenv("SHEETS_SPREADSHEET_ID");

    if (credentialsPath == null || spreadsheetId == null) {
        System.out.println("Не заданы переменные окружения SHEETS_CREDENTIALS_PATH / SHEETS_SPREADSHEET_ID");
        return;
    }

    Storage storage = new Storage();
    ResultExporter exporter = new SheetsExporter(credentialsPath, spreadsheetId);
    SubmissionService service = new SubmissionService(storage, exporter);
    ConsoleHandler handler = new ConsoleHandler(service);
    handler.run();
}
