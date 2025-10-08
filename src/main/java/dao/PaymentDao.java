// dao/PaymentDao.java
package dao;

import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao implements IPaymentDao {
    @Override
    public int createPendingBankTransfer(int orderId, BigDecimal amount, String reference) throws Exception {
        String sql = """
            INSERT INTO Payments(order_id, payment_date, amount, method, status, reference)
            OUTPUT INSERTED.payment_id
            VALUES(?, GETDATE(), ?, 'bank_transfer', 'pending', ?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, reference);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Tạo payment thất bại");
    }
    
    
    @Override
    public int insertPending(int orderId, BigDecimal amount, String reference) throws Exception {
        // Bạn đang dùng bảng Payments(order_id, payment_date, amount, method, status)
        // Ta lưu method='bank_transfer', status='pending'. Reference có thể lưu tạm trong comment nếu muốn.
        String sql = """
            INSERT INTO Payments(order_id, amount, method, status)
            OUTPUT INSERTED.payment_id
            VALUES (?, ?, 'bank_transfer', 'pending')
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    @Override
    public boolean markPaid(int paymentId) throws Exception {
        String sql = """
            UPDATE Payments
               SET status='paid', payment_date=GETDATE()
             WHERE payment_id=? AND status<>'paid'
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            return ps.executeUpdate() > 0;
        }
    }
    
    //7/10/2025
    // Lấy payments pending
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
                Object[] payment = new Object[9];
                payment[0] = rs.getInt("payment_id");
                payment[1] = rs.getInt("order_id");
                payment[2] = rs.getString("full_name");
                payment[3] = rs.getString("phone");
                payment[4] = rs.getBigDecimal("amount");
                payment[5] = rs.getString("method");
                payment[6] = rs.getTimestamp("payment_date");
                payment[7] = rs.getString("status");
                payment[8] = rs.getBigDecimal("total_price");
                payments.add(payment);
            }
        }
        return payments;
    }
    
    // Cập nhật trạng thái payment
    public boolean updatePaymentStatus(int paymentId, String status, Integer adminId) throws SQLException {
        String sql = "UPDATE Payments SET status = ?, verified_by = ?, verified_at = GETDATE() WHERE payment_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setObject(2, adminId);
            ps.setInt(3, paymentId);
            return ps.executeUpdate() > 0;
        }
    }
}
