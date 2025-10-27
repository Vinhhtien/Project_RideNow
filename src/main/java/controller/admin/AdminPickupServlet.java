package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
        req.setAttribute("today", LocalDate.now());
        req.setAttribute("todayStr", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
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
        String actionType = req.getParameter("actionType"); // NEW: Loại hành động
        String notes = req.getParameter("notes"); // NEW: Ghi chú

        if (orderIdStr != null) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                int adminId = 1;

                // Xử lý theo loại hành động
                if ("normal_pickup".equals(actionType)) {
                    // Xác nhận nhận xe bình thường
                    boolean canPickup = orderService.canPickupOrder(orderId);
                    
                    if (!canPickup) {
                        req.getSession().setAttribute("flash", "❌ Không thể cho nhận xe trước ngày thuê!");
                        resp.sendRedirect(req.getContextPath() + "/adminpickup");
                        return;
                    }

                    boolean success = orderService.confirmOrderPickup(orderId, adminId);
                    if (success) {
                        req.getSession().setAttribute("flash", "✅ Đã xác nhận khách nhận xe thành công!");
                    } else {
                        req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                    }
                    
                } else if ("overdue_pickup".equals(actionType)) {
                    // Xác nhận nhận xe quá hạn - đã giao xe thực tế
                    boolean success = orderService.confirmOrderPickup(orderId, adminId);
                    if (success) {
                        String message = "✅ Đã xác nhận khách nhận xe (quá hạn)";
                        if (notes != null && !notes.trim().isEmpty()) {
                            message += " - " + notes;
                        }
                        req.getSession().setAttribute("flash", message);
                    } else {
                        req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                    }
                    
                } else if ("mark_not_given".equals(actionType)) {
                    // Đánh dấu là chưa giao xe
                    boolean success = orderService.markOrderAsNotGiven(orderId, adminId, notes);
                    if (success) {
                        String message = "⚠️ Đã đánh dấu đơn hàng chưa giao xe";
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
            }
        }

        resp.sendRedirect(req.getContextPath() + "/adminpickup");
    }
}