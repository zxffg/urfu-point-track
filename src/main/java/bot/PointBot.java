package bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class PointBot implements LongPollingSingleThreadUpdateConsumer {
    private static final int MESSAGE_LIMIT = 4000; // лимит сообщений

    private final TelegramClient telegramClient;   // клиент: через него на сервер идут сообщения
    private final ProgressService progressService; // сводная таблица с результатами и админы

    public PointBot(String botToken, ProgressService progressService) { // ПРОКСИ: это старый варинт
        this.telegramClient = new OkHttpTelegramClient(botToken);
        this.progressService = progressService;
    }

    @Override
    public void consume(Update update) { // опрашиваем сервер / отвечаем на сообщения
        if (!update.hasMessage() || !update.getMessage().hasText() || update.getMessage().getFrom() == null) {
            return;
        }
        // тг чат имеет свой тип, если это не личный чат то лив
        if (!"private".equals(update.getMessage().getChat().getType())) {
            return;
        }

        // получаем данные для отправки сообщений
        var message = update.getMessage();
        long chatId = message.getChatId();
        long userId = message.getFrom().getId();
        String username = message.getFrom().getUserName();
        boolean admin = progressService.isAdmin(userId);

        String answerText;
        BotCommand command = BotCommand.fromText(message.getText());
        // проверяем нет ли активных диалогов
        // если есть, то текст передаем в состояния
        if (progressService.isActiveState(userId)) {
            try {
                answerText = progressService.handleDialog(userId, message.getText());
            } catch (Exception e) {
                answerText = "Ошибка состояния.";
            }
        } else {
            try {
                answerText = progressService.handle(userId, username, command);
            } catch (Exception e) {
                answerText = "Ошибка при обращении к таблице данных.";
                e.printStackTrace();
            }
        }

        try {
            send(chatId, answerText, keyboard(admin, progressService.isActiveState(userId)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // реплай клавиатуры
    private ReplyKeyboardMarkup keyboard(boolean admin, boolean dialogActive) {
        var builder = ReplyKeyboardMarkup.builder().resizeKeyboard(true);
        if (dialogActive) {
            return builder
                    .keyboardRow(new KeyboardRow("Отмена"))
                    .build();
        }

        if (admin) {
            return builder
                    .keyboardRow(new KeyboardRow("Добавить запись сдачи"))
                    .keyboardRow(new KeyboardRow("Удалить запись"))
                    .keyboardRow(new KeyboardRow("Разбаловка группы", "Пересчитать итоги"))
                    .keyboardRow(new KeyboardRow("/help"))
                    .build();
        }
        return builder
                .keyboardRow(new KeyboardRow("Мои баллы"))
                .keyboardRow(new KeyboardRow("/help"))
                .build();
    }

    private void send(long chatId, String text, ReplyKeyboardMarkup keyboard) throws Exception {
        int offset = 0;
        boolean first = true;
        while (offset < text.length()) { // если сообщение выходит за пределы допустимого лимита, переносим его в следующее
            int end = Math.min(text.length(), offset + MESSAGE_LIMIT);
            SendMessage.SendMessageBuilder<?, ?> builder = SendMessage.builder()
                    .chatId(chatId)
                    .text(text.substring(offset, end));
            if (first) {
                builder.replyMarkup(keyboard);
                first = false;
            }
            telegramClient.execute(builder.build());
            offset = end;
        }
    }
}
