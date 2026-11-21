package bot;

import submission.Submission;
import submission.SubmissionService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// пошаговое добавление записи. логика та же, что в ConsoleHandler.addSubmission(),
public class SubmissionDialog {
    private static final double MAX_SCORE = 2.0;
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter
            .ofPattern("dd-MM-uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    private final SubmissionService service;

    private DialogState state = DialogState.SURNAMES;
    private List<String> surnames;
    private String exerciseName;
    private LocalDate deadline;
    private double score;
    private boolean finished;

    public SubmissionDialog(SubmissionService service) {
        this.service = service;
    }

    public boolean isFinished() {
        return finished;
    }

    DialogState state() {
        return state;
    }

    public String handle(String text) {
        String input = text == null ? "" : text.strip();
        return switch (state) {
            case SURNAMES -> handleSurnames(input);
            case EXERCISE_NAME -> handleExerciseName(input);
            case DEADLINE -> handleDeadline(input);
            case SCORE -> handleScore(input);
            case APPROVING -> handleApproving(input);
        };
    }

    private String handleSurnames(String input) {
        List<String> parsed = Arrays.stream(input.split("[,\\s]+"))
                .filter(value -> !value.isBlank())
                .toList();
        if (parsed.isEmpty() || parsed.size() > 3) {
            return "В команде должно быть от 1 до 3 человек. Введите фамилии ещё раз:";
        }
        surnames = parsed;
        state = DialogState.EXERCISE_NAME;
        return "Введите название задачи:";
    }

    private String handleExerciseName(String input) {
        if (input.isBlank()) {
            return "Название задачи не может быть пустым. Введите название задачи ещё раз:";
        }
        exerciseName = input;
        state = DialogState.DEADLINE;
        return "Введите дедлайн задачи в формате 'DD-MM-YYYY':";
    }

    private String handleDeadline(String input) {
        try {
            deadline = LocalDate.parse(input, DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            return "Неверный формат. Ожидается 'DD-MM-YYYY'. Введите дедлайн ещё раз:";
        }
        state = DialogState.SCORE;
        return "Укажите количество баллов за задачу (от 0 до 2):";
    }

    private String handleScore(String input) {
        double parsed;
        try {
            parsed = Double.parseDouble(input.replace(",", "."));
        } catch (NumberFormatException e) {
            return "Некорректный формат балла. Введите число от 0 до 2:";
        }
        if (!Double.isFinite(parsed) || parsed < 0 || parsed > MAX_SCORE) {
            return "Балл должен быть от 0 до 2. Введите балл ещё раз:";
        }
        score = parsed;
        state = DialogState.APPROVING;
        return "Проверьте запись:\n"
                + "Команда: " + String.join(", ", surnames) + "\n"
                + "Задание: " + exerciseName + "\n"
                + "Дедлайн: " + deadline.format(DEADLINE_FORMAT) + "\n"
                + "Баллы: " + String.format(Locale.ROOT, "%.1f", score) + "\n"
                + "Подтвердить? Напишите 'да' или 'нет'.";
    }

    private String handleApproving(String input) {
        String answer = input.toLowerCase(Locale.ROOT);
        if (answer.equals("да") || answer.equals("yes")) {
            return create();
        }
        if (answer.equals("нет") || answer.equals("no")) {
            finished = true;
            return "Добавление отменено.";
        }
        return "Напишите 'да' или 'нет'.";
    }

    private String create() {
        try {
            Submission submission = service.createSubmission(surnames, exerciseName, deadline, score);
            finished = true;
            return "Успешно создана запись: команда - " + submission.team().surnames()
                    + " | задание - " + submission.exercise().name()
                    + " | баллы - " + String.format(Locale.ROOT, "%.1f", submission.score());
        } catch (IllegalArgumentException e) {
            finished = true;
            return "Ошибка валидации: " + e.getMessage();
        } catch (IOException e) {
            finished = true;
            return "Ошибка сохранения: " + e.getMessage();
        }
    }
}