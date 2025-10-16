package controller.Authetication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import utils.DBConnection;
import utils.EmailUtil;

@WebServlet(name="ForgotPasswordServlet", urlPatterns={"/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/forgot.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        if (email == null || email.isBlank()) {
            req.setAttribute("msg", "Vui lòng nhập email đã đăng ký.");
            req.getRequestDispatcher("/forgot.jsp").forward(req, resp);
            return;
        }

        final String sqlFindAcc =
            "SELECT a.account_id, c.full_name " +
            "FROM Accounts a JOIN Customers c ON c.account_id = a.account_id " +
            "WHERE c.email = ?";

        // ĐÚNG tên bảng + cột ở DB: Password_Reset_Tokens + expire_at
        final String sqlInsertTok =
            "INSERT INTO Password_Reset_Tokens(account_id, token, expire_at) " +
            "VALUES(?, ?, DATEADD(HOUR, 1, GETDATE()))";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlFindAcc)) {

            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int accId = rs.getInt("account_id");
                    String name = rs.getString("full_name");

                    String token = java.util.UUID.randomUUID().toString().replace("-", "");

                    try (PreparedStatement ps2 = con.prepareStatement(sqlInsertTok)) {
                        ps2.setInt(1, accId);
                        ps2.setString(2, token);
                        ps2.executeUpdate();
                    }

                    // ĐÚNG mapping của servlet reset: /resetpassword
                    String link = req.getRequestURL().toString()
                            .replace("/forgot", "") + "/resetpassword?token=" + token;

                    EmailUtil.sendMail(
                        email,
                        "Đặt lại mật khẩu RideNow",
                        "Chào " + name + ",\n\n" +
                        "Nhấn vào liên kết để đặt lại mật khẩu (hạn 60 phút):\n" +
                        link + "\n\nRideNow"
                    );
                }
            }

            // Trả thông điệp chung để tránh lộ email tồn tại hay không
            req.setAttribute("msg", "Nếu email tồn tại, chúng tôi đã gửi liên kết đặt lại mật khẩu.");
            req.getRequestDispatcher("/forgot.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
