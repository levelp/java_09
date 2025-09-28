# BDD Тестирование с Cucumber

## Что такое BDD?

**BDD (Behavior-Driven Development)** — это подход к разработке программного обеспечения, при котором тесты пишутся на естественном языке (русском, английском и т.д.) в формате, понятном всем участникам команды: разработчикам, тестировщикам, менеджерам и заказчикам.

## Cucumber

**Cucumber** — это фреймворк для BDD-тестирования, который позволяет писать тесты на языке **Gherkin** (синтаксис Given-When-Then).

### Преимущества Cucumber:

✅ **Понятность** — тесты читаются как обычный текст
✅ **Документация** — тесты служат живой документацией проекта
✅ **Коммуникация** — улучшает взаимодействие между техническими и нетехническими специалистами
✅ **Повторное использование** — шаги тестов можно переиспользовать
✅ **Интеграция** — работает с JUnit, TestNG, Maven, Gradle

## Структура проекта

```
01_JDBC/
├── src/
│   └── test/
│       ├── java/
│       │   └── cucumber/
│       │       ├── RunCucumberTest.java          # Точка входа для запуска тестов
│       │       ├── StudentSteps.java             # Шаги для управления студентами
│       │       └── ConnectionPoolSteps.java      # Шаги для пула соединений
│       └── resources/
│           └── features/
│               ├── student_management.feature    # Сценарии управления студентами
│               └── database_connection.feature   # Сценарии работы с БД
└── pom.xml
```

## Синтаксис Gherkin

### Ключевые слова (на русском):

- **Функционал** (Feature) — описывает тестируемую функциональность
- **Сценарий** (Scenario) — конкретный тест-кейс
- **Дано** (Given) — начальное состояние системы
- **Когда** (When) — действие пользователя
- **Тогда** (Then) — ожидаемый результат
- **И** (And) — дополнительные условия/действия/проверки
- **Структура сценария** (Scenario Outline) — параметризованный тест

### Пример на русском:

```gherkin
# language: ru
Функционал: Управление студентами
  Как администратор системы
  Я хочу управлять данными студентов
  Чтобы вести учет студентов в базе данных

  Сценарий: Добавление нового студента
    Дано база данных готова к работе
    Когда я добавляю студента с именем "Иван" и фамилией "Иванов" и email "ivan@example.com"
    Тогда студент должен быть сохранен с ID
```

### Пример на английском:

```gherkin
# language: en
Feature: Student Management
  As a system administrator
  I want to manage student data
  So that I can track students in the database

  Scenario: Add new student
    Given database is ready
    When I add a student with name "Ivan" and surname "Ivanov" and email "ivan@example.com"
    Then the student should be saved with ID
```

## Параметризованные тесты (Scenario Outline)

```gherkin
Структура сценария: Поиск студентов по фамилии
  Дано база данных готова к работе
  И в базе есть студенты:
    | имя     | фамилия  | email               |
    | Иван    | Сидоров  | ivan.s@example.com  |
    | Петр    | Сидоров  | petr.s@example.com  |
    | Ольга   | Козлова  | olga.k@example.com  |
  Когда я ищу студентов с фамилией "<фамилия>"
  Тогда я должен найти <количество> студентов

  Примеры:
    | фамилия | количество |
    | Сидоров | 2          |
    | Козлова | 1          |
    | Иванов  | 0          |
```

Этот тест выполнится **3 раза** с разными параметрами!

## Реализация шагов (Step Definitions)

### Пример с аннотациями на русском:

```java
package cucumber;

import io.cucumber.java.ru.*;
import static org.junit.Assert.*;

public class StudentSteps {

    @Дано("база данных готова к работе")
    public void базаДанныхГотоваКРаботе() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        createTableIfNotExists();
    }

    @Когда("я добавляю студента с именем {string} и фамилией {string} и email {string}")
    public void яДобавляюСтудента(String firstName, String lastName, String email) throws SQLException {
        String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    lastInsertedId = rs.getInt("id");
                }
            }
        }
    }

    @Тогда("студент должен быть сохранен с ID")
    public void студентДолженБытьСохранен() {
        assertNotNull("Student ID should not be null", lastInsertedId);
        assertTrue("Student ID should be positive", lastInsertedId > 0);
    }
}
```

### Параметры в шагах:

- `{string}` — строковый параметр
- `{int}` — целочисленный параметр
- `{double}` — дробное число
- `{word}` — одно слово без пробелов

## Таблицы данных (DataTable)

```gherkin
Дано в базе есть студенты:
  | имя     | фамилия  | email               |
  | Иван    | Сидоров  | ivan.s@example.com  |
  | Петр    | Сидоров  | petr.s@example.com  |
```

```java
@Дано("в базе есть студенты:")
public void вБазеЕстьСтуденты(DataTable dataTable) throws SQLException {
    List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class);
    String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?)";
    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
        for (Map<String, String> row : rows) {
            pstmt.setString(1, row.get("имя"));
            pstmt.setString(2, row.get("фамилия"));
            pstmt.setString(3, row.get("email"));
            pstmt.addBatch();
        }
        pstmt.executeBatch();
    }
}
```

## Хуки (Hooks)

