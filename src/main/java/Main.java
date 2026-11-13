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

        switch (runMode.get()) {
            case TERMINAL -> startTerminal(credentialsPath, spreadsheetId);
            case TELEGRAM_BOT -> startTelegramBot(credentialsPath, spreadsheetId, botToken);
            case HYBRID -> startHybrid(credentialsPath, spreadsheetId, botToken);
        }
    }

    // запускаем терминал
    private static void startTerminal(String credentialsPath, String spreadsheetId) {
        if (credentialsPath == null || spreadsheetId == null) {
            System.out.println("Не заданы SHEETS_CREDENTIALS_PATH / SHEETS_SPREADSHEET_ID");
            return;
        }
        try {
            Storage storage = new Storage();
            ResultExporter exporter = new SheetsExporter(credentialsPath, spreadsheetId);
            SubmissionService service = new SubmissionService(storage, exporter);
            new ConsoleHandler(service).run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // запускаем бота
    private static void startTelegramBot(String credentialsPath, String spreadsheetId, String botToken) {
        if (!botConfigured(credentialsPath, spreadsheetId, botToken)) { // проверяем конфигурацию
            return;
        }
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) { // создаем бота
            // регистрируем <имя> бота передаем ему токен и сводку с результатами
            botsApplication.registerBot(botToken, new PointBot(botToken, progressService(credentialsPath, spreadsheetId)));
            Thread.currentThread().join(); // оставляем поток живым
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startHybrid(String credentialsPath, String spreadsheetId, String botToken) {
        if (!botConfigured(credentialsPath, spreadsheetId, botToken)) { // конфигурация
            return;
        }
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new PointBot(botToken, progressService(credentialsPath, spreadsheetId)));
            // пускаем бота и следом за ним терминал
            startTerminal(credentialsPath, spreadsheetId);
            /* здесь не нужен Thread.currentThread().join(), так как метод .join() ставит основной поток
            * на паузу и программа не заверщится помле первого опроса пула. а здесь за удержание потока
            * отвечает .run() в ConsoleHandler */
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean botConfigured(String credentialsPath, String spreadsheetId, String botToken) {
        if (credentialsPath == null || spreadsheetId == null || botToken == null || botToken.isBlank()) {
            System.out.println("Для бота нужны SHEETS_CREDENTIALS_PATH, SHEETS_SPREADSHEET_ID и TELEGRAM_BOT_TOKEN");
            return false;
        }
        return true;
    }

    private static ProgressService progressService(String credentialsPath, String spreadsheetId) throws Exception {
        String sheet = SheetLayout.SUMMARY_SHEET;
        String adminIds = System.getenv().getOrDefault("TELEGRAM_ADMIN_IDS", "");
        Set<Long> admins = Arrays.stream(adminIds.split(","))
                .map(String::strip)
                .filter(value -> !value.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toSet());
        return new ProgressService(
                new SheetsProgressRepository(credentialsPath, spreadsheetId, sheet),
                admins,
                new SheetsExporter(credentialsPath, spreadsheetId)
        );
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
        /* оастальная настрока происходит рукчками:
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
