package bot;

public enum BotCommand {
    START("/start", "/start"),
    HELP("/help", "/help"),
    HELP_SHORT("/h", "/help"),
    POINTS("мои баллы", "/points"),
    POINTS_SLASH("/points", "/points"),
    GROUP("разбаловка группы", "/group"),
    GROUP_SLASH("/group", "/group");

    private final String commandName;    // что прислал пользователь (кнопка или текст)
    private final String serviceCommand; // то, что ждет progressService

    BotCommand(String commandName, String serviceCommand) {
        this.commandName = commandName;
        this.serviceCommand = serviceCommand;
    }

    public String getServiceCommand() {
        return serviceCommand;
    }

    public static BotCommand fromString(String text) {
        String cleanText = text.strip().toLowerCase();
        for (BotCommand cmd : values()) {
            if (cmd.commandName.equalsIgnoreCase(cleanText)) {
                return cmd;
            }
        }
        return null;
    }
}
