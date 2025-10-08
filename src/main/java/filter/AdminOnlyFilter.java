// package filter
package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import model.Account;

@WebFilter(urlPatterns = {"/admin/*"})
public class AdminOnlyFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req  = (HttpServletRequest) request;
    HttpServletResponse resp = (HttpServletResponse) response;

    Account acc = (Account) req.getSession().getAttribute("account");
    if (acc == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
    if (!"admin".equalsIgnoreCase(acc.getRole())) { resp.sendError(403); return; }

    chain.doFilter(request, response);
  }
}
