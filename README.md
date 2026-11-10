# Point Track
Консольное Java-приложение для ведения академической успеваемости потока по предмету ООП.
Позволяет вносить результаты сдачи задач (команда, задание, баллы), хранить их локально и автоматически отправлять в Google Sheets.

## Возможности
- Внесение результатов через консольное меню
- Валидация ввода (размер команды, баллы, даты)
- Поиск или создание задания по имени (без дублей)
- Просмотр всех внесённых записей
- Автоматическая синхронизация с Google Sheets через Service Account

## Быстрый старт

```bash
git clone https://github.com/zxffg/urfu-point-track
cd urfu-point-track
mvn clean install
```

Настройка Google Sheets API и переменных окружения описана в [DEVELOPER.md](DEVELOPER.md).

## Структура проекта

```bash
└── main
├── java
│   ├── bot
│   │   ├── ProgressRepository  // Интерфейс с методами 
│   │   ├── ProgressService     // Решавет что ответить пользователю
│   │   └── TelegramBot         // Содержит Long Pool опрос
│   ├── console
│   │   ├── ConsoleHandler
│   │   └── MenuOption
│   ├── sheets
│   │   ├── ResultExporter
│   │   ├── ScoreSummary
│   │   ├── SheetsExporter
│   │   └── SheetsProgressRepository
│   ├── storage >
│   ├── submission >
│   ├── BotMain
│   └── Main
└── resources
├── 3-task-structure.png
├── credentials.json
├── credentials-example.json
└── telegram-bot-example.json
```

Тесты (JUnit 5) лежат в src/test/java и покрывают Team, Exercise, Submission, Storage и ConsoleHandler.

Подробности по каждому классу в комментариях к коду.

## Статус

Проект в разработке. Актуальный список готового находится в [DEVELOPER.md](DEVELOPER.md).

Telegram-бот запускается отдельно через `BotMain`. Команда `/points` показывает баллы студента, `/group` доступна администраторам. Формат второго листа и настройки, в том числе необязательный прокси, описаны в [DEVELOPER.md](DEVELOPER.md#telegram-бот).