package storage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

/*
* Класс для хранения объектов типа Submission.
* По совету одного чувака, решено написать не статический класс, а "обычный объект".
*/
public class Storage {
    // Поле
    private final List<Submission> submissionsList;
    private final List<Exercise> exercises = new ArrayList<>();

    // Конструктор
    public Storage() {
        this.submissionsList = new ArrayList<>();
    }

    // Функция добавления
    public void add(Submission submission) { submissionsList.add(submission); }

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
