package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Account;

import java.io.IOException;

@WebFilter(urlPatterns = {"/motorbikes/manage", "/motorbikes/manage/*"})
public class RoleFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String role = acc.getRole();
        if (!"admin".equalsIgnoreCase(role) && !"partner".equalsIgnoreCase(role)) {
            resp.sendError(403);
            return;
        }
        chain.doFilter(request, response);
    }
}

