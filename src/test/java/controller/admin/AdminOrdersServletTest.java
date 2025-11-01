package controller.admin;

import controller.testsupport.Fixtures;
import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.OrderSummary;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminOrderService;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOrdersServletTest {

    @BeforeAll
    static void setUpEnv() {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;

    @Test
    void get_not_logged_in_redirects_login() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        new AdminOrdersServlet().doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void get_admin_happy_forwards_with_attributes() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        Account admin = Fixtures.account(1, "admin");
        sessionMap.put("account", admin);
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderService svc = mock(IAdminOrderService.class);
        List<OrderSummary> orders = new ArrayList<>();
        OrderSummary s = new OrderSummary(); s.setOrderId(10); orders.add(s);
        when(svc.findOrders(isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt())).thenReturn(orders);
        when(svc.countOrders(isNull(), isNull(), isNull(), isNull())).thenReturn(1);

        AdminOrdersServlet servlet = new AdminOrdersServlet();
        TestUtils.forceSet(servlet, "service", svc);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("orders"), eq(orders));
        verify(req).setAttribute("total", 1);
        verify(req).setAttribute("page", 1);
        verify(req).setAttribute("pageSize", 10);
        verify(req).setAttribute("totalPages", 1);
        verify(req).setAttribute("status", "");
        verify(req).setAttribute("q", "");
        verify(req).setAttribute("from", "");
        verify(req).setAttribute("to", "");
        verify(req).getRequestDispatcher(anyString());
        verify(rd).forward(req, resp);
    }

    @Test
    void get_admin_empty_list_branch() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(1, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderService svc = mock(IAdminOrderService.class);
        when(svc.findOrders(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(svc.countOrders(any(), any(), any(), any())).thenReturn(0);

        AdminOrdersServlet servlet = new AdminOrdersServlet();
        TestUtils.forceSet(servlet, "service", svc);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("orders"), eq(Collections.emptyList()));
        verify(req).setAttribute("total", 0);
        verify(req).setAttribute("totalPages", 0);
        verify(rd).forward(req, resp);
    }

    @Test
    void get_admin_blank_status_passes_null_to_service() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(2, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        when(req.getParameter("status")).thenReturn("");
        when(req.getParameter("q")).thenReturn("kw");

        IAdminOrderService svc = mock(IAdminOrderService.class);
        when(svc.findOrders(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(svc.countOrders(any(), any(), any(), any())).thenReturn(0);

        AdminOrdersServlet servlet = new AdminOrdersServlet();
        TestUtils.forceSet(servlet, "service", svc);

        servlet.doGet(req, resp);

        ArgumentCaptor<String> statusCap1 = ArgumentCaptor.forClass(String.class);
        verify(svc).findOrders(statusCap1.capture(), eq("kw"), isNull(), isNull(), anyInt(), anyInt());
        Assertions.assertThat(statusCap1.getValue()).isNull();

        verify(rd).forward(req, resp);
    }

    @Test
    void get_admin_service_throws_runtime_propagates() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(3, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        IAdminOrderService svc = mock(IAdminOrderService.class);
        when(svc.findOrders(any(), any(), any(), any(), anyInt(), anyInt())).thenThrow(new RuntimeException("oops"));

        AdminOrdersServlet servlet = new AdminOrdersServlet();
        TestUtils.forceSet(servlet, "service", svc);

        Assertions.assertThatThrownBy(() -> servlet.doGet(req, resp))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("oops");
    }
}

