// точка входа, собирает всё вместе

import console.ConsoleRunner;
import dialog.DialogEngine;

public class Main {
    public static void main(String[] args) {
        DialogEngine dialogEngine = new DialogEngine();
        ConsoleRunner consoleRunner = new ConsoleRunner(dialogEngine);
        consoleRunner.run();
    }
}
