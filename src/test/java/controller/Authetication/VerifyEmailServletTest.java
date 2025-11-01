package controller.Authetication;

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
class VerifyEmailServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock jakarta.servlet.http.HttpSession session;
    @Mock Connection con;
    @Mock PreparedStatement psSel;
    @Mock PreparedStatement psUpd1;
    @Mock PreparedStatement psUpd2;
    @Mock ResultSet rs;

    @Test
    @DisplayName("VERIFY-GET-001: missing token -> flash + redirect /login")
    void get_missingToken_redirect() throws Exception {
        when(req.getParameter("token")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(anyBoolean())).thenReturn(session);
        new VerifyEmailServlet().doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("VERIFY-GET-002: valid token -> commit & redirect /login")
    void get_validToken_commit() throws Exception {
        when(req.getParameter("token")).thenReturn("t");
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getSession(anyBoolean())).thenReturn(session);
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();

            when(con.prepareStatement(startsWith("SELECT account_id"))).thenReturn(psSel);
            when(psSel.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("account_id")).thenReturn(1);
            when(rs.getBoolean("used")).thenReturn(false);
            when(rs.getTimestamp("expire_at")).thenReturn(new Timestamp(System.currentTimeMillis()+3600_000));

            when(con.prepareStatement(startsWith("UPDATE Accounts"))).thenReturn(psUpd1);
            when(psUpd1.executeUpdate()).thenReturn(1);
            when(con.prepareStatement(startsWith("UPDATE Email_Verify_Tokens"))).thenReturn(psUpd2);
            when(psUpd2.executeUpdate()).thenReturn(1);

            new VerifyEmailServlet().doGet(req, resp);
            verify(resp).sendRedirect("/ctx/login");
        }
    }
}
