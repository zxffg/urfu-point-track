package storage;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TeamTest {

    // 1. Тест на простое создание списка: два участника
    @Test
    void createsWithValidSurnames() {
        Team team = new Team(List.of("Должник", "Двоешник"));
        assertEquals(2, team.getSurnames().size());
    }

    // 2. Инициализация пустым списком
    @Test
    void createsWithNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team(List.of());
        });
    }

    // 3. Инициализация с 4 людьми (можно <4)
    @Test
    void createsWithFour() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team(List.of("A", "B", "C", "D"));
        });
    }

    // 4. Список содержит
    @Test
    void createsWithOneNullSurname() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Team(List.of("", "Прогульщик"));
        });
    }

    // 5. Два пограничных теста на 1 и 3 фамилии
    @Test
    void createsWithOne() {
        Team team = new Team(List.of("Хвостовка"));
        assertEquals(1, team.getSurnames().size());
    }
    @Test
    void createsWithThree() {
        Team team = new Team(List.of("Пересдача", "Долги", "Досдать"));
        assertEquals(3, team.getSurnames().size());
    }
}
