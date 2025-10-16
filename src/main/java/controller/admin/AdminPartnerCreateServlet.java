package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import model.Account;
import service.IPartnerAdminService;
import service.PartnerAdminService;

@WebServlet(name = "AdminPartnerCreateServlet", urlPatterns = {"/adminpartnercreate"})
public class AdminPartnerCreateServlet extends HttpServlet {
    private IPartnerAdminService partnerAdminService;

    @Override
    public void init() throws ServletException {
        super.init();
        partnerAdminService = new PartnerAdminService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Account account = (Account) session.getAttribute("account");
        
        // Debug
        System.out.println("=== DEBUG AdminPartnerCreateServlet doGet ===");
        System.out.println("Account in session: " + account);
        if (account != null) {
            System.out.println("Account ID: " + account.getAccountId());
            System.out.println("Username: " + account.getUsername());
            System.out.println("Role: " + account.getRole());
        }
        
        // Kiểm tra đăng nhập và role admin
        if (account == null) {
            System.out.println("No account - redirecting to login");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"admin".equalsIgnoreCase(account.getRole())) {
            System.out.println("Not admin role - sending 403");
            resp.sendError(403);
            return;
        }
        
        System.out.println("Forwarding to admin-partner-create.jsp");
        req.getRequestDispatcher("/admin/admin-partner-create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Account account = (Account) session.getAttribute("account");
        
        // Debug
        System.out.println("=== DEBUG AdminPartnerCreateServlet doPost ===");
        System.out.println("Account in session: " + account);
        if (account != null) {
            System.out.println("Account ID: " + account.getAccountId());
            System.out.println("Username: " + account.getUsername());
        }
        
        // Kiểm tra đăng nhập và role admin
        if (account == null || !"admin".equalsIgnoreCase(account.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        int adminAccountId = account.getAccountId(); // Lấy account_id của admin

        String username = req.getParameter("username");
        String companyName = req.getParameter("companyName");
        String address = req.getParameter("address");
        String phone = req.getParameter("phone");

        // Debug form data
        System.out.println("Form data - Username: " + username + ", Company: " + companyName);
        System.out.println("Admin Account ID: " + adminAccountId);

        try {
            partnerAdminService.createPartner(username, companyName, address, phone, adminAccountId);
            req.setAttribute("success", "Tạo partner thành công! Username: " + username + " | Mật khẩu mặc định: 1");
            
            // Clear form data sau khi thành công
            req.setAttribute("username", "");
            req.setAttribute("companyName", "");
            req.setAttribute("address", "");
            req.setAttribute("phone", "");
            
        } catch (RuntimeException e) {
            req.setAttribute("error", e.getMessage());
            // Giữ lại giá trị form khi có lỗi
            req.setAttribute("username", username);
            req.setAttribute("companyName", companyName);
            req.setAttribute("address", address);
            req.setAttribute("phone", phone);
        }
        
        req.getRequestDispatcher("/admin/admin-partner-create.jsp").forward(req, resp);
    }
}