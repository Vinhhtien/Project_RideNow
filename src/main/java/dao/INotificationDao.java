package dao;

import java.sql.Connection;
import java.sql.SQLException;

public interface INotificationDao {
    int getAccountIdByOrderId(int orderId) throws SQLException;
    int getAccountIdByOrderId(Connection con, int orderId) throws SQLException; // THÊM
    void createNotification(int accountId, String title, String message) throws SQLException;
}