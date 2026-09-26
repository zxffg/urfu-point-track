package storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

// Класс задачи
public class Exercise {
    // Поля
    private final String name;
    private final LocalDate deadline;
    // Еще можно добавить поле maxscore? Хотя везде максимум 2 балла

    // Конструктор
    // Еще: решено было добавить валидацию и сюда:
    Exercise(String name, LocalDate deadline) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Поле 'имя' не может быть пустым.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Дедлайн не может быть пустым.");
        }
        this.name = name;
        this.deadline = deadline;
    }

    // Геттеры
    public String getName() { return name; }
    public LocalDate getDeadline() { return deadline; }
}
