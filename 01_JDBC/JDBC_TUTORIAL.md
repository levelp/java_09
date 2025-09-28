# Урок: Statement vs PreparedStatement и Connection Pooling

## 📚 Теоретическая часть

### 1. Недостатки Statement

#### ❌ Проблема 1: SQL-инъекции

**Statement** создает SQL-запросы путем конкатенации строк, что открывает дверь для атак:

```java
// ОПАСНЫЙ КОД! Не используйте так!
String name = userInput; // Допустим, пользователь ввел: "'; DROP TABLE contact; --"
Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery("SELECT * FROM contact WHERE name = '" + name + "'");
```

Если злоумышленник введет `'; DROP TABLE contact; --`, то запрос станет:
```sql
SELECT * FROM contact WHERE name = ''; DROP TABLE contact; --'
```

Это **удалит всю таблицу**! 😱

#### ❌ Проблема 2: Низкая производительность

Statement компилируется **каждый раз** при выполнении:

```java
for (int i = 0; i < 1000; i++) {
    stmt.executeUpdate("INSERT INTO contact (name) VALUES ('User" + i + "')");
    // Каждый раз база данных парсит и компилирует запрос заново
}
```

#### ❌ Проблема 3: Проблемы с типами данных

```java
// Неправильная обработка спецсимволов
String name = "O'Reilly"; // Апостроф сломает запрос!
stmt.executeQuery("SELECT * FROM contact WHERE name = '" + name + "'");
// Результат: SELECT * FROM contact WHERE name = 'O'Reilly' — ошибка синтаксиса!
```

---

### 2. Преимущества PreparedStatement

#### ✅ Преимущество 1: Защита от SQL-инъекций

PreparedStatement **автоматически экранирует** спецсимволы:

```java
String name = "'; DROP TABLE contact; --";
PreparedStatement pstmt = con.prepareStatement("SELECT * FROM contact WHERE name = ?");
pstmt.setString(1, name); // Безопасно! Строка будет экранирована
ResultSet rs = pstmt.executeQuery();
```

База данных получит:
```sql
SELECT * FROM contact WHERE name = '''; DROP TABLE contact; --'
```
Это будет искать буквальную строку, а не выполнять команду!

#### ✅ Преимущество 2: Высокая производительность

PreparedStatement **компилируется один раз** и переиспользуется:

```java
PreparedStatement pstmt = con.prepareStatement("INSERT INTO contact (name) VALUES (?)");
for (int i = 0; i < 1000; i++) {
    pstmt.setString(1, "User" + i);
    pstmt.executeUpdate(); // База данных не компилирует запрос заново
}
```

**Результат**: в 2-3 раза быстрее!

#### ✅ Преимущество 3: Правильная обработка типов

```java
PreparedStatement pstmt = con.prepareStatement(
    "INSERT INTO contact (name, age, created_at) VALUES (?, ?, ?)"
);
pstmt.setString(1, "O'Reilly");           // Автоматическое экранирование
pstmt.setInt(2, 25);                      // Правильный тип данных
pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis())); // Правильный формат даты
pstmt.executeUpdate();
```

---

### 3. Пул соединений (Connection Pool)

#### Проблема: каждое соединение — это дорого!

Создание нового соединения занимает:
- 🕐 **50-100 мс** — установка TCP-соединения
- 🔐 **Проверка аутентификации**
- 📦 **Выделение памяти**

Если на каждый запрос создавать новое соединение:

```java
// ПЛОХО! Создаем новое соединение для каждого запроса
public User getUser(int id) {
    Connection con = DriverManager.getConnection(url, user, password); // 50-100 мс!
    // ... выполняем запрос за 1-2 мс
    con.close();
}
```

**Результат**: 98% времени тратится на создание соединения!

#### Решение: Connection Pool

**Пул соединений** — это кэш готовых соединений:

```
┌─────────────────────────────────────┐
│      Connection Pool (10 шт)       │
│                                     │
│  [Con1] [Con2] [Con3] [Con4] [Con5]│
│  [Con6] [Con7] [Con8] [Con9] [Con10]│
└─────────────────────────────────────┘
         ↕          ↕          ↕
    [Thread1] [Thread2] [Thread3]
```

**Как это работает:**
1. При старте приложения создается N соединений (например, 10)
2. Когда нужно соединение — берем из пула (0 мс!)
3. После использования — возвращаем в пул, а не закрываем
4. Если все соединения заняты — ждем или создаем новое

