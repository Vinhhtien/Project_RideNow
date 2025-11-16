package controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Method;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReturnsServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("POST always redirects to /adminreturns (even on error)")
    void post_redirects() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST mark_processing path redirects with flash")
    void post_mark_processing() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("action")).thenReturn("mark_processing");
        when(req.getParameter("inspectionId")).thenReturn("1");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST complete_refund wallet redirects with flash")
    void post_complete_refund_wallet() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("action")).thenReturn("complete_refund");
        when(req.getParameter("orderId")).thenReturn("5");
        when(req.getParameter("refundMethod")).thenReturn("wallet");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST complete_refund cash redirects with flash")
    void post_complete_refund_cash() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("action")).thenReturn("complete_refund");
        when(req.getParameter("orderId")).thenReturn("5");
        when(req.getParameter("refundMethod")).thenReturn("cash");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("POST cancel redirects with flash")
    void post_cancel() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getParameter("action")).thenReturn("cancel");
        when(req.getParameter("inspectionId")).thenReturn("7");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturns");
    }

    @Test
    @DisplayName("markAsProcessing true/false via reflection")
    void markAsProcessing_reflection() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        Method m = AdminReturnsServlet.class.getDeclaredMethod("markAsProcessing", java.sql.Connection.class, String.class);
        m.setAccessible(true);

        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1).thenReturn(0);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        Object rTrue = m.invoke(servlet, con, "1");
        Object rFalse = m.invoke(servlet, con, "2");
        Object rBlank = m.invoke(servlet, con, " ");

        Assertions.assertThat((Boolean) rTrue).isTrue();
        Assertions.assertThat((Boolean) rFalse).isFalse();
        Assertions.assertThat((Boolean) rBlank).isFalse();
    }

    @Test
    @DisplayName("cancelRefund true/false via reflection")
    void cancelRefund_reflection() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        Method m = AdminReturnsServlet.class.getDeclaredMethod("cancelRefund", java.sql.Connection.class, String.class);
        m.setAccessible(true);

        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        when(ps.executeUpdate()).thenReturn(1).thenReturn(0);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        Object rTrue = m.invoke(servlet, con, "5");
        Object rFalse = m.invoke(servlet, con, "6");
        Object rBlank = m.invoke(servlet, con, null);

        Assertions.assertThat((Boolean) rTrue).isTrue();
        Assertions.assertThat((Boolean) rFalse).isFalse();
        Assertions.assertThat((Boolean) rBlank).isFalse();
    }

    @Test
    @DisplayName("completeRefund wallet & cash via reflection")
    void completeRefund_reflection_wallet_cash() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        Method m = AdminReturnsServlet.class.getDeclaredMethod("completeRefund", java.sql.Connection.class, String.class, String.class);
        m.setAccessible(true);

        // Build dynamic Connection mock with SQL-aware PreparedStatements
        java.sql.Connection con = mock(java.sql.Connection.class);

        // 1) SELECT refund_amount, customer_id
        java.sql.PreparedStatement psSelect = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsSelect = mock(java.sql.ResultSet.class);
        when(rsSelect.next()).thenReturn(true);
        when(rsSelect.getBigDecimal("refund_amount")).thenReturn(new java.math.BigDecimal("100"));
        when(rsSelect.getInt("customer_id")).thenReturn(77);
        when(psSelect.executeQuery()).thenReturn(rsSelect);

        // 2) UPDATE inspections completed
        java.sql.PreparedStatement psUpdInspect = mock(java.sql.PreparedStatement.class);
        when(psUpdInspect.executeUpdate()).thenReturn(1);

        // 3) UPDATE order deposit_status
        java.sql.PreparedStatement psUpdOrder = mock(java.sql.PreparedStatement.class);
        when(psUpdOrder.executeUpdate()).thenReturn(1);

        // 4a) SELECT wallet_id -> none first time, has one second time
        java.sql.PreparedStatement psSelWallet = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsSelWallet = mock(java.sql.ResultSet.class);
        when(rsSelWallet.next()).thenReturn(false).thenReturn(true);
        when(rsSelWallet.getInt(1)).thenReturn(999);
        when(psSelWallet.executeQuery()).thenReturn(rsSelWallet);

        // 4b) INSERT wallet -> returns id 999
        java.sql.PreparedStatement psInsWallet = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsInsWallet = mock(java.sql.ResultSet.class);
        when(rsInsWallet.next()).thenReturn(true);
        when(rsInsWallet.getInt(1)).thenReturn(999);
        when(psInsWallet.executeQuery()).thenReturn(rsInsWallet);

        // 4c) UPDATE wallet balance
        java.sql.PreparedStatement psUpdWallet = mock(java.sql.PreparedStatement.class);
        when(psUpdWallet.executeUpdate()).thenReturn(1);

        // 4d) INSERT wallet transaction
        java.sql.PreparedStatement psInsTxn = mock(java.sql.PreparedStatement.class);
        when(psInsTxn.executeUpdate()).thenReturn(1);

        // 5) INSERT Payments
        java.sql.PreparedStatement psInsPayment = mock(java.sql.PreparedStatement.class);
        when(psInsPayment.executeUpdate()).thenReturn(1);

        // 6) maybeUpdateOrderCompleted update
        java.sql.PreparedStatement psMaybe = mock(java.sql.PreparedStatement.class);
        when(psMaybe.executeUpdate()).thenReturn(1);

        // Provide a resilient Answer to avoid NPE when sql is null and route by content
        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql == null) return psSelect;
            if (sql.contains("FROM RefundInspections ri")) return psSelect;
            if (sql.trim().startsWith("UPDATE RefundInspections")) return psUpdInspect;
            if (sql.trim().startsWith("UPDATE RentalOrders") && sql.contains("deposit_status")) return psUpdOrder;
            if (sql.contains("FROM Wallets WHERE customer_id")) return psSelWallet;
            if (sql.contains("INSERT INTO Wallets(")) return psInsWallet;
            if (sql.trim().startsWith("UPDATE Wallets")) return psUpdWallet;
            if (sql.contains("INSERT INTO Wallet_Transactions")) return psInsTxn;
            if (sql.contains("INSERT INTO Payments")) return psInsPayment;
            if (sql.contains("UPDATE RentalOrders") && sql.contains("SET status")) return psMaybe;
            return psSelect;
        });

        Object resWallet = m.invoke(servlet, con, "15", "wallet");
        Assertions.assertThat((Boolean) resWallet).isTrue();

        // cash branch (skips wallet update path)
        Object resCash = m.invoke(servlet, con, "16", "cash");
        Assertions.assertThat((Boolean) resCash).isTrue();
    }

    @Test
    @DisplayName("tableExists/tableHasColumn true/false via reflection")
    void schema_checks_reflection() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        java.lang.reflect.Method tExists = AdminReturnsServlet.class.getDeclaredMethod("tableExists", java.sql.Connection.class, String.class);
        java.lang.reflect.Method tHas = AdminReturnsServlet.class.getDeclaredMethod("tableHasColumn", java.sql.Connection.class, String.class, String.class);
        tExists.setAccessible(true); tHas.setAccessible(true);

        // true branches
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(true);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        when(ps.executeQuery()).thenReturn(rs);
        java.sql.Connection con = mock(java.sql.Connection.class);
        when(con.prepareStatement(anyString())).thenReturn(ps);
        Assertions.assertThat((Boolean) tExists.invoke(servlet, con, "T")).isTrue();
        Assertions.assertThat((Boolean) tHas.invoke(servlet, con, "T", "C")).isTrue();

        // false via SQLException
        java.sql.Connection conErr = mock(java.sql.Connection.class);
        when(conErr.prepareStatement(anyString())).thenThrow(new java.sql.SQLException("err"));
        Assertions.assertThat((Boolean) tExists.invoke(servlet, conErr, "T")).isFalse();
        Assertions.assertThat((Boolean) tHas.invoke(servlet, conErr, "T", "C")).isFalse();
    }

    @Test
    @DisplayName("GET forwards with empty lists (DB mocked)")
    void get_forwards_empty_lists() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();

        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(false);
        when(ps.executeQuery()).thenReturn(rs);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);

            jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-returns.jsp");

            AdminReturnsServlet servletUnderTest = new AdminReturnsServlet();
            servletUnderTest.doGet(req, resp);

            verify(rd).forward(req, resp);
            verify(req).setAttribute(eq("refundOrders"), any());
            verify(req).setAttribute(eq("refundRequests"), any());
            verify(req).setAttribute(eq("totalPendingAmount"), any());
        }
    }

    @Test
    @DisplayName("GET totals uses refund_amount (null->0, non-null) covering lambda")
    void get_totals_covers_lambda() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();

        java.sql.Connection con = mock(java.sql.Connection.class);
        // ps1 -> debugAllReturned
        java.sql.PreparedStatement ps1 = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs1 = mock(java.sql.ResultSet.class);
        when(rs1.next()).thenReturn(false);
        when(ps1.executeQuery()).thenReturn(rs1);

        // ps2 -> refundOrders empty
        java.sql.PreparedStatement ps2 = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs2 = mock(java.sql.ResultSet.class);
        when(rs2.next()).thenReturn(false);
        when(ps2.executeQuery()).thenReturn(rs2);

        // ps3 -> refundRequests with null and non-null refund_amount
        java.sql.PreparedStatement ps3 = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs3 = mock(java.sql.ResultSet.class);
        when(rs3.next()).thenReturn(true, true, false);
        when(rs3.getBigDecimal("refund_amount")).thenReturn(null, new java.math.BigDecimal("45"));
        when(ps3.executeQuery()).thenReturn(rs3);

        when(con.prepareStatement(anyString())).thenReturn(ps1, ps2, ps3);

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);

            controller.testsupport.TestUtils.stubForward(req, "/admin/admin-returns.jsp");
            servlet.doGet(req, resp);

            org.mockito.ArgumentCaptor<Object> cap = org.mockito.ArgumentCaptor.forClass(Object.class);
            verify(req, atLeastOnce()).setAttribute(eq("totalPendingAmount"), cap.capture());
            java.math.BigDecimal total = null;
            for (Object v : cap.getAllValues()) if (v instanceof java.math.BigDecimal) total = (java.math.BigDecimal) v;
            Assertions.assertThat(total).isNotNull();
            Assertions.assertThat(total).isEqualByComparingTo("45");
        }
    }

    @Test
    @DisplayName("updateWalletBalance true and false via reflection")
    void updateWalletBalance_reflection() throws Exception {
        AdminReturnsServlet servlet = new AdminReturnsServlet();
        java.lang.reflect.Method m = AdminReturnsServlet.class.getDeclaredMethod("updateWalletBalance", java.sql.Connection.class, int.class);
        m.setAccessible(true);

        // True path
        java.sql.Connection conTrue = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psSelect = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsSelect = mock(java.sql.ResultSet.class);
        when(rsSelect.next()).thenReturn(true);
        when(rsSelect.getBigDecimal("refund_amount")).thenReturn(new java.math.BigDecimal("50"));
        when(rsSelect.getInt("customer_id")).thenReturn(77);
        when(psSelect.executeQuery()).thenReturn(rsSelect);

        java.sql.PreparedStatement psSelWallet = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsSelWallet = mock(java.sql.ResultSet.class);
        when(rsSelWallet.next()).thenReturn(false);
        when(psSelWallet.executeQuery()).thenReturn(rsSelWallet);

        java.sql.PreparedStatement psInsWallet = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsInsWallet = mock(java.sql.ResultSet.class);
        when(rsInsWallet.next()).thenReturn(true);
        when(rsInsWallet.getInt(1)).thenReturn(1001);
        when(psInsWallet.executeQuery()).thenReturn(rsInsWallet);

        java.sql.PreparedStatement psUpdWallet = mock(java.sql.PreparedStatement.class);
        when(psUpdWallet.executeUpdate()).thenReturn(1);

        java.sql.PreparedStatement psInsTxn = mock(java.sql.PreparedStatement.class);
        when(psInsTxn.executeUpdate()).thenReturn(1);

        when(conTrue.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql.contains("FROM RefundInspections")) return psSelect;
            if (sql.contains("FROM Wallets WHERE customer_id")) return psSelWallet;
            if (sql.contains("INSERT INTO Wallets(")) return psInsWallet;
            if (sql.contains("UPDATE Wallets")) return psUpdWallet;
            if (sql.contains("INSERT INTO Wallet_Transactions")) return psInsTxn;
            return psSelect;
        });

        Object rTrue = m.invoke(servlet, conTrue, 5);
        Assertions.assertThat((Boolean) rTrue).isTrue();

        // False path: no select row
        java.sql.Connection conFalse = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps0 = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs0 = mock(java.sql.ResultSet.class);
        when(rs0.next()).thenReturn(false);
        when(ps0.executeQuery()).thenReturn(rs0);
        when(conFalse.prepareStatement(anyString())).thenReturn(ps0);

        Object rFalse = m.invoke(servlet, conFalse, 6);
        Assertions.assertThat((Boolean) rFalse).isFalse();
    }
}
