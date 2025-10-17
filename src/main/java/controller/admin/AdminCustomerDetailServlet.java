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

@WebServlet(name = "AdminCustomerDetailServlet", urlPatterns = {"/admin/customers/detail"})
public class AdminCustomerDetailServlet extends HttpServlet {
    private final IAdminCustomerService service = new AdminCustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            int customerId = Integer.parseInt(req.getParameter("id"));
            AdminCustomerDTO detail = service.getCustomerDetail(customerId);
            
            if (detail == null) {
                req.getSession().setAttribute("flash", "Không tìm thấy khách hàng với ID: " + customerId);
                resp.sendRedirect(req.getContextPath() + "/admin/customers");
                return;
            }

            String flash = (String) req.getSession().getAttribute("flash");
            if (flash != null) {
                req.setAttribute("flash", flash);
                req.getSession().removeAttribute("flash");
            }

            req.setAttribute("detail", detail);
            req.getRequestDispatcher("/admin/admin-customer-detail.jsp").forward(req, resp);
            
        } catch (NumberFormatException e) {
            req.getSession().setAttribute("flash", "ID khách hàng không hợp lệ");
            resp.sendRedirect(req.getContextPath() + "/admin/customers");
        }
    }
}