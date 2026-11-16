package storage;

import org.junit.jupiter.api.Test;
import submission.Submission;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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

    // Та же команда и то же задание, даже в другом порядке фамилий, второй раз не пишутся.
    @Test
    void rejectsDuplicateTeamAndExercise() {
        storage.add(submission);
        Team reversed = new Team(List.of("Перервенко", "Кабаев"));
        Exercise sameTask = new Exercise("КАК ОТЧИСЛИТЬ СТУДЕНТА", LocalDate.of(2026, 10, 1));
        Submission duplicate = new Submission(9, reversed, sameTask, 1.0);
        assertThrows(IllegalArgumentException.class, () -> storage.add(duplicate));
        assertEquals(1, storage.getAll().size());
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

    // 5. Регистронезависимость (есть проверка с equalsIgnoreCase())
    @Test
    void alreadyExist() {
        storage.add(submission);
        Optional<Exercise> found = storage.findExercise("КАК ОТЧИСЛИТЬ СТУДЕНТА");
        assertTrue(found.isPresent());
        assertSame(exercise, found.get());
    }

    @Test
    void findExerciseReturnsEmptyWhenNotFound() {
        Optional<Exercise> found = storage.findExercise("Несуществующая задача");
        assertTrue(found.isEmpty());
    }

    // 6. init следующий id
    @Test
    void maxIdInInit() {
        List<Submission> initialData = List.of(new Submission(1, team, exercise, 2.0), new Submission(2, team, exercise, 1.0));
        storage.init(initialData);

        assertEquals(initialData.size() + 1, storage.nextId());
    }

    // 6. init нулевой id
    @Test
    void zeroIdInit() {
        List<Submission> initialData = new ArrayList<>();
        storage.init(initialData);

        assertEquals(initialData.size() + 1, storage.nextId());
    }
}
