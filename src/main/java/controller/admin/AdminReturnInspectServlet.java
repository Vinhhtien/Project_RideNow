package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;

@WebServlet("/adminreturninspect")
public class AdminReturnInspectServlet extends HttpServlet {

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

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Integer adminId = (Integer) session.getAttribute("admin_id");
        if (adminId == null) adminId = 1;

        String orderIdStr    = req.getParameter("orderId");
        String bikeCondition = req.getParameter("bikeCondition");
        String damageNotes   = req.getParameter("damageNotes");
        String damageFeeStr  = req.getParameter("damageFee");
        String refundMethod  = req.getParameter("refundMethod");

        try {
            // Validate
            if (orderIdStr == null || orderIdStr.isBlank()) {
                throw new IllegalArgumentException("Thiếu orderId");
            }
            int orderId = Integer.parseInt(orderIdStr.trim());

            if (!"excellent".equals(bikeCondition) &&
                !"good".equals(bikeCondition) &&
                !"damaged".equals(bikeCondition)) {
                throw new IllegalArgumentException("Tình trạng xe không hợp lệ");
            }

            if (!"cash".equals(refundMethod) && !"wallet".equals(refundMethod)) {
                refundMethod = "cash";
            }

            // Lấy tiền cọc
            BigDecimal depositAmount = getDepositAmount(orderId);
            if (depositAmount == null) depositAmount = BigDecimal.ZERO;

            // Tính phí hư hỏng
            BigDecimal damageFee = BigDecimal.ZERO;
            if (damageFeeStr != null && !damageFeeStr.isBlank()) {
                try {
                    damageFee = new BigDecimal(damageFeeStr.trim());
                } catch (NumberFormatException nfe) {
                    damageFee = BigDecimal.ZERO;
                }
            }
            if (damageFee.compareTo(BigDecimal.ZERO) < 0) damageFee = BigDecimal.ZERO;
            if (damageFee.compareTo(depositAmount) > 0) damageFee = depositAmount;

            // Tính tiền hoàn
            BigDecimal refundAmount = depositAmount.subtract(damageFee);
            if (refundAmount.compareTo(BigDecimal.ZERO) < 0) refundAmount = BigDecimal.ZERO;

            // Xử lý kiểm tra và tạo yêu cầu hoàn cọc
            boolean success = processInspection(
                    orderId, adminId, bikeCondition,
                    damageNotes, damageFee, refundAmount, refundMethod
            );

            if (success) {
                session.setAttribute("flash",
                        "✅ Đã kiểm tra xe và tạo yêu cầu hoàn cọc! Số tiền hoàn: " + 
                        refundAmount + " VNĐ");
            } else {
                session.setAttribute("flash", "❌ Xử lý kiểm tra thất bại!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash", "❌ Lỗi khi xử lý kiểm tra: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturns");
    }

        private InspectionOrderVM getOrderForInspection(int orderId) {
        String sql = """
            SELECT r.order_id, c.full_name, c.phone, b.bike_name, 
                   r.deposit_amount, r.returned_at
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            JOIN OrderDetails d ON d.order_id = r.order_id
            JOIN Motorbikes b ON b.bike_id = d.bike_id
            WHERE r.order_id = ? 
              AND r.return_status = 'returned'
              AND r.deposit_status = 'held'  -- QUAN TRỌNG: phải khớp với giá trị đã set
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

    private boolean processInspection(int orderId, int adminId, String bikeCondition,
                                   String damageNotes, BigDecimal damageFee,
                                   BigDecimal refundAmount, String refundMethod) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // Tạo bản ghi kiểm tra với trạng thái pending
            String inspectionSql = """
                INSERT INTO RefundInspections
                  (order_id, admin_id, bike_condition, damage_notes, damage_fee,
                   refund_amount, refund_method, refund_status, inspected_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', GETDATE())
            """;
            
            try (PreparedStatement ps = con.prepareStatement(inspectionSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, adminId);
                ps.setString(3, bikeCondition);
                ps.setString(4, damageNotes);
                ps.setBigDecimal(5, damageFee);
                ps.setBigDecimal(6, refundAmount);
                ps.setString(7, refundMethod);
                ps.executeUpdate();
            }

            // Cập nhật trạng thái đơn hàng thành refunding
            String updateOrderSql = "UPDATE RentalOrders SET deposit_status='refunding' WHERE order_id=?";
            try (PreparedStatement ps = con.prepareStatement(updateOrderSql)) {
                ps.setInt(1, orderId);
                ps.executeUpdate();
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
}