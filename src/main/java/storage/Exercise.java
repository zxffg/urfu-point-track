package storage;

import java.time.LocalDate;

public record Exercise(String name, LocalDate deadline) {
    public Exercise {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Поле 'название' не может быть пустым.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Поле 'дедлайн' не может быть пустым.");
        }
    }
}
