package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;

@WebServlet("/adminreturninspect")
public class AdminReturnInspectServlet extends HttpServlet {

    // Model cho trang kiểm tra
    public static class InspectionOrderVM {
        private int orderId;
        private String customerName;
        private String customerPhone;
        private String bikeName;
        private BigDecimal depositAmount;
        private Timestamp returnedAt;
        
        // Getters/Setters
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }
        public BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
        public Timestamp getReturnedAt() { return returnedAt; }
        public void setReturnedAt(Timestamp returnedAt) { this.returnedAt = returnedAt; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null) {
            resp.sendRedirect(req.getContextPath() + "/adminreturns");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr);
            InspectionOrderVM order = getOrderForInspection(orderId);
            
            if (order == null) {
                req.getSession().setAttribute("flash", "❌ Không tìm thấy đơn hàng");
                resp.sendRedirect(req.getContextPath() + "/adminreturns");
                return;
            }

            req.setAttribute("order", order);
            req.getRequestDispatcher("/admin/admin-return-inspect.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/adminreturns");
        }
    }
// do post cũ
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
//            throws ServletException, IOException {
//        
//        // Lấy admin từ session
//        HttpSession session = req.getSession();
//        Integer adminId = (Integer) session.getAttribute("admin_id");
//        if (adminId == null) adminId = 1; // Fallback
//
//        String orderIdStr = req.getParameter("orderId");
//        String bikeCondition = req.getParameter("bikeCondition");
//        String damageNotes = req.getParameter("damageNotes");
//        String damageFeeStr = req.getParameter("damageFee");
//        String refundMethod = req.getParameter("refundMethod");
//
//        try {
//            int orderId = Integer.parseInt(orderIdStr);
//            BigDecimal damageFee = new BigDecimal(damageFeeStr != null && !damageFeeStr.isEmpty() ? damageFeeStr : "0");
//            
//            // Tính toán số tiền hoàn lại
//            BigDecimal depositAmount = getDepositAmount(orderId);
//            BigDecimal refundAmount = depositAmount.subtract(damageFee);
//
//            // Lưu kết quả kiểm tra và xử lý hoàn cọc
//            boolean success = processRefundInspection(orderId, adminId, bikeCondition, 
//                    damageNotes, damageFee, refundAmount, refundMethod);
//
//            if (success) {
//                session.setAttribute("flash", "✅ Đã kiểm tra xe và xử lý hoàn cọc thành công!");
//            } else {
//                session.setAttribute("flash", "❌ Xử lý hoàn cọc thất bại!");
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            req.getSession().setAttribute("flash", "❌ Lỗi khi xử lý hoàn cọc: " + e.getMessage());
//        }
//
//        resp.sendRedirect(req.getContextPath() + "/adminreturns");
//    }
    
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer adminId = (Integer) session.getAttribute("admin_id");
        if (adminId == null) adminId = 1; // fallback demo

        String orderIdStr    = req.getParameter("orderId");
        String bikeCondition = req.getParameter("bikeCondition");
        String damageNotes   = req.getParameter("damageNotes");
        String damageFeeStr  = req.getParameter("damageFee");
        String refundMethod  = req.getParameter("refundMethod");

        try {
            // ==== Validate cơ bản ====
            if (orderIdStr == null || orderIdStr.isBlank()) {
                throw new IllegalArgumentException("Thiếu orderId");
            }
            int orderId = Integer.parseInt(orderIdStr.trim());

            // Chỉ chấp nhận 3 giá trị condition
            if (!"excellent".equals(bikeCondition) &&
                !"good".equals(bikeCondition) &&
                !"damaged".equals(bikeCondition)) {
                throw new IllegalArgumentException("Giá trị bikeCondition không hợp lệ");
            }

            // Chỉ chấp nhận 2 phương thức hoàn
            if (!"cash".equals(refundMethod) && !"wallet".equals(refundMethod)) {
                // ép về cash nếu client gửi sai
                refundMethod = "cash";
            }

            // Lấy tiền cọc của đơn
            BigDecimal depositAmount = getDepositAmount(orderId);
            if (depositAmount == null) depositAmount = BigDecimal.ZERO;

            // Parse & ràng buộc damageFee
            BigDecimal damageFee = BigDecimal.ZERO;
            if (damageFeeStr != null && !damageFeeStr.isBlank()) {
                try {
                    damageFee = new BigDecimal(damageFeeStr.trim());
                } catch (NumberFormatException nfe) {
                    damageFee = BigDecimal.ZERO; // nếu nhập rác
                }
            }
            if (damageFee.compareTo(BigDecimal.ZERO) < 0) damageFee = BigDecimal.ZERO;
            if (damageFee.compareTo(depositAmount) > 0) damageFee = depositAmount;

            // Tính tiền hoàn = cọc - phí, không âm
            BigDecimal refundAmount = depositAmount.subtract(damageFee);
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) refundAmount = BigDecimal.ZERO;

            // Gọi xử lý chính (transaction)
            boolean success = processRefundInspection(
                    orderId, adminId, bikeCondition,
                    damageNotes, damageFee, refundAmount, refundMethod
            );

            if (success) {
                session.setAttribute("flash",
                        "✅ Đã kiểm tra xe và xử lý hoàn cọc thành công! (Hoàn: " + refundAmount + ")");
            } else {
                session.setAttribute("flash", "❌ Xử lý hoàn cọc thất bại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash", "❌ Lỗi khi xử lý hoàn cọc: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturns");
    }

    
    
    
    private InspectionOrderVM getOrderForInspection(int orderId) {
//        String sql = """
//            SELECT r.order_id, c.full_name, c.phone, b.bike_name, 
//                   r.deposit_amount, r.returned_at
//            FROM RentalOrders r
//            JOIN Customers c ON c.customer_id = r.customer_id
//            JOIN OrderDetails d ON d.order_id = r.order_id
//            JOIN Motorbikes b ON b.bike_id = d.bike_id
//            WHERE r.order_id = ? AND r.pickup_status = 'returned'
//            """;
            String sql = """
        SELECT r.order_id, c.full_name, c.phone, b.bike_name, 
               r.deposit_amount, r.returned_at, r.customer_id
        FROM RentalOrders r
        JOIN Customers c ON c.customer_id = r.customer_id
        JOIN OrderDetails d ON d.order_id = r.order_id
        JOIN Motorbikes b ON b.bike_id = d.bike_id
        WHERE r.order_id = ? 
          AND r.return_status = 'returned'   -- ✅ sửa ở đây
    """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                InspectionOrderVM order = new InspectionOrderVM();
                order.setOrderId(rs.getInt("order_id"));
                order.setCustomerName(rs.getString("full_name"));
                order.setCustomerPhone(rs.getString("phone"));
                order.setBikeName(rs.getString("bike_name"));
                order.setDepositAmount(rs.getBigDecimal("deposit_amount"));
                order.setReturnedAt(rs.getTimestamp("returned_at"));
                return order;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private BigDecimal getDepositAmount(int orderId) throws SQLException {
        String sql = "SELECT deposit_amount FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("deposit_amount") : BigDecimal.ZERO;
        }
    }

    
    // phương thức cũ 
