package dao;

import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManageDao implements IOrderManageDao {
    
    @Override
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE RentalOrders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG: Order status updated to '" + status + "' - rows affected: " + rowsUpdated);
            return rowsUpdated > 0;
        }
    }
    @Override
    public boolean markOrderPickedUp(int orderId, int adminId) throws SQLException {
        String sql = "UPDATE RentalOrders SET pickup_status = 'picked_up', picked_up_at = GETDATE(), admin_pickup_id = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG: Order pickup marked - rows affected: " + rowsUpdated);
            return rowsUpdated > 0;
        }
    }
    
    @Override
    public boolean markOrderReturned(int orderId, int adminId) throws SQLException {
        // THÊM: cập nhật deposit_status = 'held'
        String sql = """
            UPDATE RentalOrders 
            SET pickup_status = 'returned', 
                returned_at = GETDATE(), 
                admin_return_id = ?,
                deposit_status = 'held'  -- THÊM DÒNG NÀY
            WHERE order_id = ?
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG: Order return marked - rows affected: " + rowsUpdated);
            return rowsUpdated > 0;
        }
    }
    
    @Override
    public void addStatusHistory(OrderStatusHistory history) throws SQLException {
        String sql = "INSERT INTO OrderStatusHistory (order_id, status, admin_id, notes) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, history.getOrderId());
            ps.setString(2, history.getStatus());
            ps.setObject(3, history.getAdminId());
            ps.setString(4, history.getNotes());
            ps.executeUpdate();
        }
    }
    
    @Override
    public List<Object[]> getOrdersForPickup() throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        String sql = """
            SELECT r.order_id, c.full_name, c.phone, c.email,
                   STUFF((SELECT ', ' + b2.bike_name 
                          FROM OrderDetails d2 
                          JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id 
                          WHERE d2.order_id = r.order_id 
                          FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,'') AS bikes,
                   r.start_date, r.end_date, r.total_price, r.deposit_amount,
                   r.created_at, r.pickup_status
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            WHERE r.status = 'confirmed' AND r.pickup_status = 'not_picked_up'
            ORDER BY r.created_at DESC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("DEBUG: Fetching orders for pickup..."); // THÊM DEBUG

            while (rs.next()) {
                Object[] order = new Object[10];
                order[0] = rs.getInt("order_id");
                order[1] = rs.getString("full_name");
                order[2] = rs.getString("phone");
                order[3] = rs.getString("email");
                order[4] = rs.getString("bikes");
                order[5] = rs.getDate("start_date");
                order[6] = rs.getDate("end_date");
                order[7] = rs.getBigDecimal("total_price");
                order[8] = rs.getBigDecimal("deposit_amount");
                order[9] = rs.getString("pickup_status");
                orders.add(order);

                System.out.println("DEBUG: Found pickup order #" + order[0] + " - " + order[1]); // THÊM DEBUG
            }

            System.out.println("DEBUG: Total pickup orders found: " + orders.size()); // THÊM DEBUG
        }
        return orders;
    }
    
    @Override
    public List<Object[]> getActiveOrders() throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        String sql = """
            SELECT r.order_id, c.full_name, c.phone, c.email,
                   STUFF((SELECT ', ' + b2.bike_name 
                          FROM OrderDetails d2 
                          JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id 
                          WHERE d2.order_id = r.order_id 
                          FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,'') AS bikes,
                   r.start_date, r.end_date, r.total_price, r.deposit_amount,
                   r.picked_up_at, r.admin_pickup_id
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            WHERE r.status = 'confirmed' AND r.pickup_status = 'picked_up'  -- SỬA: status = 'confirmed'
            ORDER BY r.end_date ASC
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("DEBUG: Fetching active orders..."); // THÊM DEBUG

            while (rs.next()) {
                Object[] order = new Object[9];
                order[0] = rs.getInt("order_id");
                order[1] = rs.getString("full_name");
                order[2] = rs.getString("phone");
                order[3] = rs.getString("email");
                order[4] = rs.getString("bikes");
                order[5] = rs.getDate("start_date");
                order[6] = rs.getDate("end_date");
                order[7] = rs.getBigDecimal("total_price");
                order[8] = rs.getBigDecimal("deposit_amount");
                orders.add(order);

                System.out.println("DEBUG: Found active order #" + order[0] + " - " + order[1]); // THÊM DEBUG
            }

            System.out.println("DEBUG: Total active orders found: " + orders.size()); // THÊM DEBUG
        }
        return orders;
    }
}