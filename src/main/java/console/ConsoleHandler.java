// Первое меню общения с пользователем

package console;

import storage.*;
import submission.Submission;
import submission.SubmissionService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.time.format.DateTimeParseException;
import java.util.*;

// Класс ConsoleHandler принимает ввод пользователя и создает запись
// в таблице.
public class ConsoleHandler {
    private final SubmissionService service;
    private final Scanner scanner;

    // Для валидации score
    private static final double MAX_SCORE = 2.0;

    public ConsoleHandler(SubmissionService service) {
        this.service = service;
        this.scanner = new Scanner(System.in);
    }

    // run() функция - старт и выбор менюшки
    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine();
            Optional<MenuOption> selected = MenuOption.fromCode(choice);

            if (selected.isEmpty()) {
                System.out.println("Неизвестная команда.");
                continue;
            }

            switch (selected.get()) {
                case ADD_SUBMISSION -> addSubmission();
                case SHOW_ALL -> showAll();
                case REWRITE_SUMMARY -> rewriteSummary();
                case EXIT -> { return; }
                default -> System.out.println("Неизвестная команда.");
            }
        }
    }

    // меню
    private void printMenu() {
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.getCode() + " - " + option.getLabel());
        }
        System.out.print("Введите команду: ");
    }

    // пересчет итогов
    private void rewriteSummary() {
        try {
            service.rewriteSummary();
            System.out.println("Итоги пересчитаны.");
        } catch (IOException e) {
            System.out.println("Не удалось пересчитать итоги: " + e.getMessage());
        }
    }

    // Получение данных для метода createSubmission()
    private void addSubmission() {
        // 1. фамилии
        System.out.println("Введите фамилии (от 1 до 3):");
        String rawSurnames = scanner.nextLine().trim();
        List<String> surnames = Arrays.stream(rawSurnames.split("[,\\s]+"))
                .filter(s -> !s.isBlank())
                .toList();
        if (surnames.isEmpty() || surnames.size() > 3) {
            System.out.println("В команде должно быть от 1 до 3 человек.");
            return;
        }

        // 2. название задачи
        System.out.println("Введите название задачи:");
        String exerciseName = scanner.nextLine();
        if (exerciseName.isBlank()) {
            System.out.println("Название задачи не может быть пустым.");
            return;
        }

        // 3. дедлайн задачи
        System.out.println("Введите дедлайн задачи в формате 'DD-MM-YYYY':");
        LocalDate deadline;
        try {
            DateTimeFormatter formatter = DateTimeFormatter // форматирование дат
                    .ofPattern("dd-MM-uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);
            deadline = LocalDate.parse(scanner.nextLine().trim(), formatter);
        } catch (DateTimeParseException e) {
            System.out.println("Неверный формат. Ожжидается 'DD-MM-YYYY'.");
            return;
        }

        // 4. оценка
        System.out.println("Укажите количество баллов за задачу (от 0 до 2):");
        double score;
        try {
            String inputScore = scanner.nextLine().trim().replace(",", ".");
            score = Double.parseDouble(inputScore);
            if (score < 0 || score > MAX_SCORE) {
                System.out.println("Балл должен быть от 0 до 2.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный формат балла.");
            return;
        }

        // 5. создание Submission
        try {
            Submission submission = service.createSubmission(surnames, exerciseName, deadline, score);
            System.out.printf("Успешно создана запись: команда - %s | задание - %s | баллы - %.1f%n",
                    submission.team().surnames(), submission.exercise().name(), submission.score());
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка валидации: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Ошибка сохранения: " + e.getMessage());
        }
    }

    private void showAll() {
        List<Submission> submissions = service.getAllSubmissions();
        if (submissions.isEmpty()) {
            System.out.println("Нет ни одной записи.");
        }

        for (Submission sub : submissions) {
            List<String> teamName = sub.team().surnames();
            String exerciseName = sub.exercise().name();

            System.out.printf("ID: %d | Команда: %s | Задание: %s | Баллы: %.1f%n",
                    sub.id(), teamName, exerciseName, sub.score());
        }
    }
}