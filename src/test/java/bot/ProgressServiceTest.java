package bot;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ProgressServiceTest {
    private final ProgressRepository repository = new ProgressRepository() {
        public List<List<Object>> readProgress() {
            return List.of(List.of("Иванов", "42"), List.of("Петров", "30"));
        }
        public List<List<Object>> readUsers() {
            return List.of(List.of("Иванов", "@Student"), List.of("Петров", "another"));
        }
    };
    private final ProgressService service = new ProgressService(repository, Set.of(100L));

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
        ProgressRepository duplicates = new ProgressRepository() {
            public List<List<Object>> readProgress() { return List.of(); }
            public List<List<Object>> readUsers() {
                return List.of(List.of("Иванов", "student"), List.of("Петров", "@Student"));
            }
        };
        assertTrue(new ProgressService(duplicates, Set.of()).handle(1, "student", "/points")
                .contains("повторяющиеся"));
    }
}
