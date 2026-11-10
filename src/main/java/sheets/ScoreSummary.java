package sheets;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Считает итоговые баллы по фамилиям. Таблица хранит результат, формулы в ней не нужны.
public final class ScoreSummary {
    private static final int SCORE_COLUMN = 5;

    private ScoreSummary() {
    }

    public static List<List<Object>> build(List<List<Object>> submissionRows, List<List<Object>> rosterRows) {
        Map<String, Double> totals = new LinkedHashMap<>();
        Map<String, String> seenNames = new LinkedHashMap<>();
        if (submissionRows != null) {
            for (List<Object> row : submissionRows) {
                Double score = score(cell(row, SCORE_COLUMN));
                if (score == null) {
                    continue;
                }
                List<String> surnames = new ArrayList<>();
                for (int column = 0; column < 3; column++) {
                    String surname = text(cell(row, column));
                    if (surname.isEmpty() || surnames.stream().anyMatch(item -> key(item).equals(key(surname)))) {
                        continue;
                    }
                    surnames.add(surname);
                }
                for (String surname : surnames) {
                    String id = key(surname);
                    totals.merge(id, score, Double::sum);
                    seenNames.putIfAbsent(id, surname);
                }
            }
        }

        List<List<Object>> summary = new ArrayList<>();
        summary.add(List.of("Студент", "Баллы"));
        Map<String, String> roster = new LinkedHashMap<>();
        if (rosterRows != null) {
            for (List<Object> row : rosterRows) {
                String surname = text(cell(row, 0));
                if (!surname.isEmpty()) {
                    roster.putIfAbsent(key(surname), surname);
                }
            }
        }
        for (Map.Entry<String, String> student : roster.entrySet()) {
            summary.add(List.of(student.getValue(), format(totals.getOrDefault(student.getKey(), 0.0))));
            seenNames.remove(student.getKey());
        }
        for (Map.Entry<String, String> student : seenNames.entrySet()) {
            summary.add(List.of(student.getValue(), format(totals.getOrDefault(student.getKey(), 0.0))));
        }
        return summary;
    }

    private static Object cell(List<Object> row, int index) {
        if (row == null || index >= row.size()) {
            return null;
        }
        return row.get(index);
    }

    private static String text(Object value) {
        return value == null ? "" : value.toString().strip();
    }

    private static String key(String surname) {
        return surname.toLowerCase(Locale.ROOT);
    }

    private static Double score(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        String text = text(value).replace(',', '.');
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Object format(double score) {
        if (!Double.isInfinite(score) && score == Math.rint(score)) {
            return (long) score;
        }
        return score;
    }
}
