package filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleFilterTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock FilterChain chain;

    private static Account acc(String role) { Account a = new Account(); a.setRole(role); return a; }

    @Test
    void noSession_redirectLogin() throws Exception {
        RoleFilter f = new RoleFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");
        f.doFilter(req, resp, chain);
        verify(resp).sendRedirect("/ctx/login");
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void wrongRole_403() throws Exception {
        RoleFilter f = new RoleFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        f.doFilter(req, resp, chain);
        verify(resp).sendError(403);
        verify(chain, never()).doFilter(any(), any());
    }

    @Test
    void adminPasses_chainCalled() throws Exception {
        RoleFilter f = new RoleFilter();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(acc("admin"));
        f.doFilter(req, resp, chain);
        verify(chain).doFilter(req, resp);
    }
}

