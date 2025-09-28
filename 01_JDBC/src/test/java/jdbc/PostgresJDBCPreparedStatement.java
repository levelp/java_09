package jdbc;

import java.sql.*;

/**
 * Улучшенная версия PostgresJDBC с использованием PreparedStatement.
 *
 * PreparedStatement решает 3 главные проблемы Statement:
 * 1. Защита от SQL-инъекций
 * 2. Лучшая производительность (запрос компилируется один раз)
 * 3. Правильная обработка типов данных и спецсимволов
 */
public class PostgresJDBCPreparedStatement {

    // Константы подключения (в реальном приложении храните в properties файле!)
    private static final String URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123";

    public static void main(String[] args) {
        // Демонстрируем различные операции с PreparedStatement
        demonstrateSelectAll();
        demonstrateSelectById();
        demonstrateInsert();
        demonstrateUpdate();
        demonstrateDelete();
        demonstrateBatchInsert();
        demonstrateSQLInjectionProtection();
    }

    /**
     * 1. SELECT всех записей
     * PreparedStatement можно использовать даже без параметров
     */
    private static void demonstrateSelectAll() {
        System.out.println("\n=== 1. SELECT всех контактов ===");

        // try-with-resources автоматически закроет ресурсы
        String sql = "SELECT id, name, surname FROM contact ORDER BY id";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String surname = rs.getString("surname");
                System.out.printf("ID: %d, Имя: %s, Фамилия: %s%n", id, name, surname);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при выборке всех контактов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 2. SELECT по ID (с параметром)
     * Это безопасно! Параметр автоматически экранируется
     */
    private static void demonstrateSelectById() {
        System.out.println("\n=== 2. SELECT контакта по ID ===");

        int searchId = 1;
        String sql = "SELECT id, name, surname FROM contact WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // Устанавливаем значение параметра (индексы начинаются с 1!)
            pstmt.setInt(1, searchId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("Найден: ID=%d, Имя=%s, Фамилия=%s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname"));
                } else {
                    System.out.println("Контакт с ID=" + searchId + " не найден");
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске контакта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 3. INSERT с возвратом сгенерированного ID
     * PreparedStatement может вернуть автоинкрементный ID
     */
    private static void demonstrateInsert() {
        System.out.println("\n=== 3. INSERT нового контакта ===");

        String sql = "INSERT INTO contact (name, surname) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             // Statement.RETURN_GENERATED_KEYS - запрашиваем возврат ID
             PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Устанавливаем параметры
            pstmt.setString(1, "Иван");
            pstmt.setString(2, "Иванов");

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Добавлено строк: " + rowsAffected);

            // Получаем сгенерированный ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long newId = generatedKeys.getLong(1);
                    System.out.println("Новый ID: " + newId);
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при вставке контакта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 4. UPDATE записи по ID
     * PreparedStatement правильно обрабатывает спецсимволы (апостроф, кавычки)
     */
    private static void demonstrateUpdate() {
        System.out.println("\n=== 4. UPDATE контакта ===");

        String sql = "UPDATE contact SET name = ?, surname = ? WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // Даже со спецсимволами - безопасно!
            pstmt.setString(1, "O'Brien");  // Апостроф обработается корректно
            pstmt.setString(2, "Smith-Johnson");
            pstmt.setInt(3, 1);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Контакт успешно обновлен");
            } else {
                System.out.println("Контакт не найден");
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при обновлении контакта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 5. DELETE записи по ID
     */
    private static void demonstrateDelete() {
        System.out.println("\n=== 5. DELETE контакта ===");

        // Сначала создадим тестовую запись
        String insertSql = "INSERT INTO contact (name, surname) VALUES (?, ?) RETURNING id";
        String deleteSql = "DELETE FROM contact WHERE id = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

            // Вставляем тестовую запись
            int testId;
            try (PreparedStatement pstmt = con.prepareStatement(insertSql)) {
                pstmt.setString(1, "Тест");
                pstmt.setString(2, "Для Удаления");

                try (ResultSet rs = pstmt.executeQuery()) {
                    rs.next();
                    testId = rs.getInt("id");
                    System.out.println("Создан тестовый контакт с ID: " + testId);
                }
            }

            // Удаляем его
            try (PreparedStatement pstmt = con.prepareStatement(deleteSql)) {
                pstmt.setInt(1, testId);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Контакт успешно удален");
                } else {
                    System.out.println("Контакт не найден");
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при удалении контакта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 6. BATCH INSERT - массовая вставка
     * В 5-10 раз быстрее чем обычные INSERT в цикле!
     */
    private static void demonstrateBatchInsert() {
        System.out.println("\n=== 6. BATCH INSERT (массовая вставка) ===");

        String sql = "INSERT INTO contact (name, surname) VALUES (?, ?)";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // Отключаем автокоммит для транзакции
            con.setAutoCommit(false);

            long startTime = System.currentTimeMillis();

            // Добавляем 100 контактов в пакет
            for (int i = 1; i <= 100; i++) {
                pstmt.setString(1, "BatchUser" + i);
                pstmt.setString(2, "Surname" + i);
                pstmt.addBatch();  // Добавляем в пакет, не выполняя

                // Каждые 50 записей - выполняем пакет
                if (i % 50 == 0) {
                    pstmt.executeBatch();  // Выполняем весь пакет
                    pstmt.clearBatch();    // Очищаем пакет
                }
            }

            // Выполняем оставшиеся записи
            pstmt.executeBatch();

            // Подтверждаем транзакцию
            con.commit();

            long endTime = System.currentTimeMillis();

            System.out.println("Добавлено 100 контактов за " + (endTime - startTime) + " мс");
            System.out.println("Это в 5-10 раз быстрее обычных INSERT!");

        } catch (SQLException e) {
            System.err.println("Ошибка при batch вставке: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 7. Демонстрация защиты от SQL-инъекций
     * PreparedStatement автоматически экранирует опасные символы
     */
    private static void demonstrateSQLInjectionProtection() {
        System.out.println("\n=== 7. Защита от SQL-инъекций ===");

        // Злонамеренный ввод, который мог бы удалить таблицу
        String maliciousInput = "'; DROP TABLE contact; --";

        String sql = "SELECT * FROM contact WHERE name = ?";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // PreparedStatement безопасно обработает этот ввод
            pstmt.setString(1, maliciousInput);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("Запрос выполнен безопасно!");
                System.out.println("Ищем буквальную строку: " + maliciousInput);

                if (!rs.next()) {
                    System.out.println("Контакт не найден (что и ожидалось)");
                    System.out.println("Таблица НЕ удалена - защита сработала! ✓");
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n❗ ВАЖНО:");
        System.out.println("Если бы мы использовали Statement с конкатенацией строк,");
        System.out.println("то запрос был бы:");
        System.out.println("SELECT * FROM contact WHERE name = ''; DROP TABLE contact; --'");
        System.out.println("И таблица была бы удалена! 😱");
    }

    /**
     * ДОПОЛНИТЕЛЬНЫЙ ПРИМЕР: Сложный запрос с несколькими параметрами
     */
    public static void searchContacts(String namePattern, String surnamePattern) {
        System.out.println("\n=== BONUS: Поиск с LIKE ===");

        String sql = "SELECT * FROM contact WHERE name LIKE ? AND surname LIKE ? ORDER BY id";

        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            // % - любое количество символов, _ - один символ
            pstmt.setString(1, "%" + namePattern + "%");
            pstmt.setString(2, "%" + surnamePattern + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                int count = 0;
                while (rs.next()) {
                    System.out.printf("ID: %d, Имя: %s %s%n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname"));
                    count++;
                }
                System.out.println("Найдено записей: " + count);
            }

        } catch (SQLException e) {
            System.err.println("Ошибка при поиске: " + e.getMessage());
            e.printStackTrace();
        }
    }
}