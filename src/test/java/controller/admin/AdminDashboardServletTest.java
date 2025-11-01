package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminService;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminDashboardServletTest {

    @BeforeAll
    static void setUpEnv() {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;



    @Test
    void get_service_throws_runtime_propagates() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        IAdminService svc = mock(IAdminService.class);
        when(svc.getKpiCards()).thenThrow(new RuntimeException("boom"));

        AdminDashboardServlet servlet = new AdminDashboardServlet();
        TestUtils.forceSet(servlet, "adminService", svc);

        Assertions.assertThatThrownBy(() -> servlet.doGet(req, resp))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("boom");
    }
}

