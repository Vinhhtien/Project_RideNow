package controller.admin;

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

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartnersManagementServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET redirects login when not admin")
    void get_redirects_not_admin() throws Exception {
        PartnersManagementServlet servlet = new PartnersManagementServlet();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("POST redirects login when not admin")
    void post_redirects_not_admin() throws Exception {
        PartnersManagementServlet servlet = new PartnersManagementServlet();
        when(req.getSession()).thenReturn(session);
        Account acc = new Account(); acc.setRole("customer");
        when(session.getAttribute("account")).thenReturn(acc);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("GET admin forwards with partners list")
    void get_admin_forwards() throws Exception {
        PartnersManagementServlet servlet = new PartnersManagementServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "partnerAdminService", mock(service.IPartnerAdminService.class));
        when(req.getSession()).thenReturn(session);
        Account acc = new Account(); acc.setRole("admin");
        when(session.getAttribute("account")).thenReturn(acc);

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-partners-management.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("partners"), any());
    }

    @Test
    @DisplayName("POST delete partner sets success and redirects")
    void post_delete_partner_redirects() throws Exception {
        PartnersManagementServlet servlet = new PartnersManagementServlet();
        service.IPartnerAdminService svc = mock(service.IPartnerAdminService.class);
        controller.testsupport.TestUtils.forceSet(servlet, "partnerAdminService", svc);
        when(req.getSession()).thenReturn(session);
        Account acc = new Account(); acc.setRole("admin");
        when(session.getAttribute("account")).thenReturn(acc);
        when(req.getParameter("partnerId")).thenReturn("2");
        when(svc.deletePartner(2)).thenReturn(true);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("success"), anyString());
        verify(resp).sendRedirect("/ctx/admin/partners");
    }
}
