package sheets;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScoreSummaryTest {
    @Test
    void teamScoreIsAddedToEveryMember() {
        List<List<Object>> rows = List.of(
                List.of("Фамилия 1", "Фамилия 2", "Фамилия 3", "Задача", "Дедлайн", "Баллы"),
                List.of("Иванов", "Петров", "", "ЛР1", "23-11-2007", 2),
                List.of("иванов", "", "", "ЛР2", "23-11-2008", "1,5")
        );
        List<List<Object>> roster = List.of(List.of("Петров"), List.of("Сидоров"));

        assertEquals(List.of(
                List.of("Студент", "Баллы"),
                List.of("Петров", 2L),
                List.of("Сидоров", 0L),
                List.of("Иванов", 3.5)
        ), ScoreSummary.build(rows, roster));
    }

    @Test
    void sameCaseStaysTheSameWhenBuiltTwice() {
        List<List<Object>> rows = List.of(List.of("Иванов", "Петров", "", "ЛР1", "23-11-2026", 2));

        List<List<Object>> first = ScoreSummary.build(rows, List.of());
        List<List<Object>> second = ScoreSummary.build(rows, List.of());

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
        ), ScoreSummary.build(rows, List.of()));
    }
}
