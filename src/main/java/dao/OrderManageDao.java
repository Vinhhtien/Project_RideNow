package dao;

import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderManageDao implements IOrderManageDao {
    
    @Override
    public List<Object[]> getOrdersForPickup() throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        String sql = """
            SELECT 
                ro.order_id, 
                c.full_name, 
                c.phone,
                b.bike_name,
                ro.start_date,
                ro.end_date,
                ro.total_price,
                ro.deposit_amount,
                ro.pickup_status
            FROM RentalOrders ro
            JOIN Customers c ON ro.customer_id = c.customer_id
            JOIN OrderDetails od ON od.order_id = ro.order_id
            JOIN Motorbikes b ON b.bike_id = od.bike_id
            WHERE ro.status = 'confirmed' 
              AND ro.pickup_status = 'not_picked_up'
            ORDER BY ro.start_date ASC
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Object[] order = new Object[9];
                order[0] = rs.getInt("order_id");
                order[1] = rs.getString("full_name");
                order[2] = rs.getString("phone");
                order[3] = rs.getString("bike_name");
                order[4] = rs.getDate("start_date");
                order[5] = rs.getDate("end_date");
                order[6] = rs.getBigDecimal("total_price");
                order[7] = rs.getBigDecimal("deposit_amount");
                order[8] = rs.getString("pickup_status");
                orders.add(order);
            }
        }
        return orders;
    }
    
    @Override
public List<Object[]> getActiveOrders() throws SQLException {
    List<Object[]> orders = new ArrayList<>();
    String sql = """
        SELECT
            ro.order_id,
            c.full_name,
            c.phone,
            b.bike_name,
            ro.start_date,
            ro.end_date,
            ro.total_price,
            ro.deposit_amount
        FROM RentalOrders ro
        JOIN Customers c ON ro.customer_id = c.customer_id
        JOIN OrderDetails od ON od.order_id = ro.order_id
        JOIN Motorbikes b ON b.bike_id = od.bike_id
        WHERE ro.status = 'confirmed'
            AND ro.pickup_status = 'picked_up'
            AND (ro.return_status = 'not_returned' OR ro.return_status = 'none')
        ORDER BY ro.end_date ASC
        """;
    
    System.out.println("DEBUG: Executing getActiveOrders query");
    
    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            Object[] order = new Object[8];
            order[0] = rs.getInt("order_id");
            order[1] = rs.getString("full_name");
            order[2] = rs.getString("phone");
            order[3] = rs.getString("bike_name");
            order[4] = rs.getDate("start_date");
            order[5] = rs.getDate("end_date");
            order[6] = rs.getBigDecimal("total_price");
            order[7] = rs.getBigDecimal("deposit_amount");
            orders.add(order);
            
            System.out.println("DEBUG: Found active order #" + order[0] + " - " + order[1]);
        }
        
        System.out.println("DEBUG: Total active orders found: " + orders.size());
    }
    return orders;
}
    
    @Override
    public boolean markOrderPickedUp(int orderId, int adminId) throws SQLException {
        String sql = """
            UPDATE RentalOrders 
            SET pickup_status = 'picked_up', 
                picked_up_at = GETDATE(), 
                admin_pickup_id = ?
            WHERE order_id = ? 
                     AND (return_status = 'not_returned' OR return_status = 'none') 
              AND pickup_status = 'not_picked_up'
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }
    
    @Override
    public boolean markOrderReturned(int orderId, int adminId) throws SQLException {
        String sql = """
            UPDATE RentalOrders 
            SET return_status = 'returned', 
                returned_at = GETDATE(), 
                admin_return_id = ?,
                deposit_status = 'held'
            WHERE order_id = ? 
              AND (return_status = 'not_returned' OR return_status = 'none')
              AND pickup_status = 'picked_up'
            """;

        System.out.println("DEBUG [DAO]: Marking order as returned - Order: " + orderId + ", Admin: " + adminId);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG [DAO]: Rows updated: " + rowsUpdated);

            return rowsUpdated > 0;
        }
    }
    
    // PHƯƠNG THỨC MỚI: Dùng connection từ transaction
    @Override
    public boolean markOrderReturned(Connection con, int orderId, int adminId) throws SQLException {
        String sql = """
            UPDATE RentalOrders 
            SET return_status = 'returned', 
                returned_at = GETDATE(), 
                admin_return_id = ?,
                deposit_status = 'held'
            WHERE order_id = ? 
              AND (return_status = 'not_returned' OR return_status = 'none')
              AND pickup_status = 'picked_up'
            """;

        System.out.println("DEBUG [DAO-with-Con]: Marking order as returned - Order: " + orderId + ", Admin: " + adminId);

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG [DAO-with-Con]: Rows updated: " + rowsUpdated);

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
            ps.setInt(3, history.getAdminId());
            ps.setString(4, history.getNotes());
            ps.executeUpdate();
        }
    }
    
    // PHƯƠNG THỨC MỚI: Dùng connection từ transaction
    @Override
    public void addStatusHistory(Connection con, OrderStatusHistory history) throws SQLException {
        String sql = "INSERT INTO OrderStatusHistory (order_id, status, admin_id, notes) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, history.getOrderId());
            ps.setString(2, history.getStatus());
            ps.setInt(3, history.getAdminId());
            ps.setString(4, history.getNotes());
            ps.executeUpdate();
        }
    }
    
    public boolean updateBikeStatus(int orderId, String status) throws SQLException {
        String sql = """
            UPDATE Motorbikes 
            SET status = ?
            WHERE bike_id IN (
                SELECT bike_id FROM OrderDetails WHERE order_id = ?
            )
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE RentalOrders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }
}