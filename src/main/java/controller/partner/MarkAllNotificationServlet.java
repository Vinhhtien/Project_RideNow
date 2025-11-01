//an

package controller.partner;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

import model.Account;
import utils.DBConnection;

@WebServlet(name = "MarkAllNotificationServlet", urlPatterns = {"/partner/notifications/mark-all"})
public class MarkAllNotificationServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"ok\":false}");
            return;
        }

        int updated = 0;
        final String sql = "UPDATE Notifications SET is_read=1 WHERE account_id=? AND is_read=0";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, acc.getAccountId());
            updated = ps.executeUpdate();
        } catch (Exception ignored) {
        }

        resp.setContentType("application/json; charset=UTF-8");
        resp.getWriter().write("{\"ok\":true,\"updated\":" + updated + "}");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
}
