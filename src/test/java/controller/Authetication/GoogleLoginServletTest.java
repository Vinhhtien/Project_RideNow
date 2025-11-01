package controller.Authetication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.GoogleUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;
import utils.GoogleUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleLoginServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    @DisplayName("GOOGLE-GET-001: no code -> redirect to Google auth URL")
    void get_noCode_redirectGoogle() throws Exception {
        try (MockedStatic<GoogleUtils> g = mockStatic(GoogleUtils.class)) {
            g.when(GoogleUtils::buildAuthURL).thenReturn("https://accounts.google.com/");
            new GoogleLoginServlet().doGet(req, resp);
            verify(resp).sendRedirect("https://accounts.google.com/");
        }
    }

    @Test
    @DisplayName("GOOGLE-GET-002: code ok -> login and redirect home")
    void get_code_ok_redirectHome() throws Exception {
        when(req.getParameter("code")).thenReturn("c");
        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class);
             MockedStatic<GoogleUtils> g = mockStatic(GoogleUtils.class)) {
            // init servlet with mocked config/context and silence log()
            var servlet = spy(new GoogleLoginServlet());
            doNothing().when(servlet).log(anyString());
            doNothing().when(servlet).log(anyString(), any(Throwable.class));
            jakarta.servlet.ServletConfig cfg = mock(jakarta.servlet.ServletConfig.class);
            jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
            when(cfg.getServletContext()).thenReturn(ctx);
            servlet.init(cfg);
            db.when(DBConnection::getConnection).thenReturn(con);
            g.when(() -> GoogleUtils.exchangeCodeForToken("c")).thenReturn("token");

            GoogleUser u = new GoogleUser();
            u.setEmail("user@example.com");
            u.setName("User");
            g.when(() -> GoogleUtils.fetchUserInfo("token")).thenReturn(u);

            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(99);

            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/home.jsp");
        }
    }
}
