//package controller;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import model.Account;
//import model.Notification;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.INotificationService;
//
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class DashboardServletTest {
//
//    DashboardServlet servlet;
//    @Mock INotificationService notificationService;
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//    @Mock HttpSession session;
//
//    private static Account acc(int id, String role) { Account a = new Account(); a.setAccountId(id); a.setRole(role); return a; }
//
//    @BeforeEach
//    void setup() {
//        servlet = new DashboardServlet();
//        TestUtils.forceSet(servlet, "notificationService", notificationService);
//        when(req.getSession(anyBoolean())).thenReturn(session);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//    }
//
//    @Test
//    void notLoggedIn_redirectLogin() throws Exception {
//        when(session.getAttribute("account")).thenReturn(null);
//        servlet.doGet(req, resp);
//        verify(resp).sendRedirect("/ctx/login");
//    }
//
//    @Test
//    void partner_forwardDashboard_withData() throws Exception {
//        when(session.getAttribute("account")).thenReturn(acc(1, "partner"));
//        when(notificationService.getUnreadToasts(1, 5)).thenReturn(Collections.emptyList());
//        when(notificationService.findByAccount(eq(1), anyInt(), anyInt(), isNull(), isNull()))
//                .thenReturn(Collections.<Notification>emptyList());
//        when(notificationService.countUnread(1)).thenReturn(0);
//
//        RequestDispatcher rd = TestUtils.stubForward(req, "partners/dashboard.jsp");
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("unreadCount"), any());
//    }
//
//    @Test
//    void admin_forwardAdminDashboard() throws Exception {
//        when(session.getAttribute("account")).thenReturn(acc(2, "admin"));
//        RequestDispatcher rd = TestUtils.stubForward(req, "admin/dashboard.jsp");
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void post_notPartner_forbidden() throws Exception {
//        when(session.getAttribute("account")).thenReturn(acc(3, "customer"));
//        servlet.doPost(req, resp);
//        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
//    }
//
//    @Test
//    void post_markRead_ok() throws Exception {
//        when(session.getAttribute("account")).thenReturn(acc(4, "partner"));
//        when(req.getParameter("action")).thenReturn("read");
//        when(req.getParameter("id")).thenReturn("10");
//        servlet.doPost(req, resp);
//        verify(notificationService).readOne(10, 4);
//        verify(resp).setStatus(HttpServletResponse.SC_NO_CONTENT);
//    }
//}
//
