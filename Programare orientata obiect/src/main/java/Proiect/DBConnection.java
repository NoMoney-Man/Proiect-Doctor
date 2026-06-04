package Proiect;

public class DBConnection {
    public static java.sql.Connection getConn() throws java.sql.SQLException {
        return java.sql.DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/doctoronline", "root", "root"
        );
    }
}
