# Учебное пособие по PostgreSQL

PostgreSQL, часто называемый просто Postgres, — это мощная объектно-реляционная система управления базами данных (СУБД) с открытым исходным кодом. Она заслужила прочную репутацию благодаря своей надежности, гибкости и соответствию стандартам SQL.

## 1. Основные возможности PostgreSQL

- **Надежность и стабильность**: PostgreSQL славится своей стабильной работой и надежным хранением данных.
- **Расширяемость**: Пользователи могут создавать собственные типы данных, операторы и функции.
- **Поддержка JSON**: Отличная встроенная поддержка для хранения и обработки данных в формате JSON.
- **Транзакции и ACID**: Полная поддержка транзакций и гарантии ACID (атомарность, согласованность, изоляция, долговечность).
- **Параллельные запросы**: Возможность выполнять запросы параллельно для повышения производительности.

## 2. Установка

PostgreSQL можно установить на большинство операционных систем, включая Linux, Windows и macOS.

- **Linux (Ubuntu/Debian)**:
  ```bash
  sudo apt update
  sudo apt install postgresql postgresql-contrib
  ```
- **Windows/macOS**: Рекомендуется использовать официальный инсталлятор с [сайта PostgreSQL](https://www.postgresql.org/download/).

После установки будет создан пользователь `postgres`. Чтобы получить доступ к командной строке Postgres, нужно переключиться на этого пользователя.

```bash
sudo -i -u postgres
psql
```

## 3. Основы работы в `psql`

`psql` — это интерактивная консоль для работы с PostgreSQL.

- `\l`: Показать список всех баз данных.
- `\c <db_name>`: Подключиться к базе данных.
- `\dt`: Показать таблицы в текущей базе данных.
- `\d <table_name>`: Описать структуру таблицы.
- `\q`: Выйти из `psql`.

### Создание базы данных и пользователя

```sql
-- Создать нового пользователя (роль)
CREATE ROLE myuser WITH LOGIN PASSWORD 'mypassword';

-- Создать новую базу данных
CREATE DATABASE mydatabase;

-- Дать пользователю все права на эту базу данных
GRANT ALL PRIVILEGES ON DATABASE mydatabase TO myuser;
```

## 4. Базовые SQL-команды

Рассмотрим основные команды на примере таблицы `students`.

### CREATE TABLE

Создание таблицы для хранения информации о студентах.

```sql
CREATE TABLE students (
    id SERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    enrollment_date DATE DEFAULT CURRENT_DATE
);
```
- `SERIAL PRIMARY KEY`: Автоинкрементный целочисленный первичный ключ.
- `VARCHAR(n)`: Строка с максимальной длиной `n`.
- `NOT NULL`: Поле не может быть пустым.
- `UNIQUE`: Значения в этом поле должны быть уникальными.
- `DEFAULT`: Значение по умолчанию.

### INSERT

Добавление данных в таблицу.

```sql
INSERT INTO students (first_name, last_name, email) VALUES
('Иван', 'Иванов', 'ivan.ivanov@example.com'),
('Петр', 'Петров', 'petr.petrov@example.com');
```

### SELECT

Получение данных из таблицы.

```sql
-- Получить все данные из таблицы
SELECT * FROM students;

-- Получить только имена и фамилии
SELECT first_name, last_name FROM students;

-- Получить студента с конкретным id
SELECT * FROM students WHERE id = 1;
```

### UPDATE

Обновление существующих данных.

```sql
UPDATE students
SET email = 'new.email@example.com'
WHERE id = 1;
```

### DELETE

Удаление данных.

```sql
DELETE FROM students WHERE id = 2;
```

## 5. Подключение из Java (JDBC)

Для подключения к PostgreSQL из Java-приложения вам понадобится JDBC-драйвер. Его можно добавить в ваш `pom.xml`:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version> <!-- Используйте актуальную версию -->
</dependency>
```

### Пример кода для подключения

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

public class PostgresExample {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/mydatabase";
        String user = "myuser";
        String password = "mypassword";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (conn != null) {
                System.out.println("Успешное подключение к PostgreSQL!");

                Statement stmt = conn.createStatement();
                String sql = "SELECT * FROM students";
                ResultSet rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id") +
                                       ", Имя: " + rs.getString("first_name") +
                                       ", Фамилия: " + rs.getString("last_name"));
                }

            } else {
                System.out.println("Не удалось подключиться.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```
Этот пример демонстрирует базовое подключение, выполнение `SELECT` запроса и вывод результатов в консоль.

---

## 6. Продвинутая работа с JDBC: PreparedStatement и Пулы Соединений

### 6.1. PreparedStatement: Безопасность и Производительность

`Statement`, который мы использовали ранее, имеет два серьезных недостатка:
1.  **Уязвимость к SQL-инъекциям**: Если вставлять в запрос данные от пользователя напрямую, злоумышленник может передать вредоносный SQL-код, который изменит логику запроса и может привести к утечке или потере данных.
2.  **Низкая производительность**: Каждый раз, когда вы выполняете запрос через `Statement`, СУБД заново его парсит, планирует и выполняет.

`PreparedStatement` решает обе эти проблемы. Он предварительно компилирует SQL-запрос с "плейсхолдерами" (`?`), а затем безопасно подставляет в них данные.

**Пример с `PreparedStatement`:**
```java
String sql = "SELECT * FROM students WHERE id = ?";
try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, 1); // Подставляем значение 1 на место первого '?'
    ResultSet rs = pstmt.executeQuery();
    // ... обработка ResultSet ...
}
```

### 6.2. Пулы Соединений (Connection Pools)

Установка соединения с базой данных — это "дорогая" операция, требующая времени и ресурсов. В реальных приложениях, где запросы к базе данных происходят постоянно, создавать новое соединение на каждый запрос — крайне неэффективно.

**Пул соединений** — это набор готовых, открытых соединений с БД. Когда приложению нужно соединение, оно берет его из пула, использует и возвращает обратно. Это значительно ускоряет работу.

Одной из самых популярных библиотек для создания пула соединений является **HikariCP**.

**Контрольные вопросы:**
1.  Что такое SQL-инъекция и как `PreparedStatement` помогает ее предотвратить?
2.  Почему `PreparedStatement` может быть быстрее, чем `Statement`, при выполнении одного и того же запроса много раз?
3.  Объясните своими словами, зачем нужен пул соединений. Какую проблему он решает?

---

## 7. Введение в ORM: Hibernate/JPA

Писать SQL-запросы вручную и преобразовывать `ResultSet` в Java-объекты — утомительно и чревато ошибками. **ORM (Object-Relational Mapping)** — это технология, которая "связывает" ваши Java-классы с таблицами в базе данных, позволяя вам работать с объектами, а не с SQL.

**JPA (Java Persistence API)** — это стандартная спецификация для ORM в Java. **Hibernate** — самая популярная реализация этого стандарта.

### 7.1. Зачем нужен Hibernate?

#### Проблемы чистого JDBC:

```java
String sql = "SELECT * FROM students WHERE id = ?";
try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
    pstmt.setInt(1, id);
    ResultSet rs = pstmt.executeQuery();
    if (rs.next()) {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));
        return student;
    }
}
```

**Недостатки:**
- Много шаблонного (boilerplate) кода
- Ручное преобразование `ResultSet` → объект
- Ручное составление SQL-запросов
- Ручное управление связями между таблицами
- Сложности при работе с транзакциями

#### Решение с Hibernate:

```java
Session session = sessionFactory.openSession();
Student student = session.get(Student.class, id);
session.close();
```

**Всего 3 строки!** 🎉

### 7.2. Ключевые концепции

#### Entity (Сущность)

Java-класс, который отображается на таблицу в БД. Помечается аннотацией `@Entity`.

```java
import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(unique = true)
    private String email;

    @Column(name = "enrollment_date")
    private LocalDate enrollmentDate;

    public Student() {}

    public Student(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.enrollmentDate = LocalDate.now();
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", firstName='" + firstName + "', " +
               "lastName='" + lastName + "', email='" + email + "'}";
    }
}
```

**Объяснение аннотаций:**
- `@Entity` — помечает класс как JPA-сущность
- `@Table(name = "students")` — указывает имя таблицы в БД
- `@Id` — первичный ключ
- `@GeneratedValue` — автоматическая генерация значения (SERIAL в PostgreSQL)
- `@Column` — настройка колонки (имя, ограничения)

#### SessionFactory и Session

- **SessionFactory** — фабрика для создания сессий. Создается **один раз** при старте приложения.
- **Session** — объект для работы с БД. Создается для каждой операции/транзакции.

```java
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    static {
        try {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Student.class)
                    .buildSessionFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
```

### 7.3. Конфигурация Hibernate

**hibernate.cfg.xml** (в `src/main/resources`):

```xml
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration PUBLIC
        "-//Hibernate/Hibernate Configuration DTD 3.0//EN"
        "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">

<hibernate-configuration>
    <session-factory>
        <!-- Настройки подключения к PostgreSQL -->
        <property name="hibernate.connection.driver_class">
            org.postgresql.Driver
        </property>
        <property name="hibernate.connection.url">
            jdbc:postgresql://localhost:5432/mydatabase
        </property>
        <property name="hibernate.connection.username">myuser</property>
        <property name="hibernate.connection.password">mypassword</property>

        <!-- Пул соединений (встроенный в Hibernate, для production используйте HikariCP) -->
        <property name="hibernate.connection.pool_size">10</property>

        <!-- SQL диалект для PostgreSQL -->
        <property name="hibernate.dialect">
            org.hibernate.dialect.PostgreSQLDialect
        </property>

        <!-- Вывод SQL в консоль (для отладки) -->
        <property name="hibernate.show_sql">true</property>
        <property name="hibernate.format_sql">true</property>

        <!-- Автоматическое создание/обновление схемы БД -->
        <!-- validate: проверка, update: обновление, create: пересоздание, create-drop: пересоздание + удаление -->
        <property name="hibernate.hbm2ddl.auto">update</property>

        <!-- Timezone для Java 8+ Date/Time API -->
        <property name="hibernate.jdbc.time_zone">UTC</property>
    </session-factory>
</hibernate-configuration>
```

**Важные настройки:**
- `hibernate.dialect` — SQL-диалект (для PostgreSQL, MySQL, Oracle и т.д.)
- `hibernate.show_sql` — показывать SQL-запросы в консоли
- `hibernate.hbm2ddl.auto` — автоматическое управление схемой:
  - `validate` — только проверка
  - `update` — обновление схемы (добавление новых таблиц/колонок)
  - `create` — пересоздание схемы при каждом запуске
  - `create-drop` — создание при старте, удаление при завершении

### 7.4. CRUD операции с Hibernate

#### CREATE (INSERT)

```java
public class StudentDAO {

    public void saveStudent(Student student) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            session.save(student);
            tx.commit();
            System.out.println("Студент сохранен с ID: " + student.getId());
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
```

**Использование:**
```java
StudentDAO dao = new StudentDAO();
Student student = new Student("Иван", "Иванов", "ivan@example.com");
dao.saveStudent(student);
```

Hibernate сам:
- Сгенерирует SQL: `INSERT INTO students (first_name, last_name, email, enrollment_date) VALUES (?, ?, ?, ?)`
- Заполнит ID после вставки
- Управит транзакцией

#### READ (SELECT)

##### Получение по ID:

```java
public Student getStudentById(int id) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    try {
        return session.get(Student.class, id);
    } finally {
        session.close();
    }
}
```

##### Получение всех записей:

```java
public List<Student> getAllStudents() {
    Session session = HibernateUtil.getSessionFactory().openSession();
    try {
        return session.createQuery("FROM Student", Student.class).list();
    } finally {
        session.close();
    }
}
```

**Обратите внимание:** `FROM Student` — это **HQL (Hibernate Query Language)**, не SQL!
- Указываем **имя класса**, а не таблицы
- Hibernate сам переведет в SQL

##### Поиск по условию (HQL):

```java
public List<Student> findStudentsByLastName(String lastName) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    try {
        return session.createQuery(
            "FROM Student WHERE lastName = :lastName", Student.class)
            .setParameter("lastName", lastName)
            .list();
    } finally {
        session.close();
    }
}
```

**`:lastName`** — именованный параметр (безопасно от SQL-инъекций!)

#### UPDATE

```java
public void updateStudent(Student student) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction tx = null;

    try {
        tx = session.beginTransaction();
        session.update(student);
        tx.commit();
        System.out.println("Студент обновлен");
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        e.printStackTrace();
    } finally {
        session.close();
    }
}
```

**Использование:**
```java
Student student = dao.getStudentById(1);
student.setEmail("new.email@example.com");
dao.updateStudent(student);
```

#### DELETE

```java
public void deleteStudent(int id) {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Transaction tx = null;

    try {
        tx = session.beginTransaction();
        Student student = session.get(Student.class, id);
        if (student != null) {
            session.delete(student);
            System.out.println("Студент удален");
        }
        tx.commit();
    } catch (Exception e) {
        if (tx != null) tx.rollback();
        e.printStackTrace();
    } finally {
        session.close();
    }
}
```

### 7.5. HQL vs SQL

#### HQL (Hibernate Query Language)

```java
String hql = "FROM Student WHERE lastName = :lastName AND enrollmentDate > :date";
List<Student> students = session.createQuery(hql, Student.class)
    .setParameter("lastName", "Иванов")
    .setParameter("date", LocalDate.now().minusYears(1))
    .list();
```

**Преимущества HQL:**
- Работает с **классами и полями**, а не таблицами и колонками
- Кроссплатформенность (тот же HQL работает с PostgreSQL, MySQL, Oracle)
- Поддержка наследования и полиморфизма
- Автоматическая защита от SQL-инъекций

#### Native SQL

Если нужен специфичный для СУБД SQL:

```java
String sql = "SELECT * FROM students WHERE EXTRACT(YEAR FROM enrollment_date) = :year";
List<Student> students = session.createNativeQuery(sql, Student.class)
    .setParameter("year", 2024)
    .list();
```

### 7.6. Связи между таблицами

#### One-to-Many (Один ко многим)

**Пример:** Один студент → много курсов

```java
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    // Конструкторы, геттеры, сеттеры
}
```

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    // Методы для управления связями
    public void addCourse(Course course) {
        courses.add(course);
        course.setStudent(this);
    }

    public void removeCourse(Course course) {
        courses.remove(course);
        course.setStudent(null);
    }
}
```

**Использование:**
```java
Student student = new Student("Иван", "Иванов", "ivan@example.com");
Course math = new Course("Математика");
Course physics = new Course("Физика");

