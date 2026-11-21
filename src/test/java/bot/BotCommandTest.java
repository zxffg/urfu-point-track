package bot;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BotCommandTest {

    // 1. Маппинг команд
    @Test
    void commandsMapping() {
        assertEquals(BotCommand.START, BotCommand.fromText("/start"));

        assertEquals(BotCommand.HELP, BotCommand.fromText("/help"));
        assertEquals(BotCommand.HELP, BotCommand.fromText("/h"));

        assertEquals(BotCommand.POINTS, BotCommand.fromText("мои баллы"));
        assertEquals(BotCommand.POINTS, BotCommand.fromText("/points"));

        assertEquals(BotCommand.GROUP, BotCommand.fromText("разбаловка группы"));
        assertEquals(BotCommand.GROUP, BotCommand.fromText("/group"));

        assertEquals(BotCommand.RECALCULATE, BotCommand.fromText("пересчитать итоги"));
        assertEquals(BotCommand.RECALCULATE, BotCommand.fromText("/recalculate"));

        assertEquals(BotCommand.ADD_SUBMISSION, BotCommand.fromText("добавить запись сдачи"));
        assertEquals(BotCommand.ADD_SUBMISSION, BotCommand.fromText("/add_submission"));

        assertEquals(BotCommand.DELETE_SUBMISSION, BotCommand.fromText("удалить запись"));
        assertEquals(BotCommand.DELETE_SUBMISSION, BotCommand.fromText("/delete_submission"));

        assertEquals(BotCommand.CANCEL, BotCommand.fromText("отмена"));
        assertEquals(BotCommand.CANCEL, BotCommand.fromText("/cancel"));

        assertEquals(BotCommand.UNKNOWN, BotCommand.fromText(""));
    }
}
