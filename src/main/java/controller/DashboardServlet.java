// an

package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

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

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = acc.getRole();
        if ("partner".equalsIgnoreCase(role)) {
            final int accountId = acc.getAccountId();

            // Lấy danh sách mới nhất (mới -> cũ), size=100 (thay cho findByAccount)
            List<Notification> all = notificationService.latestForAccount(accountId, 100, 0);
            req.setAttribute("allNotifications", all);

            // Lấy 5 thông báo chưa đọc để show toast (thay cho getUnreadToasts)
            List<Notification> toasts = all.stream()
                    .filter(n -> n != null && !n.isRead())
                    .limit(5)
                    .collect(Collectors.toList());
            req.setAttribute("toastNotifications", toasts);

            // Badge chuông (số chưa đọc)
            int unread = notificationService.countUnread(accountId);
            req.setAttribute("unreadCount", unread);

            req.setAttribute("role", role);
            req.getRequestDispatcher("/partners/dashboard.jsp").forward(req, resp);

        } else if ("admin".equalsIgnoreCase(role)) {
            req.setAttribute("role", role);
            req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);

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
