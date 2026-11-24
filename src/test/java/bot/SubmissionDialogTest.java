package bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.Storage;
import submission.Submission;
import submission.SubmissionService;
import console.FakeExporter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionDialogTest {
    private Storage storage;
    private FakeExporter exporter;
    private SubmissionDialog dialog;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        exporter = new FakeExporter();
        dialog = new SubmissionDialog(new SubmissionService(storage, exporter));
    }

    private String fillUntilApproving() {
        dialog.handle("Иванов, Петров");
        dialog.handle("ООП");
        dialog.handle("24-09-2026");
        return dialog.handle("2");
    }

    // 1. диалог начинается с ввода фамилий?
    @Test
    void startsAtSurnamesAndAdvances() {
        assertEquals(DialogState.SURNAMES, dialog.state());

        String answer = dialog.handle("Иванов, Петров");

        assertTrue(answer.contains("название задачи"));
        assertEquals(DialogState.EXERCISE_NAME, dialog.state());
        assertFalse(dialog.isFinished());
    }

    // 2. падение при неверном вводе фамилий
    @Test
    void invalidSurnamesKeepState() {
        dialog.handle("");
        assertEquals(DialogState.SURNAMES, dialog.state());

        dialog.handle("А, Б, В, Г");
        assertEquals(DialogState.SURNAMES, dialog.state());
        assertFalse(dialog.isFinished());

        dialog.handle("Иванов");
        assertEquals(DialogState.EXERCISE_NAME, dialog.state());
    }

    // 3. падение с неверным созданием Exercise
    @Test
    void blankExerciseKeepsState() {
        dialog.handle("Иванов");

        assertTrue(dialog.handle("   ").contains("не может быть пустым"));
        assertEquals(DialogState.EXERCISE_NAME, dialog.state());

        dialog.handle("ООП");
        assertEquals(DialogState.DEADLINE, dialog.state());
    }

    // 4. падение с неверным дедлайном
    @Test
    void invalidDeadlineKeepsState() {
        dialog.handle("Иванов");
        dialog.handle("ООП");

        for (String value : List.of("2026-09-24", "31-02-2026", "1-1-2026")) {
            assertTrue(dialog.handle(value).contains("Неверный формат"));
            assertEquals(DialogState.DEADLINE, dialog.state());
        }

        dialog.handle("29-02-2024");
        assertEquals(DialogState.SCORE, dialog.state());
    }

    // 5. падение с неверными баллами
    @Test
    void invalidScoreKeepsState() {
        dialog.handle("Иванов");
        dialog.handle("ООП");
        dialog.handle("24-09-2026");

        for (String value : List.of("abc", "-1", "2.1", "NaN")) {
            dialog.handle(value);
            assertEquals(DialogState.SCORE, dialog.state());
        }

        dialog.handle("1,5");
        assertEquals(DialogState.APPROVING, dialog.state());
    }

    // 6. содержит ли данные
    @Test
    void summaryContainsEnteredData() {
        String summary = fillUntilApproving();

        assertEquals(DialogState.APPROVING, dialog.state());
        assertTrue(summary.contains("Иванов, Петров"));
        assertTrue(summary.contains("ООП"));
        assertTrue(summary.contains("24-09-2026"));
        assertTrue(summary.contains("2.0"));
        assertTrue(summary.contains("Подтвердить"));
    }

    // 7. успешное создание
    @Test
    void yesCreatesSubmissionAndFinishes() {
        fillUntilApproving();

        String answer = dialog.handle("да");

        assertTrue(answer.contains("Успешно создана запись"));
        assertTrue(dialog.isFinished());
        assertEquals(1, storage.getAll().size());
        assertEquals(1, exporter.getExported().size());

        Submission created = storage.getAll().getFirst();
        assertEquals(1, created.id());
        assertEquals(2.0, created.score());
    }

    // 8. отмена и не создалась сдача задания
    @Test
    void noCancelsAndDoesNotCreate() {
        fillUntilApproving();

        String answer = dialog.handle("нет");

        assertEquals("Добавление отменено.", answer);
        assertTrue(dialog.isFinished());
        assertTrue(storage.getAll().isEmpty());
        assertTrue(exporter.getExported().isEmpty());
    }

    // 9. неверное подтверждение
    @Test
    void invalidApprovalKeepsDialog() {
        fillUntilApproving();

        dialog.handle("может быть");
        assertFalse(dialog.isFinished());
        assertEquals(DialogState.APPROVING, dialog.state());
        assertTrue(storage.getAll().isEmpty());

        dialog.handle("Да");
        assertTrue(dialog.isFinished());
        assertEquals(1, storage.getAll().size());
    }

    // 10. проверка валидации на точку при вводе баллов
    @Test
    void commaScoreIsAccepted() {
        dialog.handle("Иванов");
        dialog.handle("ООП");
        dialog.handle("24-09-2026");

        assertTrue(dialog.handle("1,5").contains("1.5"));
        dialog.handle("да");

        assertEquals(1.5, storage.getAll().getFirst().score());
    }

    // 11. проверка на дубликаты
    @Test
    void duplicateSubmissionIsRejected() {
        fillUntilApproving();
        dialog.handle("да");

        SubmissionDialog second = new SubmissionDialog(new SubmissionService(storage, exporter));
        second.handle("Иванов, Петров");
        second.handle("ООП");
        second.handle("24-09-2026");
        second.handle("2");

        String answer = second.handle("да");

        assertTrue(answer.contains("Ошибка валидации"));
        assertTrue(second.isFinished());
        assertEquals(1, storage.getAll().size());
    }
}