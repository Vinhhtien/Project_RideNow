package dao;

import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Notification;

public class NotificationDao implements INotificationDao {
    
     private Connection getConn() throws SQLException {
        return DBConnection.getConnection();
    }
    
    
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
    
    @Override
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
    
    
    // role đối tác
    @Override
    public List<Notification> findUnreadTop(int accountId, int limit) {
        String sql = "SELECT TOP (?) notification_id, account_id, title, message, is_read, created_at " +
                     "FROM Notifications WHERE account_id=? AND is_read=0 ORDER BY created_at DESC";
        List<Notification> list = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, limit));
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) { throw new RuntimeException("findUnreadTop failed", e); }
        return list;
    }

    // ===================== NEW: danh sách + đếm =====================

    @Override
    public List<Notification> findByAccount(int accountId, int page, int size, String q, Boolean onlyUnread) {
        int p = Math.max(1, page);
        int s = Math.max(1, size);
        int offset = (p - 1) * s;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT notification_id, account_id, title, message, is_read, created_at ")
          .append("FROM Notifications WHERE account_id = ? ");

        List<Object> params = new ArrayList<>();
        params.add(accountId);

        if (q != null && !q.trim().isEmpty()) {
            sb.append("AND (title LIKE ? OR message LIKE ?) ");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }
        if (onlyUnread != null && onlyUnread) {
            sb.append("AND is_read = 0 ");
        }

        // SQL Server 2012+ pagination
        sb.append("ORDER BY created_at DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(offset);
        params.add(s);

        List<Notification> list = new ArrayList<>();
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sb.toString())) {

            for (int i = 0; i < params.size(); i++) {
                Object v = params.get(i);
                if (v instanceof Integer) ps.setInt(i + 1, (Integer) v);
                else ps.setString(i + 1, v.toString());
            }

            try (ResultSet rs = ps.executeQuery()) {
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
            }
        } catch (SQLException e) {
            throw new RuntimeException("findByAccount failed", e);
        }
        return list;
    }

    @Override
    public int countUnread(int accountId) {
        String sql = "SELECT COUNT(*) FROM Notifications WHERE account_id=? AND is_read=0";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }
        } catch (SQLException e) { throw new RuntimeException("countUnread failed", e); }
    }

    // ===================== NEW: lấy 1 bản ghi theo id + account =====================

    @Override
    public Notification findOneForAccount(int notificationId, int accountId) {
        String sql = "SELECT notification_id, account_id, title, message, is_read, created_at " +
                     "FROM Notifications WHERE notification_id=? AND account_id=?";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Notification n = new Notification();
                    n.setNotificationId(rs.getInt("notification_id"));
                    n.setAccountId(rs.getInt("account_id"));
                    n.setTitle(rs.getString("title"));
                    n.setMessage(rs.getString("message"));
                    n.setRead(rs.getBoolean("is_read"));
                    n.setCreatedAt(rs.getTimestamp("created_at"));
                    return n;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("findOneForAccount failed", e);
        }
    }

    // ===================== update / create / broadcast =====================

    @Override
    public void markAsRead(int notificationId, int accountId) {
        String sql = "UPDATE Notifications SET is_read=1 WHERE notification_id=? AND account_id=?";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, notificationId);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("markAsRead failed", e); }
    }

    @Override
    public void markAllAsRead(int accountId) {
        String sql = "UPDATE Notifications SET is_read=1 WHERE account_id=? AND is_read=0";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("markAllAsRead failed", e); }
    }

    @Override
    public int create(int accountId, String title, String message) {
        String sql = "INSERT INTO Notifications(account_id, title, message, is_read, created_at) " +
                     "VALUES (?, ?, ?, 0, GETDATE())";
        try (Connection c = getConn();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException("create failed", e); }
    }

    @Override
    public int broadcastToAllPartners(String title, String message) {
        String sql =
            "INSERT INTO Notifications(account_id, title, message, is_read, created_at) " +
            "SELECT a.account_id, ?, ?, 0, GETDATE() " +
            "FROM Partners p JOIN Accounts a ON a.account_id = p.account_id " +
            "WHERE a.role='partner' AND a.status=1";
        try (Connection c = getConn(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, message);
            return ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException("broadcast failed", e); }
    }
    
}