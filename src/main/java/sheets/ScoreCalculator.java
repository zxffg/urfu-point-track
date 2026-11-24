package sheets;

import submission.Submission;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Считает итоговые баллы по фамилиям. Таблица хранит результат, формулы в ней не нужны.
public final class ScoreCalculator {

    private ScoreCalculator() {}

    // суммирует баллы из готовых submission
    public static List<StudentScore> build(List<Submission> submissions, List<String> students) {
        Map<String, Double> totals = new LinkedHashMap<>();
        Map<String, String> seenNames = new LinkedHashMap<>();
        if (submissions != null) {
            for (Submission submission : submissions) {
                Double score = submission.score();
                for (String surname : submission.team().surnames()) {
                    String key = surname.toLowerCase(Locale.ROOT);
                    totals.merge(key, score, Double::sum);
                    seenNames.putIfAbsent(key, surname);
                }
            }
        }

        // собирает итоговый список по списку группы
        List<StudentScore> summary = new ArrayList<>();
        if (students != null) {
            for (String student : students) {
                if (student.isBlank()) continue;
                String key = student.toLowerCase(Locale.ROOT);
                double total = totals.getOrDefault(key, 0.0);
                summary.add(new StudentScore(student, total));
                seenNames.remove(key);
            }
        }

        // те, кто сдавал, но кого не было в списке группы
        for (Map.Entry<String, String> extra : seenNames.entrySet()) {
            double total = totals.getOrDefault(extra.getKey(), 0.0);
            summary.add(new StudentScore(extra.getValue(), total));
        }

        return summary;
    }
}
