package controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReturnInspectServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET missing orderId redirects to /adminreturns")
    void get_missing_orderId_redirects() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();
        when(req.getParameter("orderId")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST missing orderId sets flash and redirects")
    void post_missing_orderId_redirects() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("orderId")).thenReturn("");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), contains("orderId"));
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("GET invalid orderId format -> flash + redirect")
    void get_invalid_orderId_redirects() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("orderId")).thenReturn("abc");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("GET order found -> forward to inspect JSP")
    void get_order_found_forwards() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();

        // Mock DB for getOrderForInspection to return one row
        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt("order_id")).thenReturn(1);
        when(rs.getString("customer_name")).thenReturn("A");
        when(rs.getString("customer_phone")).thenReturn("090");
        when(rs.getString("bike_name")).thenReturn("B");
        when(rs.getBigDecimal("deposit_amount")).thenReturn(new java.math.BigDecimal("100"));
        when(rs.getTimestamp("returned_at")).thenReturn(new java.sql.Timestamp(System.currentTimeMillis()));
        when(ps.executeQuery()).thenReturn(rs);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);
            when(req.getParameter("orderId")).thenReturn("1");
            jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-return-inspect.jsp");

            servlet.doGet(req, resp);

            verify(rd).forward(req, resp);
            verify(req).setAttribute(eq("order"), any());
        }
    }

    @Test
    @DisplayName("POST invalid bikeCondition -> flash + redirect")
    void post_invalid_condition_redirects() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("orderId")).thenReturn("2");
        when(req.getParameter("bikeCondition")).thenReturn("bad_value");
        when(req.getParameter("refundMethod")).thenReturn("wallet");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST valid inputs -> processInspection success and redirect")
    void post_valid_inputs_success() throws Exception {
        AdminReturnInspectServlet servlet = new AdminReturnInspectServlet();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("admin_id")).thenReturn(5);

        when(req.getParameter("orderId")).thenReturn("3");
        when(req.getParameter("bikeCondition")).thenReturn("good");
        when(req.getParameter("damageFee")).thenReturn("200"); // will be clamped to deposit
        when(req.getParameter("refundMethod")).thenReturn("invalid"); // defaults to cash
        when(req.getParameter("damageNotes")).thenReturn("notes");
        when(req.getContextPath()).thenReturn("/ctx");

        // Mock DB sequence: getDepositAmount -> 100, then processInspection (two updates) returns true
        java.sql.Connection con1 = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psDep = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsDep = mock(java.sql.ResultSet.class);
        when(rsDep.next()).thenReturn(true);
        when(rsDep.getBigDecimal("deposit_amount")).thenReturn(new java.math.BigDecimal("100"));
        when(psDep.executeQuery()).thenReturn(rsDep);
        when(con1.prepareStatement(anyString())).thenReturn(psDep);

        java.sql.Connection con2 = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psIns = mock(java.sql.PreparedStatement.class);
        when(psIns.executeUpdate()).thenReturn(1);
        java.sql.PreparedStatement psUpd = mock(java.sql.PreparedStatement.class);
        when(psUpd.executeUpdate()).thenReturn(1);
        when(con2.prepareStatement(org.mockito.ArgumentMatchers.startsWith("\n                        INSERT INTO RefundInspections"))).thenReturn(psIns);
        when(con2.prepareStatement(org.mockito.ArgumentMatchers.startsWith("UPDATE RentalOrders SET deposit_status"))).thenReturn(psUpd);

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con1, con2);

            servlet.doPost(req, resp);

            verify(resp).sendRedirect("/ctx/adminreturns");
        }
    }
}
