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
//import org.junit.jupiter.api.Assumptions;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IPaymentVerifyService;
//
//import java.util.Locale;
//import java.util.TimeZone;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminPaymentVerifyServletTest {
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
//    private Object newServletOrSkip() throws Exception {
//        try {
//            Class<?> clazz = Class.forName("controller.admin.AdminPaymentVerifyServlet");
//            return clazz.getDeclaredConstructor().newInstance();
//        } catch (ClassNotFoundException e) {
//            Assumptions.assumeTrue(false, "AdminPaymentVerifyServlet not present in this build");
//            return null; // unreachable
//        }
//    }
//
//    @Test
//    void ADMIN-PAYMENTVERIFY-001_get_list_forwards() throws Exception {
//        Object servlet = newServletOrSkip();
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        Account admin = new Account(); admin.setRole("admin"); admin.setAccountId(1);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        sessionMap.put("account", admin);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//
//        IPaymentVerifyService svc = mock(IPaymentVerifyService.class);
//        TestUtils.forceSet(servlet, "paymentService", svc);
//
//        servlet.getClass().getMethod("doGet", HttpServletRequest.class, HttpServletResponse.class)
//                .invoke(servlet, req, resp);
//
//        verify(req).setAttribute(eq("payments"), any());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-PAYMENTVERIFY-002_post_verify_happy_redirects_and_sends_email() throws Exception {
//        Object servlet = newServletOrSkip();
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        Account admin = new Account(); admin.setRole("admin"); admin.setAccountId(1);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        sessionMap.put("account", admin);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        when(req.getParameter("paymentId")).thenReturn("12");
//        when(req.getScheme()).thenReturn("http");
//        when(req.getServerName()).thenReturn("localhost");
//        when(req.getServerPort()).thenReturn(8080);
//
//        IPaymentVerifyService svc = mock(IPaymentVerifyService.class);
//        when(svc.verifyPayment(12, 1)).thenReturn(true);
//        doNothing().when(svc).sendPaymentConfirmationEmail(eq(12), anyString());
//        TestUtils.forceSet(servlet, "paymentService", svc);
//
//        servlet.getClass().getMethod("doPost", HttpServletRequest.class, HttpServletResponse.class)
//                .invoke(servlet, req, resp);
//
//        verify(svc).verifyPayment(12, 1);
//        verify(svc).sendPaymentConfirmationEmail(eq(12), anyString());
//        verify(resp).sendRedirect("/ctx/adminpaymentverify");
//    }
//
//    @Test
//    void ADMIN-PAYMENTVERIFY-003_post_invalid_id_sets_flash_redirects() throws Exception {
//        Object servlet = newServletOrSkip();
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        Account admin = new Account(); admin.setRole("admin"); admin.setAccountId(1);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        sessionMap.put("account", admin);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        when(req.getParameter("paymentId")).thenReturn("abc");
//
//        servlet.getClass().getMethod("doPost", HttpServletRequest.class, HttpServletResponse.class)
//                .invoke(servlet, req, resp);
//
//        org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//        verify(resp).sendRedirect("/ctx/adminpaymentverify");
//    }
//}
//
