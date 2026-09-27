// Фейк класс для 4го теста.

package console;

import storage.ResultExporter;
import storage.Submission;

import java.util.ArrayList;
import java.util.List;

class FakeExporter implements ResultExporter {
    private final List<Submission> exported = new ArrayList<>();

    @Override
    public void export(Submission submission) {
        exported.add(submission);
    }

    List<Submission> getExported() {
        return exported;
    }
}