package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IOrderManageService;
import service.OrderManageService;
import model.Account;
import java.io.IOException;

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
                // SỬA: Dùng admin_id = 1 từ bảng Admins
                int adminId = 1;

                boolean success = orderService.confirmOrderPickup(orderId, adminId);

                if (success) {
                    req.getSession().setAttribute("flash", "✅ Đã xác nhận khách nhận xe thành công!");
                } else {
                    req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                }

            } catch (NumberFormatException e) {
                req.getSession().setAttribute("flash", "❌ Mã đơn hàng không hợp lệ!");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/adminpickup");
    }
}
