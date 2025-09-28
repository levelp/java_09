### INSERT

Добавление данных в таблицу.

#### Вставка одной строки

```sql
INSERT INTO students (first_name, last_name, email) 
VALUES ('Иван', 'Иванов', 'ivan.ivanov@example.com');
```

#### Вставка нескольких строк

Можно вставить несколько записей одним запросом для эффективности.

```sql
INSERT INTO students (first_name, last_name, email) VALUES
('Петр', 'Петров', 'petr.petrov@example.com'),
('Анна', 'Сидорова', 'anna.sidorova@example.com'),
('Елена', 'Кузнецова', 'elena.kuznetsova@example.com');
```

#### Вставка с возвратом данных

PostgreSQL позволяет вернуть данные из только что вставленной строки с помощью `RETURNING`. Это очень удобно, чтобы сразу получить сгенерированный `id`.

```sql
INSERT INTO students (first_name, last_name, email) 
VALUES ('Мария', 'Васильева', 'maria.vasileva@example.com')
RETURNING id, enrollment_date;
```
Этот запрос вставит новую запись и вернет ее `id` и `enrollment_date`.

### SELECT

Получение данных из таблицы.