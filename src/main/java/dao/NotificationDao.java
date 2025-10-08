package dao;

import utils.DBConnection;
import java.sql.*;

public class NotificationDao implements INotificationDao {
    
    @Override
    public int getAccountIdByOrderId(int orderId) throws SQLException {
        String sql = """
            SELECT c.account_id 
            FROM RentalOrders ro
            JOIN Customers c ON ro.customer_id = c.customer_id
            WHERE ro.order_id = ?
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("account_id");
                }
            }
        }
        return 0;
    }
    
    @Override
    public void createNotification(int accountId, String title, String message) throws SQLException {
        String sql = """
            INSERT INTO Notifications (account_id, title, message, is_read, created_at)
            VALUES (?, ?, ?, 0, GETDATE())
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }
    
    // Thêm vào NotificationDao
public int getAccountIdByOrderId(Connection con, int orderId) throws SQLException {
    String sql = """
        SELECT a.account_id 
        FROM Accounts a
        JOIN Customers c ON a.account_id = c.account_id
        JOIN RentalOrders r ON r.customer_id = c.customer_id
        WHERE r.order_id = ?
        """;
    try (PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, orderId);
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt("account_id") : 0;
        }
    }
}
    
}