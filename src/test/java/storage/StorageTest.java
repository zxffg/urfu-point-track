package storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

public class StorageTest {

    // sut - переменная
    Team team = new Team(List.of("Кабаев", "Перервенко"));
    Exercise exercise = new Exercise("Как отчислить студента", LocalDate.of(2026, 9, 24));
    Submission submission = new Submission(2, team, exercise, 2.0);
    private Storage storage = new Storage();

    // 1. .add() и проверка что .getAll() содержит первую запись
    @Test
    void addIntoStorage() {
        storage.add(submission);
        assertTrue(storage.getAll().contains(submission));
    }

    // 2. Попробовать изменить получаемую коллекцию
    @Test
    void throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            storage.add(submission);
            storage.getAll().add(submission);
        });
    }

    // 3. Получение существующего ID
    @Test
    void getValidId() {
        storage.add(submission);
        assertEquals(submission, storage.getByID(2));
    }

    // 4. Получение несуществующего ID
    @Test
    void getNonValidId() {
        assertThrows(NoSuchElementException.class, () -> {
            storage.getByID(9999);
        });
    }

    // Ниже проверка функции findOrCreateEx()
    private String name = "Как отчислить студента";
    private LocalDate deadline = LocalDate.of(2026, 9, 24);

    // 5. Задания с таким именем нет -> создается новое -> возвращается
    @Test
    void newExercise() {
        Exercise result = storage.findOrCreateEx(name, deadline);
        assertEquals(name, result.getName());
        assertEquals(deadline, result.getDeadline());
    }

    // 6. Регистронезависимость (есть проверка с equalsIgnoreCase())
    @Test
    void alreadyExist() {
        String nameTwo = "КАК ОТЧИСЛИТЬ СТУДЕНТА";
        Exercise first = storage.findOrCreateEx(name, deadline);
        Exercise second = storage.findOrCreateEx(nameTwo, deadline);
        // Сравнивает идентичность двух чуваков
        assertSame(first, second);
    }

    // 7. пустое поле с дедлайном
    @Test
    void nullDeadline() {
        assertThrows(IllegalArgumentException.class, () -> {
            LocalDate date = null;
            storage.findOrCreateEx(name, date);
        });
    }

    // 8. Пустое поле с именем
    @Test
    void nullName() {
        assertThrows(IllegalArgumentException.class, () -> {
            String nameTmp = null;
            storage.findOrCreateEx(nameTmp, deadline);
        });
    }
}
