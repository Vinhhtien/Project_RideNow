package testutil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import static org.mockito.Mockito.*;

public class MockFactory {
    public static HttpServletRequest request() {
        return mock(HttpServletRequest.class);
    }
    public static HttpServletResponse response() {
        return mock(HttpServletResponse.class);
    }
    public static HttpSession session() {
        return mock(HttpSession.class);
    }
    public static RequestDispatcher dispatcher(HttpServletRequest req, String path) {
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(path)).thenReturn(rd);
        return rd;
    }
}

