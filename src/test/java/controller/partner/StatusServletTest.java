package controller.partner;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.INotificationService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StatusServletTest {

    StatusServlet servlet;
    @Mock INotificationService notificationService;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @BeforeEach
    void setup() {
        servlet = new StatusServlet();
        TestUtils.forceSet(servlet, "notificationService", notificationService);
        when(req.getSession(false)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    void notLoggedIn_redirectLogin() throws Exception {
        when(req.getSession(false)).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void wrongRole_forbidden() throws Exception {
        Account acc = new Account();
        acc.setRole("customer");
        when(session.getAttribute("account")).thenReturn(acc);
        servlet.doGet(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void partner_ok_forwardsAndSetsAttrs() throws Exception {
        Account acc = new Account();
        acc.setRole("partner");
        acc.setAccountId(7);
        when(session.getAttribute("account")).thenReturn(acc);
        when(req.getParameter("nid")).thenReturn("12");
        Notification n = new Notification();
        n.setTitle("T");
        n.setMessage("M");
        when(notificationService.findByIdForAccount(12, 7)).thenReturn(n);

        RequestDispatcher rd = TestUtils.mockDispatcher(req);
        when(req.getRequestDispatcher("/partners/maintenance.jsp")).thenReturn(rd);

        servlet.doGet(req, resp);
        verify(req).setAttribute(eq("title"), any());
        verify(req).setAttribute(eq("message"), any());
        verify(rd).forward(req, resp);
    }
}

