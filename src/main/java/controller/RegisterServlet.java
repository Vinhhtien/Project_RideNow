package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.*;

import utils.DBConnection;
import utils.EmailUtil;
import utils.PasswordUtil;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        String fullName = req.getParameter("full_name");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String address = req.getParameter("address");

        String insertAcc = "INSERT INTO Accounts(username, password, role, status, email_verified) VALUES(?, ?, 'customer', 1, 0)";
        String insertCus = "INSERT INTO Customers(account_id, full_name, email, phone, address) VALUES(?,?,?,?,?)";
        String insertTok = "INSERT INTO Email_Verify_Tokens(account_id, token, expire_at) VALUES(?,?,?)";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            int newAccId = -1;

            try (PreparedStatement ps = con.prepareStatement(insertAcc, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, PasswordUtil.hashPassword(password)); // ✅ HASH trước khi lưu
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) newAccId = rs.getInt(1);
                }
            }
            if (newAccId <= 0) throw new SQLException("Không lấy được account_id mới");

            try (PreparedStatement ps2 = con.prepareStatement(insertCus)) {
                ps2.setInt(1, newAccId);
                ps2.setString(2, fullName);
                ps2.setString(3, email);
                ps2.setString(4, phone);
                ps2.setString(5, address);
                ps2.executeUpdate();
            }

            // Tạo token verify
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            java.sql.Timestamp expireAt = new java.sql.Timestamp(System.currentTimeMillis() + 24L * 60 * 60 * 1000); // 24h

            try (PreparedStatement ps3 = con.prepareStatement(insertTok)) {
                ps3.setInt(1, newAccId);
                ps3.setString(2, token);
                ps3.setTimestamp(3, expireAt);
                ps3.executeUpdate();
            }

            con.commit();

            // Gửi email xác thực
            String verifyLink =
                    req.getScheme() + "://" + req.getServerName() +
                            ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort()) +
                            req.getContextPath() + "/verify?token=" + token;

            EmailUtil.sendMail(email, "Xác thực tài khoản RideNow",
                    "Chào " + fullName + ",\n\n" +
                            "Vui lòng bấm vào liên kết để xác thực tài khoản:\n" +
                            verifyLink + "\n\n" +
                            "Liên kết có hiệu lực trong 24 giờ.\n\nRideNow");

            req.getSession(true).setAttribute("flash", "Đăng ký thành công! Vui lòng kiểm tra email để xác thực tài khoản.");
            resp.sendRedirect(req.getContextPath() + "/login");
        } catch (SQLException e) {
            req.setAttribute("error", "Đăng ký thất bại: " + e.getMessage());
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


}
