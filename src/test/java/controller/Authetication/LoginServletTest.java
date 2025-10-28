package controller.Authetication;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAccountService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginServletTest {

    LoginServlet servlet;

    @Mock IAccountService accountService;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    Map<String, Object> sessionAttrs;

    @BeforeEach
    void setup() {
        servlet = new LoginServlet();
        TestUtils.forceSet(servlet, "accountService", accountService);
        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        sessionAttrs = new HashMap<>();
        when(session.getAttribute(anyString())).thenAnswer(i -> sessionAttrs.get(i.getArgument(0)));
        doAnswer(i -> { sessionAttrs.put(i.getArgument(0), i.getArgument(1)); return null;})
                .when(session).setAttribute(anyString(), any());
    }

    @Test
    void get_forwardLoginPage() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/login.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void post_missingParams_forwardBack() throws Exception {
        when(req.getParameter("username")).thenReturn(null);
        when(req.getParameter("password")).thenReturn(null);
        RequestDispatcher rd = TestUtils.stubForward(req, "/login.jsp");

        when(accountService.login(null, null)).thenThrow(new RuntimeException("bad"));
        servlet.doPost(req, resp);
        verify(rd).forward(req, resp);
        Assertions.assertThat(req).isNotNull();
    }

    @Test
    void post_wrongPassword_forwardBack() throws Exception {
        when(req.getParameter("username")).thenReturn("u1");
        when(req.getParameter("password")).thenReturn("pw");
        RequestDispatcher rd = TestUtils.stubForward(req, "/login.jsp");

        when(accountService.login("u1", "pw")).thenReturn(Optional.empty());
        servlet.doPost(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void post_ok_customer_redirectHome() throws Exception {
        when(req.getParameter("username")).thenReturn("u1");
        when(req.getParameter("password")).thenReturn("pw");
        when(req.getParameter("remember")).thenReturn("on");

        Account acc = new Account();
        acc.setAccountId(1);
        acc.setUsername("u1");
        acc.setRole("customer");
        acc.setStatus(true);
        acc.setEmailVerified(true);
        when(accountService.login("u1", "pw")).thenReturn(Optional.of(acc));

        servlet.doPost(req, resp);
        verify(session).setAttribute(eq("account"), any(Account.class));
        verify(resp).sendRedirect("/ctx/home.jsp");
    }

    @Test
    void post_ok_admin_redirectAdminDashboard() throws Exception {
        when(req.getParameter("username")).thenReturn("admin");
        when(req.getParameter("password")).thenReturn("pw");

        Account acc = new Account();
        acc.setAccountId(2);
        acc.setUsername("admin");
        acc.setRole("admin");
        acc.setStatus(true);
        when(accountService.login("admin", "pw")).thenReturn(Optional.of(acc));

        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/admin/dashboard");
    }

    @Test
    void post_exception_forwardLogin() throws Exception {
        when(req.getParameter("username")).thenReturn("x");
        when(req.getParameter("password")).thenReturn("y");
        RequestDispatcher rd = TestUtils.stubForward(req, "/login.jsp");
        when(accountService.login("x", "y")).thenThrow(new RuntimeException("db down"));
        servlet.doPost(req, resp);
        verify(rd).forward(req, resp);
    }
}

