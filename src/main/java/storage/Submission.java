package storage;

//Класс рекорд с уже созданными геттерами.
public record Submission(int id, Team team, Exercise exercise, double score) {

    // Генератор ID
    private static int nextId = 1;

    public Submission {
        if (score < 0) {
            throw new IllegalArgumentException("Балл не может быть ниже нуля.");
        }
    }

    // метод для нового id
    public static Submission create(Team team, Exercise exercise, double score) {
        return new Submission(nextId++, team, exercise, score);
    }
}
