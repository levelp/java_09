package jdbc;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;

/**
 * Версия с Connection Pool (HikariCP).
 *
 * Connection Pool (пул соединений) - это кэш готовых подключений к БД.
 * Вместо создания нового Connection при каждом запросе (50-100 мс),
 * мы берем готовое из пула (0 мс) и возвращаем его обратно после использования.
 *
 * Преимущества:
 * - В 50-100 раз быстрее получение соединения
 * - Экономия ресурсов БД
 * - Контроль нагрузки (ограничение max соединений)
 * - Автоматическое восстановление "мертвых" соединений
 */
public class PostgresJDBCWithPool {

    // Пул соединений - создается один раз при старте приложения
    private static HikariDataSource dataSource;

    static {
        // Инициализация пула соединений
        initializeConnectionPool();
    }

    /**
     * Настройка и создание пула соединений HikariCP
     */
    private static void initializeConnectionPool() {
        System.out.println("=== Инициализация Connection Pool ===");

        HikariConfig config = new HikariConfig();

        // 1. Основные параметры подключения
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres");
        config.setUsername("postgres");
        config.setPassword("123");

        // 2. Размер пула
        config.setMaximumPoolSize(10);  // Максимум 10 соединений
        config.setMinimumIdle(2);       // Минимум 2 "свободных" соединения

        // 3. Таймауты (в миллисекундах)
        config.setConnectionTimeout(30000);      // 30 сек - ожидание свободного соединения
        config.setIdleTimeout(600000);           // 10 мин - время простоя перед закрытием
        config.setMaxLifetime(1800000);          // 30 мин - максимальная жизнь соединения

        // 4. Тестирование соединений
        config.setConnectionTestQuery("SELECT 1");

        // 5. Оптимизация производительности PostgreSQL
        config.addDataSourceProperty("cachePrepStmts", "true");        // Кэш PreparedStatement
        config.addDataSourceProperty("prepStmtCacheSize", "250");      // Размер кэша
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048"); // Макс размер SQL

        // 6. Логирование (для отладки)
        config.setPoolName("PostgreSQL-Pool");
        config.setLeakDetectionThreshold(60000);  // Предупреждение если соединение не вернули за 60 сек

        // Создаем пул
        dataSource = new HikariDataSource(config);

        System.out.println("✓ Connection Pool создан успешно!");
        System.out.println("  Максимум соединений: " + config.getMaximumPoolSize());
        System.out.println("  Минимум idle: " + config.getMinimumIdle());
    }

