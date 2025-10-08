package dao;

import utils.DBConnection;
import java.sql.*;
import java.util.*;

public class OrderQueryDao implements IOrderQueryDao {
    
    @Override
    public List<Object[]> findOrdersOfCustomer(int customerId) throws Exception {
        String sql = "SELECT r.order_id, " +
                    "STRING_AGG(b.bike_name, ', ') WITHIN GROUP (ORDER BY d.detail_id) AS bikes, " +
                    "r.start_date, r.end_date, r.total_price, r.status " +
                    "FROM RentalOrders r " +
                    "JOIN OrderDetails d ON d.order_id = r.order_id " +
                    "JOIN Motorbikes b ON b.bike_id = d.bike_id " +
                    "WHERE r.customer_id = ? " +
                    "GROUP BY r.order_id, r.start_date, r.end_date, r.total_price, r.status " +
                    "ORDER BY r.order_id DESC";
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Object[]> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getDate(3),
                        rs.getDate(4),
                        rs.getBigDecimal(5),
                        rs.getString(6)
                    });
                }
                return list;
            }
        }
    }
    
    @Override
    public List<Object[]> findOrdersOfCustomerWithPaymentStatus(int customerId) {
        String sql = "SELECT " +
                    "r.order_id, " +
                    "b.bike_name, " +
                    "r.start_date, " +
                    "r.end_date, " +
                    "d.line_total, " +
                    "r.status, " +
                    "CASE WHEN p.payment_id IS NOT NULL AND p.status = 'pending' THEN 1 ELSE 0 END as has_pending_payment " +
                    "FROM RentalOrders r " +
                    "JOIN OrderDetails d ON d.order_id = r.order_id " +
                    "JOIN Motorbikes b ON b.bike_id = d.bike_id " +
                    "LEFT JOIN Payments p ON p.order_id = r.order_id AND p.status = 'pending' " +
                    "WHERE r.customer_id = ? " +
                    "ORDER BY r.order_id DESC";

        List<Object[]> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Object[] row = new Object[7];
                    row[0] = rs.getInt("order_id");
                    row[1] = rs.getString("bike_name");
                    row[2] = rs.getDate("start_date");
                    row[3] = rs.getDate("end_date");
                    row[4] = rs.getBigDecimal("line_total");
                    row[5] = rs.getString("status");
                    row[6] = rs.getInt("has_pending_payment") == 1; // Convert int to boolean
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }
}