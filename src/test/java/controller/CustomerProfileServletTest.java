package controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
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
import service.ICustomerService;
import controller.testsupport.TestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerProfileServletTest {

    CustomerProfileServlet servlet;
    @Mock ICustomerService service;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    private static Account acc(String role) { Account a = new Account(); a.setAccountId(1); a.setRole(role); return a; }

    @BeforeEach
    void setup() {
        servlet = new CustomerProfileServlet();
        TestUtils.forceSet(servlet, "service", service);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    void get_notLoggedIn_redirectLogin() throws Exception {
        when(session.getAttribute("account")).thenReturn(null);
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void get_wrongRole_redirectHome() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc("admin"));
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/home.jsp");
    }

    @Test
    void get_happy_forwardProfile() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        when(service.getProfile(1)).thenReturn(new Customer());
        RequestDispatcher rd = TestUtils.stubForward(req, "/customer/profile.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("profile"), any());
    }

    @Test
    void post_changePassword_invalidParams_redirect() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        when(req.getParameter("action")).thenReturn("changePassword");
        when(req.getParameter("current_pw")).thenReturn("");
        when(req.getParameter("new_pw")).thenReturn("");
        when(req.getParameter("confirm_pw")).thenReturn("");
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customer/profile#security");
    }

    @Test
    void post_changePassword_ok() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        when(req.getParameter("action")).thenReturn("changePassword");
        when(req.getParameter("current_pw")).thenReturn("old");
        when(req.getParameter("new_pw")).thenReturn("newpass");
        when(req.getParameter("confirm_pw")).thenReturn("newpass");
        when(service.changePassword(1, "old", "newpass")).thenReturn(true);
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customer/profile#security");
    }

    @Test
    void post_updateProfile_ok() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc("customer"));
        servlet.doPost(req, resp);
        verify(service).saveProfile(any(Customer.class));
        verify(resp).sendRedirect("/ctx/customer/profile");
    }
}

