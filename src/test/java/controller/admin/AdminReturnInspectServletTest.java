//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import utils.DBConnection;
//
//import java.math.BigDecimal;
//import java.sql.*;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminReturnInspectServletTest {
//
//    @BeforeAll
//    static void initEnv() {
//        Locale.setDefault(Locale.US);
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    @Mock Connection con;
//    @Mock PreparedStatement ps;
//    @Mock PreparedStatement ps2;
//    @Mock ResultSet rs;
//
//    @Test
//    void ADMIN-RETURN-INSPECT-001_get_order_happy_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("77");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getInt("order_id")).thenReturn(77);
//            when(rs.getString("full_name")).thenReturn("C");
//            when(rs.getString("phone")).thenReturn("P");
//            when(rs.getString("bike_name")).thenReturn("B");
//            when(rs.getBigDecimal("deposit_amount")).thenReturn(new BigDecimal("100.00"));
//            when(rs.getTimestamp("returned_at")).thenReturn(new Timestamp(0));
//
//            new AdminReturnInspectServlet().doGet(req, resp);
//            verify(req).setAttribute(eq("order"), any());
//            verify(rd).forward(req, resp);
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-INSPECT-002_get_invalid_id_redirects() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("abc");
//        new AdminReturnInspectServlet().doGet(req, resp);
//        verify(resp).sendRedirect("/ctx/adminreturns");
//    }
//
//    @Test
//    void ADMIN-RETURN-INSPECT-003_post_passed_commits_and_redirects() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("88");
//        when(req.getParameter("bikeCondition")).thenReturn("excellent");
//        when(req.getParameter("refundMethod")).thenReturn("wallet");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(startsWith("SELECT deposit_amount"))).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getBigDecimal("deposit_amount")).thenReturn(new BigDecimal("100.00"));
//
//            // processInspection sequence
//            Connection con2 = mock(Connection.class);
//            PreparedStatement ins = mock(PreparedStatement.class);
//            PreparedStatement upd = mock(PreparedStatement.class);
//            db.when(DBConnection::getConnection).thenReturn(con).thenReturn(con2);
//            when(con2.prepareStatement(startsWith("INSERT INTO RefundInspections"))).thenReturn(ins);
//            when(con2.prepareStatement(startsWith("UPDATE RentalOrders SET deposit_status"))).thenReturn(upd);
//
//            new AdminReturnInspectServlet().doPost(req, resp);
//
//            verify(con2).setAutoCommit(false);
//            verify(ins).setInt(1, 88);
//            verify(ins).setInt(2, 1); // default adminId
//            verify(ins).setString(3, "excellent");
//            verify(upd).setInt(1, 88);
//            verify(con2).commit();
//            verify(con2).setAutoCommit(true);
//            verify(resp).sendRedirect("/ctx/adminreturns");
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-INSPECT-004_post_damaged_with_fee_commits() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("99");
//        when(req.getParameter("bikeCondition")).thenReturn("damaged");
//        when(req.getParameter("damageFee")).thenReturn("30");
//        when(req.getParameter("refundMethod")).thenReturn("cash");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(startsWith("SELECT deposit_amount"))).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getBigDecimal("deposit_amount")).thenReturn(new BigDecimal("100.00"));
//
//            Connection con2 = mock(Connection.class);
//            PreparedStatement ins = mock(PreparedStatement.class);
//            PreparedStatement upd = mock(PreparedStatement.class);
//            db.when(DBConnection::getConnection).thenReturn(con).thenReturn(con2);
//            when(con2.prepareStatement(startsWith("INSERT INTO RefundInspections"))).thenReturn(ins);
//            when(con2.prepareStatement(startsWith("UPDATE RentalOrders SET deposit_status"))).thenReturn(upd);
//
//            new AdminReturnInspectServlet().doPost(req, resp);
//
//            verify(ins).setBigDecimal(eq(5), eq(new BigDecimal("30")));
//            verify(con2).commit();
//            verify(resp).sendRedirect("/ctx/adminreturns");
//        }
//    }
//
//    @Test
//    void ADMIN-RETURN-INSPECT-005_post_sql_exception_rolls_back() throws Exception {
//        var sm = TestUtils.createSessionMap();
//        HttpSession session = TestUtils.mockSession(sm);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("orderId")).thenReturn("77");
//        when(req.getParameter("bikeCondition")).thenReturn("good");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            // deposit amount
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getBigDecimal(anyString())).thenReturn(new BigDecimal("50.00"));
//
//            // processInspection throws on getConnection
//            db.when(DBConnection::getConnection).thenThrow(new SQLException("boom"));
//
//            new AdminReturnInspectServlet().doPost(req, resp);
//            org.assertj.core.api.Assertions.assertThat(sm.get("flash")).isNotNull();
//            verify(resp).sendRedirect("/ctx/adminreturns");
//        }
//    }
//}
//
