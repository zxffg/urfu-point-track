package storage;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExerciseTest {

    // 1. Валидный тест на обычное создание
    @Test
    void createValid() {
        Exercise exercise = new Exercise("Как поймать должника", LocalDate.of(2026, 9, 24));
        assertEquals("Как поймать должника", exercise.getName());
        assertEquals(LocalDate.of(2026, 9, 24), exercise.getDeadline());
    }

    // 2. Создание с пустым именем
    @Test
    void createNoneName() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Exercise("", LocalDate.of(2026, 9, 24));
        });
    }

    // 3. Создание с пустой датой
    @Test
    void createNoneDate() {
        assertThrows(IllegalArgumentException.class, () -> {
            LocalDate noli = null;
            new Exercise("50 пересдач", noli);
        });
    }

}
