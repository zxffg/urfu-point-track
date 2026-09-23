package storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

// Класс задачи
public class Exercise {
    // Поля
    private final String name;
    // Тут же валидация для дат
    private LocalDate deadline = LocalDate.parse("25-12-2026", DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    // Еще можно добавить поле maxscore? Хотя везде максимум 2 балла

    // Конструктор
    Exercise(String name, LocalDate deadline) {
        this.name = name;
        this.deadline = deadline;
    }

    // Геттеры
    public String getName() { return name; }
    public LocalDate getDeadline() { return deadline; }
}
