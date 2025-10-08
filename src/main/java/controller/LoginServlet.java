package controller;
// 30/9
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
        // Trang login sẽ tự đọc cookie qua EL, không cần xử lý thêm
        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String usernameOrEmail = req.getParameter("username");
        String password        = req.getParameter("password");
        boolean remember       = "on".equalsIgnoreCase(req.getParameter("remember"));

        try {
            Optional<Account> opt = accountService.login(usernameOrEmail, password);

            if (opt.isPresent()) {
                Account acc = opt.get();

                // Bắt buộc đã xác thực email
                if ("customer".equalsIgnoreCase(acc.getRole()) && !acc.isEmailVerified()) {
                    req.setAttribute("error", "Email của bạn chưa được xác thực. Vui lòng kiểm tra hộp thư.");
                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    return;
                }

                // Lưu session
                HttpSession session = req.getSession(true);
                session.setAttribute("account", acc);

                // Remember: lưu cả username & password (DEMO – không khuyến nghị production)
                if (remember) {
                    Cookie cu = new Cookie("rn_user", acc.getUsername());
                    cu.setHttpOnly(true);     // vẫn đọc được ở server (JSP EL), JS không đọc
                    cu.setMaxAge(14 * 24 * 60 * 60);
                    cu.setPath("/");
                    // cu.setSecure(true);      // bật nếu chạy HTTPS
                    resp.addCookie(cu);

                    Cookie cp = new Cookie("rn_pw", password);
                    cp.setHttpOnly(true);     // vẫn auto-fill qua EL ở server
                    cp.setMaxAge(14 * 24 * 60 * 60);
                    cp.setPath("/");
                    // cp.setSecure(true);
                    resp.addCookie(cp);
                } else {
                    // Xoá cookie nếu không nhớ
                    Cookie cu = new Cookie("rn_user", "");
                    cu.setMaxAge(0);
                    cu.setPath("/");
                    resp.addCookie(cu);

                    Cookie cp = new Cookie("rn_pw", "");
                    cp.setMaxAge(0);
                    cp.setPath("/");
                    resp.addCookie(cp);
                }

                // Điều hướng
                String role = acc.getRole();
                    if ("customer".equalsIgnoreCase(role)) {
                        resp.sendRedirect(req.getContextPath() + "/home.jsp");
                    } else if ("partner".equalsIgnoreCase(role)) {
                        resp.sendRedirect(req.getContextPath() + "/dashboard");          
                    } else { // admin
                        resp.sendRedirect(req.getContextPath() + "/admin/dashboard");     // khu admin mới
                    }
                    
            } else {
                req.setAttribute("error", "Sai tài khoản/mật khẩu hoặc tài khoản bị khóa");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
