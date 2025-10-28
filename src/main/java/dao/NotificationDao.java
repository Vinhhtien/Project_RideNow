//an
package dao;

import model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao implements INotificationDao {

    @Override
    public int createNotification(Connection c, int accountId, String title, String message) throws SQLException {
        final String sql =
            "INSERT INTO Notifications(account_id, title, message, is_read, created_at) " +
            "VALUES(?, ?, ?, 0, SYSDATETIME())";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, title == null ? "" : title.trim());
            ps.setString(3, message == null ? "" : message.trim());
            return ps.executeUpdate();
        }
    }

    @Override
    public int[] createNotificationsBatch(Connection c, List<Integer> accountIds, String title, String message) throws SQLException {
        if (accountIds == null || accountIds.isEmpty()) return new int[0];
        final String sql =
            "INSERT INTO Notifications(account_id, title, message, is_read, created_at) " +
            "VALUES(?, ?, ?, 0, SYSDATETIME())";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (Integer accId : accountIds) {
                if (accId == null) continue;
                ps.setInt(1, accId);
                ps.setString(2, title == null ? "" : title.trim());
                ps.setString(3, message == null ? "" : message.trim());
                ps.addBatch();
            }
            return ps.executeBatch();
        }
    }

    @Override
    public List<Integer> findPartnerAccountIdsByOrderId(Connection c, int orderId) throws SQLException {
        final String sql
                = "SELECT DISTINCT p.account_id "
                + "FROM RentalOrders ro "
                + "JOIN OrderDetails od ON od.order_id = ro.order_id "
                + "JOIN Motorbikes b ON b.bike_id = od.bike_id "
                + "JOIN Partners p ON p.partner_id = b.partner_id "
                + "WHERE ro.order_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Integer> ids = new java.util.ArrayList<>();
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
                return ids;
            }
        }
    }


    @Override
    public Notification findByIdForAccount(Connection c, int notificationId, int accountId) throws SQLException {
        final String sql =
            "SELECT notification_id, account_id, title, message, is_read, created_at " +
            "FROM Notifications WHERE notification_id=? AND account_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Notification n = new Notification();
                n.setNotificationId(rs.getInt("notification_id"));
                n.setAccountId(rs.getInt("account_id"));
                n.setTitle(rs.getString("title"));
                n.setMessage(rs.getString("message"));
                n.setRead(rs.getBoolean("is_read"));
                n.setCreatedAt(rs.getTimestamp("created_at"));
                return n;
            }
        }
    }

    @Override
    public List<Notification> findLatestForAccount(Connection c, int accountId, int limit, int offset) throws SQLException {
        final String sql =
            "SELECT notification_id, account_id, title, message, is_read, created_at " +
            "FROM Notifications WHERE account_id=? " +
            "ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setInt(2, Math.max(0, offset));
            ps.setInt(3, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> list = new ArrayList<>();
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setAccountId(rs.getInt("account_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(n);
                }
                return list;
            }
        }
    }

    @Override
    public int countUnread(Connection c, int accountId) throws SQLException {
        final String sql = "SELECT COUNT(*) FROM Notifications WHERE account_id=? AND is_read=0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public int markReadOne(Connection c, int notificationId, int accountId) throws SQLException {
        final String sql = "UPDATE Notifications SET is_read=1 WHERE notification_id=? AND account_id=? AND is_read=0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            return ps.executeUpdate();
        }
    }

    @Override
    public int markReadAll(Connection c, int accountId) throws SQLException {
        final String sql = "UPDATE Notifications SET is_read=1 WHERE account_id=? AND is_read=0";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate();
        }
    }

    // Back-compat cho code cũ, nếu còn nơi nào gọi
    @Override
    public int getAccountIdByOrderId(Connection c, int orderId) throws SQLException {
        final String sql = "SELECT partner_account_id FROM Orders WHERE order_id=?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    @Override
    public int deleteReadByAccountId(Connection c, int accountId) throws SQLException {
        final String sql = "DELETE FROM Notifications WHERE account_id=? AND is_read=1";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            return ps.executeUpdate();
        }
    }
}