//    private boolean processRefundInspection(int orderId, int adminId, String bikeCondition,
//                                      String damageNotes, BigDecimal damageFee, 
//                                      BigDecimal refundAmount, String refundMethod) {
//        Connection con = null;
//        try {
//            con = DBConnection.getConnection();
//            con.setAutoCommit(false);
//
//            // 1. Lưu kết quả kiểm tra
//            String inspectionSql = """
//                INSERT INTO RefundInspections (order_id, admin_id, bike_condition, 
//                damage_notes, damage_fee, refund_amount, refund_method, inspected_at)
//                VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE())
//                """;
//            try (PreparedStatement ps = con.prepareStatement(inspectionSql)) {
//                ps.setInt(1, orderId);
//                ps.setInt(2, adminId);
//                ps.setString(3, bikeCondition);
//                ps.setString(4, damageNotes);
//                ps.setBigDecimal(5, damageFee);
//                ps.setBigDecimal(6, refundAmount);
//                ps.setString(7, refundMethod);
//                ps.executeUpdate();
//            }
//
//            // 2. Cập nhật trạng thái deposit
//            String updateSql = "UPDATE RentalOrders SET deposit_status = ? WHERE order_id = ?";
//            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
//                String newDepositStatus = damageFee.compareTo(BigDecimal.ZERO) > 0 ? "forfeited" : "refunded";
//                ps.setString(1, newDepositStatus);
//                ps.setInt(2, orderId);
//                ps.executeUpdate();
//            }
//
//            // 3. XỬ LÝ HOÀN CỌC THEO HÌNH THỨC
//            if ("cash".equals(refundMethod)) {
//                // Hoàn tiền mặt - không cần làm gì thêm
//                System.out.println("DEBUG: Cash refund processed for order #" + orderId);
//
//            } else if ("wallet".equals(refundMethod)) {
//                // Hoàn vào ví customer - DÙNG HỆ THỐNG CŨ
//                refundToCustomerWallet(con, orderId, refundAmount);
//            }
//
//            con.commit();
//            System.out.println("=== REFUND PROCESSING SUCCESS ===");
//            return true;
//
//        } catch (SQLException e) {
//            System.err.println("=== REFUND PROCESSING FAILED ===");
//            System.err.println("ERROR: " + e.getMessage());
//
//            if (con != null) {
//                try { con.rollback(); } catch (SQLException ex) {
//                    System.err.println("ERROR: Rollback failed: " + ex.getMessage());
//                }
//            }
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (con != null) {
//                try { con.setAutoCommit(true); con.close(); 
//                    System.out.println("DEBUG: Connection closed");
//                } catch (SQLException e) {
//                    System.err.println("ERROR: Connection close failed: " + e.getMessage());
//                }
//            }
//        }
//    }
    
    
    private boolean processRefundInspection(int orderId, int adminId, String bikeCondition,
                                        String damageNotes, BigDecimal damageFee,
                                        BigDecimal refundAmount, String refundMethod) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1) Lấy customer_id (dùng để hoàn ví nếu cần)
            int customerId;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT customer_id FROM RentalOrders WHERE order_id = ?")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Không tìm thấy order #" + orderId);
                    customerId = rs.getInt("customer_id");
                }
            }

            // 2) Ghi biên bản kiểm tra + kết quả hoàn (status cố định 'refunded')
            try (PreparedStatement ps = con.prepareStatement("""
                INSERT INTO RefundInspections
                  (order_id, admin_id, bike_condition, damage_notes, damage_fee,
                   refund_amount, refund_method, refund_status, inspected_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'refunded', GETDATE())
            """)) {
                ps.setInt(1, orderId);
                ps.setInt(2, adminId);
                ps.setString(3, bikeCondition);
                ps.setString(4, damageNotes);
                ps.setBigDecimal(5, damageFee);
                ps.setBigDecimal(6, refundAmount);
                ps.setString(7, refundMethod); // 'cash' | 'wallet'
                ps.executeUpdate();
            }

            // 3) Cập nhật trạng thái cọc & trạng thái đơn (đóng đơn nếu cần)
            String newDepositStatus = (damageFee.compareTo(BigDecimal.ZERO) > 0) ? "forfeited" : "refunded";
            try (PreparedStatement ps = con.prepareStatement("""
                UPDATE RentalOrders
                   SET deposit_status = ?,
                       status = CASE WHEN status <> 'completed' THEN 'completed' ELSE status END
                 WHERE order_id = ?
            """)) {
                ps.setString(1, newDepositStatus);
                ps.setInt(2, orderId);
                ps.executeUpdate();
            }

            // 4) Nếu hoàn vào ví → cộng số dư ví
            if ("wallet".equals(refundMethod) && refundAmount.compareTo(BigDecimal.ZERO) > 0) {
                upsertWalletBalance(con, customerId, refundAmount);
                // (tuỳ chọn) thêm ghi log giao dịch ví nếu bạn có bảng lịch sử
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ignore) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignore) {}
        }
    }
    
    private void upsertWalletBalance(Connection con, int customerId, BigDecimal amount) throws SQLException {
        int updated;
        try (PreparedStatement up = con.prepareStatement("""
            UPDATE Wallets
               SET balance = balance + ?, updated_at = GETDATE()
             WHERE customer_id = ?
        """)) {
            up.setBigDecimal(1, amount);
            up.setInt(2, customerId);
            updated = up.executeUpdate();
        }
        if (updated == 0) {
            try (PreparedStatement ins = con.prepareStatement("""
                INSERT INTO Wallets (customer_id, balance, updated_at)
                VALUES (?, ?, GETDATE())
            """)) {
                ins.setInt(1, customerId);
                ins.setBigDecimal(2, amount);
                ins.executeUpdate();
            }
        }
    }



    // PHƯƠNG THỨC HOÀN TIỀN VÀO VÍ CUSTOMER - DÙNG HỆ THỐNG CŨ
    private void refundToCustomerWallet(Connection con, int orderId, BigDecimal amount) throws SQLException {
        // Lấy customer_id từ order
        String customerSql = """
            SELECT r.customer_id, c.full_name
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            WHERE r.order_id = ?
            """;

        try (PreparedStatement ps = con.prepareStatement(customerSql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String customerName = rs.getString("full_name");

                // Ghi giao dịch hoàn cọc vào DepositTransactions
                String transactionSql = """
                    INSERT INTO DepositTransactions (customer_id, order_id, amount, method, status, created_at)
                    VALUES (?, ?, ?, 'REFUND', 'completed', GETDATE())
                    """;
                try (PreparedStatement ps2 = con.prepareStatement(transactionSql)) {
                    ps2.setInt(1, customerId);
                    ps2.setInt(2, orderId);
                    ps2.setBigDecimal(3, amount);
                    ps2.executeUpdate();
                }

                System.out.println("DEBUG: Refunded " + amount + " to customer #" + customerId + " (" + customerName + ") for order #" + orderId);

            } else {
                throw new SQLException("Không tìm thấy customer cho order #" + orderId);
            }
        }
    }

    private void createWithdrawalRequest(Connection con, int orderId, BigDecimal amount) throws SQLException {
        // Lấy customer_id và thông tin ngân hàng từ order
        String customerSql = """
            SELECT r.customer_id, c.bank_account, c.bank_name
            FROM RentalOrders r 
            JOIN Customers c ON c.customer_id = r.customer_id 
            WHERE r.order_id = ?
            """;
        
        try (PreparedStatement ps = con.prepareStatement(customerSql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                int customerId = rs.getInt("customer_id");
                String bankAccount = rs.getString("bank_account");
                String bankName = rs.getString("bank_name");
                
                String withdrawalSql = """
                    INSERT INTO WithdrawalRequests (customer_id, amount, bank_account, bank_name, request_date, status)
                    VALUES (?, ?, ?, ?, GETDATE(), 'pending')
                    """;
                try (PreparedStatement ps2 = con.prepareStatement(withdrawalSql)) {
                    ps2.setInt(1, customerId);
                    ps2.setBigDecimal(2, amount);
                    ps2.setString(3, bankAccount);
                    ps2.setString(4, bankName);
                    ps2.executeUpdate();
                }
            }
        }
    }
}