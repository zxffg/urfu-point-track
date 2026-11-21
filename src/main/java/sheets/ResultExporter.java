// Интерфейс, который изолирует SheetsExporter от ConsoleHandler (иначе пришлось бы импортировать все
// зависимости и библиотеки).

package sheets;

import submission.Submission;
import java.io.IOException;

public interface ResultExporter {
    void export(Submission submission) throws IOException;
    void rewriteSummary() throws IOException;
}
