package bot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import sheets.ResultExporter;

public class ProgressService {
    private final Progress progress;
    private final ResultExporter exporter;
    private final Set<Long> admins;

    public ProgressService(Progress progress, Set<Long> admins, ResultExporter exporter) {
        this.progress = progress;
        this.admins = Set.copyOf(admins);
        this.exporter = exporter;
    }

    // проверяем является ли админом
    public boolean isAdmin(long userId) {
        return admins.contains(userId);
    }

    private String normalize(String value) {
        return value.strip().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
    }

    // команды
    public String handle(long userId, String username, BotCommand command) throws IOException {
        return switch (command) {
            case START, HELP -> {
                if (!isAdmin(userId)) {
                    yield "Команды: /points - мои баллы.";
                }
                yield "Команды: /group - успеваемость группы, /recalculate - пересчитать итоги.";
            }
            case GROUP -> {
                if (!isAdmin(userId)) {
                    yield "Доступ запрещен.";
                }
                yield groupReport();
            }
            case RECALCULATE -> {
                if (!isAdmin(userId)) {
                    yield "Доступ запрещен.";
                }
                exporter.rewriteSummary();
                yield "Итоги пересчитаны.";
            }
            case POINTS -> {
                if (username == null || username.isBlank()) {
                    yield "Укажите username в настройках Telegram.";
                }
                yield ownScore(username);
            }
            case UNKNOWN -> "Неизвестная команда. Используйте /help.";
        };
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
        List<TelegramAccount> accounts = progress.readUsers().stream()
                .filter(account -> normalize(account.username()).equals(normalize(username)))
                .toList();

        if (accounts.size() > 1) {
            return "Найдены повторяющиеся записи пользователя. Обратитесь к преподавателю.";
        }
        if (accounts.isEmpty()) {
            return "Ваш username не найден в таблице. Обратитесь к преподавателю.";
        }
        String student = accounts.getFirst().surname();

        List<StudentScore> scores = progress.readProgress().stream()
                .filter(score -> score.surname().equalsIgnoreCase(student))
                .toList();
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