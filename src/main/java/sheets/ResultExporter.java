// Интерфейс, который изолирует SheetsExporter от ConsoleHandler (иначе пришлось бы импортировать все
// зависимости и библиотеки).

package sheets;

import submission.Submission;

public interface ResultExporter {
    void export(Submission submission);
}