student.addCourse(math);
student.addCourse(physics);

session.save(student); // Сохранятся и курсы благодаря cascade = CascadeType.ALL
```

#### Many-to-Many (Многие ко многим)

**Пример:** Студенты ↔ Курсы (один студент на многих курсах, один курс у многих студентов)

```java
@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String firstName;
    private String lastName;

    @ManyToMany
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}
```

```java
@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

Hibernate автоматически создаст промежуточную таблицу `student_courses`.

### 7.7. Интеграция с HikariCP

Встроенный пул соединений Hibernate слабый. Для production используйте HikariCP:

**pom.xml:**
```xml
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>6.2.1</version>
</dependency>
```

**hibernate.cfg.xml:**
```xml
<!-- Убираем hibernate.connection.pool_size -->

<!-- Добавляем настройки HikariCP -->
<property name="hibernate.connection.provider_class">
    org.hibernate.hikaricp.internal.HikariCPConnectionProvider
</property>
<property name="hibernate.hikari.minimumIdle">5</property>
<property name="hibernate.hikari.maximumPoolSize">20</property>
<property name="hibernate.hikari.idleTimeout">300000</property>
```

### 7.8. Преимущества Hibernate

✅ **Меньше кода** — в 3-5 раз меньше, чем с JDBC
✅ **Автоматическое преобразование** объектов ↔ таблицы
✅ **Управление связями** (One-to-Many, Many-to-Many)
✅ **Кэширование** (1st level, 2nd level cache)
✅ **Lazy/Eager loading** — контроль загрузки связанных данных
✅ **HQL/JPQL** — объектно-ориентированные запросы
✅ **Кроссплатформенность** — один код для разных БД
✅ **Транзакции и Dirty Checking** — автоматическое отслеживание изменений

