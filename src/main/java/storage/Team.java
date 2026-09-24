package storage;

import java.util.ArrayList;
import java.util.List;

// Класс представляет одну запись типа Team с полями список фамилий и ID
public class Team {
    // Поля класса
    private final int id;
    private final List<String> surnames;
    private static int nextId = 1;

    public Team(List<String> surnames) throws IllegalArgumentException {
        // Проверка, что массив имеет допустимый размер
       if (surnames == null || surnames.size() < 1 || surnames.size() > 3) {
           throw new IllegalArgumentException("Недопустимый размер массива.");
       }

        // Проверка, что имена не null
       for (String tmpName : surnames) {
           if (tmpName == null || tmpName.isBlank()) {
               throw new IllegalArgumentException("Фамилия не может быть пустой.");
           }
       }

        // Конструктор
        //Так как геттер возвращает изменяемый список тут сохраняется копия
        this.surnames = new ArrayList<>(surnames);
        this.id = nextId++;
    }

    // Геттеры
    public int getId() {
        return id;
    }
    public List<String> getSurnames() {
        return surnames;
    }
}