### @Before — выполняется перед каждым сценарием:

```java
@io.cucumber.java.Before
public void setUp() {
    System.out.println("Настройка перед тестом");
}
```

### @After — выполняется после каждого сценария:

```java
@io.cucumber.java.After
public void tearDown() throws SQLException {
    if (connection != null && !connection.isClosed()) {
        cleanupTestData();
        connection.close();
    }
}
```

## Конфигурация базы данных

### H2 Database (по умолчанию для тестов)

Тесты теперь используют встроенную базу данных H2 в режиме совместимости с PostgreSQL. Никакой дополнительной настройки не требуется!

**Преимущества H2 для тестов:**
- 🚀 Не требует установки или настройки
- ⚡ Очень быстрая работа в памяти (in-memory)
- ✅ Полная совместимость с PostgreSQL синтаксисом
- 🔄 Автоматическая очистка между тестами
- 📦 Включена как зависимость в проекте

### PostgreSQL (для production)

Если вы хотите запустить тесты с реальным PostgreSQL, установите переменные окружения:

```bash
# Установите переменные окружения
export DB_URL="jdbc:postgresql://localhost:5432/postgres"
export DB_USER="postgres"
export DB_PASSWORD="123"

# Или создайте файл db.properties
cp db.properties.example db.properties
# Отредактируйте db.properties с вашими настройками
```

Подробности в [DATABASE_CONFIG.md](DATABASE_CONFIG.md).

## Запуск тестов

### Через Maven:

```bash
mvn test
```

### Запуск только Cucumber BDD тестов:

```bash
mvn test -Dtest=RunCucumberTest
```

### Через класс-раннер:

```java
@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "cucumber",
    plugin = {
        "pretty",
        "html:target/cucumber-reports/cucumber.html",
        "json:target/cucumber-reports/cucumber.json"
    },
    monochrome = true
)
public class RunCucumberTest {
}
```

### Параметры @CucumberOptions:

- `features` — путь к .feature файлам
- `glue` — пакет с реализацией шагов
- `plugin` — форматы отчетов (pretty, html, json, junit)
- `monochrome` — цветной вывод в консоль
- `tags` — фильтрация по тегам (`@smoke`, `@regression`)

## Отчеты

После выполнения тестов Cucumber генерирует отчеты:

```
target/
└── cucumber-reports/
    ├── cucumber.html    # HTML-отчет
    └── cucumber.json    # JSON-отчет
```

Откройте `cucumber.html` в браузере для просмотра результатов.

## Теги (Tags)

Можно помечать сценарии тегами для выборочного запуска:

```gherkin
@smoke
Сценарий: Добавление нового студента
  Дано база данных готова к работе
  ...

@regression
Сценарий: Удаление студента
  Дано база данных готова к работе
  ...
```

Запуск только smoke-тестов:

```java
@CucumberOptions(
    tags = "@smoke"
)
```

Запуск всех, кроме slow-тестов:

```java
@CucumberOptions(
    tags = "not @slow"
)
```

## Примеры тестов в проекте

### 1. student_management.feature

Проверяет CRUD операции со студентами:
- ✅ Добавление студента
- ✅ Поиск по ID
- ✅ Обновление email
- ✅ Удаление студента
- ✅ Поиск по фамилии (параметризованный)

### 2. database_connection.feature

Проверяет работу с подключениями:
- ✅ Производительность пула соединений HikariCP
- ✅ Множественные одновременные подключения
- ✅ Защита от SQL-инъекций
- ✅ Статистика пула соединений

## Best Practices

1. **Один сценарий = один тест-кейс** — не перегружайте сценарии
2. **Переиспользуйте шаги** — пишите универсальные @Дано/@Когда/@Тогда
3. **Очистка данных** — всегда используйте @After для cleanup
4. **Говорящие имена** — пишите понятные описания
5. **Независимость** — каждый тест должен работать отдельно
6. **Не тестируйте реализацию** — тестируйте поведение

## Сравнение с JUnit

| JUnit | Cucumber BDD |
|-------|--------------|
| Код на Java | Тесты на естественном языке |
| `@Test` методы | Сценарии (Scenario) |
| `@Before` | `@io.cucumber.java.Before` |
| `@After` | `@io.cucumber.java.After` |
| Для программистов | Для всей команды |
| Техническая документация | Бизнес-документация |

## Полезные ссылки

- [Cucumber Documentation](https://cucumber.io/docs/cucumber/)
- [Cucumber-JVM](https://github.com/cucumber/cucumber-jvm)
- [Gherkin Reference](https://cucumber.io/docs/gherkin/reference/)
- [Cucumber на русском (перевод)](https://cucumber.io/docs/gherkin/languages/)

## Контрольные вопросы

1. Что такое BDD и чем он отличается от TDD?
2. Что такое Gherkin и какие у него ключевые слова?
3. В чем разница между Scenario и Scenario Outline?
4. Как передать параметры в шаги тестов?
5. Зачем нужны хуки @Before и @After?
6. Как работать с таблицами данных (DataTable)?
7. Как запустить только тесты с определенным тегом?
8. В каких случаях BDD эффективнее обычных unit-тестов?

Удачи в тестировании! 🚀