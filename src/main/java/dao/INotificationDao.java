//an

package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import model.Notification;

public interface INotificationDao {
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
    int getAccountIdByOrderId(Connection c, int orderId) throws SQLException;
    int deleteReadByAccountId(Connection c, int accountId) throws SQLException;
}
