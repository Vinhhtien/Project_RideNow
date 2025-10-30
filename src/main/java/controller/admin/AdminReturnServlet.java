package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import service.IOrderManageService;
import service.OrderManageService;
import utils.DBConnection;

import java.io.IOException;
import java.sql.*;
import java.util.List;

@WebServlet("/adminreturn")
public class AdminReturnServlet extends HttpServlet {
    private final IOrderManageService orderService = new OrderManageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equalsIgnoreCase(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        System.out.println("DEBUG: Loading active orders for return page");
        List<Object[]> activeOrders = orderService.getActiveOrders();
        System.out.println("DEBUG: Active orders count: " + activeOrders.size());

        req.setAttribute("activeOrders", activeOrders);
        req.getRequestDispatcher("/admin/admin-return.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equalsIgnoreCase(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        final String orderIdStr = req.getParameter("orderId");
        final String actionType = req.getParameter("actionType");
        final String notesParam = req.getParameter("notes");
        final String lateFeeStr = req.getParameter("lateFee");

        System.out.println("DEBUG: Processing return -> orderId=" + orderIdStr +
                ", actionType=" + actionType + ", lateFee=" + lateFeeStr);

        if (orderIdStr == null || orderIdStr.isBlank()) {
            req.getSession().setAttribute("flash", "❌ Không tìm thấy mã đơn hàng!");
            resp.sendRedirect(req.getContextPath() + "/adminreturn");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr.trim());

            // Lấy admin_id theo account đang đăng nhập
            Integer adminId = findAdminIdByAccountId(admin.getAccountId());
            if (adminId == null) {
                req.getSession().setAttribute("flash", "❌ Không tìm thấy admin_id cho tài khoản hiện tại!");
                resp.sendRedirect(req.getContextPath() + "/adminreturn");
                return;
            }

            // 1) GIỮ WORKFLOW CŨ: gọi service xác nhận trả xe
            System.out.println("DEBUG: Calling confirmOrderReturn with adminId: " + adminId);
            boolean success = orderService.confirmOrderReturn(orderId, adminId);

            if (!success) {
                req.getSession().setAttribute("flash", "❌ Xác nhận thất bại! Đơn hàng không tồn tại hoặc đã được trả.");
                resp.sendRedirect(req.getContextPath() + "/adminreturn");
                return;
            }

            // 2) Ghi nhận phiếu kiểm tra/ghi chú/phí trễ (không đổi schema)
            if ("overdue_return".equalsIgnoreCase(actionType)) {
                long fee = safeParseLong(lateFeeStr, 0L);
                String adminNotes = "[OVERDUE_RETURN] " + (notesParam == null ? "(no-notes)" : notesParam.trim());
                upsertInspectionOnReturn(orderId, adminId, adminNotes, fee);
            } else if ("normal_return".equalsIgnoreCase(actionType)) {
                String adminNotes = "[NORMAL_RETURN] " + (notesParam == null ? "(no-notes)" : notesParam.trim());
                upsertInspectionOnReturn(orderId, adminId, adminNotes, 0L);
            } else if ("mark_not_returned".equalsIgnoreCase(actionType)) {
                // chỉ thông báo, không thay đổi dữ liệu
                req.getSession().setAttribute("flash", "📝 Đã ghi nhận: đơn hàng chưa trả xe.");
                resp.sendRedirect(req.getContextPath() + "/adminreturn");
                return;
            }

            // 3) Gửi thông báo cho partner (fail-safe)
            String notifMsg = ("overdue_return".equalsIgnoreCase(actionType))
                    ? "Khách đã trả xe (trễ). Đơn hàng chuyển sang kiểm tra. [ORDER:" + orderId + "]"
                    : "Khách đã trả xe. Đơn hàng chuyển sang kiểm tra. [ORDER:" + orderId + "]";
            notifyPartnerOrderCompleted(orderId, "Đơn hàng đã nhận xe", notifMsg);

            req.getSession().setAttribute("flash",
                    "✅ Đã xác nhận trả xe" +
                            ("overdue_return".equalsIgnoreCase(actionType) ? " (quá hạn, đã ghi phí trễ)" : "") +
                            "! Vui lòng qua mục Verify & Refund để kiểm tra/hoàn cọc.");

        } catch (NumberFormatException e) {
            System.err.println("ERROR: Invalid order ID format: " + orderIdStr);
            req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ!");
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            req.getSession().setAttribute("flash", "❌ Lỗi hệ thống khi xác nhận trả xe: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturn");
    }

    /** Parse long an toàn */
    private long safeParseLong(String s, long def) {
        try { return (s == null || s.isBlank()) ? def : Long.parseLong(s.trim()); }
        catch (Exception e) { return def; }
    }

    /** Lấy admin_id từ bảng Admins theo account_id hiện tại */
    private Integer findAdminIdByAccountId(int accountId) {
        String sql = "SELECT admin_id FROM Admins WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 
     * FIX: Tạo/cập nhật RefundInspections với inspected_at = NULL 
     * để đơn hàng xuất hiện trong "Chờ kiểm tra"
     */
    private void upsertInspectionOnReturn(int orderId, int adminId, String adminNotes, long lateFee) {
        final String findOpenSql = """
            SELECT TOP 1 inspection_id
            FROM RefundInspections
            WHERE order_id = ? AND refund_status IN ('pending','processing')
            ORDER BY inspected_at DESC
            """;
        
        // FIX: Thay đổi inspected_at thành NULL
        final String insertSql = """
            INSERT INTO RefundInspections(
              order_id, admin_id, bike_condition, damage_notes, damage_fee,
              refund_amount, refund_method, refund_status, admin_notes, inspected_at, updated_at
            ) VALUES (?, ?, 'good', NULL, ?, 0, 'wallet', 'pending', ?, NULL, GETDATE())
            """;
        
        final String updateSql = """
            UPDATE RefundInspections
               SET damage_fee = ?,
                   admin_notes = CASE WHEN ? IS NOT NULL AND LEN(RTRIM(LTRIM(?)))>0 THEN ? ELSE admin_notes END,
                   updated_at = GETDATE()
             WHERE inspection_id = ?
            """;

        try (Connection con = DBConnection.getConnection()) {
            Integer existId = null;
            try (PreparedStatement ps = con.prepareStatement(findOpenSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) existId = rs.getInt(1);
                }
            }

            if (existId == null) {
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, adminId);
                    ps.setLong(3, lateFee);
                    ps.setString(4, adminNotes);
                    int rows = ps.executeUpdate();
                    System.out.println("DEBUG: Created inspection with NULL inspected_at for order " + orderId + ", rows=" + rows);
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setLong(1, lateFee);
                    ps.setString(2, adminNotes);
                    ps.setString(3, adminNotes);
                    ps.setString(4, adminNotes);
                    ps.setInt(5, existId);
                    int rows = ps.executeUpdate();
                    System.out.println("DEBUG: Updated existing inspection for order " + orderId + ", rows=" + rows);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("WARN: upsertInspectionOnReturn failed: " + e.getMessage());
        }
    }

    /** Gửi thông báo cho partner; fail-safe nếu thiếu bảng Notifications */
    private void notifyPartnerOrderCompleted(int orderId, String title, String message) {
        final String findPartnerAccountSql = """
            SELECT TOP 1 a.account_id
            FROM RentalOrders r
            JOIN OrderDetails d ON d.order_id = r.order_id
            JOIN Motorbikes b  ON b.bike_id  = d.bike_id
            JOIN Partners p    ON p.partner_id = b.partner_id
            JOIN Accounts a    ON a.account_id = p.account_id
            WHERE r.order_id = ?
            """;

        final String insertNotificationSql = """
            INSERT INTO Notifications (account_id, title, message, is_read, created_at)
            VALUES (?, ?, ?, 0, GETDATE())
            """;

        try (Connection con = DBConnection.getConnection()) {
            Integer partnerAccountId = null;

            try (PreparedStatement ps = con.prepareStatement(findPartnerAccountSql)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) partnerAccountId = rs.getInt(1);
                }
            }

            if (partnerAccountId == null) {
                System.out.println("[Notify] No partner found for order #" + orderId);
                return;
            }

            if (!tableExists(con, "Notifications")) {
                System.out.println("[Notify] Table 'Notifications' not found. Skip notification for order #" + orderId);
                return;
            }

            try (PreparedStatement ps2 = con.prepareStatement(insertNotificationSql)) {
                ps2.setInt(1, partnerAccountId);
                ps2.setString(2, title);
                ps2.setString(3, message);
                ps2.executeUpdate();
            }
            System.out.println("[Notify] Notification sent to partner for order #" + orderId);

        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("[Notify] Error sending notification: " + ex.getMessage());
        }
    }

    /** Kiểm tra bảng tồn tại (SQL Server) */
    private boolean tableExists(Connection con, String tableName) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?")) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }
}