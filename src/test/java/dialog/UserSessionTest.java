package dialog;

import org.junit.jupiter.api.Test;
import questions.Question;
import static org.junit.jupiter.api.Assertions.*;

public class UserSessionTest {

    @Test
    public void testSessionInitialization() {
        Question initialQuestion = new Question("Вопрос 1", "Ответ 1");
        UserSession session = new UserSession(initialQuestion);
        
        assertEquals(initialQuestion, session.getCurrentQuestion(), "Сессия должна сохранять стартовый вопрос");
    }

    @Test
    public void testSetCurrentQuestion() {
        Question q1 = new Question("Вопрос 1", "Ответ 1");
        Question q2 = new Question("Вопрос 2", "Ответ 2");
        
        UserSession session = new UserSession(q1);
        session.setCurrentQuestion(q2);
        
        assertEquals(q2, session.getCurrentQuestion(), "Сессия должна корректно обновлять текущий вопрос");
    }
}