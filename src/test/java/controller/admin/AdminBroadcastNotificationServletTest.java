package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.DisplayName;
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
class AdminBroadcastNotificationServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET redirects to login when not admin")
    void get_redirects_when_not_admin() throws Exception {
        AdminBroadcastNotificationServlet servlet = new AdminBroadcastNotificationServlet();
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("POST returns 403 when not admin")
    void post_forbidden_when_not_admin() throws Exception {
        AdminBroadcastNotificationServlet servlet = new AdminBroadcastNotificationServlet();
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);

        servlet.doPost(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("POST redirect invalid when missing title/message")
    void post_redirect_invalid_when_missing_params() throws Exception {
        AdminBroadcastNotificationServlet servlet = new AdminBroadcastNotificationServlet();
        Account admin = new Account();
        admin.setRole("admin");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("title")).thenReturn("");
        when(req.getParameter("message")).thenReturn("");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/admin/notify?broadcast=invalid");
    }

    @Test
    @DisplayName("GET admin forwards to notify JSP")
    void get_admin_forwards() throws Exception {
        AdminBroadcastNotificationServlet servlet = new AdminBroadcastNotificationServlet();
        Account admin = new Account(); admin.setRole("admin");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-notify-partners.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
    }
}
