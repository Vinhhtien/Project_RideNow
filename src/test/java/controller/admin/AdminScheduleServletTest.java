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
class AdminScheduleServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET redirects login when no session")
    void get_redirects_no_session() throws Exception {
        AdminScheduleServlet servlet = new AdminScheduleServlet();
        when(req.getSession(false)).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("GET forbids when not admin")
    void get_forbidden_not_admin() throws Exception {
        AdminScheduleServlet servlet = new AdminScheduleServlet();
        when(req.getSession(false)).thenReturn(session);
        Account acc = new Account(); acc.setRole("customer");
        when(session.getAttribute("account")).thenReturn(acc);

        servlet.doGet(req, resp);

        verify(resp).sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    @DisplayName("GET admin forwards with items")
    void get_admin_forwards() throws Exception {
        AdminScheduleServlet servlet = new AdminScheduleServlet();
        when(req.getSession(false)).thenReturn(session);
        Account acc = new Account(); acc.setRole("admin"); acc.setAccountId(99);
        when(session.getAttribute("account")).thenReturn(acc);
        // cache adminId in session to skip DAO
        when(session.getAttribute("adminId")).thenReturn(1);

        service.IScheduleService mockSvc = mock(service.IScheduleService.class);
        controller.testsupport.TestUtils.forceSet(servlet, "scheduleService", mockSvc);
        when(mockSvc.getAdminSchedule(anyInt(), any(), any())).thenReturn(java.util.Collections.emptyList());

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-schedule.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("items"), any());
    }
}
