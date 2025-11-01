//package controller.admin;
//
//import controller.testsupport.Fixtures;
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
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IOrderManageService;
//import utils.DBConnection;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.List;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminPickupServletTest {
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
//    @Mock Connection con;
//    @Mock PreparedStatement ps;
//    @Mock ResultSet rs;
//
//    private HttpSession admin(java.util.Map<String, Object> sm) {
//        Account a = new Account(); a.setRole("admin"); a.setAccountId(1); sm.put("account", a);
//        return TestUtils.mockSession(sm);
//    }
//
//    @Test
//    void ADMIN-PICKUP-001_get_list_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(admin(sm));
//
//        IOrderManageService svc = mock(IOrderManageService.class);
//        when(svc.getOrdersForPickup()).thenReturn(List.of(new Object[]{}));
//
//        AdminPickupServlet servlet = new AdminPickupServlet();
//        TestUtils.forceSet(servlet, "orderService", svc);
//
//        servlet.doGet(req, resp);
//        verify(req).setAttribute(eq("orders"), any());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-PICKUP-002_post_confirm_success_redirects_and_notifies() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(admin(sm));
//        when(req.getParameter("orderId")).thenReturn("42");
//
//        IOrderManageService svc = mock(IOrderManageService.class);
//        when(svc.confirmOrderPickup(42, 1)).thenReturn(true);
//
//        AdminPickupServlet servlet = new AdminPickupServlet();
//        TestUtils.forceSet(servlet, "orderService", svc);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getInt(1)).thenReturn(99); // partner account id
//
//            servlet.doPost(req, resp);
//
//            verify(svc).confirmOrderPickup(42, 1);
//            verify(resp).sendRedirect("/ctx/adminpickup");
//        }
//    }
//
//    @Test
//    void ADMIN-PICKUP-003_post_invalid_id_flash_redirect() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        when(req.getSession()).thenReturn(admin(sm));
//        when(req.getParameter("orderId")).thenReturn("abc");
//
//        new AdminPickupServlet().doPost(req, resp);
//        verify(resp).sendRedirect("/ctx/adminpickup");
//        org.assertj.core.api.Assertions.assertThat(sm.get("flash")).isNotNull();
//    }
//}
//
