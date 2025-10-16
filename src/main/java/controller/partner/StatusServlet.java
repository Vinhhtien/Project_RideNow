
package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import model.Account;
import model.Notification;
import service.INotificationService;
import service.NotificationService;

@WebServlet(name = "StatusServlet", urlPatterns = {"/status/maintenance", "/status"})
public class StatusServlet extends HttpServlet {
    private final INotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        // Chỉ partner xem trang này (tuỳ bạn, có thể cho admin xem luôn)
        if (!"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Lấy nid từ query ?nid=...
        int nid = 0;
        try { nid = Integer.parseInt(req.getParameter("nid")); } catch (Exception ignored) {}

        Notification n = null;
        if (nid > 0) {
            n = notificationService.findByIdForAccount(nid, acc.getAccountId());
            // Mark read ngay khi mở chi tiết (nếu có)
            if (n != null && !n.isRead()) {
                notificationService.readOne(nid, acc.getAccountId());
            }
        }

        // Gán attribute cho JSP (động nếu có n, fallback nếu không)
        req.setAttribute("title", (n != null && n.getTitle() != null) ? n.getTitle() : "Bảo trì hệ thống");
        req.setAttribute("message", (n != null && n.getMessage() != null) ? n.getMessage()
                : "Trang trạng thái bảo trì. Nếu bạn truy cập từ thông báo thì nội dung sẽ hiển thị tại đây.");
        req.setAttribute("createdAt", (n != null) ? n.getCreatedAt() : new java.util.Date());
        req.setAttribute("nid", nid);

        // Forward tới JSP trong /partners
        req.getRequestDispatcher("/partners/maintenance.jsp").forward(req, resp);
    }
}
