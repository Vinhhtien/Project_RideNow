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
    // Khai báo service xử lý logic tạo partner
    private IPartnerAdminService partnerAdminService;

    @Override
    public void init() throws ServletException {
        super.init();
        // Khởi tạo service khi servlet được load
        partnerAdminService = new PartnerAdminService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Lấy session hiện tại
        HttpSession session = req.getSession();
        Account account = (Account) session.getAttribute("account");

        // In thông tin tài khoản để debug
        System.out.println("=== DEBUG AdminPartnerCreateServlet doGet ===");
        System.out.println("Account in session: " + account);
        if (account != null) {
            System.out.println("Account ID: " + account.getAccountId());
            System.out.println("Username: " + account.getUsername());
            System.out.println("Role: " + account.getRole());
        }

        // Kiểm tra đăng nhập
        if (account == null) {
            System.out.println("No account - redirecting to login");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Kiểm tra quyền admin
        if (!"admin".equalsIgnoreCase(account.getRole())) {
            System.out.println("Not admin role - sending 403");
            resp.sendError(403); // Trả về lỗi 403 nếu không phải admin
            return;
        }

        // Nếu hợp lệ, chuyển hướng đến trang tạo partner
        System.out.println("Forwarding to admin-partner-create.jsp");
        req.getRequestDispatcher("/admin/admin-partner-create.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Lấy session và tài khoản hiện tại
        HttpSession session = req.getSession();
        Account account = (Account) session.getAttribute("account");

        // In thông tin tài khoản để debug
        System.out.println("=== DEBUG AdminPartnerCreateServlet doPost ===");
        System.out.println("Account in session: " + account);
        if (account != null) {
            System.out.println("Account ID: " + account.getAccountId());
System.out.println("Username: " + account.getUsername());
        }

        // Kiểm tra đăng nhập và quyền admin
        if (account == null || !"admin".equalsIgnoreCase(account.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Lấy thông tin từ form
        int adminAccountId = account.getAccountId(); // ID của admin tạo partner
        String username = req.getParameter("username");
        String companyName = req.getParameter("companyName");
        String address = req.getParameter("address");
        String phone = req.getParameter("phone");

        // In thông tin form để debug
        System.out.println("Form data - Username: " + username + ", Company: " + companyName);
        System.out.println("Admin Account ID: " + adminAccountId);

        try {
            // Gọi service để tạo partner mới
            partnerAdminService.createPartner(username, companyName, address, phone, adminAccountId);

            // Nếu thành công, hiển thị thông báo và xóa dữ liệu form
            req.setAttribute("success", "Tạo partner thành công! Username: " + username + " | Mật khẩu mặc định: 1");
            req.setAttribute("username", "");
            req.setAttribute("companyName", "");
            req.setAttribute("address", "");
            req.setAttribute("phone", "");

        } catch (RuntimeException e) {
            // Nếu có lỗi, hiển thị thông báo lỗi và giữ lại dữ liệu form
            req.setAttribute("error", e.getMessage());
            req.setAttribute("username", username);
            req.setAttribute("companyName", companyName);
            req.setAttribute("address", address);
            req.setAttribute("phone", phone);
        }

        // Chuyển hướng lại trang tạo partner để hiển thị kết quả
        req.getRequestDispatcher("/admin/admin-partner-create.jsp").forward(req, resp);
    }
}