package console;

import java.util.Optional;

public enum ModeOption {
    // enum для клавиатуры пользователя
    TERMINAL("1", "Терминал."),
    TELEGRAM_BOT("2", "Телеграм бот."),
    HYBRID("3", "Терминал и телеграм бот"),
    EXIT("0", "Выйти.");

    private final String code;
    private final String label;

    ModeOption(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode()  { return code;  }
    public String getLabel() { return label; }

    // поиск enum-значения по введённому пользователем коду
    public static Optional<ModeOption> fromCode(String code) {
        for (ModeOption option : values()) {
            if (option.code.equals(code)) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }
}
