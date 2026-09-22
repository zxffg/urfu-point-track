// Банк вопросов для перебора

package questions;
import java.util.ArrayList;
import java.util.List;

public class QuestionBank {
    // поля
    private int counter = 0;
    private final List<Question> questions;

    // конструктор
    public QuestionBank() {
        this.questions = new ArrayList<Question>();
        // хардкод вопросов
        questions.add(new Question("Столица Франции?", "Париж"));
        questions.add(new Question("Столица России?", "Москва"));
        questions.add(new Question("Столица Китая?", "Пекин"));
        questions.add(new Question("Столица США?", "Вашингтон"));
        questions.add(new Question("Столица Японии?", "Токио"));
        questions.add(new Question("Столица Греции?", "Афины"));
    }

    // возврат следующего вопроса и обновление каунтера
    public Question getNextQuestion() {
        Question tmp = this.questions.get(counter);
        ++counter;

        // закальцовывание счетчика
        if (counter >= questions.size()) counter = 0;

        return tmp;
    }

}
