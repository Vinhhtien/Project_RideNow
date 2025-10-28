//an
package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;

import java.io.IOException;
import java.util.List;

// thêm import dùng DBConnection như các servlet admin khác
import utils.DBConnection;
import java.sql.*;

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

        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                int adminId = 1; // theo dự án hiện tại

                boolean success = orderService.confirmOrderReturn(orderId, adminId);

                if (success) {
                    // 1) flash cho admin
                    req.getSession().setAttribute("flash", "✅ Đơn hàng đã hoàn tất.");

                    // 2) gửi thông báo cho Partner sở hữu xe của đơn này
                    notifyPartnerOrderCompleted(orderId);

                } else {
                    req.getSession().setAttribute("flash",
                            "❌ Xác nhận thất bại! Đơn không tồn tại hoặc đã trả.");
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
     * Gửi thông báo cho Partner của đơn hàng: “Khách đã trả xe. Đơn hàng đã hoàn tất.”
     * Không thay đổi cấu trúc, dùng trực tiếp DBConnection và bảng Notifications sẵn có.
     * Message kèm token [ORDER:{id}] để trang chi tiết của Partner tự dẫn tới lịch sử.
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
            if (partnerAccountId == null) return; // không tìm thấy chủ xe -> bỏ qua yên lặng

            String title = "Đơn hàng đã hoàn tất";
            String message = "Khách đã trả xe. Đơn hàng đã hoàn tất. [ORDER:" + orderId + "]";

            try (PreparedStatement ps2 = con.prepareStatement(insertNotificationSql)) {
                ps2.setInt(1, partnerAccountId);
                ps2.setString(2, title);
                ps2.setString(3, message);
                ps2.executeUpdate();
            }
        } catch (SQLException ex) {
            // ghi log, không chặn luồng admin
            ex.printStackTrace();
        }
    }
}
