package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;

import java.io.IOException;

import model.Account;

@WebFilter(urlPatterns = {"/admin/*", "/availability", "/adminpartnercreate"})
public class AdminOnlyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession();
        Account acc = (Account) session.getAttribute("account");

        // Debug: In ra thông tin session
        System.out.println("=== DEBUG AdminOnlyFilter ===");
        System.out.println("Request URL: " + req.getRequestURL());
        System.out.println("Account in session: " + acc);
        if (acc != null) {
            System.out.println("Account role: " + acc.getRole());
            System.out.println("Account username: " + acc.getUsername());
        }

        if (acc == null) {
            System.out.println("Redirecting to login - No account in session");
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!"admin".equalsIgnoreCase(acc.getRole())) {
            System.out.println("403 Forbidden - Not admin role");
            resp.sendError(403);
            return;
        }

        System.out.println("Admin access granted");
        chain.doFilter(request, response);
    }
}