## Point track
Консольное Java-приложение для ведения академической успеваемости потока по предмету ООП.
Проект создан в рамках учебного задания. Предназначен для учета количества сданных задач и просмотра набранных баллов.

### Сборка
1. Проверьте, что у вас JDK 26
2. `git clone https://github.com/zxffg/urfu-point-track`
3. `mvn compile` или используйте IDE IntelliJ
4. Настройте переменные окружения (см. раздел ниже) до запуска, иначе программа завершится с сообщением о недостающих переменных
5. Запустите класс `Main`

### Список реализованного (Пункт будет здесь, до написания первой рабочей версии)
- [x] Пакет `storage` содержит три класса: [`Exercise`](src/main/java/storage/Exercise.java), [`Submission`](src/main/java/storage/Submission.java) и [`Team`](src/main/java/storage/Team.java)
  (подробнее в комментариях к коду).
- [x] `Storage` хранит записи `Submission` и задания `Exercise`, умеет искать или создавать задание по имени (`findOrCreateEx`) и находить запись по id (`getByID`)
- [x] `ConsoleHandler` консольное меню с внесением результатов и просмотром всех записей
- [x] Интеграция с Google Sheets через `SheetsExporter` (интерфейс `ResultsExporter`), каждая новая запись сразу отправляется в таблицу
- [ ] Обработка дубликатов (та же команда плюс то же задание)
- [ ] Полное покрытие тестами (`Storage`, `ConsoleHandler`)

### Настройка API

1. Создай проект в [Google Cloud Console](https://console.cloud.google.com)
2. Включи Google Sheets API: в меню APIs & Services → Library, найди "Google Sheets API", нажми Enable
3. Создай Service Account: Credentials → Create Credentials → Service Account
4. Открой созданный Service Account → вкладка Keys → Add Key → Create new key → выбери JSON. Файл скачается автоматически (**обязательно добавь его в `.gitignore`, это фактически пароль**)
5. Открой нужную Google таблицу → кнопка "Настройки доступа" / Share → вставь email Service Account (вида `xxx@yyy.iam.gserviceaccount.com`, виден в настройках Service Account) → дай права "Редактор"

Без шага 5 API будет отвечать 403 Forbidden, даже если всё остальное настроено верно.

### Переменные окружения

Приложению нужны две переменные окружения:

| Переменная | Значение |
|---|---|
| `SHEETS_CREDENTIALS_PATH` | Полный путь до скачанного JSON-ключа Service Account |
| `SHEETS_SPREADSHEET_ID` | ID таблицы из её URL: `docs.google.com/spreadsheets/d/ВОТ_ЭТОТ_КУСОК/edit` |

**Через IntelliJ:** Run → Edit Configurations → выбери конфигурацию запуска `Main` → поле Environment variables → добавь обе переменные через точку с запятой:

```
SHEETS_CREDENTIALS_PATH=/полный/путь/до/credentials.json;SHEETS_SPREADSHEET_ID=твой_id_таблицы
```

**Через терминал (действует только на текущую сессию):**

```bash
export SHEETS_CREDENTIALS_PATH=/полный/путь/до/credentials.json
export SHEETS_SPREADSHEET_ID=твой_id_таблицы
```

Если переменные не заданы, программа выводит сообщение об этом и завершает работу, не пытаясь подключиться к API.

Название листа внутри таблицы, куда пишутся данные, задано в коде как `Sheet1`. Если вкладка листа в твоей таблице называется иначе (например `Лист1`), поменяй значение в `SheetsExporter.export()`.

### Известные проблемы при настройке

**`NoClassDefFoundError: com.google.auth.CredentialTypeForMetrics`**
Конфликт версий транзитивных зависимостей между `google-api-client` и `google-auth-library-oauth2-http`. Решается явным добавлением зависимости в `pom.xml`:

```xml
<dependency>
    <groupId>com.google.auth</groupId>
    <artifactId>google-auth-library-credentials</artifactId>
    <version>1.29.0</version>
</dependency>
```

**Ошибка "Unexpected error refreshing access token"**
Проверь по порядку: доступность `oauth2.googleapis.com` (`curl -v https://oauth2.googleapis.com`), корректность и целостность файла `credentials.json`, и что системное время на компьютере синхронизировано автоматически.

### Тесты
Тесты написаны на JUnit 5, лежат в `src/test/java`.
```bash
mvn test
```