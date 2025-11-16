package controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminPickupServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET redirects login when not admin")
    void get_redirects_not_admin() throws Exception {
        AdminPickupServlet servlet = new AdminPickupServlet();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("POST confirm pickup sets flash and redirects")
    void post_confirm_pickup() throws Exception {
        AdminPickupServlet servlet = new AdminPickupServlet();
        // inject admin account
        model.Account admin = new model.Account(); admin.setRole("admin");
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("orderId")).thenReturn("123");
        when(req.getContextPath()).thenReturn("/ctx");

        // bypass OrderManageService by forcing true through reflection
        service.IOrderManageService mockSvc = mock(service.IOrderManageService.class);
        controller.testsupport.TestUtils.forceSet(servlet, "orderService", mockSvc);
        when(mockSvc.confirmOrderPickup(123, 1)).thenReturn(true);

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminpickup");
    }
}
