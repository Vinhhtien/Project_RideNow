package dao;

import utils.DBConnection;
import java.sql.*;

public class NotificationDao implements INotificationDao {
    
    @Override
    public void createNotification(int accountId, String title, String message) throws SQLException {
        String sql = "INSERT INTO Notifications (account_id, title, message) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
        }
    }
    
    @Override
    public int getAccountIdByOrderId(int orderId) throws SQLException {
        String sql = "SELECT c.account_id FROM Customers c JOIN RentalOrders r ON r.customer_id = c.customer_id WHERE r.order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("account_id") : 0;
            }
        }
    }
}