package submission;

import sheets.ResultExporter;
import storage.Exercise;
import storage.Storage;
import storage.Team;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class SubmissionService {
    private final Storage storage;
    // Интерфейс SheetsExporter
    private final ResultExporter exporter;

    public SubmissionService(Storage storage, ResultExporter exporter) {
        this.storage = storage;
        this.exporter = exporter;
    }

    // Добавление Submission в таблицу
    public Submission createSubmission(List<String> surnames,
                                       String exerciseName,
                                       LocalDate deadline,
                                       double score) throws IOException {
        Team team = new Team(surnames);
        // если такая задача уже есть, то переиспользуем ее
        Exercise exercise = storage.findExercise(exerciseName)
                .orElseGet(() -> new Exercise(exerciseName, deadline));

        Submission submission = Submission.create(team, exercise, score);

        exporter.export(submission);
        storage.add(submission);

        return submission;
    }

    // Использует getAll() из Storage.java
    public List<Submission> getAllSubmissions() {
        return storage.getAll();
    }

    // перерасчет итогов
    public void rewriteSummary() throws IOException {
        exporter.rewriteSummary();
    }
}
