package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;
import utils.EmailUtil;
import utils.PasswordUtil;

import java.sql.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegisterServletTest {

    RegisterServlet servlet;

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Mock Connection con;
    @Mock PreparedStatement psAcc;
    @Mock PreparedStatement psCus;
    @Mock PreparedStatement psTok;
    @Mock ResultSet rsKeys;

    @BeforeEach
    void setup() {
        servlet = new RegisterServlet();
        when(req.getSession(anyBoolean())).thenReturn(session);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getScheme()).thenReturn("http");
        when(req.getServerName()).thenReturn("localhost");
        when(req.getServerPort()).thenReturn(8080);
    }

    @Test
    void get_forwardForm() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/register.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void post_sqlError_forwardRegister() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/register.jsp");
        when(req.getParameter(anyString())).thenReturn("v");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<PasswordUtil> pwd = Mockito.mockStatic(PasswordUtil.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("down"));
            pwd.when(() -> PasswordUtil.hashPassword(anyString())).thenReturn("hash");
            servlet.doPost(req, resp);
            verify(rd).forward(req, resp);
        }
    }

    @Test
    void post_success_redirectLogin_andSendEmail() throws Exception {
        when(req.getParameter("username")).thenReturn("u");
        when(req.getParameter("password")).thenReturn("p");
        when(req.getParameter("full_name")).thenReturn("Full");
        when(req.getParameter("email")).thenReturn("a@b.com");
        when(req.getParameter("phone")).thenReturn("1");
        when(req.getParameter("address")).thenReturn("addr");

        when(con.prepareStatement(startsWith("INSERT INTO Accounts"), anyInt())).thenReturn(psAcc);
        when(psAcc.executeUpdate()).thenReturn(1);
        when(psAcc.getGeneratedKeys()).thenReturn(rsKeys);
        when(rsKeys.next()).thenReturn(true);
        when(rsKeys.getInt(1)).thenReturn(100);

        when(con.prepareStatement(startsWith("INSERT INTO Customers"))).thenReturn(psCus);
        when(psCus.executeUpdate()).thenReturn(1);

        when(con.prepareStatement(startsWith("INSERT INTO Email_Verify_Tokens"))).thenReturn(psTok);
        when(psTok.executeUpdate()).thenReturn(1);

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<PasswordUtil> pwd = Mockito.mockStatic(PasswordUtil.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);
            pwd.when(() -> PasswordUtil.hashPassword(anyString())).thenReturn("hash");

            servlet.doPost(req, resp);
            verify(resp).sendRedirect("/ctx/login");
            mail.verify(() -> EmailUtil.sendMail(eq("a@b.com"), anyString(), anyString()));
        }
    }
}
