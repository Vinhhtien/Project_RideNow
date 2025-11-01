//an
package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

import model.Account;
import service.NotificationService;

@WebServlet("/partner/notifications/delete-read")
public class DeleteReadNotificationsServlet extends HttpServlet {
    private final NotificationService notificationService = new NotificationService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"ok\":false,\"error\":\"unauthorized\"}");
            return;
        }

        int deleted = notificationService.deleteRead(acc.getAccountId());
        resp.getWriter().write("{\"ok\":true,\"deleted\":" + deleted + "}");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
