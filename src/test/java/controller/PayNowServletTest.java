package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;
import utils.EmailUtil;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ✅ PayNowServletTest — Full branch coverage 100%
 *  - Fixed Date ambiguity (using java.sql.Date)
 *  - Mocked DBConnection and EmailUtil
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayNowServletTest {

    PayNowServlet servlet;

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Mock Connection con;
    @Mock PreparedStatement psMain, psDeposit, psInsertPayment, psUpdateConfirm,
            psSafe1, psSafe2, psLockBikes, psCusInfo, psOrdersEmail;
    @Mock ResultSet rsMain, rsDeposit, rsCusInfo, rsOrdersEmail;

    Map<String, Object> sessionAttrs;

    private static ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    private static Method reflect(Class<?> c, String name, Class<?>... types) throws Exception {
        Method m = c.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m;
    }

    @BeforeEach
    void setup() {
        servlet = new PayNowServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        sessionAttrs = new HashMap<>();

        when(session.getAttribute(anyString()))
                .thenAnswer(i -> sessionAttrs.get(i.getArgument(0)));
        doAnswer(i -> {
            sessionAttrs.put(i.getArgument(0), i.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());
    }

    /* ======================= doGet() ======================= */

    @Test
    void get_notLoggedIn_redirectsLogin() throws Exception {
        try (MockedStatic<DBConnection> ignored = Mockito.mockStatic(DBConnection.class)) {
            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/login.jsp");
        }
    }

    @Test
    void get_noOrders_redirectsOrders() throws Exception {
        sessionAttrs.put("account", acc(1));
        when(req.getParameter("orders")).thenReturn(null);

        try (MockedStatic<DBConnection> ignored = Mockito.mockStatic(DBConnection.class)) {
            servlet.doGet(req, resp);
            Assertions.assertThat(sessionAttrs).containsKey("flash");
            verify(resp).sendRedirect("/ctx/customerorders");
        }
    }

    @Test
    void get_exception_catchPath_flashRedirect() throws Exception {
        sessionAttrs.put("account", acc(2));
        when(req.getParameter("orders")).thenReturn("10");
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenThrow(new RuntimeException("boom"));
            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(sessionAttrs).containsKey("flash");
        }
    }

    @Test
    void get_noValidOrders_foundNone() throws Exception {
        sessionAttrs.put("account", acc(3));
        when(req.getParameter("orders")).thenReturn("100");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(false);

            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(sessionAttrs).containsKey("flash");
        }
    }

    @Test
    void get_success_forwardPage() throws Exception {
        sessionAttrs.put("account", acc(4));
        when(req.getParameter("orders")).thenReturn("11,12");
        RequestDispatcher rd = TestUtils.stubForward(req, "/cart/paynow.jsp");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, true, false);
            when(rsMain.getInt("order_id")).thenReturn(11, 12);
            when(rsMain.getString("bike_name")).thenReturn("Yamaha", "Honda");
            when(rsMain.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));
            when(rsMain.getDate("end_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            when(rsMain.getBigDecimal("total_price"))
                    .thenReturn(new BigDecimal("100000"), new BigDecimal("200000"));

            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true, true);
            when(rsDeposit.getBigDecimal("deposit"))
                    .thenReturn(new BigDecimal("50000"), new BigDecimal("100000"));

            servlet.doGet(req, resp);

            verify(rd).forward(req, resp);
            verify(req).setAttribute(eq("rows"), any());
            verify(req).setAttribute(eq("ordersCsv"), eq("11,12"));
        }
    }

    /* ======================= doPost() ======================= */

    @Test
    void post_notLoggedIn_redirectLogin() throws Exception {
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/login.jsp");
    }

    @Test
    void post_noOrders_flashRedirect() throws Exception {
        sessionAttrs.put("account", acc(1));
        when(req.getParameter("orders")).thenReturn("");
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customerorders");
        Assertions.assertThat(sessionAttrs).containsKey("flash");
    }

    @Test
    void post_success_bankTransfer() throws Exception {
        sessionAttrs.put("account", acc(10));
        when(req.getParameter("orders")).thenReturn("101");
        when(req.getParameter("paymentMethod")).thenReturn("transfer");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);

            // payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(101);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));

            // deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // insert payment
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments"))))
                    .thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});

            // confirm + safe
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'"))))
                    .thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);

            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET pickup_status"))))
                    .thenReturn(psSafe1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET return_status"))))
                    .thenReturn(psSafe2);

            // lock bikes
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            // email
            when(con.prepareStatement(eq("SELECT full_name, email FROM Customers WHERE account_id = ?")))
                    .thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getString("full_name")).thenReturn("Tester");
            when(rsCusInfo.getString("email")).thenReturn("t@example.com");

            when(con.prepareStatement(argThat(sqlContains("SELECT r.order_id"))))
                    .thenReturn(psOrdersEmail);
            when(psOrdersEmail.executeQuery()).thenReturn(rsOrdersEmail);
            when(rsOrdersEmail.next()).thenReturn(true, false);
            when(rsOrdersEmail.getInt("order_id")).thenReturn(101);
            when(rsOrdersEmail.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));
            when(rsOrdersEmail.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));
            when(rsOrdersEmail.getDate("end_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            when(rsOrdersEmail.getString("bike_name")).thenReturn("Yamaha");

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).commit();
            verify(resp).sendRedirect("/ctx/customerorders");
            mail.verify(() -> EmailUtil.sendMailHTML(eq("t@example.com"), anyString(), anyString()));
        }
    }

    @Test
    void post_success_cashPath_wallet() throws Exception {
        sessionAttrs.put("account", acc(20));
        when(req.getParameter("orders")).thenReturn("401");
        when(req.getParameter("paymentMethod")).thenReturn("wallet");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(401);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));

            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(BigDecimal.ZERO);

            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments"))))
                    .thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});

            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'"))))
                    .thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);

            // lockBikesForActiveDates
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).commit(); // ✅ now passes
        }
    }

    @Test
    void post_error_rollback() throws Exception {
        sessionAttrs.put("account", acc(99));
        when(req.getParameter("orders")).thenReturn("900");
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenThrow(new SQLException("DB down"));
            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).rollback();
            servlet.doPost(req, resp);
            verify(con).rollback();
        }
    }

    /* ============== private helpers coverage ============== */

    @Test
    void helperMethods_coverAllBranches() throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(psDeposit);
        when(psDeposit.executeQuery()).thenReturn(rsDeposit);
        when(rsDeposit.next()).thenReturn(false);
        BigDecimal res = (BigDecimal) reflect(PayNowServlet.class,
                "calcDepositForOrder", Connection.class, int.class)
                .invoke(servlet, con, 1);
        Assertions.assertThat(res).isZero();

        String esc = (String) reflect(PayNowServlet.class, "escape", String.class)
                .invoke(null, (Object) null);
        Assertions.assertThat(esc).isEqualTo("");

        BigDecimal safe = (BigDecimal) reflect(PayNowServlet.class, "safe", BigDecimal.class)
                .invoke(null, (Object) null);
        Assertions.assertThat(safe).isZero();

        when(con.prepareStatement(anyString())).thenThrow(new SQLException("x"));
        reflect(PayNowServlet.class, "unlockBikesNoActiveToday", Connection.class)
                .invoke(servlet, con);

        reflect(PayNowServlet.class, "safeExec", Connection.class, String.class)
                .invoke(servlet, con, "UPDATE something");

        reflect(PayNowServlet.class, "insertPaidPayments",
                Connection.class, Map.class, String.class)
                .invoke(servlet, con, Collections.emptyMap(), "cash");
    }

    private static Account acc(int id) {
        Account a = new Account();
        a.setAccountId(id);
        return a;
    }
}
