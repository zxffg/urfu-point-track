// общение с пользователем, хранение состояния и движка

package dialog;// логика диалога
import questions.Question;

public class UserSession {
    // поле класса
    private Question currentQuestion;

    // конструктор
    public UserSession (Question question) {
        this.currentQuestion = question;
    }

    // геттеры и сеттеры
    // геттер получения текущего активного вопроса
    public Question getCurrentQuestion() {
        return currentQuestion;
    }
    // сеттер изменяет текущий вопрос
    public void setCurrentQuestion(Question newQuestion) {
        currentQuestion = newQuestion;
    }
}
