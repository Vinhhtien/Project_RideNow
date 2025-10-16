package controller.Authetication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.GoogleUser;
import utils.DBConnection;
import utils.GoogleUtils;

import java.io.IOException;
import java.sql.*;
import utils.PasswordUtil;

@WebServlet(name = "GoogleLoginServlet", urlPatterns = {"/logingoogle"})
public class GoogleLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String code = req.getParameter("code");

        // 1) Chưa có code -> sang Google
        if (code == null || code.isBlank()) {
            resp.sendRedirect(GoogleUtils.buildAuthURL());
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            // 2) Đổi code lấy token + info
            String accessToken = GoogleUtils.exchangeCodeForToken(code);
            if (accessToken == null || accessToken.isBlank()) {
                log("[GoogleLogin] exchangeCodeForToken trả về null/blank");
                req.getSession(true).setAttribute("flash", "Không lấy được thông tin Google (token).");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            GoogleUser gUser = GoogleUtils.fetchUserInfo(accessToken);
            if (gUser == null || gUser.getEmail() == null || gUser.getEmail().isBlank()) {
                log("[GoogleLogin] fetchUserInfo thất bại hoặc email null");
                req.getSession(true).setAttribute("flash", "Không lấy được thông tin Google.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // 3) Tìm account theo email
            Integer accId = null;
            String findSql = """
                SELECT a.account_id
                FROM Accounts a
                JOIN Customers c ON c.account_id = a.account_id
                WHERE c.email = ?
            """;
            try (PreparedStatement ps = con.prepareStatement(findSql)) {
                ps.setString(1, gUser.getEmail());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) accId = rs.getInt(1);
                }
            }

            // 4) Nếu chưa có -> tạo mới Accounts + Customers
            if (accId == null) {
                con.setAutoCommit(false);
                String username = baseUsername(gUser.getEmail());
                username = ensureUniqueUsername(con, username);

                String insertAcc = """
                    INSERT INTO Accounts(username, password, role, status, created_at, email_verified)
                    OUTPUT INSERTED.account_id
                    VALUES(?, ?, 'customer', 1, GETDATE(), 1)
                """;
                try (PreparedStatement ps = con.prepareStatement(insertAcc)) {
                    ps.setString(1, username);
                    ps.setString(2, PasswordUtil.hashPassword(randomPassword())); // ✅ hash
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) accId = rs.getInt(1);
                    }
}

                String insertCus = """
                    INSERT INTO Customers(account_id, full_name, email, phone, address)
                    VALUES(?,?,?,?,?)
                """;
                try (PreparedStatement ps = con.prepareStatement(insertCus)) {
                    ps.setInt(1, accId);
                    ps.setString(2, gUser.getName() != null ? gUser.getName() : username);
                    ps.setString(3, gUser.getEmail());
                    ps.setString(4, null);
                    ps.setString(5, null);
                    ps.executeUpdate();
                }

                con.commit();
            }

            // 5) Lưu session
            Account acc = new Account();
            acc.setAccountId(accId);
            acc.setUsername(extractLocalPart(gUser.getEmail()));
            acc.setRole("customer");
            acc.setStatus(true);
            acc.setEmailVerified(true);

            HttpSession session = req.getSession(true);
            session.setAttribute("account", acc);
            session.setAttribute("customerName", gUser.getName());

            resp.sendRedirect(req.getContextPath() + "/home.jsp");

        } catch (Exception e) {
            log("[GoogleLogin] Exception", e);
            throw new ServletException(e);
        }
    }

    // ===== helpers =====
    private static String baseUsername(String email) {
        return extractLocalPart(email).replaceAll("[^a-zA-Z0-9._-]", "");
    }
    private static String extractLocalPart(String email) {
        int i = email.indexOf('@');
        return i > 0 ? email.substring(0, i) : email;
    }
    private static String randomPassword() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
    private static String ensureUniqueUsername(Connection con, String base) throws SQLException {
        String u = base;
        int n = 0;
        String sql = "SELECT COUNT(*) FROM Accounts WHERE username = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            while (true) {
                ps.setString(1, u);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) == 0) return u;
                }
                n++;
                u = base + n;
            }
        }
    }
}
