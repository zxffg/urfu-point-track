package sheets;

import org.junit.jupiter.api.Test;
import storage.Exercise;
import storage.Team;
import submission.Submission;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreSummaryTest {
    @Test
    void teamScoreIsAddedToEveryMember() {
        List<Submission> rows = (
            new Submission(1, new Team(List.of("Фамилия 1", "Фамилия 2", "Фамииля 3")), new Exercise("ЛР1", LocalDate.of(2007, 11, 23)), 0),
            new Submission(1, new Team(List.of("Иванов", "Петров", "")), new Exercise("ЛР1", LocalDate.of(2007, 11, 23)), 2),
            new Submission(1, new Team(List.of("иванов", "", "")), new Exercise("ЛР2", LocalDate.of(2008, 11, 23)), 1.5),
        );
        List<List<Object>> roster = List.of(List.of("Петров"), List.of("Сидоров"));

        assertEquals(List.of(
                List.of("Студент", "Баллы"),
                List.of("Петров", 2L),
                List.of("Сидоров", 0L),
                List.of("Иванов", 3.5)
        ), ScoreCalculator.build(rows, roster));
    }

    @Test
    void sameCaseStaysTheSameWhenBuiltTwice() {
        List<List<Object>> rows = List.of(List.of("Иванов", "Петров", "", "ЛР1", "23-11-2026", 2));

        List<List<Object>> first = ScoreCalculator.build(rows, List.of());
        List<List<Object>> second = ScoreCalculator.build(rows, List.of());

        assertEquals(List.of(
                List.of("Студент", "Баллы"),
                List.of("Иванов", 2L),
                List.of("Петров", 2L)
        ), first);
        assertEquals(first, second);
    }

    @Test
    void sameSurnameInOneRowIsCountedOnce() {
        List<List<Object>> rows = List.of(List.of("Иванов", "Иванов", "", "ЛР1",  "23-11-2025", 2));

        assertEquals(List.of(
                List.of("Студент", "Баллы"),
                List.of("Иванов", 2L)
        ), ScoreCalculator.build(rows, List.of()));
    }
}
