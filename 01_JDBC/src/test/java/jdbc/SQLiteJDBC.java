package jdbc;

/**
 * Пример использования JDBC - "нижний" уровень работы с БД
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class SQLiteJDBC {
    public static void main(String args[]) {
        Connection c = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:test.db");
            assertNotNull(c);

            Statement stmt = c.createStatement();
            String sql = "CREATE TABLE COMPANY " +
                    "(ID INT PRIMARY KEY     NOT NULL," +
                    " NAME           TEXT    NOT NULL, " +
                    " AGE            INT     NOT NULL, " +
                    " ADDRESS        CHAR(50), " +
                    " SALARY         REAL)";
            assertEquals(0, stmt.executeUpdate(sql));
            stmt.close();
            c.close();


        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Opened database successfully");
    }
}