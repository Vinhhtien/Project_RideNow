package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IPartnerAdminService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminPartnerCreateServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock IPartnerAdminService partnerService;

    AdminPartnerCreateServlet servlet;

    @BeforeEach
    void setup() {
        servlet = new AdminPartnerCreateServlet();
        TestUtils.forceSet(servlet, "partnerAdminService", partnerService);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    @DisplayName("GET redirects to login when not logged in")
    void get_redirects_login_when_null_account() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("POST redirects to login when not admin")
    void post_redirects_login_when_not_admin() throws Exception {
        Account acc = new Account(); acc.setRole("customer");
        when(session.getAttribute("account")).thenReturn(acc);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("GET admin forwards to create JSP")
    void get_admin_forwards() throws Exception {
        Account acc = new Account(); acc.setRole("admin");
        when(session.getAttribute("account")).thenReturn(acc);
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-partner-create.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
    }

    @Test
    @DisplayName("POST admin calls service and forwards with success")
    void post_admin_create_forwards() throws Exception {
        Account acc = new Account(); acc.setRole("admin"); acc.setAccountId(1);
        when(session.getAttribute("account")).thenReturn(acc);
        when(req.getParameter("username")).thenReturn("u");
        when(req.getParameter("companyName")).thenReturn("c");
        when(req.getParameter("address")).thenReturn("a");
        when(req.getParameter("phone")).thenReturn("p");
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-partner-create.jsp");

        servlet.doPost(req, resp);

        verify(rd).forward(req, resp);
        verify(req, atLeastOnce()).setAttribute(eq("success"), anyString());
    }
}
