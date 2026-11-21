package bot;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import submission.Submission;
import submission.SubmissionService;

public class ProgressService {
    private final Progress progress;
    private final SubmissionService submissionService;
    private final Set<Long> admins;
    private final Map<Long, SubmissionDialog> dialogs; // активные диалоги пользователей
    private final Map<Long, List<Submission>> deletePages; // что показали при удалении
    private final Map<Long, Submission> deleteChoices;

    public ProgressService(Progress progress, Set<Long> admins, SubmissionService submissionService) {
        this.progress = progress;
        this.admins = Set.copyOf(admins);
        this.submissionService = submissionService;
        this.dialogs = new HashMap<>();
        this.deletePages = new HashMap<>();
        this.deleteChoices = new HashMap<>();
    }

    public boolean isActiveState(long userId) {
        return dialogs.containsKey(userId) || deletePages.containsKey(userId);
    }

    public boolean isAdmin(long userId) {
        return admins.contains(userId);
    }

    private String normalize(String value) {
        return value.strip().replaceFirst("^@", "").toLowerCase(Locale.ROOT);
    }

    public String handle(long userId, String username, BotCommand command) throws IOException {
        return switch (command) {
            case START, HELP -> {
                if (!isAdmin(userId)) {
                    yield "Команды: /points - мои баллы.";
                }
                yield "Команды: /group - успеваемость группы, /recalculate - пересчитать итоги, "
                        + "/add_submission - добавить запись, /delete_submission - удалить запись.";
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
                submissionService.rewriteSummary();
                yield "Итоги пересчитаны.";
            }
            case POINTS -> {
                if (username == null || username.isBlank()) {
                    yield "Укажите username в настройках Telegram.";
                }
                yield ownScore(username);
            }
            case ADD_SUBMISSION -> {
                if (!isAdmin(userId)) {
                    yield "Доступ запрещен.";
                }
                yield addSubmissionState(userId);
            }
            case DELETE_SUBMISSION -> {
                if (!isAdmin(userId)) {
                    yield "Доступ запрещен.";
                }
                yield startDelete(userId);
            }
            case CANCEL -> cancel(userId);
            case UNKNOWN -> "Неизвестная команда. Используйте /help.";
        };
    }

    // диалог с пользователем
    public String handleDialog(long userId, String text) {
        if (BotCommand.fromText(text) == BotCommand.CANCEL) {
            return cancel(userId);
        }

        SubmissionDialog dialog = dialogs.get(userId);
        if (dialog != null) {
            String answer = dialog.handle(text);
            if (dialog.isFinished()) {
                dialogs.remove(userId);
            }
            return answer;
        }

        if (deletePages.containsKey(userId)) {
            return handleDelete(userId, text);
        }

        return "Диалог не найден. Начните заново: /add_submission";
    }

    private String groupReport() throws IOException {
        StringBuilder result = new StringBuilder("Успеваемость группы:\n");
        for (StudentScore score : progress.readProgress()) {
            result.append(score.surname()).append(": ").append(format(score.score())).append('\n');
        }
        return result.toString();
    }

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

    private String addSubmissionState(long userId) {
        deletePages.remove(userId);
        deleteChoices.remove(userId);
        dialogs.put(userId, new SubmissionDialog(submissionService));
        return "Введите фамилии (от 1 до 3):";
    }

    // показываем 5 последних записей и запоминаем их для пользователя
    private String startDelete(long userId) {
        List<Submission> all = submissionService.getAllSubmissions();
        if (all.isEmpty()) {
            return "Нет записей для удаления.";
        }

        List<Submission> page = List.copyOf(all.subList(Math.max(0, all.size() - 5), all.size()));
        dialogs.remove(userId);
        deletePages.put(userId, page);
        deleteChoices.remove(userId);

        StringBuilder result = new StringBuilder("Последние записи:\n");
        for (int i = 0; i < page.size(); i++) {
            result.append(i + 1).append(") ").append(describe(page.get(i))).append('\n');
        }
        result.append("Введите номер от 1 до ").append(page.size()).append(" или «Отмена».");
        return result.toString();
    }

    private String handleDelete(long userId, String text) {
        String input = text == null ? "" : text.strip();
        Submission chosen = deleteChoices.get(userId);

        // шаг 2: подтверждение
        if (chosen != null) {
            String answer = input.toLowerCase(Locale.ROOT);
            if (answer.equals("да")) {
                deletePages.remove(userId);
                deleteChoices.remove(userId);
                try {
                    submissionService.deleteSubmission(chosen);
                    return "Запись удалена.";
                } catch (IOException | RuntimeException e) {
                    return "Ошибка удаления: " + e.getMessage();
                }
            }
            if (answer.equals("нет")) {
                deletePages.remove(userId);
                deleteChoices.remove(userId);
                return "Удаление отменено.";
            }
            return "Напишите «да» или «нет».";
        }

        // шаг 1: выбор номера
        List<Submission> page = deletePages.get(userId);
        int number;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return "Введите номер от 1 до " + page.size() + " или «Отмена».";
        }
        if (number < 1 || number > page.size()) {
            return "Введите номер от 1 до " + page.size() + " или «Отмена».";
        }

        Submission selected = page.get(number - 1);
        deleteChoices.put(userId, selected);
        return "Удалить запись " + number + ": " + describe(selected) + "?\nНапишите «да» или «нет».";
    }

    private String cancel(long userId) {
        if (dialogs.remove(userId) != null) {
            return "Добавление записи прервано.";
        }
        if (deletePages.remove(userId) != null) {
            deleteChoices.remove(userId);
            return "Удаление отменено.";
        }
        return "Нет активного добавления.";
    }

    private String describe(Submission submission) {
        return String.join(", ", submission.team().surnames())
                + " | " + submission.exercise().name()
                + " | " + format(submission.score());
    }

    private String format(double score) {
        if (Double.isFinite(score) && score % 1 == 0) {
            return Integer.toString((int) score);
        }
        return Double.toString(score);
    }
}