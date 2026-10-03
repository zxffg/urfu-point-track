// точка входа, собирает всё вместе

import console.ConsoleHandler;
import storage.ResultExporter;
import storage.SheetsExporter;
import storage.Storage;

public static void main(String[] args) throws Exception {
    String credentialsPath = System.getenv("SHEETS_CREDENTIALS_PATH");
    String spreadsheetId = System.getenv("SHEETS_SPREADSHEET_ID");

    if (credentialsPath == null || spreadsheetId == null) {
        System.out.println("Не заданы переменные окружения SHEETS_CREDENTIALS_PATH / SHEETS_SPREADSHEET_ID");
        return;
    }

    Storage storage = new Storage();
    ResultExporter exporter = new SheetsExporter(credentialsPath, spreadsheetId);
    ConsoleHandler handler = new ConsoleHandler(storage, exporter);
    handler.run();
}
