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

Пакет `storage` содержит основные сущности и логику:

- [`Team`](src/main/java/storage/Team.java) команда из 1 до 3 участников
- [`Exercise`](src/main/java/storage/Exercise.java) учебное задание
- [`Submission`](src/main/java/storage/Submission.java) запись о сдаче задания командой
- [`Storage`](src/main/java/storage/Storage.java) хранилище записей и заданий
- `ConsoleHandler` консольное меню и ввод данных
- `SheetsExporter` отправка записей в Google Sheets

Подробности по каждому классу в комментариях к коду.

## Статус

Проект в разработке. Актуальный список готового находится в [DEVELOPER.md](DEVELOPER.md).