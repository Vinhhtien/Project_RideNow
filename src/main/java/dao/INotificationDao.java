package dao;

import java.sql.SQLException;

public interface INotificationDao {
    void createNotification(int accountId, String title, String message) throws SQLException;
    int getAccountIdByOrderId(int orderId) throws SQLException;
}