package dao;

import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentVerifyDao implements IPaymentVerifyDao {
    
    @Override
    public List<Object[]> getPendingPayments() throws SQLException {
        List<Object[]> payments = new ArrayList<>();
        String sql = """
            SELECT p.payment_id, p.order_id, c.full_name, c.phone, 
                   p.amount, p.method, p.payment_date, p.status,
                   r.total_price, r.deposit_amount
            FROM Payments p
            JOIN RentalOrders r ON r.order_id = p.order_id
            JOIN Customers c ON c.customer_id = r.customer_id
            WHERE p.status = 'pending'
            ORDER BY p.payment_date DESC
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Object[] payment = new Object[10]; // Tăng lên 10 phần tử
                payment[0] = rs.getInt("payment_id");
                payment[1] = rs.getInt("order_id");
                payment[2] = rs.getString("full_name");
                payment[3] = rs.getString("phone");
                payment[4] = rs.getBigDecimal("amount");
                payment[5] = rs.getString("method");
                payment[6] = rs.getTimestamp("payment_date");
                payment[7] = rs.getString("status");
                payment[8] = rs.getBigDecimal("total_price");
                payment[9] = rs.getBigDecimal("deposit_amount");
                payments.add(payment);
            }
        }
        return payments;
    }
    
    @Override
    public boolean updatePaymentStatus(int paymentId, String status, Integer adminId) throws SQLException {
        String sql = "UPDATE Payments SET status = ?, verified_by = ?, verified_at = GETDATE() WHERE payment_id = ? AND status = 'pending'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, adminId); // adminId = 1
            ps.setInt(3, paymentId);

            int rowsUpdated = ps.executeUpdate();
            System.out.println("DEBUG: Updated " + rowsUpdated + " rows for payment #" + paymentId);
            return rowsUpdated > 0;
        }
    }
    
    @Override
    public int getOrderIdByPayment(int paymentId) throws SQLException {
        String sql = "SELECT order_id FROM Payments WHERE payment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    System.out.println("DEBUG: Found order_id " + orderId + " for payment #" + paymentId);
                    return orderId;
                } else {
                    System.out.println("DEBUG: No order_id found for payment #" + paymentId);
                    return 0;
                }
            }
        }
    }
}