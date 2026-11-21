# Point Track
Java-приложение для ведения академической успеваемости учащихся.
Позволяет вносить результаты сдачи задач (команда, задание, баллы), хранить их локально и автоматически отправлять в Google Sheets.

Взаимодействие с кодом осуществленно по средствам терминала, телеграм бота и совместного режима.

Данный репозиторий является учебным заданием по курсу ООП в УрФУ 2026.

1. [Возможности](#возможности)
2. [Быстрый старт](#быстрый-старт)
3. [Структура проекта](#структура-проекта)

## Возможности
- Внесение результатов через консоль и телеграм бота
- Валидация ввода (размер команды, баллы, даты)
- Поиск или создание задания по имени (валидация дублей)
- Просмотр всех внесённых записей
- Автоматическая синхронизация с Google Sheets через Service Account

## Быстрый старт

```bash
git clone https://github.com/zxffg/urfu-point-track
cd urfu-point-track
mvn clean install
```

Настройка Google Sheets API, Telegram Bot Token и  переменных окружения описана в [DEVELOPER.md](DEVELOPER.md).

## Структура проекта

```bash

└── main
├── java
│   ├── bot
│   │   ├── BotCommand       // enum для команд бота
│   │   ├── PointBot         // Бот с опросом сервера и murkup клавиатурами
│   │   ├── Progress         // Интерфейс с методами (для удобства тестирования)
│   │   ├── ProgressService  // Логика ответа на команды
│   │   ├── StudentScore     // обьект с полями (surname, score)
│   │   └── TelegramAccount  // обьект с полями (surname, username)
│   ├── console
│   │   ├── ConsoleHandler   // Полный вывод в консоль тут
│   │   ├── ChoiceMode       // Вызывается в Main для выбора режима работы
│   │   ├── ModeOption       // enum для выбора режима работы
│   │   └── MenuOption       // enum меню режима terminal
│   ├── sheets
│   │   ├── ResultExporter   // 
│   │   ├── SheetLayout      // Единая точка откуда меняем все константы для настройки таблицы
│   │   ├── ScoreSummary     // Считает баллы по фамилиям
│   │   ├── SheetsExporter   // Отправляет все в таблицу
│   │   └── SheetsProgres... // читает таблицу: формирует результаты и аккаунты
│   ├── > storage
│   ├── > submission
│   └── Main                 // точка входа
└── resources
    ├── 3-task-structure.png
    ├── credentials-example.json  // пример api ключей google
    └── telegram-bot-example.json // пример токена и списка юзеров
```

Тесты (JUnit 5) лежат в src/test/java и покрывают Team, Exercise, Submission, Storage и ConsoleHandler.

Подробности по каждому классу в комментариях к коду.

## Статус

Проект в разработке. Актуальный список готового находится в [DEVELOPER.md](DEVELOPER.md).