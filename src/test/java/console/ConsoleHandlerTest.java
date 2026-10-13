// Тесты функции createSubmission(). Был произведен небольшой рефакторинг
// с разделением общения в консоли и созданием Submission

package console;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sheets.ResultExporter;
import storage.Storage;
import submission.Submission;
import submission.SubmissionService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConsoleHandlerTest {

    //SUT переменные
    private Storage storage;
    private SubmissionService service;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        ResultExporter fakeExporter = new FakeExporter();
        this.service = new SubmissionService(storage, fakeExporter);
    }

    // 1. Валидное создание
    @Test
    void validCreateSubmission() {
        Submission result = service.createSubmission(
                List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);

        assertEquals(1, storage.getAll().size());
        assertTrue(storage.getAll().contains(result));
    }

    // 2. Проверка, что переиспользуется существующая задача
    @Test
    void reusesExistingExerciseAcrossSubmissions() {
        Submission first = service.createSubmission(
                List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        Submission second = service.createSubmission(
                List.of("Петров"), "ООП", LocalDate.of(2026, 9, 24), 1.5);

        assertSame(first.exercise(), second.exercise());
    }

    // 3. Валидация
    @Test
    void throwsWhenTooManySurnames() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.createSubmission(
                    List.of("A", "B", "C", "D"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        });
    }

    // 4. Проверка вызова .exporter()
    @Test
    void callsExporterOnEverySubmission() {
        FakeExporter fakeExporter = new FakeExporter();
        SubmissionService service = new SubmissionService(storage, fakeExporter);

        Submission result = service.createSubmission(
                List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);

        assertEquals(1, fakeExporter.getExported().size());
        assertEquals(result, fakeExporter.getExported().get(0));
    }
}
