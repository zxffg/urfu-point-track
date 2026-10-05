// Первое меню общения с пользователем

package console;

import storage.*;
import submission.Submission;
import submission.SubmissionService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

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
                case EXIT -> { return; }
                default -> System.out.println("Неизвестная команда.");
            }
        }
    }

    // Меню
    private void printMenu() {
        for (MenuOption option : MenuOption.values()) {
            System.out.println(option.getCode() + " - " + option.getLabel());
        }
    }

    // Получение данных для метода createSubmission()
    private void addSubmission() {
        System.out.println("Введите фамилии (до 3, для завершения ввода раньше — /q):");

        // Получение фамилий для команды
        List<String> surnames = new ArrayList<>();
        while (surnames.size() < 3) {
            String input = scanner.nextLine();
            if (input.equals("/q")) {
                break; // пользователь решил закончить раньше
            }
            surnames.add(input);
        }

        // Название задачи
        System.out.println("Введите название задачи:");
        String exerciseName = scanner.nextLine();
        if (exerciseName.isBlank()) {
            System.out.println("Название задачи не может быть пустым.");
            return;
        }

        // Дедлайн задачи
        System.out.println("Введите дедлайн задачи в формате 'DD-MM-YYYY':");
        LocalDate deadline = null;
        try {
            deadline = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("Ошибка парсинга deadline: " + e.getMessage());
            return;
        }

        // Получение результата
        System.out.println("Укажите количество баллов за задачу:");
        double score;
        try {
            score = Double.parseDouble(scanner.nextLine());
            // Тут макс. число баллов было просто хардкодом, для простоты сделана константа.
            if (score < 0 || score > MAX_SCORE) {
                System.out.println("Минимальное число баллов 0, а максимальное 2.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка парсинга double: " + e.getMessage());
            return;
        }

        // Создание Submission + добавление в Storage и SheetsAPI
        try {
            Submission returnedSubmission = service.createSubmission(surnames, exerciseName, deadline, score);
            System.out.println("Добавлена запись: " + returnedSubmission);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания записи: " + e.getMessage());
        }
    }

    private void showAll() {
        List<Submission> submissions = service.getAllSubmissions();
        if (submissions.isEmpty()) {
            System.out.println("Нет ни одной записи.");
        }

        for (Submission sub : submissions) {
            // красивый и понятный вывод
            List<String> teamName = sub.team().getSurnames();
            String exerciseName = sub.exercise().getName();

            System.out.printf("ID: %d | Команда: %s | Задание: %s | Баллы: %.1f%n",
                    sub.id(), teamName, exerciseName, sub.score());
        }
    }
}