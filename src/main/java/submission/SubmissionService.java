package submission;

import storage.Exercise;
import sheets.ResultExporter;
import storage.Storage;
import storage.Team;

import java.time.LocalDate;
import java.util.List;

public class SubmissionService {
    private final Storage storage;
    // Интерфейс связанный с SheetsExporter
    private final ResultExporter exporter;

    public SubmissionService(Storage storage, ResultExporter exporter) {
        this.storage = storage;
        this.exporter = exporter;
    }

    // Метод добавления Submission
    // Фамилии -> Задания -> Баллы
    public Submission createSubmission(List<String> surnames, String exerciseName, LocalDate deadline, double score) {
        // Команда -> проверка "существует ли такое задание?" -> создание решения.
        Team team = new Team(surnames);
        Exercise exercise = storage.findOrCreateEx(exerciseName, deadline);
        Submission newSubmission = Submission.create(team, exercise, score);
        storage.add(newSubmission);
        exporter.export(newSubmission);
        return newSubmission;
    }

    // Использует getAll() из Storage.java
    public List<Submission> getAllSubmissions() {
        return storage.getAll();
    }
}
