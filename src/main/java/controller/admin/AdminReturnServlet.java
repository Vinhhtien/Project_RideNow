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
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equalsIgnoreCase(admin.getRole())) {
          resp.sendRedirect(req.getContextPath()+"/login"); return;
        }

        String action       = req.getParameter("action");              // mark_processing | complete_refund | cancel
        String refundMethod = req.getParameter("refundMethod");        // wallet | cash
        String orderIdStr   = req.getParameter("orderId");
        String insIdStr     = req.getParameter("inspectionId");

        try (Connection con = DBConnection.getConnection()) {
          con.setAutoCommit(false);

          if ("mark_processing".equals(action)) {
            int inspectionId = Integer.parseInt(insIdStr);
            try (PreparedStatement ps = con.prepareStatement(
              "UPDATE RefundInspections SET refund_status='processing', updated_at=GETDATE(), admin_id=? " +
              "WHERE inspection_id=? AND refund_status='pending'")) {
              ps.setInt(1, 1); // TODO: lấy admin_id thật nếu có
              ps.setInt(2, inspectionId);
              ps.executeUpdate();
            }
            con.commit();
            req.getSession().setAttribute("flash","✅ Đã duyệt yêu cầu");

          } else if ("complete_refund".equals(action)) {
            int orderId = Integer.parseInt(orderIdStr);

            // Lấy inspectionId nếu chưa gửi từ form
            Integer inspectionId = null;
            if (insIdStr != null && !insIdStr.isBlank()) {
              inspectionId = Integer.parseInt(insIdStr);
            } else {
              try (PreparedStatement ps = con.prepareStatement(
                "SELECT TOP 1 inspection_id FROM RefundInspections " +
                "WHERE order_id=? AND refund_status IN ('pending','processing') " +
                "ORDER BY inspected_at DESC")) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                  if (rs.next()) inspectionId = rs.getInt(1);
                }
              }
            }

            if (inspectionId == null) throw new SQLException("Không tìm thấy phiếu kiểm tra cho order #" + orderId);

            // 1) hoàn tất inspection + set phương thức
            try (PreparedStatement ps = con.prepareStatement(
              "UPDATE RefundInspections SET refund_status='completed', refund_method=?, updated_at=GETDATE(), admin_id=? WHERE inspection_id=?")) {
              ps.setString(1, "cash".equalsIgnoreCase(refundMethod) ? "cash" : "wallet");
              ps.setInt(2, 1);
              ps.setInt(3, inspectionId);
              ps.executeUpdate();
            }

            // 2) (khuyến nghị) ghi log 1 payment reverse cho audit
            try (PreparedStatement ps = con.prepareStatement(
              "INSERT INTO Payments(order_id, amount, method, status, payment_date, verified_by, verified_at) " +
              "SELECT order_id, refund_amount, ?, 'refunded', GETDATE(), ?, GETDATE() FROM RefundInspections WHERE inspection_id=?")) {
              ps.setString(1, "cash".equalsIgnoreCase(refundMethod) ? "cash" : "bank_transfer");
              ps.setInt(2, 1);
              ps.setInt(3, inspectionId);
              ps.executeUpdate();
            }

            // 3) QUAN TRỌNG: set đơn hàng → completed
            try (PreparedStatement ps = con.prepareStatement(
              "UPDATE RentalOrders SET status='completed' WHERE order_id=?")) {
              ps.setInt(1, orderId);
              ps.executeUpdate();
            }

            con.commit();
            req.getSession().setAttribute("flash","✅ Hoàn tiền thành công, đơn đã chuyển 'Hoàn thành'");

          } else if ("cancel".equals(action)) {
            int inspectionId = Integer.parseInt(insIdStr);
            try (PreparedStatement ps = con.prepareStatement(
              "UPDATE RefundInspections SET refund_status='cancelled', updated_at=GETDATE(), admin_id=? " +
              "WHERE inspection_id=? AND refund_status IN ('pending','processing')")) {
              ps.setInt(1, 1);
              ps.setInt(2, inspectionId);
              ps.executeUpdate();
            }
            con.commit();
            req.getSession().setAttribute("flash","⛔ Đã từ chối yêu cầu");
          }

        } catch (Exception e) {
          e.printStackTrace();
          req.getSession().setAttribute("flash","❌ Lỗi: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath()+"/adminreturns");
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