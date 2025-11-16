package controller.admin;

import controller.testsupport.TestUtils;
import dao.IAdminOrderDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOrderDetailServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock IAdminOrderDAO orderDAO;

    AdminOrderDetailServlet servlet;

    @BeforeEach
    void setup() {
        servlet = new AdminOrderDetailServlet();
        TestUtils.forceSet(servlet, "orderDAO", orderDAO);
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
    @DisplayName("GET missing id -> notFound forward")
    void get_missing_id_forwards_notfound() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn(null);
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-order-detail.jsp");

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("notFound"), eq(true));
        verify(rd).forward(req, resp);
    }

    @Test
    @DisplayName("GET found order -> forward with attributes")
    void get_found_order_forwards() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn("7");

        java.util.Optional<model.OrderSummary> opt = java.util.Optional.of(new model.OrderSummary());
        when(orderDAO.findOrderHeader(7)).thenReturn(opt);
        when(orderDAO.findOrderItems(7)).thenReturn(java.util.Collections.emptyList());
        when(orderDAO.findPayments(7)).thenReturn(java.util.Collections.emptyList());

        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-order-detail.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("order"), any());
        verify(req).setAttribute(eq("items"), any());
        verify(req).setAttribute(eq("payments"), any());
    }
}
