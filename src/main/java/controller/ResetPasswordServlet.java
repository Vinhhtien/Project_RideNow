package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import utils.DBConnection;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/resetpassword"})
public class ResetPasswordServlet extends HttpServlet {

    // GET: người dùng bấm link từ email ?token=...
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token = req.getParameter("token");
        if (token == null || token.isBlank()) {
            req.getSession(true).setAttribute("flash", "Liên kết đặt lại mật khẩu không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        final String sqlCheckToken = """
            SELECT TOP 1 account_id
            FROM Password_Reset_Tokens
            WHERE token = ? AND used = 0 AND expire_at > GETDATE()
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlCheckToken)) {

            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Token hợp lệ -> hiển thị form đổi mật khẩu
                    req.setAttribute("token", token);
                    req.getRequestDispatcher("/reset.jsp").forward(req, resp);
                } else {
                    req.getSession(true).setAttribute("flash",
                            "Liên kết đặt lại không hợp lệ hoặc đã hết hạn.");
                    resp.sendRedirect(req.getContextPath() + "/login");
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // POST: người dùng submit mật khẩu mới
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String token   = req.getParameter("token");
        String newPw   = req.getParameter("password");
        String confirm = req.getParameter("confirm");

        if (token == null || token.isBlank()) {
            req.getSession(true).setAttribute("flash", "Thiếu token đặt lại.");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (newPw == null || newPw.isBlank()) {
            req.setAttribute("token", token);
            req.setAttribute("err", "Vui lòng nhập mật khẩu mới.");
            req.getRequestDispatcher("/reset.jsp").forward(req, resp);
            return;
        }
        if (confirm != null && !newPw.equals(confirm)) {
            req.setAttribute("token", token);
            req.setAttribute("err", "Xác nhận mật khẩu không khớp.");
            req.getRequestDispatcher("/reset.jsp").forward(req, resp);
            return;
        }

        // Cập nhật Accounts.password và đánh dấu token used=1 (đúng tên bảng/cột)
        final String sqlUpdate = """
            DECLARE @acc INT;
            SELECT TOP 1 @acc = account_id
            FROM Password_Reset_Tokens
            WHERE token = ? AND used = 0 AND expire_at > GETDATE();

            IF @acc IS NOT NULL
            BEGIN
                UPDATE Accounts SET password = ? WHERE account_id = @acc; -- DEMO: plaintext
                UPDATE Password_Reset_Tokens SET used = 1 WHERE token = ?;
                SELECT 1 AS ok;
            END
            ELSE
                SELECT 0 AS ok;
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlUpdate)) {

            ps.setString(1, token);
            ps.setString(2, newPw);     // TODO: hash mật khẩu ở môi trường sản xuất
            ps.setString(3, token);

            try (ResultSet rs = ps.executeQuery()) {
                boolean ok = rs.next() && rs.getInt("ok") == 1;
                if (ok) {
                    req.getSession(true).setAttribute("flash",
                            "Đổi mật khẩu thành công! Hãy đăng nhập.");
                    resp.sendRedirect(req.getContextPath() + "/login");
                } else {
                    req.getSession(true).setAttribute("flash",
                            "Liên kết đặt lại không hợp lệ hoặc đã hết hạn.");
                    resp.sendRedirect(req.getContextPath() + "/login");
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
