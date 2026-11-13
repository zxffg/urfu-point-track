package sheets;

// здесь лежат константы настройки таблицы
public class SheetLayout {

    // будьте внимательны, диапазон столбцов необходимо писать без '!' знака
    public final static String SUBMISSION_SHEET = "Sheet1"; // журнал с записями о сдаче задачи
    public final static String SUMMARY_SHEET = "Sheet2";    // сводка результатов пользователей

    public final static String SUBMISSIONS_APPEND = "A1";   // куда дописывается задача?
    public final static String SUBMISSIONS_READ = "A:F";    // диапазон, из которого сводка считает результат

    public final static String SUMMARY_SCORES = "A2:B";     // сводка/фамилия и итог
    public final static String SUMMARY_USERS = "D2:E";      // сводка/фамилия и username
    public final static String SUMMARY_ROSTER = "D2:D";     // сводка/только фамилии
    public final static String SUMMARY_CLEAR = "A:B";       // сводка/что стирать перед новой сводкой
    public final static String SUMMARY_WRITE = "A1";        // сводка/куда писать новую сводку

    public final static int TEAM_SIZE = 3;     // для цикла фамилий в SheetsExporter и ScoreSummary
    public final static int SCORE_COLUMN = 5;  // номер колонки с баллами
    
    private SheetLayout() {};

    // удобно склеиваем диапазон и название таюблицы
    public static String range(String sheet, String cells) {
        return "'" + sheet.replace("'", "''") + "'!" + cells;
    }
}
