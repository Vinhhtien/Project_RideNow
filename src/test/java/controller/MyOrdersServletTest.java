package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import dao.IOrderQueryDao;
import service.ICustomerService;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MyOrdersServletTest {

    MyOrdersServlet servlet;
    @Mock IOrderQueryDao qdao;
    @Mock ICustomerService customerService;

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    private static Account acc(int id) { Account a = new Account(); a.setAccountId(id); a.setRole("customer"); return a; }

    @BeforeEach
    void setup() {
        servlet = new MyOrdersServlet();
        TestUtils.forceSet(servlet, "qdao", qdao);
        TestUtils.forceSet(servlet, "customerService", customerService);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    void notLoggedIn_redirectLogin() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login.jsp");
    }

    @Test
    void noProfile_redirectNeedProfile() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(1));
        when(customerService.getProfile(1)).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/customer/profile.jsp?need=1");
    }

    @Test
    void happyPath_forwardOrders() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/customer/my-orders.jsp");
        when(session.getAttribute("account")).thenReturn(acc(2));
        Customer c = new Customer(); c.setCustomerId(10);
        when(customerService.getProfile(2)).thenReturn(c);
        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{101, "Yamaha", Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(1)), new BigDecimal("100000"), "pending", true});
        when(qdao.findOrdersOfCustomerWithPaymentStatus(10)).thenReturn(rows);
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("ordersVm"), any());
    }

    @Test
    void doPost_cancel_success_redirect() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(3));
        when(req.getParameter("action")).thenReturn("cancel");
        when(req.getParameter("orderId")).thenReturn("100");
        Customer c = new Customer(); c.setCustomerId(11);
        when(customerService.getProfile(3)).thenReturn(c);
        when(customerService.cancelOrder(11, 100)).thenReturn(true);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customerorders");
    }

    @Test
    void exception_inGet_redirectSelf() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(4));
        when(customerService.getProfile(4)).thenThrow(new RuntimeException("x"));
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/customerorders");
    }
}

