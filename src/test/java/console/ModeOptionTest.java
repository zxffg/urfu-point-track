package console;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModeOptionTest {

    // 1. Возврат валидного значения
    @ParameterizedTest
    @EnumSource(MenuOption.class)
    void shouldReturnModeOptionForValidCode(MenuOption expectedOption) {
        String code = expectedOption.getCode();

        Optional<MenuOption> result = MenuOption.fromCode(code);

        assertTrue(result.isPresent(), "Ожидалось непустое значение для кода: " + code);
        assertEquals(expectedOption, result.get());
    }

    // 2. Несуществующие коды
    @ParameterizedTest
    @ValueSource(strings = {"ZERO", "ZERODOWN", "  "})
    void shouldReturnModeOptionForInvalidCode(String unknownCode) {
        Optional<MenuOption> result = MenuOption.fromCode(unknownCode);

        assertTrue(result.isEmpty());
    }

    // 3. Пустой код
    @ParameterizedTest
    @NullAndEmptySource
    void ShouldReturnNullOrEmpty(String invalidCode) {
        Optional<MenuOption> result = MenuOption.fromCode(invalidCode);

        assertTrue(result.isEmpty());
    }
}