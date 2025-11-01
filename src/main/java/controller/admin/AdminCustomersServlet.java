package controller.admin;

import model.Account;
import model.AdminCustomerDTO;
import service.AdminCustomerService;
import service.IAdminCustomerService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "AdminCustomersServlet", urlPatterns = {"/admin/customers"})
public class AdminCustomersServlet extends HttpServlet {
    private final IAdminCustomerService service = new AdminCustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String q = Optional.ofNullable(req.getParameter("q")).orElse("");
        String status = Optional.ofNullable(req.getParameter("status")).orElse("all");
        String walletFilter = Optional.ofNullable(req.getParameter("walletFilter")).orElse("all");
        String sort = Optional.ofNullable(req.getParameter("sort")).orElse("createdAt");
        String dir = Optional.ofNullable(req.getParameter("dir")).orElse("desc");
        int page = Integer.parseInt(Optional.ofNullable(req.getParameter("page")).orElse("1"));
        int pageSize = Integer.parseInt(Optional.ofNullable(req.getParameter("pageSize")).orElse("10"));

        List<AdminCustomerDTO> customers = service.searchCustomers(q, status, walletFilter, sort, dir, page, pageSize);
        int totalItems = service.countCustomers(q, status, walletFilter);
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        java.math.BigDecimal totalWalletBalance = service.getTotalWalletBalance();

        req.setAttribute("customers", customers);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", pageSize);
        req.setAttribute("totalItems", totalItems);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("totalWalletBalance", totalWalletBalance);
        req.setAttribute("q", q);
        req.setAttribute("status", status);
        req.setAttribute("walletFilter", walletFilter);
        req.setAttribute("sort", sort);
        req.setAttribute("dir", dir);

        String flash = (String) req.getSession().getAttribute("flash");
        if (flash != null) {
            req.setAttribute("flash", flash);
            req.getSession().removeAttribute("flash");
        }

        req.getRequestDispatcher("/admin/admin-customers-management.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("toggle".equals(action)) {
            int customerId = Integer.parseInt(req.getParameter("customerId"));
            service.toggleCustomerStatus(customerId);
            req.getSession().setAttribute("flash", "Đã cập nhật trạng thái tài khoản #" + customerId);
        }

        resp.sendRedirect(req.getHeader("referer"));
    }
}