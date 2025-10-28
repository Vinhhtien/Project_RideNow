package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Customer;
import model.MotorbikeListItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.ICustomerService;
import service.IMotorbikeService;
import service.IOrderService;

import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServletTest {

    BookingServlet servlet;
    @Mock IMotorbikeService bikeService;
    @Mock ICustomerService customerService;
    @Mock IOrderService orderService;

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @BeforeEach
    void setup() {
        servlet = new BookingServlet();
        TestUtils.forceSet(servlet, "bikeService", bikeService);
        TestUtils.forceSet(servlet, "customerService", customerService);
        TestUtils.forceSet(servlet, "orderService", orderService);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    private static Account acc(int id) { Account a = new Account(); a.setAccountId(id); a.setRole("customer"); return a; }

    @Test
    void notLoggedIn_redirectLogin() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void notFoundBike_404() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(1));
        when(req.getParameter("bikeId")).thenReturn("10");
        when(req.getParameter("start")).thenReturn(LocalDate.now().toString());
        when(req.getParameter("end")).thenReturn(LocalDate.now().plusDays(1).toString());
        when(bikeService.getDetail(10)).thenReturn(null);
        servlet.doPost(req, resp);
        verify(resp).sendError(eq(404), anyString());
    }

    @Test
    void missingCustomerProfile_redirectProfile() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(2));
        when(req.getParameter("bikeId")).thenReturn("11");
        when(req.getParameter("start")).thenReturn(LocalDate.now().toString());
        when(req.getParameter("end")).thenReturn(LocalDate.now().plusDays(1).toString());
        when(bikeService.getDetail(11)).thenReturn(new MotorbikeListItem());
        when(customerService.getProfile(2)).thenReturn(null);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customer/profile?need=1");
    }

    @Test
    void invalidDates_errorAndBackToDetail() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(3));
        when(req.getParameter("bikeId")).thenReturn("12");
        when(req.getParameter("start")).thenReturn(LocalDate.now().plusDays(3).toString());
        when(req.getParameter("end")).thenReturn(LocalDate.now().plusDays(1).toString());
        when(bikeService.getDetail(12)).thenReturn(new MotorbikeListItem());
        when(customerService.getProfile(3)).thenReturn(new Customer());
        when(orderService.bookOneBike(eq(0), anyInt(), any(Date.class), any(Date.class)))
                .thenThrow(new IllegalArgumentException("bad dates"));

        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=12");
    }

    @Test
    void success_redirectOrders() throws Exception {
        Account a = acc(4);
        when(session.getAttribute("account")).thenReturn(a);
        when(req.getParameter("bikeId")).thenReturn("13");
        when(req.getParameter("start")).thenReturn(LocalDate.now().toString());
        when(req.getParameter("end")).thenReturn(LocalDate.now().plusDays(1).toString());
        when(bikeService.getDetail(13)).thenReturn(new MotorbikeListItem());
        Customer c = new Customer(); c.setCustomerId(9); when(customerService.getProfile(4)).thenReturn(c);
        when(orderService.bookOneBike(eq(9), eq(13), any(Date.class), any(Date.class))).thenReturn(777);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customerorders?justCreated=777");
    }
}
