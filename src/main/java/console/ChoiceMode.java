package console;

import java.util.Optional;
import java.util.Scanner;

public class ChoiceMode {
    private static final Scanner scanner = new Scanner(System.in);

    // возвращает Optional<ModeOption> для Main
    public static Optional<ModeOption> choiceMode() {
        while (true) {
            printMode();
            String choice = scanner.nextLine();
            Optional<ModeOption> selected = ModeOption.fromCode(choice);

            if (selected.isEmpty()) {
                System.out.println("Неизвестная команда.\n");
                continue;
            }

            ModeOption mode = selected.get();

            // если EXIT — возвращаем пустой Optional, Main поймет, что это выход
            if (mode == ModeOption.EXIT) {
                return Optional.empty();
            }

            // Для всех остальных режимов — возвращаем сам режим
            return Optional.of(mode);
        }
    }

    // выбор режима работы
    private static void printMode() {
        for (ModeOption option : ModeOption.values()) {
            System.out.println(option.getCode() + " - " + option.getLabel());
        }
        System.out.print("Введите команду: ");
    }
}
