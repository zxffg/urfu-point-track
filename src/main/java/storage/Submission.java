package storage;

//Класс рекорд с уже созданными геттерами.
public record Submission(int id, Team team, Exercise exercise, double score) {
    public Submission {
        if (score < 0) {
            throw new IllegalArgumentException("Балл не может быть ниже нуля.");
        }
    }
}
