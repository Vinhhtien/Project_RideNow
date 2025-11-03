package dao;

import utils.DBConnection;
import model.ChangeOrderVM;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderChangeDao implements IOrderChangeDao {

    @Override
    public ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception {
        System.out.println("[OrderChangeDao] Loading change order VM for orderId: " + orderId + ", accountId: " + accountId);
        
        final String sql = 
            "SELECT r.order_id, r.status, r.start_date, r.end_date, r.confirmed_at, " +
            "  CASE WHEN r.status='confirmed' AND r.confirmed_at IS NOT NULL " +
            "       THEN CASE WHEN (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) < 0 THEN 0 " +
            "                 ELSE (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) END " +
            "       ELSE 0 END AS remaining_min " +
            "FROM RentalOrders r " +
            "JOIN Customers c ON c.customer_id = r.customer_id " +
            "JOIN Accounts  a ON a.account_id  = c.account_id " +
            "WHERE r.order_id = ? AND a.account_id = ?";

        System.out.println("[OrderChangeDao] SQL: " + sql);
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            
            System.out.println("[OrderChangeDao] Parameters: orderId=" + orderId + ", accountId=" + accountId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[OrderChangeDao] No result found in ResultSet");
                    return null;
                }
                
                ChangeOrderVM vm = new ChangeOrderVM();
                vm.setOrderId(rs.getInt("order_id"));
                vm.setStatus(rs.getString("status"));
                vm.setStart(rs.getDate("start_date"));
                vm.setEnd(rs.getDate("end_date"));
                vm.setConfirmedAt(rs.getTimestamp("confirmed_at"));
                vm.setRemainingMinutes(rs.getInt("remaining_min"));
                
                System.out.println("[OrderChangeDao] Loaded VM: " +
                    "orderId=" + vm.getOrderId() + 
                    ", status=" + vm.getStatus() + 
                    ", confirmedAt=" + vm.getConfirmedAt() +
                    ", remainingMinutes=" + vm.getRemainingMinutes());
                
                return vm;
            }
        } catch (Exception e) {
            System.out.println("[OrderChangeDao] Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception {
        final String sql =
            "UPDATE r SET r.status='cancelled' " +
            "FROM RentalOrders r " +
            "JOIN Customers c ON c.customer_id = r.customer_id " +
            "JOIN Accounts  a ON a.account_id  = c.account_id " +
            "WHERE r.order_id = ? AND a.account_id = ? " +
            "  AND r.status = 'confirmed' " +
            "  AND r.confirmed_at IS NOT NULL " +
            "  AND DATEDIFF(MINUTE, r.confirmed_at, GETDATE()) <= 30";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            return ps.executeUpdate();
        }
    }

    @Override
    public ChangeResult updateOrderDatesWithin30Min(int orderId, int accountId, Date newStart, Date newEnd) throws Exception {
        if (newStart.after(newEnd)) return ChangeResult.FAIL;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1) Kiểm tra còn trong 30'
                final String checkSql =
                    "SELECT r.confirmed_at, r.status " +
                    "FROM RentalOrders r " +
                    "JOIN Customers c ON c.customer_id = r.customer_id " +
                    "JOIN Accounts  a ON a.account_id  = c.account_id " +
                    "WHERE r.order_id=? AND a.account_id=?";
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, accountId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { 
                            con.rollback(); 
                            return ChangeResult.FAIL; 
                        }
                        Timestamp confirmedAt = rs.getTimestamp("confirmed_at");
                        String status = rs.getString("status");
                        if (!"confirmed".equalsIgnoreCase(status) || confirmedAt == null) {
                            con.rollback(); 
                            return ChangeResult.EXPIRED;
                        }
                        final String stillSql = "SELECT CASE WHEN DATEDIFF(MINUTE, ?, GETDATE()) <= 30 THEN 1 ELSE 0 END";
                        try (PreparedStatement ps2 = con.prepareStatement(stillSql)) {
                            ps2.setTimestamp(1, confirmedAt);
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                rs2.next();
                                if (rs2.getInt(1) != 1) { 
                                    con.rollback(); 
                                    return ChangeResult.EXPIRED; 
                                }
                            }
                        }
                    }
                }

                // 2) Lấy danh sách xe của order
                List<Integer> bikeIds = new ArrayList<>();
                final String bikesSql = "SELECT d.bike_id FROM OrderDetails d WHERE d.order_id=?";
                try (PreparedStatement ps = con.prepareStatement(bikesSql)) {
                    ps.setInt(1, orderId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) bikeIds.add(rs.getInt(1));
                    }
                }
                if (bikeIds.isEmpty()) { 
                    con.rollback(); 
                    return ChangeResult.FAIL; 
                }

                // 3) Kiểm tra overlap với đơn khác (confirmed/completed)
                StringBuilder inClause = new StringBuilder("(");
                for (int i = 0; i < bikeIds.size(); i++) {
                    inClause.append(bikeIds.get(i));
                    if (i < bikeIds.size() - 1) inClause.append(",");
                }
                inClause.append(")");
                
                final String conflictSql =
                    "SELECT TOP 1 1 " +
                    "FROM OrderDetails d2 " +
                    "JOIN RentalOrders r2 ON r2.order_id = d2.order_id " +
                    "WHERE d2.bike_id IN " + inClause.toString() + " " +
                    "  AND r2.order_id <> ? " +
                    "  AND r2.status IN ('confirmed','completed') " +
                    "  AND NOT (r2.end_date < ? OR r2.start_date > ?)";
                try (PreparedStatement ps = con.prepareStatement(conflictSql)) {
                    ps.setInt(1, orderId);
                    ps.setDate(2, newStart);
                    ps.setDate(3, newEnd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { 
                            con.rollback(); 
                            return ChangeResult.CONFLICT; 
                        }
                    }
                }

                // 4) Update ngày
                final String updateSql =
                    "UPDATE RentalOrders SET start_date=?, end_date=? WHERE order_id=?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setDate(1, newStart);
                    ps.setDate(2, newEnd);
                    ps.setInt(3, orderId);
                    int rows = ps.executeUpdate();
                    if (rows == 0) { 
                        con.rollback(); 
                        return ChangeResult.FAIL; 
                    }
                }

                con.commit();
                return ChangeResult.OK;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
}