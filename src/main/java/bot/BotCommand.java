package bot;

import java.util.Locale;

public enum BotCommand {
    START,
    HELP,
    POINTS,
    GROUP,
    RECALCULATE,
    UNKNOWN;

    public static BotCommand fromText(String text) {
        if (text == null || text.isBlank()) {
            return UNKNOWN;
        }

        String clean = text.strip().toLowerCase(Locale.ROOT);

        return switch (clean) {
            case "/start" -> START;
            case "/help", "/h" -> HELP;
            case "мои баллы", "/points" -> POINTS;
            case "разбаловка группы", "/group" -> GROUP;
            case "пересчитать итоги", "/recalculate" -> RECALCULATE;
            default -> UNKNOWN;
        };
    }
}
