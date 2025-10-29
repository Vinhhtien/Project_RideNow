package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.OrderDetailItem;
import model.OrderSummary;
import model.PaymentInfo;
import service.AdminOrderService;
import service.IAdminOrderService;

import java.io.IOException;
import java.util.*;

@WebServlet(name = "AdminOrderDetailServlet", urlPatterns = {"/admin/orders/detail"})
public class AdminOrderDetailServlet extends HttpServlet {
    private final IAdminOrderService service = new AdminOrderService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Giữ đúng kiểu check session như mẫu
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Integer id = null;
        try { id = Integer.valueOf(req.getParameter("id")); } catch (Exception ignored) {}
        if (id == null) {
            resp.sendRedirect(req.getContextPath() + "/admin/orders");
            return;
        }

        Optional<OrderSummary> header = service.findOrderHeader(id);
        if (header.isEmpty()) {
            req.setAttribute("notFound", true);
            req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
            return;
        }

        List<OrderDetailItem> items = service.findOrderItems(id);
        List<PaymentInfo> payments = service.findPayments(id);

        req.setAttribute("order", header.get());
        req.setAttribute("items", items);
        req.setAttribute("payments", payments);

        // ĐÚNG với cấu trúc hiện tại của bạn
        req.getRequestDispatcher("/admin/admin-order-detail.jsp").forward(req, resp);
    }
}
