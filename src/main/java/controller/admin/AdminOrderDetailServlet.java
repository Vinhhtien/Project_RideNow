package controller.admin;

import dao.AdminOrderDAO;
import dao.IAdminOrderDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.OrderDetailItem;
import model.OrderSummary;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import utils.DBConnection;

@WebServlet(name = "AdminOrderDetailServlet", urlPatterns = {"/admin/orders/detail"})
public class AdminOrderDetailServlet extends HttpServlet {

    private final IAdminOrderDAO orderDAO = new AdminOrderDAO();

    // DTO nhỏ cho phần refund (đúng các field jsp đang dùng: refund.*, inspectedAt ...)
    public static class RefundInfoDTO {
        private BigDecimal refundAmount;
        private String refundMethod;     // cash | wallet
        private String refundStatus;     // pending/processing/completed/cancelled/refunded
        private String bikeCondition;    // excellent/good/damaged
        private BigDecimal damageFee;
        private String damageNotes;
        private String adminNotes;
        private Timestamp inspectedAt;

        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getRefundMethod() { return refundMethod; }
        public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
        public String getRefundStatus() { return refundStatus; }
        public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }
        public String getBikeCondition() { return bikeCondition; }
        public void setBikeCondition(String bikeCondition) { this.bikeCondition = bikeCondition; }
        public BigDecimal getDamageFee() { return damageFee; }
        public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
        public String getDamageNotes() { return damageNotes; }
        public void setDamageNotes(String damageNotes) { this.damageNotes = damageNotes; }
        public String getAdminNotes() { return adminNotes; }
        public void setAdminNotes(String adminNotes) { this.adminNotes = adminNotes; }
        public Timestamp getInspectedAt() { return inspectedAt; }
        public void setInspectedAt(Timestamp inspectedAt) { this.inspectedAt = inspectedAt; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Bảo vệ admin
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // LƯU Ý: link ở JSP dùng ?id= → đọc tham số "id"
        String idStr = req.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            req.setAttribute("notFound", true);
            req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            req.setAttribute("notFound", true);
            req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
            return;
        }

        try {
            // Header
            Optional<OrderSummary> opt = orderDAO.findOrderHeader(orderId);
            if (opt.isEmpty()) {
                req.setAttribute("notFound", true);
                req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
                return;
            }
            OrderSummary order = opt.get();

            // Items + Payments
            List<OrderDetailItem> items = orderDAO.findOrderItems(orderId);
            List<model.PaymentInfo> payments = orderDAO.findPayments(orderId);

            req.setAttribute("order", order);
            req.setAttribute("items", items);
            req.setAttribute("payments", payments);

            // REFUND (lấy bản mới nhất nếu có)
            RefundInfoDTO refund = fetchLatestRefund(orderId);
            if (refund != null) {
                req.setAttribute("refund", refund);
            }

            req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
        } catch (Exception ex) {
            ex.printStackTrace();
            req.setAttribute("notFound", true);
            req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
        }
    }

    private RefundInfoDTO fetchLatestRefund(int orderId) {
        final String sql = """
            SELECT TOP 1
                   refund_amount, refund_method, refund_status,
                   bike_condition, damage_fee, damage_notes, admin_notes, inspected_at
            FROM RefundInspections
            WHERE order_id = ?
            ORDER BY inspected_at DESC, inspection_id DESC
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                RefundInfoDTO r = new RefundInfoDTO();
                r.setRefundAmount(rs.getBigDecimal("refund_amount"));
                r.setRefundMethod(rs.getString("refund_method"));
                r.setRefundStatus(rs.getString("refund_status"));
                r.setBikeCondition(rs.getString("bike_condition"));
                r.setDamageFee(rs.getBigDecimal("damage_fee"));
                r.setDamageNotes(rs.getString("damage_notes"));
                r.setAdminNotes(rs.getString("admin_notes"));
                r.setInspectedAt(rs.getTimestamp("inspected_at"));
                return r;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
