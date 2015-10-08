package jdbc;

import org.postgresql.Driver;

import java.sql.*;
import java.util.Enumeration;

/**
 * Работа с PostgreSQL через JDBC
 */
public class PostgresJDBC {
    public static void main(String[] args) throws Exception {
        Enumeration<java.sql.Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            java.sql.Driver driver = drivers.nextElement();
            System.out.println("driver = " + driver);
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        Driver driver = new org.postgresql.Driver();
        Class.forName("org.postgresql.Driver");

        DriverManager.getDriver("jdbc:postgresql://localhost:5432/postgres");
        Connection con = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres",
                "postgres", "123");

        // Создаём запрос
        Statement query = con.createStatement();

        // Набор результатов
        ResultSet resultSet =
                query.executeQuery("SELECT * FROM contact");
        // Пока есть результаты
        while (resultSet.next()) {
            System.out.println(resultSet.getString("name"));
        }
        resultSet.close();

        // class Contact {
        //   int id;
        //   String name;
        //   String surname;
        // }
        ResultSet res2 = query.executeQuery("SELECT contact.id, contact.name, contact.surname, " +
                " phone.number FROM phone LEFT JOIN contact " +
                "ON contact.id = phone.contact_id");

        ResultSetMetaData rsmd = res2.getMetaData();
        while (res2.next()) {
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                System.out.println(rsmd.getColumnName(i) + " = " + res2.getString(i));
            }
        }
        res2.close();

        //query.executeUpdate("UPDATE contact SET ... WHERE ...");
        //query.executeUpdate("DELETE FROM contact WHERE ...");
        //query.executeUpdate("INSERT INTO contact VALUES(...)")
        //query.execute("CREATE TABLE ...");
    }
}
