package storage;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubmissionTest {
    // 1. Тест на обычное создание подчинения
    @Test
    void createValid() {
        Team team = new Team(List.of("Кабаев", "Перервенко"));
        Exercise exercise = new Exercise("Как отчислить студента", LocalDate.of(2026, 9, 24));
        Submission submission = new Submission(2, team, exercise, 2.0);

        assertEquals(2, submission.id());
        assertEquals(team, submission.team());
        assertEquals(exercise, submission.exercise());
        assertEquals(2.0, submission.score());
    }

    // 2. Тест на .create
    // Пожалуй логично создать два объекта и проверить айди следующего
    @Test
    void createWithIdIncrement() {
        Team team = new Team(List.of("Кабаев", "Перервенко"));
        Exercise exercise = new Exercise("Как отчислить студента", LocalDate.of(2026, 9, 24));

        Submission first = Submission.create(team, exercise, 2.0);
        Submission second = Submission.create(team, exercise, 2.0);

        // Так как nextId - это статик поле и оно общее для всех, то
        // надо отталкиваться от фактически присвоенных значений.
        assertEquals(first.id() + 1, second.id());
    }
}

