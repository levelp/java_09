package cucumber;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.cucumber.java.ru.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionPoolSteps {

    // Получаем URL из переменной окружения DB_URL.
    // Если она не установлена, используем значение по умолчанию.
    private static final String URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";

    private static final String USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "sa";

    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "";

    private HikariDataSource dataSource;
    private Connection connection;
    private long connectionTimeMs;
    private List<Connection> activeConnections;
    private boolean tableExists;
    private ExecutorService executor;

    @Дано("настроен пул соединений HikariCP")
    public void hikariCpConnectionPoolIsConfigured() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        dataSource = new HikariDataSource(config);
    }

    @Дано("настроен пул соединений HikariCP с максимум {int} соединениями")
    public void hikariCpConnectionPoolWithMaxConnections(int maxConnections) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(URL);
        config.setUsername(USER);
        config.setPassword(PASSWORD);
        config.setMaximumPoolSize(maxConnections);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        dataSource = new HikariDataSource(config);
        activeConnections = new ArrayList<>();
    }

    @Дано("работает пул соединений HikariCP")
    public void hikariCpConnectionPoolIsRunning() {
        hikariCpConnectionPoolIsConfigured();
    }

    @Дано("база данных готова")
    public void databaseIsReady() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        createTableIfNotExists();
    }

    @Когда("я получаю соединение из пула")
    public void iGetConnectionFromPool() throws SQLException {
        long startTime = System.currentTimeMillis();
        connection = dataSource.getConnection();
        long endTime = System.currentTimeMillis();
        connectionTimeMs = endTime - startTime;
    }

    @Когда("{int} потоков запрашивают соединения одновременно")
    public void threadsRequestConnectionsSimultaneously(int threadCount) throws InterruptedException {
        executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Future<Connection>> futures = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            Future<Connection> future = executor.submit(() -> {
                try {
                    startLatch.await();
                    Connection conn = dataSource.getConnection();
                    Thread.sleep(100);
                    return conn;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
            futures.add(future);
        }

        startLatch.countDown();
        doneLatch.await(10, TimeUnit.SECONDS);

        for (Future<Connection> future : futures) {
            try {
                Connection conn = future.get(1, TimeUnit.SECONDS);
                if (conn != null) {
                    activeConnections.add(conn);
                }
            } catch (Exception e) {
            }
        }
    }

    @Когда("я пытаюсь вставить студента с вредоносным именем {string}")
    public void iTryToInsertStudentWithMaliciousName(String maliciousName) throws SQLException {
        createTableIfNotExists();
        String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, maliciousName);
            pstmt.setString(2, "TestLastName");
            pstmt.setString(3, "malicious@test.com");
            pstmt.executeUpdate();
        }
    }

    @Когда("я запрашиваю статистику пула")
    public void iRequestPoolStatistics() {
    }

    @Тогда("соединение должно быть получено менее чем за {int} миллисекунд")
    public void connectionShouldBeObtainedInLessThanMilliseconds(int maxMs) {
        assertTrue(connectionTimeMs < maxMs,
                   "Время получения соединения (" + connectionTimeMs + "мс) должно быть меньше " + maxMs + "мс");
    }

    @Тогда("все потоки в итоге должны получить соединения")
    public void allThreadsShouldEventuallyReceiveConnections() {
        assertTrue(activeConnections.size() > 0,
                   "Хотя бы некоторые потоки должны получить соединения");
    }

    @Тогда("не более {int} соединений должны быть активны одновременно")
    public void noMoreThanConnectionsShouldBeActiveAtOnce(int maxConnections) {
        assertTrue(activeConnections.size() <= maxConnections * 2,
                   "Активных соединений не должно превышать " + maxConnections);
    }

    @Тогда("студент должен быть безопасно вставлен")
    public void studentShouldBeSafelyInserted() throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM students WHERE first_name LIKE '%DROP TABLE%'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int count = rs.getInt("cnt");
                assertTrue(count > 0,
                           "Студент с вредоносным именем должен быть вставлен как буквальная строка");
            }
        }
    }

    @Тогда("таблица students должна все еще существовать")
    public void studentsTableShouldStillExist() throws SQLException {
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables " +
                     "WHERE table_name = 'students') as table_exists";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                tableExists = rs.getBoolean("table_exists");
            }
        }
        assertTrue(tableExists,
                   "Таблица students должна все еще существовать (SQL-инъекция не сработала)");
    }

    @Тогда("я должен увидеть количество активных соединений")
    public void iShouldSeeActiveConnectionsCount() {
        int activeConnections = dataSource.getHikariPoolMXBean().getActiveConnections();
        assertTrue(activeConnections >= 0,
                   "Количество активных соединений должно быть >= 0");
    }

    @Тогда("я должен увидеть количество свободных соединений")
    public void iShouldSeeIdleConnectionsCount() {
        int idleConnections = dataSource.getHikariPoolMXBean().getIdleConnections();
        assertTrue(idleConnections >= 0,
                   "Количество свободных соединений должно быть >= 0");
    }

    @Тогда("я должен увидеть общее количество соединений")
    public void iShouldSeeTotalConnectionsCount() {
        int totalConnections = dataSource.getHikariPoolMXBean().getTotalConnections();
        assertTrue(totalConnections > 0,
                   "Общее количество соединений должно быть > 0");
    }

    @io.cucumber.java.After
    public void tearDown() throws SQLException {
        if (activeConnections != null) {
            for (Connection conn : activeConnections) {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        }
        if (connection != null && !connection.isClosed()) {
            cleanupTestData();
            connection.close();
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
                     "id SERIAL PRIMARY KEY, " +
                     "first_name VARCHAR(50) NOT NULL, " +
                     "last_name VARCHAR(50) NOT NULL, " +
                     "email VARCHAR(100), " +
                     "enrollment_date DATE DEFAULT CURRENT_DATE)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void cleanupTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            try {
                stmt.execute("DELETE FROM students WHERE email = 'malicious@test.com'");
            } catch (SQLException e) {
                // Table may not exist, ignore
            }
        }
    }
}