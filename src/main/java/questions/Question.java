package questions;

public class Question {
    // поля
    private String question;
    private String answer;

    // конструктор
    public Question(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    // геттер
    public String getQuestion() { return question; }

    // проверка корректности ответа
    public boolean isCorrect(String userAnswer) {
        return this.answer.equalsIgnoreCase(userAnswer);
    }
}
