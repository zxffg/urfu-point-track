package questions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestionTest {

    @Test
    public void testQuestionCreationAndGetter() {
        Question q = new Question("Столица Франции?", "Париж");
        assertEquals("Столица Франции?", q.getQuestion(), "Геттер должен возвращать правильный вопрос");
    }

    @Test
    public void testIsCorrectAnswer() {
        Question q = new Question("Столица России?", "Москва");
        
        // Проверка точного совпадения
        assertTrue(q.isCorrect("Москва"), "Должен принимать точный ответ");
        // Проверка игнорирования регистра
        assertTrue(q.isCorrect("москва"), "Должен игнорировать регистр (маленькие буквы)");
        assertTrue(q.isCorrect("МОСКВА"), "Должен игнорировать регистр (заглавные буквы)");
        // Проверка неверного ответа
        assertFalse(q.isCorrect("Питер"), "Должен возвращать false на неверный ответ");
    }
}