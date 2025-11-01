//package controller.admin;
//
//import controller.testsupport.Fixtures;
//import controller.testsupport.TestUtils;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import model.Account;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IOrderManageService;
//import utils.DBConnection;
//
//import java.sql.*;
//import java.util.Locale;
//import java.util.Map;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminReturnServletTest {
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
//    @Mock PreparedStatement psAdmins;
//    @Mock PreparedStatement psFindOpen;
//    @Mock PreparedStatement psInsert;
//    @Mock PreparedStatement psUpdate;
//    @Mock ResultSet rsAdmins;
//    @Mock ResultSet rsFindOpen;
//
//    private void putAdminSession(Map<String, Object> sessionMap) {
//        Account admin = Fixtures.account(10, "admin");
//        sessionMap.put("account", admin);
//    }
//
//    @Test
//    void ADMIN-RETURN-001_post_normal_return_success_inserts_inspection_and_redirects() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("77");
//        when(req.getParameter("actionType")).thenReturn("normal_return");
//        when(req.getParameter("notes")).thenReturn("ok");
//
//        IOrderManageService service = mock(IOrderManageService.class);
//        when(service.confirmOrderReturn(77, 10)).thenReturn(true);
//
//        AdminReturnServlet servlet = new AdminReturnServlet();
//        TestUtils.forceSet(servlet, "orderService", service);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//
//            when(con.prepareStatement(contains("Admins"))).thenReturn(psAdmins);
//            when(psAdmins.executeQuery()).thenReturn(rsAdmins);
//            when(rsAdmins.next()).thenReturn(true);
//            when(rsAdmins.getInt(1)).thenReturn(10); // adminId
//
//            when(con.prepareStatement(startsWith("SELECT TOP 1"))).thenReturn(psFindOpen);
//            when(psFindOpen.executeQuery()).thenReturn(rsFindOpen);
//            when(rsFindOpen.next()).thenReturn(false); // no existing inspection
//
//            when(con.prepareStatement(startsWith("INSERT INTO RefundInspections"))).thenReturn(psInsert);
//            when(psInsert.executeUpdate()).thenReturn(1);
//
//            servlet.doPost(req, resp);
//
//            verify(psAdmins).setInt(1, 10); // account id
//            verify(psFindOpen).setInt(1, 77); // order id
//            verify(psInsert).setInt(1, 77);
//            verify(psInsert).setInt(2, 10);
//            verify(psInsert).setLong(3, 0L);
//            verify(psInsert).setString(eq(4), startsWith("[NORMAL_RETURN]"));
//            verify(resp).sendRedirect("/ctx/adminreturn");
//            org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-002_post_overdue_return_fee_branch_updates_inspection() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("88");
//        when(req.getParameter("actionType")).thenReturn("overdue_return");
//        when(req.getParameter("lateFee")).thenReturn("5000");
//        when(req.getParameter("notes")).thenReturn("late");
//
//        IOrderManageService service = mock(IOrderManageService.class);
//        when(service.confirmOrderReturn(88, 10)).thenReturn(true);
//
//        AdminReturnServlet servlet = new AdminReturnServlet();
//        TestUtils.forceSet(servlet, "orderService", service);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//
//            when(con.prepareStatement(contains("Admins"))).thenReturn(psAdmins);
//            when(psAdmins.executeQuery()).thenReturn(rsAdmins);
//            when(rsAdmins.next()).thenReturn(true);
//            when(rsAdmins.getInt(1)).thenReturn(10);
//
//            when(con.prepareStatement(startsWith("SELECT TOP 1"))).thenReturn(psFindOpen);
//            when(psFindOpen.executeQuery()).thenReturn(rsFindOpen);
//            when(rsFindOpen.next()).thenReturn(false);
//
//            when(con.prepareStatement(startsWith("INSERT INTO RefundInspections"))).thenReturn(psInsert);
//            when(psInsert.executeUpdate()).thenReturn(1);
//
//            servlet.doPost(req, resp);
//
//            verify(psInsert).setLong(3, 5000L);
//            verify(psInsert).setString(eq(4), startsWith("[OVERDUE_RETURN]"));
//            verify(resp).sendRedirect("/ctx/adminreturn");
//            org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-003_post_missing_orderId_error_branch() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        AdminReturnServlet servlet = new AdminReturnServlet();
//
//        servlet.doPost(req, resp);
//        verify(resp).sendRedirect("/ctx/adminreturn");
//        org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//    }
//
//    @Test
//    void ADMIN-RETURN-004_post_invalid_orderId_number_format() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("abc");
//
//        AdminReturnServlet servlet = new AdminReturnServlet();
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/adminreturn");
//        org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//    }
//
//    @Test
//    void ADMIN-RETURN-005_admin_id_not_found_branch() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("90");
//        when(req.getParameter("actionType")).thenReturn("normal_return");
//
//        IOrderManageService service = mock(IOrderManageService.class);
//        AdminReturnServlet servlet = new AdminReturnServlet();
//        TestUtils.forceSet(servlet, "orderService", service);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(contains("Admins"))).thenReturn(psAdmins);
//            when(psAdmins.executeQuery()).thenReturn(rsAdmins);
//            when(rsAdmins.next()).thenReturn(false); // admin id not found
//
//            servlet.doPost(req, resp);
//
//            verify(resp).sendRedirect("/ctx/adminreturn");
//            org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//            verify(service, never()).confirmOrderReturn(anyInt(), anyInt());
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-006_confirm_order_return_false_branch() throws Exception {
//        Map<String, Object> sessionMap = TestUtils.createSessionMap();
//        putAdminSession(sessionMap);
//        HttpSession session = TestUtils.mockSession(sessionMap);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("91");
//        when(req.getParameter("actionType")).thenReturn("normal_return");
//
//        IOrderManageService service = mock(IOrderManageService.class);
//        when(service.confirmOrderReturn(91, 10)).thenReturn(false);
//
//        AdminReturnServlet servlet = new AdminReturnServlet();
//        TestUtils.forceSet(servlet, "orderService", service);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(contains("Admins"))).thenReturn(psAdmins);
//            when(psAdmins.executeQuery()).thenReturn(rsAdmins);
//            when(rsAdmins.next()).thenReturn(true);
//            when(rsAdmins.getInt(1)).thenReturn(10);
//
//            servlet.doPost(req, resp);
//
//            verify(service).confirmOrderReturn(91, 10);
//            verify(resp).sendRedirect("/ctx/adminreturn");
//            org.assertj.core.api.Assertions.assertThat(sessionMap.get("flash")).isNotNull();
//        }
//    }
//}
//
