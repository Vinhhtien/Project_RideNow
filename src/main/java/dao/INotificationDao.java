package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import model.Notification;

public interface INotificationDao {
    int getAccountIdByOrderId(int orderId) throws SQLException;

    int getAccountIdByOrderId(Connection con, int orderId) throws SQLException; // THÊM

    void createNotification(int accountId, String title, String message) throws SQLException;

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

    //Merge thông báo từ ẩn 28/10/2025
    // Create
    int createNotification(Connection c, int accountId, String title, String message) throws SQLException;

    int[] createNotificationsBatch(Connection c, List<Integer> accountIds, String title, String message) throws SQLException;

    // Partner targets
    List<Integer> findPartnerAccountIdsByOrderId(Connection c, int orderId) throws SQLException;

    // Query / Read
    Notification findByIdForAccount(Connection c, int notificationId, int accountId) throws SQLException;

    List<Notification> findLatestForAccount(Connection c, int accountId, int limit, int offset) throws SQLException;

    int countUnread(Connection c, int accountId) throws SQLException;

    // State changes
    int markReadOne(Connection c, int notificationId, int accountId) throws SQLException;

    int markReadAll(Connection c, int accountId) throws SQLException;

    // Back-compat (khách): GIỮ để không vỡ compile ở nơi khác (nếu còn dùng)
    // chỗ này hên xui 
    // int getAccountIdByOrderId(Connection c, int orderId) throws SQLException;
    int deleteReadByAccountId(Connection c, int accountId) throws SQLException;

}