**Преимущества:**
- ⚡ **В 50-100 раз быстрее** получение соединения
- 💰 **Экономия ресурсов** БД (не нужно создавать/удалять соединения)
- 🎛️ **Контроль нагрузки** (ограничение максимального количества соединений)
- 🔧 **Автоматическое восстановление** неработающих соединений

#### HikariCP — самый быстрый пул соединений

**HikariCP** (яп. 光 — "свет") — это самая быстрая и надежная библиотека для пула соединений в Java.

**Почему HikariCP?**
- 🏆 **Используется по умолчанию** в Spring Boot
- 🚀 **Самый быстрый** (бенчмарки показывают превосходство над Apache DBCP, C3P0)
- 📦 **Легковесный** (130 KB)
- 🛡️ **Надежный** (автоматическое обнаружение мертвых соединений)

---

## 💻 Практическая часть

### Структура примеров

```
01_JDBC/src/test/java/jdbc/
├── PostgresJDBC.java                    // Оригинальный код (Statement)
├── PostgresJDBCPreparedStatement.java   // Улучшенная версия (PreparedStatement)
├── PostgresJDBCWithPool.java            // С пулом соединений (HikariCP)
└── DatabaseConfig.java                  // Конфигурация подключения
```

### Сравнение производительности

```
Операция: 1000 INSERT
┌──────────────────────────────┬──────────┬─────────────┐
│ Метод                        │ Время    │ Скорость    │
├──────────────────────────────┼──────────┼─────────────┤
│ Statement                    │ 2500 мс  │ 400 оп/сек  │
│ PreparedStatement            │ 850 мс   │ 1176 оп/сек │
│ PreparedStatement + Pool     │ 150 мс   │ 6666 оп/сек │
└──────────────────────────────┴──────────┴─────────────┘

Вывод: Пул соединений + PreparedStatement в 16 раз быстрее!
```

---

## 🎯 Задание

Изучите три файла:
1. `PostgresJDBC.java` — оригинальный код
2. `PostgresJDBCPreparedStatement.java` — с PreparedStatement
3. `PostgresJDBCWithPool.java` — с HikariCP

Обратите внимание на:
- Как параметризуются запросы
- Как используется try-with-resources
- Как настраивается HikariCP
- Как получать и возвращать соединения из пула

---

## 📖 Дополнительные материалы

### Настройки HikariCP

```java
HikariConfig config = new HikariConfig();

// Основные настройки
config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
config.setUsername("postgres");
config.setPassword("123");

// Размер пула
config.setMaximumPoolSize(10);          // Максимум соединений
config.setMinimumIdle(2);               // Минимум простаивающих соединений

// Таймауты
config.setConnectionTimeout(30000);     // 30 сек на получение соединения
config.setIdleTimeout(600000);          // 10 мин простоя перед закрытием
config.setMaxLifetime(1800000);         // 30 мин максимальная жизнь соединения

// Тестирование соединений
config.setConnectionTestQuery("SELECT 1");  // Проверка живости

// Производительность
config.addDataSourceProperty("cachePrepStmts", "true");
config.addDataSourceProperty("prepStmtCacheSize", "250");
config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
```

### Batch операции

Для массовой вставки используйте batch:

```java
PreparedStatement pstmt = con.prepareStatement(
    "INSERT INTO contact (name, surname) VALUES (?, ?)"
);

for (int i = 0; i < 1000; i++) {
    pstmt.setString(1, "Name" + i);
    pstmt.setString(2, "Surname" + i);
    pstmt.addBatch(); // Добавляем в пакет
}

int[] results = pstmt.executeBatch(); // Выполняем все за раз
```

**Результат**: еще в 5-10 раз быстрее!

---

## ✅ Контрольные вопросы

1. Почему Statement уязвим к SQL-инъекциям?
2. Как PreparedStatement защищает от SQL-инъекций?
3. Что такое Connection Pool и зачем он нужен?
4. Почему создание нового Connection — это дорогая операция?
5. Что происходит при вызове `connection.close()` при использовании пула?
6. Какие преимущества дает HikariCP?
7. Когда стоит использовать batch операции?

---

## 🎓 Домашнее задание

1. Добавьте метод для обновления контакта по ID используя PreparedStatement
2. Добавьте метод для удаления контакта с проверкой существования
3. Реализуйте метод поиска контактов по части имени (LIKE)
4. Измерьте время выполнения 1000 INSERT с и без пула соединений
5. Настройте HikariCP с логированием всех операций

Удачи! 🚀