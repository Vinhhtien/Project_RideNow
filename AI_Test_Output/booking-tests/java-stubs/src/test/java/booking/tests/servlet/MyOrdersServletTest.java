package booking.tests.servlet;

import booking.servlets.MyOrdersServletUnderTest;
import booking.stubs.dao.IOrderQueryDao;
import booking.stubs.model.Account;
import booking.stubs.model.Customer;
import booking.stubs.service.ICustomerService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MyOrdersServletTest {
    @Mock private IOrderQueryDao qdao;
    @Mock private ICustomerService customerService;
    @org.mockito.InjectMocks private MyOrdersServletUnderTest servlet;

    @Test
    @DisplayName("TC-CTL-ORDERS-003: testDoGet_LoggedInCustomer_ShouldListOrders")
    void testDoGet_LoggedInCustomer_ShouldListOrders() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        RequestDispatcher rd = org.mockito.Mockito.mock(RequestDispatcher.class);
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(new Account(2));
        when(customerService.getProfile(2)).thenReturn(new Customer(20));
        when(req.getRequestDispatcher("/customer/my-orders.jsp")).thenReturn(rd);

        List<Object[]> rows = new ArrayList<>();
        rows.add(new Object[]{1, "Bike A", Date.valueOf("2025-10-25"), Date.valueOf("2025-10-26"), new BigDecimal("150000.00"), "pending", true, "CARD", false});
        when(qdao.findOrdersOfCustomerWithPaymentStatus(20)).thenReturn(rows);

        servlet.doGetPublic(req, resp);

        verify(req, times(1)).setAttribute(org.mockito.ArgumentMatchers.eq("ordersVm"), org.mockito.ArgumentMatchers.any());
        verify(rd, times(1)).forward(req, resp);
    }

    @Test
    @DisplayName("TC-CTL-ORDERS-001: testDoGet_NotLoggedIn_ShouldRedirectToLogin")
    void testDoGet_NotLoggedIn_ShouldRedirectToLogin() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGetPublic(req, resp);

        verify(resp, times(1)).sendRedirect("/ctx/login.jsp");
    }
}
