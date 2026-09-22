package dialog;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class DialogEngineTest {

    // sut - system under test
    private DialogEngine engine = new DialogEngine();

    @Test
    public void testHelpCommand() {
        String responseHelp = engine.handleMessage("user1", "/help");
        String responseH = engine.handleMessage("user2", "/h");
        
        assertTrue(responseHelp.contains("Привет! Я бот-опросник."), "Команда /help должна выводить справку");
        assertTrue(responseH.contains("Привет! Я бот-опросник."), "Команда /h должна выводить справку");
    }

    @Test
    public void testNewUserGetsFirstQuestion() {
        // При первом сообщении (не help) создается сессия и выдается вопрос
        String response = engine.handleMessage("user1", "Привет");
        
        assertNotNull(response, "Ответ не должен быть null");
        assertFalse(response.contains("Верно!"), "Первое сообщение не должно оцениваться как ответ");
        assertFalse(response.contains("Неверно!!"), "Первое сообщение не должно оцениваться как ответ");
    }

    @Test
    public void testAnswerProcessingAndStateChange() {
        String userId = "user1";

        // 1. Фиксируем начальное состояние (выдача первого вопроса из QuestionBank)
        String step1 = engine.handleMessage(userId, "Привет");
        assertTrue(step1.contains("Столица Франции?"), "Ожидался первый вопрос про Францию");

        // 2. Отправляем правильный ответ и жестко проверяем СМЕНУ СОСТОЯНИЯ
        String step2 = engine.handleMessage(userId, "Париж");
        assertTrue(step2.contains("Верно!"), "Ответ должен быть засчитан");
        
        // 3. Главная проверка: убеждаемся, что сессия обновилась и выдала следующий вопрос
        assertTrue(step2.contains("Столица России?"), "Ошибка состояния: внутренний индекс не сдвинулся, бот не перешел к следующему вопросу");
    }
} 