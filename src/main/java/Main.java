import bot.PointBot;
import console.ChoiceMode;
import console.ConsoleHandler;
import console.ModeOption;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import sheets.ResultExporter;
import sheets.SheetsExporter;
import storage.Storage;
import submission.SubmissionService;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        String credentialsPath = System.getenv("SHEETS_CREDENTIALS_PATH");
        String spreadsheetId = System.getenv("SHEETS_SPREADSHEET_ID");
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");

        // вызываем опрос из пакета console
        Optional<ModeOption> runMode = ChoiceMode.choiceMode();

        // empty - значит пользователь выбрал EXIT
        if (runMode.isEmpty()) {
            System.exit(0);
        }

        // обрабатываем запуск конкретного
        switch (runMode.get()) {
            case TERMINAL -> startTerminal(credentialsPath, spreadsheetId);
            case TELEGRAM_BOT -> startTelegramBot(botToken);
            case HYBRID -> startHybrid();
        }
    }

    // запускаем терминал
    private static void startTerminal(String credentialsPath, String spreadsheetId) {
        try {
            Storage storage = new Storage();
            ResultExporter exporter = new SheetsExporter(credentialsPath, spreadsheetId);
            SubmissionService service = new SubmissionService(storage, exporter);
            ConsoleHandler handler = new ConsoleHandler(service);
            handler.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // запускаем ботика
    private static void startTelegramBot(String botToken) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new PointBot(botToken));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHybrid() {
        // запуск обоих сервисов
    }
}