package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;

import java.io.IOException;

// THÊM: dùng DBConnection để chèn Notification
import utils.DBConnection;

import java.sql.*;

@WebServlet("/adminpickup")
public class AdminPickupServlet extends HttpServlet {
    private final IOrderManageService orderService = new OrderManageService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        req.setAttribute("orders", orderService.getOrdersForPickup());
        req.getRequestDispatcher("/admin/admin-pickup.jsp").forward(req, resp);
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

        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                int adminId = 1; // theo dự án hiện tại

                boolean success = orderService.confirmOrderPickup(orderId, adminId);

                if (success) {
                    // Flash giữ nguyên
                    req.getSession().setAttribute("flash", "✅ Đã xác nhận khách nhận xe thành công!");
                    // THÊM: gửi thông báo cho partner sở hữu xe của đơn này
                    notifyPartnerPickupConfirmed(orderId);
                } else {
                    req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                }

            } catch (NumberFormatException e) {
                req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ!");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/adminpickup");
    }

    // THÊM: helper nội bộ, không đổi cấu trúc dự án
    private void notifyPartnerPickupConfirmed(int orderId) {
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
            if (partnerAccountId == null) return;

            String title = "Khách đã nhận xe";
            String message = "Khách đã tới nhận xe. [ORDER:" + orderId + "]";

            try (PreparedStatement ps2 = con.prepareStatement(insertNotificationSql)) {
                ps2.setInt(1, partnerAccountId);
                ps2.setString(2, title);
                ps2.setString(3, message);
                ps2.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace(); // chỉ log, không ảnh hưởng luồng admin
        }
    }
}
