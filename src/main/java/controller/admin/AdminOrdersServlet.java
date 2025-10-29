package controller.admin;

import model.Account;
import model.OrderSummary;
import service.AdminOrderService;
import service.IAdminOrderService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "AdminOrdersServlet", urlPatterns = {"/admin/orders"})
public class AdminOrdersServlet extends HttpServlet {
    private final IAdminOrderService service = new AdminOrderService();
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    private java.util.Date parseDate(String s){
        try { return (s == null || s.isBlank()) ? null : DF.parse(s.trim()); }
        catch (Exception e){ return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Giữ đúng logic như mẫu: lấy 'account' trong session và kiểm tra role
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String status   = Optional.ofNullable(req.getParameter("status")).orElse("");
        String q        = Optional.ofNullable(req.getParameter("q")).orElse("");
        String fromStr  = req.getParameter("from");
        String toStr    = req.getParameter("to");
        int page        = Integer.parseInt(Optional.ofNullable(req.getParameter("page")).orElse("1"));
        int pageSize    = Integer.parseInt(Optional.ofNullable(req.getParameter("pageSize")).orElse(String.valueOf(DEFAULT_PAGE_SIZE)));

        java.util.Date from = parseDate(fromStr);
        java.util.Date to   = parseDate(toStr);

        List<OrderSummary> orders = service.findOrders(
                status.isBlank() ? null : status,
                q.isBlank() ? null : q,
                from, to, page, pageSize
        );
        int total = service.countOrders(status.isBlank()?null:status, q.isBlank()?null:q, from, to);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("orders", orders);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("total", total);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("status", status);
        req.setAttribute("q", q);
        req.setAttribute("from", fromStr);
        req.setAttribute("to", toStr);

        // ĐÚNG với cấu trúc hiện tại của bạn (JSP nằm trong /admin)
        req.getRequestDispatcher("/admin/admin-order-list.jsp").forward(req, resp);
    }
}
