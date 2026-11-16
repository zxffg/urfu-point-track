package storage;

import java.util.ArrayList;
import java.util.List;

// Класс представляет одну запись типа Team с полями список фамилий и ID
public record Team(List<String> surnames) {
    public Team {
        if (surnames == null || surnames.size() < 1 || surnames.size() > 3) {
            throw new IllegalArgumentException("Team/Недопустимое число участников.");
        }
        for (String name : surnames) {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Team/Фамилия не может быть пустой.");
            }
        }
        // защитное копирование
        surnames = List.copyOf(surnames);
    }
}
