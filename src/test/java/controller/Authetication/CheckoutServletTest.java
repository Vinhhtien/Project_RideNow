package controller.Authetication;

import controller.testsupport.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.CartItem;
import model.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.ICustomerService;
import service.IOrderService;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CheckoutServletTest {

    CheckoutServlet servlet;
    @Mock ICustomerService customerService;
    @Mock IOrderService orderService;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    Map<String,Object> bag;

    @BeforeEach
    void setup() {
        servlet = new CheckoutServlet();
        TestUtils.forceSet(servlet, "customerService", customerService);
        TestUtils.forceSet(servlet, "orderService", orderService);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        bag = new HashMap<>();
        when(session.getAttribute(anyString())).thenAnswer(i -> bag.get(i.getArgument(0)));
        doAnswer(i -> { bag.put(i.getArgument(0), i.getArgument(1)); return null; }).when(session).setAttribute(anyString(), any());
        doAnswer(i -> { bag.remove(i.getArgument(0)); return null; }).when(session).removeAttribute(anyString());
    }

    private static Account acc(int id) { Account a = new Account(); a.setAccountId(id); a.setRole("customer"); return a; }

    @Test
    @DisplayName("CHECKOUT-GET-001: not logged in -> redirect /login")
    void get_notLoggedIn_redirectLogin() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("CHECKOUT-GET-002: happy path -> create orders then redirect /paynow")
    void get_happy_createsOrders_redirectPaynow() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(1));
        Customer c = new Customer(); c.setCustomerId(9);
        when(customerService.getProfile(1)).thenReturn(c);
        List<CartItem> cart = new ArrayList<>();
        cart.add(new CartItem(5, "Bike A", new BigDecimal("100"), "Type", Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(1))));
        bag.put("cart", cart);
        when(orderService.bookOneBike(eq(9), eq(5), any(Date.class), any(Date.class))).thenReturn(101);
        servlet.doGet(req, resp);
        verify(session).removeAttribute("cart");
        verify(resp).sendRedirect(eq("/ctx/paynow?orders=101"));
    }

    @Test
    @DisplayName("CHECKOUT-GET-003: empty cart -> error then redirect /cart")
    void get_emptyCart_redirectCart() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(2));
        when(customerService.getProfile(2)).thenReturn(new Customer());
        bag.put("cart", new ArrayList<>());
        servlet.doGet(req, resp);
        Assertions.assertThat(bag).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CHECKOUT-GET-004: item fails -> error then redirect /cart")
    void get_itemBookingFails_redirectCart() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(3));
        Customer c = new Customer(); c.setCustomerId(10);
        when(customerService.getProfile(3)).thenReturn(c);
        List<CartItem> cart = new ArrayList<>();
        cart.add(new CartItem(7, "Bike X", new BigDecimal("100"), "Type", Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(1))));
        bag.put("cart", cart);
        when(orderService.bookOneBike(eq(10), eq(7), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("fail"));
        servlet.doGet(req, resp);
        Assertions.assertThat(bag).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CHECKOUT-GET-005: no profile -> redirect /customer/profile")
    void get_noProfile_redirectProfile() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(4));
        when(customerService.getProfile(4)).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/customer/profile");
    }
}

