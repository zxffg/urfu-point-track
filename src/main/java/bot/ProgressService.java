package bot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// Логика команд не зависит от способа хранения таблицы.
public class ProgressService {
    private final ProgressRepository repository;
    private final Set<Long> admins;

    public ProgressService(ProgressRepository repository, Set<Long> admins) {
        this.repository = repository;
        this.admins = Set.copyOf(admins);
    }

    private String normalize(String value) {
        return value.strip().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
    }

    public String handle(long userId, String username, String text) throws IOException {
        String command = text.strip().split("\\s+", 2)[0].split("@", 2)[0].toLowerCase(Locale.ROOT);
        if (command.equals("/start") || command.equals("/help")) {
            return "Команды: /points - мои баллы, /group - успеваемость группы для администратора.";
        }
        if (command.equals("/group")) {
            if (!admins.contains(userId)) return "Доступ запрещен.";
            StringBuilder result = new StringBuilder("Успеваемость группы:\n");
            for (List<Object> row : repository.readProgress()) {
                if (row.size() >= 2 && !row.get(0).toString().isBlank()) {
                    result.append(row.get(0)).append(": ").append(row.get(1)).append("\n");
                }
            }
            return result.toString();
        }
        if (!command.equals("/points")) return "Неизвестная команда. Используйте /help.";
        if (username == null || username.isBlank()) return "Укажите username в настройках Telegram.";
        String student = null;
        for (List<Object> row : repository.readUsers()) {
            if (row.size() >= 2 && !row.get(0).toString().isBlank()
                    && normalize(row.get(1).toString()).equals(normalize(username))) {
                if (student != null) return "Найдены повторяющиеся записи пользователя. Обратитесь к преподавателю.";
                student = row.get(0).toString().strip();
            }
        }
        if (student == null) return "Ваш username не найден в таблице. Обратитесь к преподавателю.";
        String score = null;
        for (List<Object> row : repository.readProgress()) {
            if (row.size() >= 2 && row.get(0).toString().strip().equalsIgnoreCase(student)) {
                if (score != null) return "Найдены повторяющиеся записи студента. Обратитесь к преподавателю.";
                score = row.get(1).toString();
            }
        }
        return score == null ? "Баллы пока не найдены." : student + ": " + score;
    }
}
