package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.AdminCustomerDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminCustomerService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminCustomerDetailServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock IAdminCustomerService service;

    AdminCustomerDetailServlet servlet;

    @BeforeEach
    void setup() {
        servlet = new AdminCustomerDetailServlet();
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
    @DisplayName("GET invalid id -> redirect with flash")
    void get_invalid_id_redirects() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn("abc");

        servlet.doGet(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/admin/customers");
    }

    @Test
    @DisplayName("GET id not found -> redirect with flash")
    void get_id_not_found_redirects() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn("10");
        when(service.getCustomerDetail(10)).thenReturn(null);

        servlet.doGet(req, resp);

        verify(session).setAttribute(eq("flash"), contains("10"));
        verify(resp).sendRedirect("/ctx/admin/customers");
    }

    @Test
    @DisplayName("GET id found -> forward to JSP")
    void get_id_found_forwards() throws Exception {
        Account admin = new Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("id")).thenReturn("5");
        when(service.getCustomerDetail(5)).thenReturn(mock(AdminCustomerDTO.class));
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-customer-detail.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("detail"), any());
    }
}

