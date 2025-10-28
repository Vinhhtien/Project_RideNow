package controller.partner;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.Motorbike;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IMotorbikeService;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotorbikeManageListServletTest {

    MotorbikeManageListServlet servlet;
    @Mock IMotorbikeService service;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    private static Account acc(int id, String role) { Account a = new Account(); a.setAccountId(id); a.setRole(role); return a; }

    @BeforeEach
    void setup() {
        servlet = new MotorbikeManageListServlet();
        TestUtils.forceSet(servlet, "service", service);
        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
    }

    @Test
    void notAuthorized_redirectLogin() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(1, "customer"));
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void admin_list_forward() throws Exception {
        when(session.getAttribute("account")).thenReturn(acc(2, "admin"));
        when(service.findAllByOwnerAccount(2, "admin")).thenReturn(Collections.<Motorbike>emptyList());
        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/manage/handleBike.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("items"), any());
    }
}

