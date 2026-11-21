// ненастоящий класс для 4го теста.

package console;

import sheets.ResultExporter;
import submission.Submission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class FakeExporter implements ResultExporter {
    private final List<Submission> exported = new ArrayList<>();

    @Override
    public void export(Submission submission) {
        exported.add(submission);
    }

    @Override
    public void rewriteSummary() throws IOException {

    }

    List<Submission> getExported() {
        return exported;
    }
}