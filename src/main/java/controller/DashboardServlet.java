
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import model.Account;
import model.Notification;
import service.INotificationService;
import service.NotificationService;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
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

        String role = acc.getRole();
        if ("partner".equalsIgnoreCase(role)) {
            // Pop-up: lấy tối đa 5 thông báo chưa đọc
            List<Notification> toasts =
                    notificationService.getUnreadToasts(acc.getAccountId(), 5);
            req.setAttribute("toastNotifications", toasts);

            // Drawer: lấy toàn bộ thông báo (mới -> cũ) để hiển thị trong panel
            // page=1, size=100, q=null, onlyUnread=null (tức là tất cả)
            List<Notification> all =
                    notificationService.findByAccount(acc.getAccountId(), 1, 100, null, null);
            req.setAttribute("allNotifications", all);

            // Badge chuông (tổng số chưa đọc)
            int unread = notificationService.countUnread(acc.getAccountId());
            req.setAttribute("unreadCount", unread);

            req.setAttribute("role", role);
            req.getRequestDispatcher("partners/dashboard.jsp").forward(req, resp);

        } else if ("admin".equalsIgnoreCase(role)) {
            req.setAttribute("role", role);
            req.getRequestDispatcher("admin/dashboard.jsp").forward(req, resp);

        } else {
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("read".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                notificationService.readOne(id, acc.getAccountId());
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }

        if ("readAll".equals(action)) {
            notificationService.readAll(acc.getAccountId());
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
}
