package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnection {
    // Thông tin kết nối - ĐỔI lại cho đúng DB của bạn
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=MotorbikeRentalDB;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASS = "12345";

    public static Connection getConnection() {
        Connection con = null;
        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, USER, PASS);
            return con;
        } catch (Exception ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    // Test nhanh kết nối khi chạy main
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                System.out.println(" Kết nối thành công tới MotorbikeRentalDB");
            } else {
                System.out.println(" Kết nối thất bại");
            }
        } catch (SQLException ex) {
            Logger.getLogger(DBConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
