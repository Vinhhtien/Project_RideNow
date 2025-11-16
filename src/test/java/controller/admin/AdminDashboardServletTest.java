package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminService;

import java.util.Collections;
import java.util.HashMap;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDashboardServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock IAdminService adminService;

    @Test
    @DisplayName("GET forwards to dashboard with attributes")
    void get_forwards_dashboard() throws Exception {
        AdminDashboardServlet servlet = new AdminDashboardServlet();
        TestUtils.forceSet(servlet, "adminService", adminService);
        when(adminService.getKpiCards()).thenReturn(new HashMap<>());
        when(adminService.getLatestOrders(anyInt())).thenReturn(Collections.emptyList());
        when(adminService.getMaintenanceBikes(anyInt())).thenReturn(Collections.emptyList());

        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/dashboard.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("kpi"), any());
        verify(req).setAttribute(eq("latestOrders"), any());
        verify(req).setAttribute(eq("maintenanceBikes"), any());
    }
}

