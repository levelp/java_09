package jdbc;

// Импортируем необходимые классы для работы с JDBC и PostgreSQL.
import java.sql.*; // Основной пакет JDBC, содержит классы Connection, Statement, ResultSet и т.д.
import java.util.Enumeration; // Используется для перечисления драйверов.

/**
 * Этот класс демонстрирует, как работать с базой данных PostgreSQL,
 * используя стандартный Java-интерфейс JDBC (Java Database Connectivity).
 */
public class PostgresJDBC {
    public static void main(String[] args) throws Exception {
        /*
         * Перед тем как регистрировать наш драйвер, хорошей практикой является
         * удаление всех ранее зарегистрированных JDBC-драйверов.
         * Это помогает избежать конфликтов, если в classpath случайно попали
         * несколько разных драйверов для одной и той же СУБД.
         */
        Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            System.out.println("Найден драйвер: " + driver);
            try {
                // Пытаемся "отменить регистрацию" драйвера.
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                // Если произошла ошибка, выводим ее в консоль.
                e.printStackTrace();
            }
        }

        /*
         * ШАГ 1: Регистрация JDBC-драйвера.
         *
         * Class.forName("org.postgresql.Driver") — это стандартный способ
         * динамической загрузки класса драйвера. Когда класс загружается,
         * он автоматически регистрирует себя в DriverManager.
         * Это самый распространенный и рекомендуемый способ.
         */
        Class.forName("org.postgresql.Driver");

        /*
         * ШАГ 2: Установка соединения с базой данных.
         *
         * Мы используем DriverManager для получения соединения (Connection).
         * Для этого мы передаем ему:
         * 1. URL базы данных (строка подключения).
         *    Формат: jdbc:<СУБД>://<хост>:<порт>/<имя_базы_данных>
         * 2. Имя пользователя.
         * 3. Пароль.
         */
        Connection con = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", // URL
                "postgres", // Пользователь
                "123");     // Пароль

        // Если соединение установлено успешно, `con` не будет null.
        System.out.println("Соединение с базой данных установлено!");

        /*
         * ШАГ 3: Создание объекта Statement.
         *
         * Statement — это объект, который позволяет нам выполнять SQL-запросы.
         * Мы создаем его из нашего объекта Connection.
         */
        Statement query = con.createStatement();

        /*
         * ШАГ 4: Выполнение SQL-запроса.
         *
         * query.executeQuery() используется для запросов, которые возвращают данные (SELECT).
         * Результат возвращается в виде объекта ResultSet.
         */
        System.out.println("\nВыполняем запрос SELECT * FROM contact...");
        ResultSet resultSet = query.executeQuery("SELECT * FROM contact");

        /*
         * ШАГ 5: Обработка результатов.
         *
         * ResultSet представляет собой набор строк из таблицы.
         * Мы используем цикл while с resultSet.next() для перебора всех строк.
         * resultSet.next() возвращает true, если есть следующая строка, и false, если нет.
         */
        while (resultSet.next()) {
            // Внутри цикла мы можем получать данные из колонок по их имени.
            System.out.println("Имя: " + resultSet.getString("name"));
        }
        // Важно закрывать ResultSet после использования, чтобы освободить ресурсы.
        resultSet.close();

        /*
         * Пример более сложного запроса с JOIN.
         * Этот запрос объединяет таблицы 'phone' и 'contact'.
         */
        System.out.println("\nВыполняем запрос с JOIN...");
        ResultSet res2 = query.executeQuery("SELECT contact.id, contact.name, contact.surname, " +
                " phone.number FROM phone LEFT JOIN contact " +
                "ON contact.id = phone.contact_id");

        /*
         * Использование ResultSetMetaData для получения информации о колонках.
         * Это полезно, когда мы не знаем заранее, какие колонки вернет запрос.
         */
        ResultSetMetaData rsmd = res2.getMetaData();
        int columnCount = rsmd.getColumnCount(); // Получаем количество колонок.

        while (res2.next()) {
            System.out.println("--- Новая строка ---");
            // Проходим по всем колонкам текущей строки.
            for (int i = 1; i <= columnCount; i++) {
                // Получаем имя колонки и ее значение.
                String columnName = rsmd.getColumnName(i);
                String value = res2.getString(i);
                System.out.println(columnName + " = " + value);
            }
        }
        // Не забываем закрыть и этот ResultSet.
        res2.close();

        /*
         * Для выполнения запросов, которые изменяют данные (INSERT, UPDATE, DELETE)
         * или структуру базы данных (CREATE, DROP), используется метод executeUpdate().
         * Он возвращает количество измененных строк.
         *
         * Ниже приведены закомментированные примеры.
         */
        // query.executeUpdate("UPDATE contact SET name = 'НовоеИмя' WHERE id = 1");
        // query.executeUpdate("DELETE FROM contact WHERE id = 2");
        // query.executeUpdate("INSERT INTO contact (name, surname) VALUES ('Имя', 'Фамилия')");
        // query.execute("CREATE TABLE new_table (id INT PRIMARY KEY)");

        // ШАГ 6: Закрытие соединения.
        // Это самый важный шаг. Всегда закрывайте соединение в блоке finally
        // или используйте try-with-resources, чтобы гарантировать его закрытие.
        // В данном примере мы не используем try-finally, но в реальных приложениях это обязательно.
        query.close(); // Закрываем Statement.
        con.close();   // Закрываем Connection.
        System.out.println("\nСоединение с базой данных закрыто.");
    }
}
