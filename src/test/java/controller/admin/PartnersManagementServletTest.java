//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import model.Account;
//import model.Partner;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IPartnerAdminService;
//
//import java.util.List;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class PartnersManagementServletTest {
//
//    @BeforeAll
//    static void initEnv() {
//        Locale.setDefault(Locale.US);
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    private HttpSession adminSession(java.util.Map<String, Object> sm) {
//        Account a = new Account(); a.setRole("admin"); a.setAccountId(1); sm.put("account", a);
//        return TestUtils.mockSession(sm);
//    }
//
//    @Test
//    void ADMIN-PARTNERS-001_get_list_happy_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//        when(svc.getAllPartners()).thenReturn(List.of(new Partner()));
//
//        PartnersManagementServlet servlet = new PartnersManagementServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doGet(req, resp);
//        verify(req).setAttribute(eq("partners"), any());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-PARTNERS-002_get_list_empty_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//        when(svc.getAllPartners()).thenReturn(List.of());
//
//        PartnersManagementServlet servlet = new PartnersManagementServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-PARTNERS-003_post_delete_success_redirects() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//        when(req.getParameter("partnerId")).thenReturn("7");
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//        when(svc.deletePartner(7)).thenReturn(true);
//
//        PartnersManagementServlet servlet = new PartnersManagementServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doPost(req, resp);
//
//        verify(svc).deletePartner(7);
//        verify(resp).sendRedirect("/ctx/admin/partners");
//        org.assertj.core.api.Assertions.assertThat(sm.get("success")).isNotNull();
//    }
//
//    @Test
//    void ADMIN-PARTNERS-004_post_invalid_id_redirects_with_error() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//        when(req.getParameter("partnerId")).thenReturn("abc");
//
//        PartnersManagementServlet servlet = new PartnersManagementServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", mock(IPartnerAdminService.class));
//
//        servlet.doPost(req, resp);
//        verify(resp).sendRedirect("/ctx/admin/partners");
//        org.assertj.core.api.Assertions.assertThat(sm.get("error")).isNotNull();
//    }
//
//    @Test
//    void ADMIN-PARTNERS-005_post_delete_runtime_error() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//        when(req.getParameter("partnerId")).thenReturn("8");
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//        when(svc.deletePartner(8)).thenThrow(new RuntimeException("db"));
//
//        PartnersManagementServlet servlet = new PartnersManagementServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doPost(req, resp);
//        verify(resp).sendRedirect("/ctx/admin/partners");
//        org.assertj.core.api.Assertions.assertThat(sm.get("error")).isNotNull();
//    }
//}
//
