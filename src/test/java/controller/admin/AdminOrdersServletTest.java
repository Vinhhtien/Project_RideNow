package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.OrderSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminOrderService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOrdersServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock IAdminOrderService service;

    AdminOrdersServlet servlet;

    @BeforeEach
    void setup() {
        servlet = new AdminOrdersServlet();
        TestUtils.forceSet(servlet, "service", service);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    @DisplayName("GET redirects when not admin")
    void get_redirects_not_admin() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("GET admin forwards to list JSP")
    void get_admin_forwards() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(service.findOrders(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Collections.<OrderSummary>emptyList());
        when(service.countOrders(any(), any(), any(), any())).thenReturn(0);
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-order-list.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("orders"), any());
    }

    @Test
    @DisplayName("GET parses paging and filters safely")
    void get_parsing_safe() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);

        when(req.getParameter("page")).thenReturn("not-a-number");
        when(req.getParameter("pageSize")).thenReturn("-5");
        when(service.findOrders(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());
        when(service.countOrders(any(), any(), any(), any())).thenReturn(0);

        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-order-list.jsp");
        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("page"), anyInt());
        verify(req).setAttribute(eq("pageSize"), anyInt());
    }
}
