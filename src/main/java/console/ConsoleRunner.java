// почти что совмещение всех классов вместе.

package console;

import dialog.DialogEngine;

import java.util.Scanner;

public class ConsoleRunner {
    // поля
    private final DialogEngine dialogEngine;
    private static final String STATIC_ID = "console-user";

    // конструктор
    public ConsoleRunner(DialogEngine dialogEngine) {
        this.dialogEngine = dialogEngine;
    }

    // метод который принимает данные от пользователя и печатает в консоль
    public void run() {
        System.out.println(
                "Привет! Я бот-опросник. \n" +
                "Помогу проверить знания, пройти тест или просто весело провести время! Готов к первому вопросу?" + "\n" +
                "Возникли трудности? Справка /help или /h"
        );
        Scanner sc = new Scanner(System.in);
        while (true) {
            String message = sc.nextLine();

            // прерывание цикла
            if (message.equals("/q")) break;

            System.out.println(dialogEngine.handleMessage(STATIC_ID, message));
        }
    }

}
