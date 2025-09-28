# Java 09 - Работа с PostgreSQL

Примеры работы с базой данных PostgreSQL через JDBC.

## Содержание

- [CREATE TABLE - Создание таблиц](#create-table---создание-таблиц)
- [INSERT - Вставка данных](#insert---вставка-данных)
- [SELECT - Выборка данных](#select---выборка-данных)
- [UPDATE - Обновление данных](#update---обновление-данных)
- [DELETE - Удаление данных](#delete---удаление-данных)
- [Работа с JDBC](#работа-с-jdbc)
- [Подготовленные запросы (PreparedStatement)](#подготовленные-запросы-preparedstatement)

---

## CREATE TABLE - Создание таблиц

Оператор `CREATE TABLE` используется для создания новых таблиц в базе данных.

### Синтаксис

```sql
CREATE TABLE table_name (
    column1 datatype constraints,
    column2 datatype constraints,
    column3 datatype constraints,
    ...
);
```

### Примеры

#### Простая таблица пользователей

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    age INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Объяснение:**
- `id SERIAL PRIMARY KEY` - автоинкрементное поле, первичный ключ
- `VARCHAR(50)` - строка переменной длины до 50 символов
- `NOT NULL` - поле обязательно для заполнения
- `UNIQUE` - значения должны быть уникальными
- `DEFAULT CURRENT_TIMESTAMP` - значение по умолчанию

#### Таблица с внешним ключом

```sql
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    product_name VARCHAR(200) NOT NULL,
    quantity INTEGER DEFAULT 1,
    total_price DECIMAL(10, 2),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Объяснение:**
- `FOREIGN KEY` - внешний ключ, связь с таблицей users
- `ON DELETE CASCADE` - при удалении пользователя удаляются его заказы
- `DECIMAL(10, 2)` - число с фиксированной точностью (10 цифр, 2 после запятой)

#### Таблица с несколькими ограничениями

```sql
CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) CHECK (price >= 0),
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    category VARCHAR(50),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Объяснение:**
- `CHECK (price >= 0)` - проверка, что цена не отрицательная
- `TEXT` - текст неограниченной длины
- `BOOLEAN` - логический тип (true/false)

---

## INSERT - Вставка данных

Оператор `INSERT` добавляет новые строки в таблицу.

### Синтаксис

```sql
INSERT INTO table_name (column1, column2, column3, ...)
VALUES (value1, value2, value3, ...);
```

### Примеры

#### Вставка одной записи

```sql
INSERT INTO users (username, email, age)
VALUES ('john_doe', 'john@example.com', 25);
```

**Объяснение:**
- Указываем только нужные поля (id создастся автоматически)
- created_at получит значение по умолчанию

#### Вставка нескольких записей

```sql
INSERT INTO users (username, email, age)
VALUES
    ('alice_smith', 'alice@example.com', 30),
    ('bob_jones', 'bob@example.com', 28),
    ('carol_white', 'carol@example.com', 35);
```

**Объяснение:**
- Можно вставить несколько записей за один запрос
- Повышает производительность при массовой вставке

#### Вставка с возвратом id

```sql
INSERT INTO products (name, price, stock_quantity, category)
VALUES ('Laptop', 999.99, 10, 'Electronics')
RETURNING id, name;
```

**Объяснение:**
- `RETURNING` возвращает указанные поля вставленной записи
- Полезно для получения сгенерированного id

#### Вставка с подзапросом

```sql
INSERT INTO orders (user_id, product_name, quantity, total_price)
SELECT id, 'Premium Subscription', 1, 99.99
FROM users
WHERE username = 'john_doe';
```

**Объяснение:**
- Данные для вставки берутся из результата SELECT
- Удобно для копирования/переноса данных

---

## SELECT - Выборка данных

Оператор `SELECT` используется для извлечения данных из таблиц.

### Синтаксис

```sql
SELECT column1, column2, ...
FROM table_name
WHERE condition
ORDER BY column
LIMIT number;
```

### Примеры

#### Простая выборка всех данных

```sql
SELECT * FROM users;
```

**Объяснение:**
- `*` означает все столбцы
- Возвращает все записи из таблицы

#### Выборка конкретных столбцов

```sql
SELECT username, email, age FROM users;
```

**Объяснение:**
- Указываем только нужные столбцы
- Уменьшает объем передаваемых данных

#### Выборка с условием WHERE

```sql
SELECT username, email
FROM users
WHERE age >= 25 AND age <= 35;
```

**Объяснение:**
- `WHERE` фильтрует записи по условию
- Можно использовать `AND`, `OR`, `NOT`

#### Выборка с LIKE (поиск по шаблону)

```sql
SELECT * FROM users
WHERE email LIKE '%@gmail.com';
```

**Объяснение:**
- `LIKE` - поиск по шаблону
- `%` - любое количество любых символов
- `_` - ровно один любой символ

#### Выборка с сортировкой

```sql
SELECT username, age
FROM users
ORDER BY age DESC, username ASC;
```

**Объяснение:**
- `ORDER BY` сортирует результаты
- `DESC` - по убыванию, `ASC` - по возрастанию (по умолчанию)
- Можно сортировать по нескольким полям

#### Выборка с ограничением количества

```sql
SELECT username, email
FROM users
ORDER BY created_at DESC
LIMIT 10 OFFSET 20;
```

**Объяснение:**
- `LIMIT 10` - вернуть максимум 10 записей
- `OFFSET 20` - пропустить первые 20 записей
- Используется для пагинации

#### Агрегатные функции

```sql
SELECT
    COUNT(*) as total_users,
    AVG(age) as average_age,
    MIN(age) as youngest,
    MAX(age) as oldest
FROM users;
```

**Объяснение:**
- `COUNT()` - подсчет записей
- `AVG()` - среднее значение
- `MIN()`, `MAX()` - минимум и максимум
- `as` создает псевдоним для столбца

#### Группировка с GROUP BY

```sql
SELECT category, COUNT(*) as product_count, AVG(price) as avg_price
FROM products
WHERE is_active = true
GROUP BY category
HAVING COUNT(*) > 5
ORDER BY product_count DESC;
```

**Объяснение:**
- `GROUP BY` группирует записи
- `HAVING` - условие для групп (работает после группировки)
- Агрегатные функции применяются к каждой группе

#### JOIN - соединение таблиц

```sql
SELECT
    u.username,
    u.email,
    o.product_name,
    o.total_price,
    o.order_date
FROM users u
INNER JOIN orders o ON u.id = o.user_id
WHERE o.order_date >= CURRENT_DATE - INTERVAL '30 days'
ORDER BY o.order_date DESC;
```

**Объяснение:**
- `INNER JOIN` - возвращает только совпадающие записи
- `LEFT JOIN` - все записи из левой таблицы + совпадения справа
- `ON` - условие соединения
- `u` и `o` - алиасы для таблиц

---

## UPDATE - Обновление данных

Оператор `UPDATE` изменяет существующие записи в таблице.

### Синтаксис

```sql
UPDATE table_name
SET column1 = value1, column2 = value2, ...
WHERE condition;
```

⚠️ **ВНИМАНИЕ:** Без WHERE будут обновлены ВСЕ записи!

### Примеры

#### Обновление одной записи

```sql
UPDATE users
SET age = 26, email = 'john.doe@example.com'
WHERE username = 'john_doe';
```

**Объяснение:**
- Обновляет возраст и email конкретного пользователя
- `WHERE` гарантирует обновление только нужной записи

#### Обновление нескольких записей

```sql
UPDATE products
SET price = price * 0.9, updated_at = CURRENT_TIMESTAMP
WHERE category = 'Electronics' AND stock_quantity > 0;
```

**Объяснение:**
- Скидка 10% на электронику в наличии
- `price * 0.9` - использование текущего значения
- Обновляется время изменения

#### Обновление с подзапросом

```sql
UPDATE orders
SET total_price = total_price * 1.1
WHERE user_id IN (
    SELECT id FROM users WHERE age < 25
);
```

**Объяснение:**
- Увеличивает цену заказов для пользователей младше 25
- `IN` проверяет вхождение в список значений из подзапроса

#### Условное обновление (CASE)

```sql
UPDATE products
SET price = CASE
    WHEN stock_quantity = 0 THEN price * 1.2
    WHEN stock_quantity < 10 THEN price * 1.1
    ELSE price * 0.95
END,
updated_at = CURRENT_TIMESTAMP;
```

**Объяснение:**
- `CASE` - условная логика в SQL
- Разные изменения цены в зависимости от остатка
- +20% если нет в наличии, +10% если мало, -5% если много

#### Обновление с RETURNING

```sql
UPDATE users
SET age = age + 1
WHERE created_at < CURRENT_DATE - INTERVAL '1 year'
RETURNING id, username, age;
```

**Объяснение:**
- Увеличивает возраст пользователей старше года
- Возвращает обновленные данные

---

## DELETE - Удаление данных

Оператор `DELETE` удаляет записи из таблицы.

### Синтаксис

```sql
DELETE FROM table_name
WHERE condition;
```

⚠️ **ВНИМАНИЕ:** Без WHERE будут удалены ВСЕ записи!

### Примеры

#### Удаление одной записи

```sql
DELETE FROM users
WHERE username = 'john_doe';
```

**Объяснение:**
- Удаляет конкретного пользователя
- Если есть ON DELETE CASCADE, удалятся связанные заказы

#### Удаление нескольких записей

```sql
DELETE FROM orders
WHERE order_date < CURRENT_DATE - INTERVAL '1 year';
```

**Объяснение:**
- Удаляет заказы старше года
- Полезно для очистки старых данных

#### Удаление с подзапросом

```sql
DELETE FROM orders
WHERE user_id IN (
    SELECT id FROM users WHERE email LIKE '%@temporary.com'
);
```

**Объяснение:**
- Удаляет заказы временных пользователей
- Сначала находим id временных пользователей

#### Удаление с RETURNING

```sql
DELETE FROM products
WHERE stock_quantity = 0 AND updated_at < CURRENT_DATE - INTERVAL '6 months'
RETURNING id, name, category;
```

**Объяснение:**
- Удаляет товары без остатка, не обновлявшиеся 6 месяцев
- Возвращает информацию об удаленных товарах

#### Каскадное удаление

```sql
-- При создании таблицы с ON DELETE CASCADE:
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL,
    comment_text TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Удаление заказа автоматически удалит все комментарии:
DELETE FROM orders WHERE id = 123;
```

**Объяснение:**
- `ON DELETE CASCADE` автоматически удаляет связанные записи
- Альтернативы: `ON DELETE SET NULL`, `ON DELETE RESTRICT`

---

## Работа с JDBC

### Подключение к PostgreSQL

```java
import java.sql.*;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String USER = "postgres";
    private static final String PASSWORD = "password";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

### Выполнение SELECT запроса

```java
public List<User> getAllUsers() {
    List<User> users = new ArrayList<>();
    String sql = "SELECT id, username, email, age FROM users";

    try (Connection conn = DatabaseConnection.getConnection();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {

        while (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setAge(rs.getInt("age"));
            users.add(user);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return users;
}
```

### Выполнение INSERT запроса

```java
public boolean insertUser(String username, String email, int age) {
    String sql = "INSERT INTO users (username, email, age) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        pstmt.setString(2, email);
        pstmt.setInt(3, age);

        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;

    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
```

---

## Подготовленные запросы (PreparedStatement)

### Зачем нужны PreparedStatement?

1. **Защита от SQL-инъекций**
2. **Повышение производительности** (запрос компилируется один раз)
3. **Автоматическое экранирование специальных символов**

### Небезопасный способ (НЕ ИСПОЛЬЗУЙТЕ!)

```java
// ОПАСНО! Уязвимость SQL-инъекции!
String username = request.getParameter("username");
String sql = "SELECT * FROM users WHERE username = '" + username + "'";
// Если username = "admin' OR '1'='1", то будут выбраны все пользователи!
```

### Безопасный способ с PreparedStatement

```java
public User findUserByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = ?";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        pstmt.setString(1, username);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            User user = new User();
            user.setId(rs.getInt("id"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            return user;
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }

    return null;
}
```

### Пакетная обработка (Batch)

```java
public void insertMultipleUsers(List<User> users) {
    String sql = "INSERT INTO users (username, email, age) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

        conn.setAutoCommit(false); // Начинаем транзакцию

        for (User user : users) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getAge());
            pstmt.addBatch(); // Добавляем в пакет
        }

        pstmt.executeBatch(); // Выполняем все запросы
        conn.commit(); // Подтверждаем транзакцию

    } catch (SQLException e) {
        e.printStackTrace();
        try {
            conn.rollback(); // Откатываем при ошибке
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
```

---

## Полезные ссылки

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JDBC Tutorial](https://docs.oracle.com/javase/tutorial/jdbc/)
- [SQL Tutorial](https://www.w3schools.com/sql/)

## Требования

- Java 21+
- PostgreSQL 12+
- Maven 3.9+

## Запуск тестов

```bash
mvn test
```

## CI/CD

Проект настроен для автоматической сборки и тестирования:
- **GitHub Actions**: `.github/workflows/maven.yml`
- **GitLab CI**: `.gitlab-ci.yml`