### 7.9. Недостатки Hibernate

❌ **Сложность изучения** — много концепций и аннотаций
❌ **"Магия" под капотом** — не всегда понятно, какой SQL генерируется
❌ **N+1 проблема** — может генерировать много лишних запросов
❌ **Overhead** — для простых запросов может быть медленнее JDBC
❌ **Сложная отладка** — иногда трудно понять, почему что-то не работает

### 7.10. Когда использовать Hibernate?

**Используйте Hibernate когда:**
- Сложная доменная модель с множеством связей
- Нужна кроссплатформенность (разные БД)
- Много CRUD операций
- Команда знакома с ORM

**Используйте чистый JDBC когда:**
- Простые запросы (1-2 таблицы)
- Критична производительность
- Сложные аналитические запросы
- Работа с legacy базой со сложной структурой

**Контрольные вопросы:**
1.  Что такое ORM и какую основную проблему она решает для разработчика?
2.  Чем отличается HQL от обычного SQL?
3.  Какие аннотации JPA вы знаете и для чего они используются (`@Entity`, `@Id`, `@Column`, `@ManyToOne`, `@OneToMany`)?
4.  Что такое Session и SessionFactory? Какая между ними разница?
5.  В чем преимущества и недостатки использования Hibernate по сравнению с JDBC?
6.  Что такое Lazy Loading и Eager Loading?
7.  Объясните, что такое `cascade = CascadeType.ALL` и `orphanRemoval = true`.

