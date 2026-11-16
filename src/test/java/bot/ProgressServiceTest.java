package bot;

import org.junit.jupiter.api.Test;
import sheets.ResultExporter;
import submission.Submission;

import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ProgressServiceTest {
    private final ResultExporter exporter = new ResultExporter() {
        @Override
        public void export(Submission submission) {
        }

        @Override
        public void rewriteSummary() {
        }
    };

    private final Progress repository = new Progress() {
        public List<StudentScore> readProgress() {
            return List.of(new StudentScore("Иванов", 42), new StudentScore("Петров", 30));
        }
        public List<TelegramAccount> readUsers() {
            return List.of(new TelegramAccount("Иванов", "@Student"), new TelegramAccount("Петров", "another"));
        }
    };
    private final ProgressService service = new ProgressService(repository, Set.of(100L), exporter);

    @Test
    void studentSeesOnlyOwnScore() throws Exception {
        assertEquals("Иванов: 42", service.handle(1, "student", BotCommand.POINTS));
        assertFalse(service.handle(1, "student", BotCommand.POINTS).contains("Петров"));
        assertTrue(service.handle(1, null, BotCommand.POINTS).contains("username"));
        assertTrue(service.handle(1, "missing", BotCommand.POINTS).contains("не найден"));
    }

    @Test
    void groupIsRestrictedByNumericId() throws Exception {
        assertEquals("Доступ запрещен.", service.handle(1, "admin", BotCommand.GROUP));
        assertTrue(service.handle(100, null, BotCommand.GROUP).contains("Петров: 30"));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        Progress duplicates = new Progress() {
            public List<StudentScore> readProgress() { return List.of(); }
            public List<TelegramAccount> readUsers() {
                return List.of(new TelegramAccount("Петров", "student"), new TelegramAccount("Петров", "@Student"));
            }
        };
        assertTrue(new ProgressService(duplicates, Set.of(), exporter).handle(1, "student", BotCommand.POINTS)
                .contains("повторяющиеся"));
    }

    @Test
    void isAdmin() {
        ProgressService service_admins = new ProgressService(repository, Set.of(882279504L), exporter);
        long userId_admin = 882279504;
        long userId_user  = 498509348;

        assertTrue(service_admins.isAdmin(userId_admin));
        assertFalse(service_admins.isAdmin(userId_user));
    }
}
