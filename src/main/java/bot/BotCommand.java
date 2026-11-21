package bot;

import java.util.Locale;

public enum BotCommand {
    START,
    HELP,
    POINTS,
    GROUP,
    RECALCULATE,
    ADD_SUBMISSION,
    DELETE_SUBMISSION,
    CANCEL,
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
            case "добавить запись сдачи", "/add_submission" -> ADD_SUBMISSION;
            case "удалить запись", "/delete_submission" -> DELETE_SUBMISSION;
            case "отмена", "/cancel" -> CANCEL;
            default -> UNKNOWN;
        };
    }
}
