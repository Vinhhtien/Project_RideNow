package controller.Authetication;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Optional;

import model.Account;
import service.IAccountService;
import service.AccountService;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private final IAccountService accountService = new AccountService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usernameOrEmail = req.getParameter("username");
        String password = req.getParameter("password");
        boolean remember = "on".equalsIgnoreCase(req.getParameter("remember"));

        try {
            Optional<Account> opt = accountService.login(usernameOrEmail, password);

            if (opt.isPresent()) {
                Account acc = opt.get();

                // DEBUG: Kiểm tra giá trị status
                System.out.println("Login attempt - Username: " + usernameOrEmail +
                        ", Status: " + acc.isStatus() +
                        ", Role: " + acc.getRole());


                if (acc.isStatus() == false) {
                    req.setAttribute("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    return;
                }

                // Email verification for customers
                if ("customer".equalsIgnoreCase(acc.getRole()) && !acc.isEmailVerified()) {
                    req.setAttribute("error", "Email của bạn chưa được xác thực. Vui lòng kiểm tra hộp thư.");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    return;
                }

                // Lưu session
                HttpSession session = req.getSession(true);
                session.setAttribute("account", acc);

                // Remember me functionality
                handleRememberMe(resp, remember, acc.getUsername(), password);

                // Redirect based on role with password enforcement for partners
                redirectBasedOnRole(req, resp, acc);

            } else {
                req.setAttribute("error", "Sai tài khoản/mật khẩu hoặc tài khoản bị khóa");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();
            req.setAttribute("error", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private void handleRememberMe(HttpServletResponse resp, boolean remember, String username, String password) {
        if (remember) {
            // Store username cookie (safe)
            Cookie userCookie = new Cookie("rn_user", username);
            userCookie.setHttpOnly(true);
//            userCookie.setMaxAge(14 * 24 * 60 * 60); // 14 ngày 
            userCookie.setMaxAge(60 * 60); // 1 tiếng  
            userCookie.setPath("/");
            resp.addCookie(userCookie);

            // Store password cookie (for demo only - consider security risks)
            Cookie pwdCookie = new Cookie("rn_pw", password);
            pwdCookie.setHttpOnly(true);
            pwdCookie.setMaxAge(14 * 24 * 60 * 60);
            pwdCookie.setPath("/");
            resp.addCookie(pwdCookie);
        } else {
            // Clear remember me cookies
            clearCookie(resp, "rn_user");
            clearCookie(resp, "rn_pw");
        }
    }

    private void clearCookie(HttpServletResponse resp, String cookieName) {
        Cookie cookie = new Cookie(cookieName, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        resp.addCookie(cookie);
    }

    private void redirectBasedOnRole(HttpServletRequest req, HttpServletResponse resp, Account acc)
            throws IOException {
        String role = acc.getRole();
        String contextPath = req.getContextPath();

        switch (role.toLowerCase()) {
            case "customer":
                resp.sendRedirect(contextPath + "/home.jsp");
                break;

            case "partner":
                // Check if partner is using default password "1"
                if ("1".equals(acc.getPassword())) {
                    // Force password change
                    resp.sendRedirect(contextPath + "/partner?action=editProfile&forcePwd=1");
                } else {
                    resp.sendRedirect(contextPath + "/dashboard");
                }
                break;

            case "admin":
                resp.sendRedirect(contextPath + "/admin/dashboard");
                break;

            default:
                // Unknown role - redirect to home
                resp.sendRedirect(contextPath + "/home.jsp");
                break;
        }
    }
}