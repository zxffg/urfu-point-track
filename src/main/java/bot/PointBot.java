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

    public PointBot(String botToken, ProgressService progressService) {
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

        BotCommand command = BotCommand.fromString(message.getText());
        String answerText;
        if (command == null) {
            answerText = "Неизвестная команда. Используйте /help.";
        } else {
            try {
                answerText = progressService.handle(userId, username, command.getServiceCommand());
            } catch (Exception e) {
                answerText = "Ошибка при обращении к таблице данных.";
                e.printStackTrace();
            }
        }

        try {
            send(chatId, answerText, keyboard(admin));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ReplyKeyboardMarkup keyboard(boolean admin) {
        KeyboardRow actions = new KeyboardRow("Мои баллы");
        if (admin) {
            actions.add("Разбаловка группы");
        }
        return ReplyKeyboardMarkup.builder()
                .keyboardRow(actions)
                .keyboardRow(new KeyboardRow("/help"))
                .resizeKeyboard(true)
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
