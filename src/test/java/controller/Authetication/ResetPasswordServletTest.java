package controller.Authetication;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.sql.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResetPasswordServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock jakarta.servlet.http.HttpSession session;
    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    @DisplayName("RESET-GET-001: missing token -> flash + redirect /login")
    void get_missingToken_redirect() throws Exception {
        when(req.getParameter("token")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(anyBoolean())).thenReturn(session);
        new ResetPasswordServlet().doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("RESET-GET-002: token ok -> forward reset.jsp")
    void get_tokenOk_forward() throws Exception {
        when(req.getParameter("token")).thenReturn("t");
        when(req.getSession(anyBoolean())).thenReturn(session);
        RequestDispatcher rd = TestUtils.stubForward(req, "/reset.jsp");
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            new ResetPasswordServlet().doGet(req, resp);
            verify(rd).forward(req, resp);
            verify(req).setAttribute(eq("token"), eq("t"));
        }
    }

    @Test
    @DisplayName("RESET-POST-003: valid -> redirect /login with flash")
    void post_valid_redirectLogin() throws Exception {
        when(req.getParameter("token")).thenReturn("tok");
        when(req.getParameter("password")).thenReturn("pw");
        when(req.getParameter("confirm")).thenReturn("pw");
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(anyBoolean())).thenReturn(session);
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("ok")).thenReturn(1);
            new ResetPasswordServlet().doPost(req, resp);
            verify(resp).sendRedirect("/ctx/login");
        }
    }
}
