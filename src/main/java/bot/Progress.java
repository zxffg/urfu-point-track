package bot;

import java.io.IOException;
import java.util.List;

// Контракт чтения сводки, не зависит от Google API.
public interface Progress {
    List<StudentScore> readProgress() throws IOException;
    List<TelegramAccount> readUsers() throws IOException;
}
