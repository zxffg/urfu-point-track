package console;

import java.util.Optional;

public enum MenuOption {
    // enum для клавиатуры пользователя
    ADD_SUBMISSION("1", "Добавить решение."),
    SHOW_ALL("2", "Показать все решения."),
    EXIT("0", "Выйти.");

    private final String code;
    private final String label;

    MenuOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    // поиск enum-значения по введённому пользователем коду
    public static Optional<MenuOption> fromCode(String code) {
        for (MenuOption option : values()) {
            if (option.code.equals(code)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }
}
