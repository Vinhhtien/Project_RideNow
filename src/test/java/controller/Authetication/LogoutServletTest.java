package controller.Authetication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogoutServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    void get_clearsSession_redirectLogin() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        when(req.getSession(false)).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        servlet.doGet(req, resp);
        verify(session).invalidate();
        verify(resp).sendRedirect("/ctx/home.jsp");
    }
}

