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
                SELECT 
                    p.payment_id,
                    p.order_id,
                    c.full_name,
                    c.phone,
                    p.amount,
                    p.method,
                    p.payment_date,
                    p.status
                FROM Payments p
                JOIN RentalOrders ro ON ro.order_id = p.order_id
                JOIN Customers c ON c.customer_id = ro.customer_id
                WHERE p.status = 'pending'
                ORDER BY p.payment_date ASC
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] payment = new Object[8];
                payment[0] = rs.getInt("payment_id");
                payment[1] = rs.getInt("order_id");
                payment[2] = rs.getString("full_name");
                payment[3] = rs.getString("phone");
                payment[4] = rs.getBigDecimal("amount");
                payment[5] = rs.getString("method");
                payment[6] = rs.getTimestamp("payment_date");
                payment[7] = rs.getString("status");
                payments.add(payment);
            }
        }
        return payments;
    }


    public boolean verifyPayment(int paymentId, int adminId) throws SQLException {
        String sql = """
                UPDATE Payments 
                SET status = 'paid', 
                    verified_by = ?, 
                    verified_at = GETDATE()
                WHERE payment_id = ? 
                  AND status = 'pending'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, paymentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updatePaymentStatus(int paymentId, String status, Integer adminId) throws SQLException {
        String sql = """
                UPDATE Payments 
                SET status = ?, 
                    verified_by = ?, 
                    verified_at = GETDATE()
                WHERE payment_id = ? 
                  AND status = 'pending'
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, adminId); // Sử dụng Object vì có thể null
            ps.setInt(3, paymentId);
            return ps.executeUpdate() > 0;
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
                    return rs.getInt("order_id");
                }
            }
        }
        return 0; // Trả về 0 nếu không tìm thấy
    }
}