package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import model.Motorbike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IMotorbikeAdminService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminMotorbikesServletTest {

    AdminMotorbikesServlet servlet;
    @Mock IMotorbikeAdminService svc;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;

    @BeforeEach
    void setup() {
        servlet = new AdminMotorbikesServlet();
        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
    }

    @Test
    void list_forwardManagementPage() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbikes-management.jsp");
        when(req.getParameter("action")).thenReturn(null);
        when(svc.getAllMotorbikes()).thenReturn(new ArrayList<>());
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void post_create_duplicateLicensePlate_redirectError() throws Exception {
        when(req.getParameter("action")).thenReturn("create");
        when(req.getParameter("bikeName")).thenReturn("Bike");
        when(req.getParameter("licensePlate")).thenReturn("43E1-68932");
        when(req.getParameter("pricePerDay")).thenReturn("100000");
        when(req.getParameter("status")).thenReturn("available");
        when(req.getParameter("typeId")).thenReturn("1");

        // parts empty (no upload check)
        when(req.getParts()).thenReturn(List.<Part>of());

        Motorbike exists = new Motorbike(); exists.setLicensePlate("43E1-68932");
        when(svc.getAllMotorbikes()).thenReturn(List.of(exists));

        when(req.getContextPath()).thenReturn("/ctx");
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/admin/bikes?error=create_failed");
    }
}
