package bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import storage.Storage;
import submission.SubmissionService;
import console.FakeExporter;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProgressServiceTest {
    private FakeExporter exporter;
    private Progress repository;
    private SubmissionService submissions;
    private ProgressService service;

    @BeforeEach
    void setUp() {
        exporter = new FakeExporter();
        repository = new Progress() {
            public List<StudentScore> readProgress() {
                return List.of(new StudentScore("Иванов", 42), new StudentScore("Петров", 30));
            }

            public List<TelegramAccount> readUsers() {
                return List.of(new TelegramAccount("Иванов", "@Student"), new TelegramAccount("Петров", "another"));
            }
        };
        submissions = new SubmissionService(new Storage(), exporter);
        service = new ProgressService(repository, Set.of(100L), submissions);
    }

    // 1. студенты видят только свою успеваемость
    @Test
    void studentSeesOnlyOwnScore() throws Exception {
        assertEquals("Иванов: 42", service.handle(1, "student", BotCommand.POINTS));
        assertFalse(service.handle(1, "student", BotCommand.POINTS).contains("Петров"));
        assertTrue(service.handle(1, null, BotCommand.POINTS).contains("username"));
        assertTrue(service.handle(1, "missing", BotCommand.POINTS).contains("не найден"));
    }

    // 2. запрещен доступ если не админ
    @Test
    void groupIsRestrictedByNumericId() throws Exception {
        assertEquals("Доступ запрещен.", service.handle(1, "admin", BotCommand.GROUP));
        assertTrue(service.handle(100, null, BotCommand.GROUP).contains("Петров: 30"));
    }

    // 3. проверка на дубли в юзернейме
    @Test
    void duplicateUsernameIsRejected() throws Exception {
        Progress duplicates = new Progress() {
            public List<StudentScore> readProgress() {
                return List.of();
            }

            public List<TelegramAccount> readUsers() {
                return List.of(new TelegramAccount("Петров", "student"), new TelegramAccount("Петров", "@Student"));
            }
        };
        ProgressService duplicateService = new ProgressService(duplicates, Set.of(), submissions);

        assertTrue(duplicateService.handle(1, "student", BotCommand.POINTS).contains("повторяющиеся"));
    }

    // 4. проверка админ не админ (тут надо вставить айди любого админа из перменной окружения)
    @Test
    void isAdmin() {
        ProgressService adminService = new ProgressService(repository, Set.of(882279504L), submissions);
        long adminId = 882279504;
        long userId = 498509348;

        assertTrue(adminService.isAdmin(adminId));
        assertFalse(adminService.isAdmin(userId));
    }

    // 5. старт диалога для добавления записи
    @Test
    void adminStartsDialog() throws Exception {
        String answer = service.handle(100, null, BotCommand.ADD_SUBMISSION);

        assertEquals("Введите фамилии (от 1 до 3):", answer);
        assertTrue(service.isActiveState(100));
    }

    // 6. не админ не может начать диалог
    @Test
    void nonAdminCannotStartDialog() throws Exception {
        assertEquals("Доступ запрещен.", service.handle(1, null, BotCommand.ADD_SUBMISSION));
        assertFalse(service.isActiveState(1));
    }

    // 7. очитска состояния после диалога
    @Test
    void dialogRoutesInputAndClearsStateAfterFinish() throws Exception {
        service.handle(100, null, BotCommand.ADD_SUBMISSION);

        service.handleDialog(100, "Иванов");
        service.handleDialog(100, "ООП");
        service.handleDialog(100, "24-09-2026");
        service.handleDialog(100, "2");

        assertTrue(service.handleDialog(100, "да").contains("Успешно создана запись"));
        assertFalse(service.isActiveState(100));
    }

    // 8. прерывание записи диалога
    @Test
    void cancelClearsDialog() throws Exception {
        service.handle(100, null, BotCommand.ADD_SUBMISSION);

        assertEquals("Добавление записи прервано.", service.handleDialog(100, "/cancel"));
        assertFalse(service.isActiveState(100));
    }

    // 9. отмена без активного диалога
    @Test
    void cancelWithoutActiveDialog() throws Exception {
        assertEquals("Нет активного добавления.", service.handle(100, null, BotCommand.CANCEL));
        assertFalse(service.isActiveState(100));
    }

    // 10. вход без активного диалога
    @Test
    void dialogInputWithoutActiveDialog() {
        assertEquals("Диалог не найден. Начните заново: /add_submission",
                service.handleDialog(1, "Иванов"));
    }

    // 11. пересчет итогов при ошибке добавления (фиктивной)
    @Test
    void recalculateDelegatesToSubmissionService() throws Exception {
        service.handle(100, null, BotCommand.RECALCULATE);

        assertEquals(1, exporter.getRewriteSummaryCalls());
    }

    // 12. удаление: показ последних записей, выбор и подтверждение
    @Test
    void deleteFlowRemovesRecord() throws Exception {
        submissions.createSubmission(List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        submissions.createSubmission(List.of("Петров"), "ООП", LocalDate.of(2026, 9, 24), 1.0);

        String list = service.handle(100, null, BotCommand.DELETE_SUBMISSION);
        assertTrue(list.contains("1) Иванов"));
        assertTrue(list.contains("2) Петров"));
        assertTrue(service.isActiveState(100));

        assertTrue(service.handleDialog(100, "2").contains("Удалить запись 2"));
        assertEquals("Запись удалена.", service.handleDialog(100, "да"));

        assertFalse(service.isActiveState(100));
        assertEquals(1, submissions.getAllSubmissions().size());
        assertEquals("Иванов",
                submissions.getAllSubmissions().getFirst().team().surnames().getFirst());
    }

    // 13. удаление: неверный номер не сбрасывает состояние
    @Test
    void deleteInvalidNumberKeepsState() throws Exception {
        submissions.createSubmission(List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        service.handle(100, null, BotCommand.DELETE_SUBMISSION);

        assertTrue(service.handleDialog(100, "abc").contains("от 1 до 1"));
        assertTrue(service.isActiveState(100));
    }

    // 14. удаление: пустой список не запускает диалог
    @Test
    void deleteWithoutRecordsDoesNotStart() throws Exception {
        assertEquals("Нет записей для удаления.",
                service.handle(100, null, BotCommand.DELETE_SUBMISSION));
        assertFalse(service.isActiveState(100));
    }

    // 15. удаление: не админ
    @Test
    void nonAdminCannotDelete() throws Exception {
        assertEquals("Доступ запрещен.",
                service.handle(1, null, BotCommand.DELETE_SUBMISSION));
        assertFalse(service.isActiveState(1));
    }

    // 16. удаление: отмена очищает состояние
    @Test
    void cancelClearsDeleteDialog() throws Exception {
        submissions.createSubmission(List.of("Иванов"), "ООП", LocalDate.of(2026, 9, 24), 2.0);
        service.handle(100, null, BotCommand.DELETE_SUBMISSION);

        assertEquals("Удаление отменено.", service.handleDialog(100, "/cancel"));
        assertFalse(service.isActiveState(100));
    }
}