---

## 8. Основы веб-приложений: Сервлеты и паттерн MVC

**Сервлет** — это Java-класс, который может принимать HTTP-запросы и генерировать HTTP-ответы. Это основа веб-приложений на Java.

**Паттерн MVC (Model-View-Controller)** — это архитектурный подход к построению приложений, который разделяет их на три части:
- **Model (Модель)**: Данные и бизнес-логика (например, классы-сущности, сервисы для работы с БД).
- **View (Представление)**: Пользовательский интерфейс (в нашем случае — JSP-страницы).
- **Controller (Контроллер)**: Обрабатывает ввод пользователя, взаимодействует с Моделью и выбирает Представление для отображения. Роль контроллера выполняют сервлеты.

**Жизненный цикл запроса в MVC:**
1.  Пользователь отправляет запрос (например, кликает на ссылку).
2.  Сервлет-контроллер принимает запрос.
3.  Контроллер обращается к Модели, чтобы получить или изменить данные.
4.  Контроллер передает данные в Представление (JSP).
5.  JSP генерирует HTML-страницу и отправляет ее пользователю.

**Контрольные вопросы:**
1.  Какова роль сервлета в веб-приложении?
2.  Опишите ответственность каждой из трех компонент MVC: Модели, Представления и Контроллера.
3.  Почему разделение на Model, View и Controller считается хорошей практикой в разработке?

