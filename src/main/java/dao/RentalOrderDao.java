
package dao;

import model.RentalOrder;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RentalOrderDao implements IRentalOrderDao {
    private Connection connection;

    public RentalOrderDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<RentalOrder> findAll() throws Exception {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM RentalOrders";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                orders.add(mapResultSetToRentalOrder(rs));
            }
        }
        return orders;
    }

    @Override
    public List<RentalOrder> findById(int orderId) throws Exception {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM RentalOrders WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                orders.add(mapResultSetToRentalOrder(rs));
            }
        }
        return orders;
    }

    // Helper method to map ResultSet to RentalOrder
    private RentalOrder mapResultSetToRentalOrder(ResultSet rs) throws SQLException {
        RentalOrder order = new RentalOrder();
        order.setOrderId(rs.getInt("order_id"));
        order.setCustomerId(rs.getInt("customer_id"));
        order.setStartDate(rs.getDate("start_date"));
        order.setEndDate(rs.getDate("end_date"));
        order.setTotalPrice(rs.getBigDecimal("total_price"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setDepositStatus(rs.getString("deposit_status"));
        order.setPaymentSubmitted(rs.getBoolean("payment_submitted"));
        order.setReturnStatus(rs.getString("return_status"));
        order.setPickupStatus(rs.getString("pickup_status"));
        order.setPickedUpAt(rs.getTimestamp("picked_up_at"));
        order.setReturnedAt(rs.getTimestamp("returned_at"));
        order.setAdminPickupId(getNullableInt(rs, "admin_pickup_id"));
        order.setAdminReturnId(getNullableInt(rs, "admin_return_id"));
        return order;
    }

    private Integer getNullableInt(ResultSet rs, String columnLabel) throws SQLException {
        int value = rs.getInt(columnLabel);
        return rs.wasNull() ? null : value;
    }
}