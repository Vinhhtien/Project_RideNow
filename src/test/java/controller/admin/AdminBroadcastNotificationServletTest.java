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
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import utils.DBConnection;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminBroadcastNotificationServletTest {
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
//
//    private void putAdmin(HttpSession session, java.util.Map<String, Object> sm) {
//        Account a = new Account(); a.setRole("admin"); sm.put("account", a);
//    }
//
//    @Test
//    void ADMIN-BROADCAST-001_get_form_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm); putAdmin(session, sm);
//        when(req.getSession(false)).thenReturn(session);
//
//        new AdminBroadcastNotificationServlet().doGet(req, resp);
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-BROADCAST-002_post_all_partners_happy_redirect_ok() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm); putAdmin(session, sm);
//        when(req.getSession(false)).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("title")).thenReturn("Hello");
//        when(req.getParameter("message")).thenReturn("World");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeUpdate()).thenReturn(3);
//
//            new AdminBroadcastNotificationServlet().doPost(req, resp);
//
//            verify(ps).setNString(1, "Hello");
//            verify(ps).setNString(2, "World");
//            verify(resp).sendRedirect("/ctx/admin/notify?broadcast=ok&sent=3");
//        }
//    }
//
//    @Test
//    void ADMIN-BROADCAST-003_post_targeted_partner_happy_redirect_ok() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm); putAdmin(session, sm);
//        when(req.getSession(false)).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("title")).thenReturn("Hi");
//        when(req.getParameter("message")).thenReturn("Msg");
//        when(req.getParameter("username")).thenReturn("partner1");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeUpdate()).thenReturn(1);
//
//            new AdminBroadcastNotificationServlet().doPost(req, resp);
//
//            verify(ps).setNString(1, "Hi");
//            verify(ps).setNString(2, "Msg");
//            verify(ps).setNString(3, "partner1");
//            verify(ps).setNString(4, "partner1");
//            verify(resp).sendRedirect(startsWith("/ctx/admin/notify?broadcast=ok"));
//        }
//    }
//
//    @Test
//    void ADMIN-BROADCAST-004_post_missing_fields_redirect_invalid() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm); putAdmin(session, sm);
//        when(req.getSession(false)).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("title")).thenReturn("");
//        when(req.getParameter("message")).thenReturn(" ");
//
//        new AdminBroadcastNotificationServlet().doPost(req, resp);
//        verify(resp).sendRedirect("/ctx/admin/notify?broadcast=invalid");
//    }
//
//    @Test
//    void ADMIN-BROADCAST-005_jdbc_throws_redirect_fail() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm); putAdmin(session, sm);
//        when(req.getSession(false)).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("title")).thenReturn("T");
//        when(req.getParameter("message")).thenReturn("M");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenThrow(new RuntimeException("db down"));
//            new AdminBroadcastNotificationServlet().doPost(req, resp);
//            verify(resp).sendRedirect("/ctx/admin/notify?broadcast=fail");
//        }
//    }
//}
//
