import bot.PointBot;
import bot.ProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import console.ChoiceMode;
import console.ConsoleHandler;
import console.ModeOption;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import sheets.ResultExporter;
import sheets.SheetLayout;
import sheets.SheetsExporter;
import sheets.SheetsProgressRepository;
import storage.Storage;
import submission.SubmissionService;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        String credentialsPath = System.getenv("SHEETS_CREDENTIALS_PATH");
        String spreadsheetId = System.getenv("SHEETS_SPREADSHEET_ID");
        String botToken = System.getenv("TELEGRAM_BOT_TOKEN");

        Optional<ModeOption> runMode = ChoiceMode.choiceMode();
        if (runMode.isEmpty()) {
            return;
        }

        ModeOption mode = runMode.get();
        if (credentialsPath == null || spreadsheetId == null) {
            System.out.println("Не заданы SHEETS_CREDENTIALS_PATH / SHEETS_SPREADSHEET_ID");
            return;
        }
        if (mode != ModeOption.TERMINAL && (botToken == null || botToken.isBlank())) {
            System.out.println("Для бота нужен TELEGRAM_BOT_TOKEN");
            return;
        }

        try {
            SheetsProgressRepository repository = new SheetsProgressRepository(
                    credentialsPath, spreadsheetId, SheetLayout.SUMMARY_SHEET);

            Storage storage = new Storage();
            storage.init(repository.loadAllSubmissions());

            ResultExporter exporter = new SheetsExporter(credentialsPath, spreadsheetId);
            SubmissionService submissionService = new SubmissionService(storage, exporter);
            ProgressService progressService = new ProgressService(repository, admins(), submissionService);

            switch (mode) {
                case TERMINAL -> new ConsoleHandler(submissionService).run();
                case TELEGRAM_BOT -> startTelegramBot(botToken, progressService);
                case HYBRID -> startHybrid(botToken, progressService, submissionService);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startTelegramBot(String botToken, ProgressService progressService) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new PointBot(botToken, progressService));
            Thread.currentThread().join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHybrid(String botToken, ProgressService progressService, SubmissionService submissionService) {
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new PointBot(botToken, progressService));
            new ConsoleHandler(submissionService).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Set<Long> admins() {
        String adminIds = System.getenv().getOrDefault("TELEGRAM_ADMIN_IDS", "");
        return Arrays.stream(adminIds.split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toSet());
    }

    private static TelegramClient telegramProxy(String botToken) {
        // 1. настраиваем прокси
        String proxyHost = "";
        int proxyPort = 0;
        String proxyUser = "";
        String proxyPass = "";

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .proxy(proxy)
                .proxyAuthenticator((route, response) -> {
                    String credential = Credentials.basic(proxyUser, proxyPass); // с авторизацией
                    return response.request().newBuilder()
                            .header("Proxy-Authorization", credential)
                            .build();
                })
                .build();
        // 2. отдаем TelegramClient с этим прокси
        return new OkHttpTelegramClient(okHttpClient, botToken);
        /* оастальная настрока происходит руками:
         3. передаем okHttpClient в TelegramBotsLongPollingApplication (для получения сообщений)
         Вместо `TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()`
         делаем так TelegramBotsLongPollingApplication(ObjectMapper::new, () -> okHttpClient);

         и в pointBot вместо botToken передаем telegramClient (то, что возвращает эта функция и в

         Дополнительно надо изменить конструктор PointBot на этот:
         public PointBot(TelegramClient telegramClient, ProgressService progressService) {
            this.telegramClient = telegramClient;
            this.progressService = progressService;
         }
         */
    }
}
