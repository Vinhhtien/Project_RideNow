package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.OrderSummary;
import service.AdminOrderService;
import service.IAdminOrderService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "AdminOrdersServlet", urlPatterns = {"/admin/orders"})
public class AdminOrdersServlet extends HttpServlet {
    private final IAdminOrderService service = new AdminOrderService();
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    private Date parse(String s) {
        try {
            return (s == null || s.isBlank()) ? null : DF.parse(s);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String status = req.getParameter("status"); // "", pending, confirmed, cancelled, completed
        String q = req.getParameter("q");
        Date from = parse(req.getParameter("from"));
        Date to = parse(req.getParameter("to"));

        int page = 1, pageSize = 10;
        try {
            page = Math.max(1, Integer.parseInt(req.getParameter("page")));
        } catch (Exception ignore) {
        }
        try {
            pageSize = Math.max(1, Integer.parseInt(req.getParameter("pageSize")));
        } catch (Exception ignore) {
        }

        // gọi service -> DAO đã filter s.order_status = ?
        List<OrderSummary> orders = service.findOrders(
                (status != null && !status.isBlank()) ? status : null, q, from, to, page, pageSize
        );
        int total = service.countOrders(
                (status != null && !status.isBlank()) ? status : null, q, from, to
        );
        int totalPages = (int) Math.ceil((double) total / pageSize);

        req.setAttribute("orders", orders);
        req.setAttribute("total", total);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("status", status == null ? "" : status);
        req.setAttribute("q", q == null ? "" : q);
        req.setAttribute("from", req.getParameter("from") == null ? "" : req.getParameter("from"));
        req.setAttribute("to", req.getParameter("to") == null ? "" : req.getParameter("to"));

        req.getRequestDispatcher("/admin/admin-order-list.jsp").forward(req, resp);
    }
}
