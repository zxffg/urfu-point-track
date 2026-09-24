// Первое меню общения с пользователем

package console;

import storage.Exercise;
import storage.Storage;
import storage.Submission;
import storage.Team;

import java.security.DrbgParameters;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsoleHandler {
    // Поля
    private final Storage storage;
    private final Scanner scanner;

    // Конструктор
    public ConsoleHandler(Storage storage) {
        this.storage = storage;
        this.scanner = new Scanner(System.in);
    }

    // run() функция - старт и выбор менюшки
    public void run() {
        while (true) {
            printMenu();
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> addSubmission();
                case "2" -> showAll();
                case "0" -> { return; }
                default -> System.out.println("Неизвестная команда.");
            }
        }
    }

    // Использует getAll() из Storage.java
    private void showAll() {
        List<Submission> submissions = storage.getAll();
        if (submissions.isEmpty()) {
            System.out.println("Нет ни одной записи.");
        }

        for (Submission sub : submissions) {
            System.out.println(sub);
        }
    }

    // Меню
    private void printMenu() {
        System.out.println("1 - Добавить решение.");
        System.out.println("2 - Показать все решения.");
        System.out.println("0 - Выйти.");
    }

    // Метод добавления Submission
    // Фамилии -> Задания -> Баллы
    private void addSubmission() {
        System.out.println("Введите фамилии (до 3, для завершения ввода раньше — /q):");

        // Команда
        List<String> surnames = new ArrayList<>();
        while (surnames.size() < 3) {
            String input = scanner.nextLine();
            if (input.equals("/q")) {
                break; // пользователь решил закончить раньше
            }
            surnames.add(input);
        }

        // Отсюда и ниже try-catch стоит на разных блоках
        // и в каждом свое личное сообщение об ошибке - проще дебажить
        Team team;
        try {
            team = new Team(surnames);
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка создания объекта Team: " + e.getMessage());
            return;
        }

        // Ввод задания
        System.out.println("Введите название задачи:");
        String name = scanner.nextLine();

        System.out.println("Введите дедлайн задачи в формате 'DD-MM-YYYY':");
        LocalDate deadline = null;
        try {
            deadline = LocalDate.parse(scanner.nextLine(), DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (DateTimeParseException e) {
            System.out.println("Ошибка парсинга deadline: " + e.getMessage());
            return;
        }

        if (name.isBlank()) {
            System.out.println("Название задачи не может быть пустым.");
            return;
        }
        Exercise exercise = storage.findOrCreateEx(name, deadline);


        // Создание Submission
        System.out.println("Укажите количество баллов за задачу:");
        double score;
        try {
            score = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Ошибка парсинга double: " + e.getMessage());
            return;
        }
        Submission newSubmission = Submission.create(team, exercise, score);

        storage.add(newSubmission);
        System.out.println("Добавлена запись: " + newSubmission);
    }
}
