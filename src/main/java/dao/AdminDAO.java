package dao;

import utils.DBConnection;
import model.OrderListItem;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AdminDAO implements IAdminDAO {

    @Override public int countCustomers() { return singleInt("SELECT COUNT(*) FROM Customers"); }
    @Override public int countPartners()  { return singleInt("SELECT COUNT(*) FROM Partners"); }
    @Override public int countBikes()     { return singleInt("SELECT COUNT(*) FROM Motorbikes"); }
    @Override public int countOrders()    { return singleInt("SELECT COUNT(*) FROM RentalOrders"); }

    // Doanh thu = tổng Payments.status='paid'
    @Override public BigDecimal sumRevenueToday() {
        String sql = """
            SELECT COALESCE(SUM(amount),0)
            FROM Payments
            WHERE status='paid'
              AND CAST(payment_date AS DATE) = CAST(GETDATE() AS DATE)
        """;
        return singleDecimal(sql);
    }

    @Override public BigDecimal sumRevenueThisMonth() {
        String sql = """
            SELECT COALESCE(SUM(amount),0)
            FROM Payments
            WHERE status='paid'
              AND YEAR(payment_date)=YEAR(GETDATE())
              AND MONTH(payment_date)=MONTH(GETDATE())
        """;
        return singleDecimal(sql);
    }

    @Override public List<OrderListItem> findLatestOrders(int limit) {
        String sql = """
            SELECT TOP (?) r.order_id, r.status, r.total_price, r.created_at, c.full_name
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            ORDER BY r.created_at DESC
        """;
        List<OrderListItem> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()){
                    OrderListItem o = new OrderListItem();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setStatus(rs.getString("status"));
                    o.setTotalPrice(rs.getBigDecimal("total_price"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    o.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
                    o.setCustomerName(rs.getString("full_name"));
                    list.add(o);
                }
            }
        } catch (Exception e){ throw new RuntimeException(e); }
        return list;
    }

    // Vì schema hiện chưa có verify_status, mình tạm show xe đang bảo trì
    @Override public List<String[]> findBikesMaintenance(int limit) {
        String sql = """
            SELECT TOP (?) bike_name, license_plate
            FROM Motorbikes
            WHERE status='maintenance'
            ORDER BY bike_id DESC
        """;
        List<String[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    rows.add(new String[]{rs.getString(1), rs.getString(2)});
                }
            }
        } catch (Exception e){ throw new RuntimeException(e); }
        return rows;
    }

    // helpers
    private int singleInt(String sql) {
        try (Connection c=DBConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            return rs.next()? rs.getInt(1):0;
        } catch (Exception e){ throw new RuntimeException(e); }
    }

    private BigDecimal singleDecimal(String sql) {
        try (Connection c=DBConnection.getConnection();
             PreparedStatement ps=c.prepareStatement(sql);
             ResultSet rs=ps.executeQuery()) {
            return rs.next()? rs.getBigDecimal(1): BigDecimal.ZERO;
        } catch (Exception e){ throw new RuntimeException(e); }
    }
}
