package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import utils.DBConnection;

@WebServlet("/adminreturn")
public class AdminReturnServlet extends HttpServlet {
    private final IOrderManageService orderService = new OrderManageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Object[]> activeOrders = orderService.getActiveOrders();
        req.setAttribute("activeOrders", activeOrders);
        req.setAttribute("today", LocalDate.now());
        req.setAttribute("todayStr", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        req.getRequestDispatcher("/admin/admin-return.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        String actionType = req.getParameter("actionType"); // NEW: Loại hành động
        String notes = req.getParameter("notes"); // NEW: Ghi chú
        String lateFee = req.getParameter("lateFee"); // NEW: Phí trễ

        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                int adminId = 1;

                // Xử lý theo loại hành động
                if ("normal_return".equals(actionType)) {
                    // Trả xe bình thường
                    boolean canReturn = orderService.canReturnOrder(orderId);
                    
                    if (!canReturn) {
                        req.getSession().setAttribute("flash", "❌ Không thể trả xe trước ngày kết thúc thuê!");
                        resp.sendRedirect(req.getContextPath() + "/adminreturn");
                        return;
                    }

                    boolean success = orderService.confirmOrderReturn(orderId, adminId);
                    if (success) {
                        String message = "✅ Đã xác nhận khách trả xe thành công!";
                        req.getSession().setAttribute("flash", message);
                        notifyPartnerOrderCompleted(orderId);
                    } else {
                        req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                    }
                    
                } else if ("overdue_return".equals(actionType)) {
                    // Trả xe quá hạn - có phí trễ
                    boolean success = orderService.confirmOverdueReturn(orderId, adminId, lateFee, notes);
                    if (success) {
                        String message = "⚠️ Đã xác nhận trả xe quá hạn";
                        if (lateFee != null && !lateFee.trim().isEmpty()) {
                            message += " - Phí trễ: " + lateFee;
                        }
                        if (notes != null && !notes.trim().isEmpty()) {
                            message += " - " + notes;
                        }
                        req.getSession().setAttribute("flash", message);
                        notifyPartnerOrderCompleted(orderId);
                    } else {
                        req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                    }
                    
                } else if ("mark_not_returned".equals(actionType)) {
                    // Đánh dấu là chưa trả xe
                    boolean success = orderService.markOrderAsNotReturned(orderId, adminId, notes);
                    if (success) {
                        String message = "🔴 Đã đánh dấu đơn hàng chưa trả xe";
                        if (notes != null && !notes.trim().isEmpty()) {
                            message += " - " + notes;
                        }
                        req.getSession().setAttribute("flash", message);
                    } else {
                        req.getSession().setAttribute("flash", "❌ Cập nhật thất bại!");
                    }
                }

            } catch (NumberFormatException e) {
                req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ!");
            } catch (Exception e) {
                e.printStackTrace();
                req.getSession().setAttribute("flash",
                        "❌ Lỗi hệ thống khi xác nhận trả xe: " + e.getMessage());
            }
        } else {
            req.getSession().setAttribute("flash", "❌ Không tìm thấy mã đơn hàng!");
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturn");
    }

    /**
     * Gửi thông báo cho Partner của đơn hàng: "Khách đã trả xe. Đơn hàng đã hoàn tất."
     */
    private void notifyPartnerOrderCompleted(int orderId) {
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
                    if (rs.next()) {
                        partnerAccountId = rs.getInt(1);
                    }
                }
            }
            if (partnerAccountId == null) return;

            String title = "Đơn hàng đã hoàn tất";
            String message = "Khách đã trả xe. Đơn hàng đã hoàn tất. [ORDER:" + orderId + "]";

            try (PreparedStatement ps2 = con.prepareStatement(insertNotificationSql)) {
                ps2.setInt(1, partnerAccountId);
                ps2.setString(2, title);
                ps2.setString(3, message);
                ps2.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}