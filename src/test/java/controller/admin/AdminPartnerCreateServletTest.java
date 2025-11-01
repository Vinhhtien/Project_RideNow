//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import model.Account;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IPartnerAdminService;
//
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminPartnerCreateServletTest {
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
//        Account a = new Account(); a.setRole("admin"); a.setAccountId(2); sm.put("account", a);
//        return TestUtils.mockSession(sm);
//    }
//
//    @Test
//    void ADMIN-PARTNERCREATE-001_get_form_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//
//        new AdminPartnerCreateServlet().doGet(req, resp);
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-PARTNERCREATE-002_post_create_happy_forwards_success() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//        when(req.getParameter("username")).thenReturn("newpartner");
//        when(req.getParameter("companyName")).thenReturn("ACME");
//        when(req.getParameter("address")).thenReturn("addr");
//        when(req.getParameter("phone")).thenReturn("123");
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//
//        AdminPartnerCreateServlet servlet = new AdminPartnerCreateServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doPost(req, resp);
//        verify(svc).createPartner(anyString(), anyString(), anyString(), anyString(), eq(2));
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("success"), any());
//    }
//
//    @Test
//    void ADMIN-PARTNERCREATE-003_post_duplicate_validation_error_forward() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(adminSession(sm));
//        when(req.getParameter("username")).thenReturn("dup");
//        when(req.getParameter("companyName")).thenReturn("ACME");
//
//        IPartnerAdminService svc = mock(IPartnerAdminService.class);
//        doThrow(new RuntimeException("duplicate")).when(svc)
//                .createPartner(anyString(), anyString(), anyString(), anyString(), anyInt());
//
//        AdminPartnerCreateServlet servlet = new AdminPartnerCreateServlet();
//        TestUtils.forceSet(servlet, "partnerAdminService", svc);
//
//        servlet.doPost(req, resp);
//        verify(req).setAttribute(eq("error"), any());
//        verify(rd).forward(req, resp);
//    }
//}
//
