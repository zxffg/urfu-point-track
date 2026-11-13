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
        assertEquals("Иванов: 42", service.handle(1, "student", "/points"));
        assertFalse(service.handle(1, "student", "/points").contains("Петров"));
        assertTrue(service.handle(1, null, "/points").contains("username"));
        assertTrue(service.handle(1, "missing", "/points").contains("не найден"));
    }

    @Test
    void groupIsRestrictedByNumericId() throws Exception {
        assertEquals("Доступ запрещен.", service.handle(1, "admin", "/group"));
        assertTrue(service.handle(100, null, "/group").contains("Петров: 30"));
    }

    @Test
    void duplicateUsernameIsRejected() throws Exception {
        Progress duplicates = new Progress() {
            public List<StudentScore> readProgress() { return List.of(); }
            public List<TelegramAccount> readUsers() {
                return List.of(new TelegramAccount("Петров", "student"), new TelegramAccount("Петров", "@Student"));
            }
        };
        assertTrue(new ProgressService(duplicates, Set.of(), exporter).handle(1, "student", "/points")
                .contains("повторяющиеся"));
    }
}