    /**
     * Получение соединения из пула
     * Этот метод возвращает соединение мгновенно (0 мс вместо 50-100 мс)!
     */
    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Закрытие пула (обычно при завершении приложения)
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Connection Pool закрыт");
        }
    }

    public static void main(String[] args) {
        try {
            // Демонстрируем работу с пулом
            demonstratePoolPerformance();
            demonstrateSelectWithPool();
            demonstrateInsertWithPool();
            demonstrateConcurrentAccess();

        } finally {
            // Закрываем пул при завершении
            closePool();
        }
    }

    /**
     * 1. Демонстрация производительности пула
     * Сравниваем время получения соединения
     */
    private static void demonstratePoolPerformance() {
        System.out.println("\n=== 1. Тест производительности ===");

        try {
            // Измеряем время получения соединения ИЗ ПУЛА
            long poolStart = System.nanoTime();
            try (Connection con = getConnection()) {
                // Просто получили соединение
            }
            long poolTime = (System.nanoTime() - poolStart) / 1_000_000; // В миллисекундах

            System.out.println("⚡ Получение соединения из ПУЛА: " + poolTime + " мс");

            // Для сравнения: получение БЕЗ пула (создание нового каждый раз)
            long directStart = System.nanoTime();
            try (Connection con = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/postgres", "postgres", "123")) {
                // Создали новое соединение
            }
            long directTime = (System.nanoTime() - directStart) / 1_000_000;

            System.out.println("🐌 Создание НОВОГО соединения: " + directTime + " мс");
            System.out.println("📊 Пул быстрее в " + (directTime / Math.max(1, poolTime)) + " раз!");

        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 2. SELECT с использованием пула
     * Обратите внимание: код почти не отличается от версии без пула!
     */
    private static void demonstrateSelectWithPool() {
        System.out.println("\n=== 2. SELECT с использованием пула ===");

        String sql = "SELECT id, name, surname FROM contact LIMIT 5";

        try (Connection con = getConnection();  // Получаем из ПУЛА!
             PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                System.out.printf("ID: %d, Имя: %s %s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("surname"));
            }

            System.out.println("✓ Соединение автоматически вернется в пул после закрытия");

        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 3. INSERT с использованием пула
     */
    private static void demonstrateInsertWithPool() {
        System.out.println("\n=== 3. INSERT с использованием пула ===");

        String sql = "INSERT INTO contact (name, surname) VALUES (?, ?) RETURNING id";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, "Пул");
            pstmt.setString(2, "Соединений");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("✓ Контакт добавлен с ID: " + rs.getInt("id"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 4. Одновременный доступ из нескольких потоков
     * Демонстрирует, как пул управляет соединениями при конкурентном доступе
     */
    private static void demonstrateConcurrentAccess() {
        System.out.println("\n=== 4. Конкурентный доступ (10 потоков) ===");

        // Создаем 10 потоков, каждый выполняет запрос
        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            final int threadId = i + 1;

            threads[i] = new Thread(() -> {
                try (Connection con = getConnection()) {
                    // Каждый поток выполняет свой запрос
                    String sql = "SELECT COUNT(*) as count FROM contact";

                    try (PreparedStatement pstmt = con.prepareStatement(sql);
                         ResultSet rs = pstmt.executeQuery()) {

                        if (rs.next()) {
                            System.out.println("Thread " + threadId + " получил: " +
                                rs.getInt("count") + " контактов");
                        }
                    }

                    // Имитируем работу
                    Thread.sleep(100);

                } catch (SQLException | InterruptedException e) {
                    System.err.println("Thread " + threadId + " ошибка: " + e.getMessage());
                }
            });

            threads[i].start();
        }

        // Ждем завершения всех потоков
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("✓ Все потоки завершены!");
        System.out.println("  Пул автоматически управлял доступом к соединениям");
    }

    /**
     * ДОПОЛНИТЕЛЬНЫЕ МЕТОДЫ ДЛЯ РАБОТЫ С БД
     */

    /**
     * Получить контакт по ID
     */
    public static Contact getContactById(int id) throws SQLException {
        String sql = "SELECT id, name, surname FROM contact WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Contact(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("surname")
                    );
                }
            }
        }

        return null;
    }

    /**
     * Вставить контакт
     */
    public static int insertContact(String name, String surname) throws SQLException {
        String sql = "INSERT INTO contact (name, surname) VALUES (?, ?) RETURNING id";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, surname);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new SQLException("Не удалось получить ID");
    }

    /**
     * Обновить контакт
     */
    public static boolean updateContact(int id, String name, String surname) throws SQLException {
        String sql = "UPDATE contact SET name = ?, surname = ? WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            pstmt.setInt(3, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Удалить контакт
     */
    public static boolean deleteContact(int id) throws SQLException {
        String sql = "DELETE FROM contact WHERE id = ?";

        try (Connection con = getConnection();
             PreparedStatement pstmt = con.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Получить статистику пула
     */
    public static void printPoolStats() {
        System.out.println("\n=== Статистика Connection Pool ===");
        System.out.println("Активных соединений: " + dataSource.getHikariPoolMXBean().getActiveConnections());
        System.out.println("Idle соединений: " + dataSource.getHikariPoolMXBean().getIdleConnections());
        System.out.println("Всего соединений: " + dataSource.getHikariPoolMXBean().getTotalConnections());
        System.out.println("Ожидают соединения: " + dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
    }

    /**
     * Простой класс для хранения данных контакта
     */
    static class Contact {
        private int id;
        private String name;
        private String surname;

        public Contact(int id, String name, String surname) {
            this.id = id;
            this.name = name;
            this.surname = surname;
        }

        @Override
        public String toString() {
            return String.format("Contact{id=%d, name='%s', surname='%s'}", id, name, surname);
        }

        // Геттеры
        public int getId() { return id; }
        public String getName() { return name; }
        public String getSurname() { return surname; }
    }
}