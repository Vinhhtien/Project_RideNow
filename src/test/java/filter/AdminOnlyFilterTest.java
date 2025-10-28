package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOnlyFilterTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock FilterChain chain;

    private static Account acc(String role) { Account a = new Account(); a.setRole(role); a.setUsername("u"); return a; }

    @Test
    void noSession_redirectLogin() throws Exception {
        AdminOnlyFilter f = new AdminOnlyFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");
        f.doFilter(req, resp, chain);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void wrongRole_403() throws Exception {
        AdminOnlyFilter f = new AdminOnlyFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        f.doFilter(req, resp, chain);
        verify(resp).sendError(403);
    }

    @Test
    void admin_ok_chain() throws Exception {
        AdminOnlyFilter f = new AdminOnlyFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(acc("admin"));
        f.doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
    }
}

