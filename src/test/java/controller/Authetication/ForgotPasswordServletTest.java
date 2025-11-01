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
import utils.EmailUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ForgotPasswordServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock Connection con;
    @Mock PreparedStatement psFind;
    @Mock PreparedStatement psTok;
    @Mock ResultSet rs;

    @Test
    @DisplayName("FORGOT-GET-001: forward forgot.jsp")
    void get_forwards() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/forgot.jsp");
        new ForgotPasswordServlet().doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    @DisplayName("FORGOT-POST-002: missing email -> forward with msg")
    void post_missingEmail_forward() throws Exception {
        when(req.getParameter("email")).thenReturn(null);
        RequestDispatcher rd = TestUtils.stubForward(req, "/forgot.jsp");
        new ForgotPasswordServlet().doPost(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("msg"), any());
    }

    @Test
    @DisplayName("FORGOT-POST-003: happy -> insert token and send email")
    void post_happy_sendsEmail() throws Exception {
        when(req.getParameter("email")).thenReturn("a@ex.com");
        when(req.getRequestURL()).thenReturn(new StringBuffer("http://localhost/forgot"));
        RequestDispatcher rd = TestUtils.stubForward(req, "/forgot.jsp");

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = mockStatic(EmailUtil.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(startsWith("SELECT a.account_id"))).thenReturn(psFind);
            when(psFind.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt("account_id")).thenReturn(1);
            when(rs.getString("full_name")).thenReturn("Name");

            when(con.prepareStatement(startsWith("INSERT INTO Password_Reset_Tokens"))).thenReturn(psTok);
            when(psTok.executeUpdate()).thenReturn(1);

            new ForgotPasswordServlet().doPost(req, resp);
            mail.verify(() -> EmailUtil.sendMail(eq("a@ex.com"), anyString(), contains("resetpassword")));
            verify(rd).forward(req, resp);
        }
    }
}

