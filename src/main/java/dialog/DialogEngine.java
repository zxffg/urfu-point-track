// движок общения с пользователем (ветвления)

package dialog;

import questions.Question;
import questions.QuestionBank;

import java.util.HashMap;
import java.util.Map;

public class DialogEngine {
    //поле Map вопрос
    private final Map<String, UserSession> sessions;
    private final QuestionBank questionBank;

    // конструктор хэшмап
    public DialogEngine() {
        this.sessions = new HashMap<>();
        this.questionBank = new QuestionBank();
    }

    // ручка общения
    public String handleMessage(String userId, String message) {

        // подсказка для пользователя, если он ввел '\h'
        if (message.equals("/help") ||  message.equals("/h")) {
            return (
                "Привет! Я бот-опросник." + "\n" +
                "Помогу проверить знания, пройти тест или просто весело провести время!" + "\n" +
                "Сейчас я умею проводить викторину по странам.\n"
            );
        }

        // подсказка для пользователя, если он новый + начало новой сессии
        if (!sessions.containsKey(userId)) {
            // получение вопроса
            Question tmpQ = questionBank.getNextQuestion();
            // создание сессии
            UserSession newSession = new UserSession(tmpQ);
            // кладет сессию
            sessions.put(userId, newSession);

            // получение вопроса и возврат полноценного ответа
            return tmpQ.getQuestion();
        } else {
            // третее ветвление: пользователь уже есть и диалог продолжается
            // получение сессии
            UserSession userSession = sessions.get(userId);
            // ниже получение текущего вопроса и последующая обработка
            Question qNow = userSession.getCurrentQuestion();
            if (qNow.isCorrect(message)) {
                // сессия обновляется новым вопросом
                Question tmpQ = questionBank.getNextQuestion();
                userSession.setCurrentQuestion(tmpQ);
                return "Верно!\n" + tmpQ.getQuestion();
            } else {
                Question tmpQ = questionBank.getNextQuestion();
                userSession.setCurrentQuestion(tmpQ);
                return "Неверно!!\n" + tmpQ.getQuestion();
            }
        }
    }

}