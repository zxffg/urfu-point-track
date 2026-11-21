package submission;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sheets.ResultExporter;
import storage.Exercise;
import storage.Storage;
import storage.Team;
import console.FakeExporter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SubmissionServiceTest {
    private Storage storage;
    private FakeExporter exporter;
    private SubmissionService service;

    @BeforeEach
    void setUp() {
        storage = new Storage();
        exporter = new FakeExporter();
        service = new SubmissionService(storage, exporter);
    }

    // 1. проверка присваивания следующего айди
    @Test
    void assignsSequentialIds() throws Exception {
        Submission first = service.createSubmission(
                List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        Submission second = service.createSubmission(
                List.of("Петров"), "ООП", LocalDate.of(2026, 9, 24), 1.5);

        assertEquals(1, first.id());
        assertEquals(2, second.id());
    }

    // 2. продолжение айди после создания сторадж
    @Test
    void continuesIdsAfterInit() throws Exception {
        storage.init(List.of(
                new Submission(3, new Team(List.of("Иванов")),
                        new Exercise("ООП", LocalDate.of(2026, 9, 24)), 2.0),
                new Submission(7, new Team(List.of("Петров")),
                        new Exercise("ООП", LocalDate.of(2026, 9, 24)), 1.0)
        ));

        Submission created = service.createSubmission(
                List.of("Сидоров"), "ООП", LocalDate.of(2026, 9, 24), 2.0);

        assertEquals(8, created.id());
        assertEquals(created, storage.getByID(8));
    }

    // 3. проверка на дубликаты
    @Test
    void rejectsDuplicateWithoutAddingSecondCopy() throws Exception {
        service.createSubmission(List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);

        assertThrows(IllegalArgumentException.class, () ->
                service.createSubmission(List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 1.0));

        assertEquals(1, storage.getAll().size());
    }

    // 4. переписывание итогов
    @Test
    void rewriteSummaryDelegatesToExporter() throws Exception {
        service.rewriteSummary();

        assertEquals(1, exporter.getRewriteSummaryCalls());
    }

    // 5. удаление убирает запись из Storage и зовёт таблицу
    @Test
    void deleteRemovesFromStorageAndSheet() throws Exception {
        Submission created = service.createSubmission(
                List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);

        service.deleteSubmission(created);

        assertTrue(storage.getAll().isEmpty());
        assertEquals(List.of(created), exporter.getDeleted());
        assertEquals(1, exporter.getRewriteSummaryCalls());
    }

    // 6. если таблица не удалила, Storage не трогаем
    @Test
    void deleteFailureKeepsStorage() {
        Submission created = new Submission(1, new Team(List.of("Иванов")),
                new Exercise("ООП", LocalDate.of(2026, 9, 24)), 2.0);
        storage.init(List.of(created));

        ResultExporter failing = new ResultExporter() {
            public void export(Submission submission) {}

            public void deleteSubmission(Submission submission) throws IOException {
                throw new IOException("boom");
            }

            public void rewriteSummary() {}
        };

        SubmissionService failingService = new SubmissionService(storage, failing);

        assertThrows(IOException.class, () -> failingService.deleteSubmission(created));
        assertEquals(1, storage.getAll().size());
    }
}