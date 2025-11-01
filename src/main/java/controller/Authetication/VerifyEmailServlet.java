package controller.Authetication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import utils.DBConnection;

@WebServlet(name = "VerifyEmailServlet", urlPatterns = {"/verify"})
public class VerifyEmailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isEmpty()) {
            req.getSession(true).setAttribute("flash", "Liên kết không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String sel = "SELECT account_id, used, expire_at FROM Email_Verify_Tokens WHERE token = ?";
        String updAcc = "UPDATE Accounts SET email_verified = 1 WHERE account_id = ?";
        String updTok = "UPDATE Email_Verify_Tokens SET used = 1 WHERE token = ?";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);

            Integer accId = null;
            boolean used = true;
            Timestamp expireAt = null;

            // 1) Đọc token
            try (PreparedStatement ps = con.prepareStatement(sel)) {
                ps.setString(1, token);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        accId = rs.getInt("account_id");
                        used = rs.getBoolean("used");
                        expireAt = rs.getTimestamp("expire_at");
                    }
                }
            }

            // 2) Kiểm tra hợp lệ
            if (accId == null || used || expireAt == null || expireAt.before(new Timestamp(System.currentTimeMillis()))) {
                con.rollback();
                req.getSession(true).setAttribute("flash", "Liên kết xác thực không hợp lệ hoặc đã hết hạn.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            // 3) Cập nhật
            try (PreparedStatement ps1 = con.prepareStatement(updAcc)) {
                ps1.setInt(1, accId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = con.prepareStatement(updTok)) {
                ps2.setString(1, token);
                ps2.executeUpdate();
            }

            con.commit();
            req.getSession(true).setAttribute("flash", "Xác thực email thành công! Hãy đăng nhập.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
