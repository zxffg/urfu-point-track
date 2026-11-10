package bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

// Логика команд не зависит от способа хранения таблицы.
public class ProgressService {
    private final Progress progress;
    private final Set<Long> admins;

    public ProgressService(Progress progress, Set<Long> admins) {
        this.progress = progress;
        this.admins = Set.copyOf(admins);
    }

    private String normalize(String value) {
        return value.strip().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
    }

    // шаблон функции поиска по условию
    private <T> List<T> matches(List<T> items, Predicate<T> predicate) {
        List<T> found = new ArrayList<>();
        for (T item : items) {
            if (predicate.test(item)) {
                found.add(item);
            }
        }
        return found;
    }

    // разбираем полученную команду и уходим в ветвление
    public String handle(long userId, String username, String text) throws IOException {
        String command = text.strip().split("\\s+", 2)[0].split("@", 2)[0].toLowerCase(Locale.ROOT);
        if (command.equals("/start") || command.equals("/help")) {
            return "Команды: /points - мои баллы, /group - успеваемость группы для администратора.";
        }
        if (command.equals("/group")) {
            if (!admins.contains(userId)) {
                return "Доступ запрещен.";
            }
            return groupReport();
        }
        if (!command.equals("/points")) {
            return "Неизвестная команда. Используйте /help.";
        }
        if (username == null || username.isBlank()) {
            return "Укажите username в настройках Telegram.";
        }
        return ownScore(username);
    }

    // отчет по всей группе
    private String groupReport() throws IOException {
        StringBuilder result = new StringBuilder("Успеваемость группы:\n");
        for (StudentScore score : progress.readProgress()) {
            result.append(score.surname()).append(": ").append(format(score.score())).append('\n');
        }
        return result.toString();
    }

    // личный результат учащегося
    private String ownScore(String username) throws IOException {
        List<TelegramAccount> accounts = matches(
                progress.readUsers(),
                account -> normalize(account.username()).equals(normalize(username))
        );
        if (accounts.size() > 1) {
            return "Найдены повторяющиеся записи пользователя. Обратитесь к преподавателю.";
        }
        if (accounts.isEmpty()) {
            return "Ваш username не найден в таблице. Обратитесь к преподавателю.";
        }
        String student = accounts.getFirst().surname();

        List<StudentScore> scores = matches(
                progress.readProgress(),
                score -> score.surname().equalsIgnoreCase(student)
        );
        if (scores.size() > 1) {
            return "Найдены повторяющиеся записи студента. Обратитесь к преподавателю.";
        }
        if (scores.isEmpty()) {
            return "Баллы пока не найдены.";
        }
        StudentScore score = scores.getFirst();
        return score.surname() + ": " + format(score.score());
    }

    // функция переводит double в красивый string
    private String format(double score) {
        if (Double.isFinite(score) && score % 1 == 0) {
            return Integer.toString((int) score); // возвращаем число без дробной части
        }
        return Double.toString(score); // c дробной частью
    }
}