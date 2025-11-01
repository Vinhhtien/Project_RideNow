//an

package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;

import model.Account;
import utils.DBConnection;

@WebServlet(name = "AdminBroadcastNotificationServlet", urlPatterns = {"/admin/notify"})
public class AdminBroadcastNotificationServlet extends HttpServlet {

    private static final int TITLE_MAX = 200;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null || acc.getRole() == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        req.getRequestDispatcher("/admin/admin-notify-partners.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null || acc.getRole() == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String title = safeTrim(req.getParameter("title"));
        String message = safeTrim(req.getParameter("message"));
        // dùng cùng ô "username" cho cả username hoặc company_name
        String recipient = safeTrim(req.getParameter("username"));

        if (title == null || title.isEmpty() || message == null || message.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/admin/notify?broadcast=invalid");
            return;
        }
        if (title.length() > TITLE_MAX) title = title.substring(0, TITLE_MAX);

        boolean hasRecipient = recipient != null && !recipient.isEmpty();

        String sqlAll = """
                INSERT INTO Notifications (account_id, title, message, is_read, created_at)
                SELECT DISTINCT a.account_id, ?, ?, 0, SYSDATETIME()
                FROM Accounts a
                JOIN Partners p ON p.account_id = a.account_id
                WHERE a.role = 'partner'
                """;

        // So khớp không phân biệt hoa/thường và loại bỏ khoảng trắng hai đầu
        String sqlTarget = """
                INSERT INTO Notifications (account_id, title, message, is_read, created_at)
                SELECT DISTINCT a.account_id, ?, ?, 0, SYSDATETIME()
                FROM Accounts a
                JOIN Partners p ON p.account_id = a.account_id
                WHERE a.role = 'partner'
                  AND (
                        LOWER(LTRIM(RTRIM(a.username))) = LOWER(LTRIM(RTRIM(?))) OR
                        LOWER(LTRIM(RTRIM(COALESCE(p.company_name, '')))) = LOWER(LTRIM(RTRIM(?)))
                      )
                """;

        int inserted = 0;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(hasRecipient ? sqlTarget : sqlAll)) {

            int i = 1;
            ps.setNString(i++, title);
            ps.setNString(i++, message);

            if (hasRecipient) {
                ps.setNString(i++, recipient);
                ps.setNString(i++, recipient);
            }

            inserted = ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/notify?broadcast=fail");
            return;
        }

        if (hasRecipient && inserted == 0) {
            resp.sendRedirect(req.getContextPath() + "/admin/notify?broadcast=notfound");
        } else {
            resp.sendRedirect(req.getContextPath() + "/admin/notify?broadcast=ok&sent=" + inserted);
        }
    }

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
