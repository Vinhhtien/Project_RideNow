//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.junit.jupiter.api.Assumptions;
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
//class AdminPaymentsServletTest {
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
//    @Mock ResultSet rs;
//
//    private Object newServletOrSkip() throws Exception {
//        try {
//            Class<?> clazz = Class.forName("controller.admin.AdminPaymentsServlet");
//            return clazz.getDeclaredConstructor().newInstance();
//        } catch (ClassNotFoundException e) {
//            Assumptions.assumeTrue(false, "AdminPaymentsServlet not present in this build");
//            return null; // unreachable
//        }
//    }
//
//    @Test
//    void ADMIN;-PAYMENTS-001_get_list_happy_forwards_and_sets_rows() throws Exception {
//        Object servlet = newServletOrSkip();
//
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true, false);
//            when(rs.getInt("payment_id")).thenReturn(100);
//            when(rs.getInt("order_id")).thenReturn(200);
//            when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("50.00"));
//            when(rs.getString("method")).thenReturn("cash");
//            when(rs.getString("status")).thenReturn("pending");
//            when(rs.getTimestamp("payment_date")).thenReturn(new Timestamp(0));
//            when(rs.getString("full_name")).thenReturn("John Doe");
//
//            servlet.getClass().getMethod("doGet", HttpServletRequest.class, HttpServletResponse.class)
//                    .invoke(servlet, req, resp);
//
//            verify(req).setAttribute(eq("rows"), any());
//            verify(req).getRequestDispatcher(anyString());
//            verify(rd).forward(req, resp);
//        }
//    }
//
//    @Test
//    void ADMIN-PAYMENTS-002_get_empty_list() throws Exception {
//        Object servlet = newServletOrSkip();
//
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(false);
//
//            servlet.getClass().getMethod("doGet", HttpServletRequest.class, HttpServletResponse.class)
//                    .invoke(servlet, req, resp);
//
//            verify(req).setAttribute(eq("rows"), any());
//            verify(rd).forward(req, resp);
//        }
//    }
//
//    @Test
//    void ADMIN-PAYMENTS-003_jdbc_exception_wrapped_as_servletexception() throws Exception {
//        Object servlet = newServletOrSkip();
//        when(req.getContextPath()).thenReturn("/ctx");
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenThrow(new SQLException("boom"));
//            try {
//                servlet.getClass().getMethod("doGet", HttpServletRequest.class, HttpServletResponse.class)
//                        .invoke(servlet, req, resp);
//            } catch (java.lang.reflect.InvocationTargetException ite) {
//                Throwable cause = ite.getCause();
//                org.assertj.core.api.Assertions.assertThat(cause)
//                        .isInstanceOf(jakarta.servlet.ServletException.class);
//            }
//        }
//    }
//}
//
