package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;
import java.io.IOException;
import java.util.List;

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
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String orderIdStr = req.getParameter("orderId");
        System.out.println("DEBUG: Processing return for order: " + orderIdStr);

        if (orderIdStr != null && !orderIdStr.trim().isEmpty()) {
            try {
                int orderId = Integer.parseInt(orderIdStr);
                
                // ĐƠN GIẢN: Luôn dùng admin_id = 1
                int adminId = 1;

                System.out.println("DEBUG: Calling confirmOrderReturn with adminId: " + adminId);
                boolean success = orderService.confirmOrderReturn(orderId, adminId);

                if (success) {
                    req.getSession().setAttribute("flash", "✅ Đã xác nhận trả xe thành công! Tiến hành kiểm tra xe để hoàn cọc.");
                } else {
                    req.getSession().setAttribute("flash", "❌ Xác nhận thất bại! Đơn hàng không tồn tại hoặc đã được trả.");
                }

            } catch (NumberFormatException e) {
                System.err.println("ERROR: Invalid order ID format: " + orderIdStr);
                req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ!");
            } catch (Exception e) {
                System.err.println("ERROR: Unexpected error: " + e.getMessage());
                e.printStackTrace();
                req.getSession().setAttribute("flash", "❌ Lỗi hệ thống khi xác nhận trả xe: " + e.getMessage());
            }
        } else {
            req.getSession().setAttribute("flash", "❌ Không tìm thấy mã đơn hàng!");
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturn");
    }
}