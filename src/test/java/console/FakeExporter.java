// ненастоящий класс для тестов.

package console;

import sheets.ResultExporter;
import submission.Submission;

import java.util.ArrayList;
import java.util.List;

public class FakeExporter implements ResultExporter {
    private final List<Submission> exported = new ArrayList<>();
    private final List<Submission> deleted = new ArrayList<>();
    private int rewriteSummaryCalls;

    @Override
    public void export(Submission submission) {
        exported.add(submission);
    }

    @Override
    public void deleteSubmission(Submission submission) {
        deleted.add(submission);
    }

    @Override
    public void rewriteSummary() {
        rewriteSummaryCalls++;
    }

    public List<Submission> getExported() {
        return exported;
    }

    public List<Submission> getDeleted() {
        return deleted;
    }

    public int getRewriteSummaryCalls() {
        return rewriteSummaryCalls;
    }
}