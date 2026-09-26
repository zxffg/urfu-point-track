// Интерфейс, который изолирует SheetsExporter от ConsoleHandler (иначе пришлось бы импортировать все
// зависимости и библиотеки).

package storage;

public interface ResultExporter {
    void export(Submission submission);
}