---

## 9. Многопоточность в Swing-приложениях

Графический интерфейс в Swing работает в одном специальном потоке, который называется **Event Dispatch Thread (EDT)**. Вся отрисовка компонентов и обработка событий (клики мыши, нажатия клавиш) происходит именно в этом потоке.

Если вы запустите в EDT долгую операцию (например, загрузку файла из сети или сложный запрос к БД), интерфейс "зависнет" и перестанет отвечать на действия пользователя.

**Правило**: Никогда не выполняйте долгие, блокирующие операции в Event Dispatch Thread.

Для выполнения таких задач в фоновом потоке и безопасного обновления UI из него в Swing есть специальный класс — `SwingWorker`.

**Как работает `SwingWorker`:**
1.  Вы создаете свой класс, наследующийся от `SwingWorker`.
2.  Долгую операцию вы помещаете в метод `doInBackground()`. Этот код будет выполнен в фоновом потоке.
3.  Когда операция завершена, вы можете безопасно обновить UI в методе `done()`, который выполняется уже в EDT.

**Контрольные вопросы:**
1.  Что такое Event Dispatch Thread (EDT) и почему он так важен в Swing?
2.  Что произойдет с вашим Swing-приложением, если вы попытаетесь выполнить 10-секундный запрос к базе данных прямо в обработчике нажатия кнопки?
3.  Какую проблему решает класс `SwingWorker`? Опишите назначение его основных методов (`doInBackground`, `done`).