package submission;

import storage.Exercise;
import storage.Team;

public record Submission(int id, Team team, Exercise exercise, double score) {

    // Генератор ID
    private static int nextId = 1;

    public Submission {
        if (!Double.isFinite(score) || score < 0 || score > 2) {
            throw new IllegalArgumentException("Балл должен быть числом от 0 до 2.");
        }
    }

    // метод для нового id
    public static Submission create(Team team, Exercise exercise, double score) {
        return new Submission(nextId++, team, exercise, score);
    }
}
