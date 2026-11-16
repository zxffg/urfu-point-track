package sheets;

import org.junit.jupiter.api.Test;
import storage.Exercise;
import storage.Team;
import submission.Submission;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreSummaryTest {

    // 1. Проверяем, что баллы за командную работу начисляются каждому участнику команды
    @Test
    void teamScoreIsAddedToEveryMember() {
        Exercise ex1 = new Exercise("ЛР1", LocalDate.of(2007, 11, 23));
        Exercise ex2 = new Exercise("ЛР2", LocalDate.of(2008, 11, 23));

        List<Submission> submissions = List.of(
                new Submission(1, new Team(List.of("Иванов", "Петров")), ex1, 2.0),
                new Submission(2, new Team(List.of("иванов")), ex2, 1.5)
        );

        List<String> roster = List.of("Петров", "Сидоров");

        List<StudentScore> result = ScoreCalculator.build(submissions, roster);

        // Петров набрал 2.0 (был в ЛР1)
        // Сидоров набрал 0.0 (есть в списке группы, но ничего не сдал)
        // Иванов набрал 3.5 (2.0 за ЛР1 + 1.5 за ЛР2, и добавлен, хотя его нет в ростере)
        assertEquals(List.of(
                new StudentScore("Петров", 2.0),
                new StudentScore("Сидоров", 0.0),
                new StudentScore("Иванов", 3.5)
        ), result);
    }

    // 2. Проверяем детерминированность: два одинаковых вызова возвращают идентичный результат
    @Test
    void sameCaseStaysTheSameWhenBuiltTwice() {
        Exercise ex = new Exercise("ЛР1", LocalDate.of(2026, 11, 23));
        List<Submission> submissions = List.of(
                new Submission(1, new Team(List.of("Иванов", "Петров")), ex, 2.0)
        );

        List<StudentScore> first = ScoreCalculator.build(submissions, List.of());
        List<StudentScore> second = ScoreCalculator.build(submissions, List.of());

        assertEquals(List.of(
                new StudentScore("Иванов", 2.0),
                new StudentScore("Петров", 2.0)
        ), first);
        assertEquals(first, second);
    }

    // 3. Одиночная сдача корректно считается
    @Test
    void singleSubmissionCountedCorrectly() {
        Exercise ex = new Exercise("ЛР1", LocalDate.of(2025, 11, 23));
        List<Submission> submissions = List.of(
                new Submission(1, new Team(List.of("Иванов")), ex, 2.0)
        );

        assertEquals(List.of(
                new StudentScore("Иванов", 2.0)
        ), ScoreCalculator.build(submissions, List.of()));
    }
}