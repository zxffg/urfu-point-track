package bot;

import java.io.IOException;
import java.util.List;

// Контракт чтения сводки, не зависит от Google API.
public interface ProgressRepository {
    List<List<Object>> readProgress() throws IOException;
    List<List<Object>> readUsers() throws IOException;
}