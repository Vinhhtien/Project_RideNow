package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

import model.Account;
import model.Partner;
import service.IPartnerAdminService;
import service.PartnerAdminService;

@WebServlet(name = "PartnersManagementServlet", urlPatterns = {"/admin/partners"})
public class PartnersManagementServlet extends HttpServlet {
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
        
        // Kiểm tra đăng nhập và role admin
        if (account == null || !"admin".equalsIgnoreCase(account.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Lấy danh sách partners để hiển thị
        try {
            List<Partner> partners = partnerAdminService.getAllPartners();
            req.setAttribute("partners", partners);
        } catch (Exception e) {
            req.setAttribute("error", "Không thể tải danh sách đối tác: " + e.getMessage());
        }
        
        req.getRequestDispatcher("/admin/admin-partners-management.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        HttpSession session = req.getSession();
        Account account = (Account) session.getAttribute("account");
        
        // Kiểm tra đăng nhập và role admin
        if (account == null || !"admin".equalsIgnoreCase(account.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // Chỉ xử lý xóa partner
        handleDeletePartner(req, resp);
    }

    private void handleDeletePartner(HttpServletRequest req, HttpServletResponse resp) 
            throws IOException {
        HttpSession session = req.getSession();
        String partnerIdStr = req.getParameter("partnerId");
        
        if (partnerIdStr != null) {
            try {
                int partnerId = Integer.parseInt(partnerIdStr);
                boolean success = partnerAdminService.deletePartner(partnerId);
                
                if (success) {
                    session.setAttribute("success", "Xóa partner thành công!");
                } else {
                    session.setAttribute("error", "Không thể xóa partner!");
                }
            } catch (NumberFormatException e) {
                session.setAttribute("error", "ID partner không hợp lệ!");
            } catch (RuntimeException e) {
                session.setAttribute("error", "Lỗi khi xóa partner: " + e.getMessage());
            }
        } else {
            session.setAttribute("error", "Thiếu thông tin partner ID!");
        }
        
        resp.sendRedirect(req.getContextPath() + "/admin/partners");
    }
}