package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

import model.Account;
import model.Customer;
import service.CustomerService;
import service.ICustomerService;

@WebServlet(name = "CustomerProfileServlet", urlPatterns = {"/customer/profile"})
public class CustomerProfileServlet extends HttpServlet {

    private final ICustomerService service = new CustomerService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"customer".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
            return;
        }
        try {
            Customer c = service.getProfile(acc.getAccountId());
            req.setAttribute("profile", c);
            req.getRequestDispatcher("/customer/profile.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"customer".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
            return;
        }

        String action = req.getParameter("action");
        if ("changePassword".equalsIgnoreCase(action)) {
            // ===== ĐỔI MẬT KHẨU =====
            String current = req.getParameter("current_pw");
            String npw = req.getParameter("new_pw");
            String cfpw = req.getParameter("confirm_pw");

            try {
                if (current == null || current.isBlank()
                        || npw == null || npw.isBlank()
                        || cfpw == null || cfpw.isBlank()) {
                    req.getSession().setAttribute("flash_err", "Vui lòng nhập đầy đủ các trường mật khẩu.");
                    resp.sendRedirect(req.getContextPath() + "/customer/profile#security");
                    return;
                }
                if (!npw.equals(cfpw)) {
                    req.getSession().setAttribute("flash_err", "Xác nhận mật khẩu không khớp.");
                    resp.sendRedirect(req.getContextPath() + "/customer/profile#security");
                    return;
                }
                if (npw.length() < 6) {
                    req.getSession().setAttribute("flash_err", "Mật khẩu mới tối thiểu 6 ký tự.");
                    resp.sendRedirect(req.getContextPath() + "/customer/profile#security");
                    return;
                }

                boolean ok = service.changePassword(acc.getAccountId(), current, npw);
                if (ok) {
                    req.getSession().setAttribute("flash", "Đổi mật khẩu thành công.");
                } else {
                    req.getSession().setAttribute("flash_err", "Mật khẩu hiện tại không đúng.");
                }
                resp.sendRedirect(req.getContextPath() + "/customer/profile#security");
            } catch (Exception e) {
                throw new ServletException(e);
            }
            return;
        }

        // ===== CẬP NHẬT HỒ SƠ =====
        try {
            Customer c = new Customer();
            c.setAccountId(acc.getAccountId());
            c.setFullName(req.getParameter("full_name"));
            c.setEmail(req.getParameter("email"));
            c.setPhone(req.getParameter("phone"));
            c.setAddress(req.getParameter("address"));

            service.saveProfile(c);
            req.getSession().setAttribute("flash", "Cập nhật hồ sơ thành công.");
            resp.sendRedirect(req.getContextPath() + "/customer/profile");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
