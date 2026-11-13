package storage;

import submission.Submission;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * Класс для хранения объектов типа Submission.
 * По совету одного чувака, решено написать не статический класс, а "обычный объект".
 */
public class Storage {
    private final List<Submission> submissionsList;
    private final List<Exercise> exercises = new ArrayList<>();

    public Storage() {
        this.submissionsList = new ArrayList<>();
    }

    // Одна и та же команда не может сдать одно и то же задание второй раз.
    // Сравнение по множеству фамилий: порядок в команде не важен.
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
        return left.exercise().getName().equalsIgnoreCase(right.exercise().getName())
                && surnames(left).equals(surnames(right));
    }

    private Set<String> surnames(Submission submission) {
        return submission.team().getSurnames().stream()
                .map(surname -> surname.strip().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
    }

    // Получение списка getAll()
    // UnmodifiableList - защищает отдаваемый список от изменений
    public List<Submission> getAll() { return Collections.unmodifiableList(submissionsList); }

    // getByID - поиск по айди
    public Submission getByID(int id) throws NoSuchElementException {
        for (Submission subM : submissionsList) {
            if (subM.id() == id) {
                return subM;
            }
        }
        throw new NoSuchElementException("Submission с id=\" + id + \" не найдена.");
    }

    // Метод поиска ИЛИ создания новой задачи + возвращает ее.
    public Exercise findOrCreateEx(String name, LocalDate deadline) {
        // Проверка, что name и deadline не null
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Поле 'имя' не может быть пустым.");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Дедлайн не может быть пустым.");
        }

        for (Exercise ex : exercises) {
            if (ex.getName().equalsIgnoreCase(name)) {
                return ex;
            }
        }
        Exercise newExercise = new Exercise(name, deadline);
        exercises.add(newExercise);
        return newExercise;
    }
}