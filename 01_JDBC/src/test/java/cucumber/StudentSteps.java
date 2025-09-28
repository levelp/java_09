package cucumber;

import io.cucumber.java.ru.*;
import io.cucumber.datatable.DataTable;

import java.sql.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StudentSteps {

    private static final String URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH";

    private static final String USER = System.getenv("DB_USER") != null
            ? System.getenv("DB_USER")
            : "sa";

    private static final String PASSWORD = System.getenv("DB_PASSWORD") != null
            ? System.getenv("DB_PASSWORD")
            : "";

    private Connection connection;
    private Integer lastInsertedId;
    private Student foundStudent;
    private int foundCount;

    @Дано("база данных готова к работе")
    public void databaseIsReady() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
        createTableIfNotExists();
    }

    @Дано("в базе есть студент с именем {string} и фамилией {string} и email {string}")
    public void studentExistsInDatabase(String firstName, String lastName, String email) throws SQLException {
        String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lastInsertedId = rs.getInt(1);
                }
            }
        }
    }

    @Дано("в базе есть студенты:")
    public void studentsExistInDatabase(DataTable dataTable) throws SQLException {
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

    @Когда("я добавляю студента с именем {string} и фамилией {string} и email {string}")
    public void iAddStudent(String firstName, String lastName, String email) throws SQLException {
        String sql = "INSERT INTO students (first_name, last_name, email) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, email);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    lastInsertedId = rs.getInt(1);
                }
            }
        }
    }

    @Когда("я ищу студента по его ID")
    public void iSearchStudentById() throws SQLException {
        String sql = "SELECT id, first_name, last_name, email FROM students WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, lastInsertedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundStudent = new Student(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email")
                    );
                }
            }
        }
    }

    @Когда("я обновляю email студента на {string}")
    public void iUpdateStudentEmail(String newEmail) throws SQLException {
        String sql = "UPDATE students SET email = ? WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, newEmail);
            pstmt.setInt(2, lastInsertedId);
            pstmt.executeUpdate();
        }
    }

    @Когда("я удаляю студента")
    public void iDeleteStudent() throws SQLException {
        String sql = "DELETE FROM students WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, lastInsertedId);
            pstmt.executeUpdate();
        }
    }

    @Когда("я ищу студентов с фамилией {string}")
    public void iSearchStudentsByLastName(String lastName) throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM students WHERE last_name = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, lastName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    foundCount = rs.getInt("cnt");
                }
            }
        }
    }

    @Тогда("студент должен быть сохранен с ID")
    public void studentShouldBeSavedWithId() {
        assertNotNull(lastInsertedId, "Student ID should not be null");
        assertTrue(lastInsertedId > 0, "Student ID should be positive");
    }

    @Тогда("я должен получить студента с именем {string} и фамилией {string}")
    public void iShouldGetStudentWithName(String firstName, String lastName) {
        assertNotNull(foundStudent, "Student should be found");
        assertEquals(firstName, foundStudent.getFirstName(), "First name should match");
        assertEquals(lastName, foundStudent.getLastName(), "Last name should match");
    }

    @Тогда("email студента должен быть {string}")
    public void studentEmailShouldBe(String expectedEmail) throws SQLException {
        String sql = "SELECT email FROM students WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, lastInsertedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String actualEmail = rs.getString("email");
                    assertEquals(expectedEmail, actualEmail, "Email should match");
                }
            }
        }
    }

    @Тогда("студент не должен существовать в базе данных")
    public void studentShouldNotExist() throws SQLException {
        String sql = "SELECT COUNT(*) as cnt FROM students WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, lastInsertedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("cnt");
                    assertEquals(0, count, "Student should not exist");
                }
            }
        }
    }

    @Тогда("я должен найти {int} студентов")
    public void iShouldFindStudents(int expectedCount) {
        assertEquals(expectedCount, foundCount, "Number of found students should match");
    }

    @io.cucumber.java.After
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            cleanupTestData();
            connection.close();
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS students (" +
                     "id SERIAL PRIMARY KEY, " +
                     "first_name VARCHAR(50) NOT NULL, " +
                     "last_name VARCHAR(50) NOT NULL, " +
                     "email VARCHAR(100) UNIQUE, " +
                     "enrollment_date DATE DEFAULT CURRENT_DATE)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void cleanupTestData() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("DELETE FROM students");
            stmt.execute("ALTER TABLE students ALTER COLUMN id RESTART WITH 1");
        }
    }

    static class Student {
        private final int id;
        private final String firstName;
        private final String lastName;
        private final String email;

        public Student(int id, String firstName, String lastName, String email) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        public int getId() { return id; }
        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getEmail() { return email; }
    }
}