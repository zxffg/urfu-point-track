package sheets;

public record StudentScore(String surname, Double score) {
    public StudentScore {
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Недопустимая фамилия участника.");
        }
        if (score < 0) {
            throw new IllegalArgumentException("Недопустимая количество баллов.");
        }
    }
}
