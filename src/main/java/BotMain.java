import bot.ProgressService;
import bot.TelegramBot;
import sheets.SheetsProgressRepository;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

// Отдельная точка входа для бота.
public class BotMain {
    public static void main(String[] args) throws Exception {
        String credentials = required("SHEETS_CREDENTIALS_PATH");
        String spreadsheet = required("SHEETS_SPREADSHEET_ID");
        String token = required("TELEGRAM_BOT_TOKEN");
        String adminIds = System.getenv().getOrDefault("TELEGRAM_ADMIN_IDS", "");
        Set<Long> admins = Arrays.stream(adminIds.split(","))
                .map(String::strip).filter(s -> !s.isEmpty()).map(Long::valueOf).collect(Collectors.toSet());
        String host = System.getenv("TELEGRAM_PROXY_HOST");
        int port = host == null || host.isBlank() ? 0 : Integer.parseInt(required("TELEGRAM_PROXY_PORT"));
        String sheet = System.getenv().getOrDefault("SHEETS_SUMMARY_NAME", "Sheet2");
        ProgressService service = new ProgressService(new SheetsProgressRepository(credentials, spreadsheet, sheet), admins);
        new TelegramBot(token, host, port, service).run();
    }

    private static String required(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Не задана переменная " + name);
        return value;
    }
}
