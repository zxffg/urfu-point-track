// точка входа, собирает всё вместе

import console.ConsoleHandler;
import storage.Storage;

public class Main {
    public static void main(String[] args) {
        Storage storage = new Storage();
        ConsoleHandler consoleRunner = new ConsoleHandler(storage);
        consoleRunner.run();
    }
}
