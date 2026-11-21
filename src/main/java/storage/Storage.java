package storage;

import submission.Submission;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Storage {
    private final List<Submission> submissionsList;
    private final List<Exercise> exercises = new ArrayList<>();
    private int lastId = 0;

    public int nextId() {
        return ++lastId;
    }

    public Storage() {
        this.submissionsList = new ArrayList<>();
    }

    // Одна и та же команда не может сдать одно и то же задание второй раз.
    // Сравнение по фамилиям: порядок в команде не важен.
    public void add(Submission submission) {
        if (submission == null) {
            throw new IllegalArgumentException("Запись не может быть пустой.");
        }
        for (Submission existing : submissionsList) {
            if (sameWork(existing, submission)) {
                throw new IllegalArgumentException("Эта команда уже сдала это задание.");
            }
        }
        submissionsList.add(submission);
    }

    private boolean sameWork(Submission left, Submission right) {
        return left.exercise().name().equalsIgnoreCase(right.exercise().name())
                && surnames(left).equals(surnames(right));
    }

    private Set<String> surnames(Submission submission) {
        return submission.team().surnames().stream()
                .map(surname -> surname.strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    // получение списка getAll()
    public List<Submission> getAll() { return Collections.unmodifiableList(submissionsList); }

    // getByID - поиск по айди
    public Submission getByID(int id) throws NoSuchElementException {
        for (Submission submission : submissionsList) {
            if (submission.id() == id) {
                return submission;
            }
        }
        throw new NoSuchElementException("Submission с id=" + id + " не найдена.");
    }

    // поиск похожей задачи
    public Optional<Exercise> findExercise(String name) {
        return submissionsList.stream()
                .map(Submission::exercise)
                .filter(ex -> ex.name().equalsIgnoreCase(name))
                .findFirst();
    }

    // первичное чтение записей после запуска Main
    public void init(List<Submission> initialData) {
        this.submissionsList.addAll(initialData);
        this.lastId = initialData.stream().mapToInt(Submission::id).max().orElse(0);
    }
}