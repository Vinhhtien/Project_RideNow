package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Notification;

public interface INotificationDao {
    int getAccountIdByOrderId(int orderId) throws SQLException;
    int getAccountIdByOrderId(Connection con, int orderId) throws SQLException; // THÊM
    void createNotification(int accountId, String title, String message) throws SQLException;
    
    // role đối tác
    // Lấy nhanh các thông báo chưa đọc (cho toast)
    List<Notification> findUnreadTop(int accountId, int limit);

    // Danh sách thông báo (mới → cũ) có phân trang + lọc
    List<Notification> findByAccount(int accountId, int page, int size, String q, Boolean onlyUnread);

    // Đếm chưa đọc (hiển thị badge chuông)
    int countUnread(int accountId);

    // NEW: Lấy 1 thông báo theo id, kiểm tra đúng account
    Notification findOneForAccount(int notificationId, int accountId);

    void markAsRead(int notificationId, int accountId);
    void markAllAsRead(int accountId);

    int create(int accountId, String title, String message);
    int broadcastToAllPartners(String title, String message);
}