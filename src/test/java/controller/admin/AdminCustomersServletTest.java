package controller.admin;

import controller.testsupport.TestUtils;
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
import service.IAdminCustomerService;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminCustomersServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock IAdminCustomerService service;

    AdminCustomersServlet servlet;

    @BeforeEach
    void setup() {
        servlet = new AdminCustomersServlet();
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
    @DisplayName("POST toggle -> redirects referer when admin")
    void post_toggle_redirects() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("action")).thenReturn("toggle");
        when(req.getParameter("customerId")).thenReturn("42");
        when(req.getHeader("referer")).thenReturn("/ctx/admin/customers");

        servlet.doPost(req, resp);

        verify(service).toggleCustomerStatus(42);
        verify(resp).sendRedirect("/ctx/admin/customers");
    }

    @Test
    @DisplayName("GET admin forwards to management JSP")
    void get_admin_forwards() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(service.searchCustomers(anyString(), anyString(), anyString(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());
        when(service.countCustomers(anyString(), anyString(), anyString())).thenReturn(0);
        when(service.getTotalWalletBalance()).thenReturn(BigDecimal.ZERO);

        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-customers-management.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("customers"), any());
    }
}

