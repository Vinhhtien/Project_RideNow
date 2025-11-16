package controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReturnServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;

    @Test
    @DisplayName("GET redirects when not admin")
    void get_redirects_not_admin() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doGet(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("GET admin forwards with activeOrders")
    void get_admin_forwards() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        // inject mock orderService to avoid DB
        service.IOrderManageService mockSvc = mock(service.IOrderManageService.class);
        controller.testsupport.TestUtils.forceSet(servlet, "orderService", mockSvc);
        when(mockSvc.getActiveOrders()).thenReturn(java.util.Collections.emptyList());

        when(req.getSession()).thenReturn(session);
        model.Account admin = new model.Account(); admin.setRole("admin");
        when(session.getAttribute("account")).thenReturn(admin);
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-return.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("activeOrders"), any());
    }

    @Test
    @DisplayName("POST redirects to login when not admin")
    void post_redirects_not_admin() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("POST missing orderId -> flash + redirect")
    void post_missing_orderId_redirects() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        Account admin = new Account(); admin.setRole("admin");
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("orderId")).thenReturn("");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturn");
    }

    @Test
    @DisplayName("POST mark_not_returned sets flash and redirects")
    void post_mark_not_returned_redirects() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        Account admin = new Account(); admin.setRole("admin");
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("orderId")).thenReturn("1");
        when(req.getParameter("actionType")).thenReturn("mark_not_returned");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturn");
    }

    @Test
    @DisplayName("POST invalid orderId format -> flash + redirect")
    void post_invalid_orderId_format() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        model.Account admin = new model.Account(); admin.setRole("admin");
        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(admin);
        when(req.getParameter("orderId")).thenReturn("abc");
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPost(req, resp);

        verify(session).setAttribute(eq("flash"), anyString());
        verify(resp).sendRedirect("/ctx/adminreturn");
    }

    @Test
    @DisplayName("POST adminId not found -> flash + redirect")
    void post_admin_id_not_found() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        // mock DBConnection to return a connection that yields no rows
        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(false);
        when(ps.executeQuery()).thenReturn(rs);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        try (MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);

            model.Account admin = new model.Account(); admin.setRole("admin"); admin.setAccountId(1);
            when(req.getSession()).thenReturn(session);
            when(session.getAttribute("account")).thenReturn(admin);
            when(req.getParameter("orderId")).thenReturn("10");
            when(req.getContextPath()).thenReturn("/ctx");

            servlet.doPost(req, resp);

            verify(session).setAttribute(eq("flash"), anyString());
            verify(resp).sendRedirect("/ctx/adminreturn");
        }
    }

    @Test
    @DisplayName("POST confirmOrderReturn=false -> flash + redirect")
    void post_confirm_return_false() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        // mock adminId lookup to return value
        java.sql.Connection con = mock(java.sql.Connection.class);
        java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(123);
        when(ps.executeQuery()).thenReturn(rs);
        when(con.prepareStatement(anyString())).thenReturn(ps);

        service.IOrderManageService orderSvc = mock(service.IOrderManageService.class);
        controller.testsupport.TestUtils.forceSet(servlet, "orderService", orderSvc);
        when(orderSvc.confirmOrderReturn(10, 123)).thenReturn(false);

        try (MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);

            model.Account admin = new model.Account(); admin.setRole("admin"); admin.setAccountId(1);
            when(req.getSession()).thenReturn(session);
            when(session.getAttribute("account")).thenReturn(admin);
            when(req.getParameter("orderId")).thenReturn("10");
            when(req.getContextPath()).thenReturn("/ctx");

            servlet.doPost(req, resp);

            verify(session).setAttribute(eq("flash"), anyString());
            verify(resp).sendRedirect("/ctx/adminreturn");
        }
    }

    @Test
    @DisplayName("POST normal_return success path (DB mocked) -> flash + redirect")
    void post_normal_return_success() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        // 1) adminId lookup -> returns 123
        java.sql.Connection conAdmin = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psAdmin = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsAdmin = mock(java.sql.ResultSet.class);
        when(rsAdmin.next()).thenReturn(true);
        when(rsAdmin.getInt(1)).thenReturn(123);
        when(psAdmin.executeQuery()).thenReturn(rsAdmin);
        when(conAdmin.prepareStatement(anyString())).thenReturn(psAdmin);

        // 2) upsert inspection -> findOpen returns empty then insert executes
        java.sql.Connection conUpsert = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psFind = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFind = mock(java.sql.ResultSet.class);
        when(rsFind.next()).thenReturn(false);
        when(psFind.executeQuery()).thenReturn(rsFind);
        java.sql.PreparedStatement psInsert = mock(java.sql.PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(conUpsert.prepareStatement(argThat(sql -> sql != null && sql.startsWith("SELECT TOP 1")))).thenReturn(psFind);
        when(conUpsert.prepareStatement(argThat(sql -> sql != null && sql.startsWith("\n                INSERT INTO RefundInspections")))).thenReturn(psInsert);

        // 3) notify partner -> first query returns no partner so returns early
        java.sql.Connection conNotify = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psFindPartner = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFindPartner = mock(java.sql.ResultSet.class);
        when(rsFindPartner.next()).thenReturn(false);
        when(psFindPartner.executeQuery()).thenReturn(rsFindPartner);
        when(conNotify.prepareStatement(anyString())).thenReturn(psFindPartner);

        // Static mocking sequence: return different connections per call
        try (MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection)
                    .thenReturn(conAdmin)   // findAdminIdByAccountId
                    .thenReturn(conUpsert)  // upsertInspectionOnReturn
                    .thenReturn(conNotify); // notifyPartnerOrderCompleted

            service.IOrderManageService orderSvc = mock(service.IOrderManageService.class);
            controller.testsupport.TestUtils.forceSet(servlet, "orderService", orderSvc);
            when(orderSvc.confirmOrderReturn(10, 123)).thenReturn(true);

            model.Account admin = new model.Account(); admin.setRole("admin"); admin.setAccountId(1);
            when(req.getSession()).thenReturn(session);
            when(session.getAttribute("account")).thenReturn(admin);
            when(req.getParameter("orderId")).thenReturn("10");
            when(req.getParameter("actionType")).thenReturn("normal_return");
            when(req.getContextPath()).thenReturn("/ctx");

            servlet.doPost(req, resp);

            verify(session).setAttribute(eq("flash"), anyString());
            verify(resp).sendRedirect("/ctx/adminreturn");
        }
    }

    @Test
    @DisplayName("safeParseLong covers null/blank/invalid/valid")
    void safeParseLong_variants() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        java.lang.reflect.Method m = AdminReturnServlet.class.getDeclaredMethod("safeParseLong", String.class, long.class);
        m.setAccessible(true);
        org.assertj.core.api.Assertions.assertThat((Long) m.invoke(servlet, null, 5L)).isEqualTo(5L);
        org.assertj.core.api.Assertions.assertThat((Long) m.invoke(servlet, " ", 7L)).isEqualTo(7L);
        org.assertj.core.api.Assertions.assertThat((Long) m.invoke(servlet, "xyz", 9L)).isEqualTo(9L);
        org.assertj.core.api.Assertions.assertThat((Long) m.invoke(servlet, "42", 0L)).isEqualTo(42L);
    }

    @Test
    @DisplayName("tableExists returns true/false with mocked Connection")
    void tableExists_mocked_connection() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        java.lang.reflect.Method m = AdminReturnServlet.class.getDeclaredMethod("tableExists", java.sql.Connection.class, String.class);
        m.setAccessible(true);

        // true branch
        java.sql.ResultSet rsTrue = mock(java.sql.ResultSet.class);
        when(rsTrue.next()).thenReturn(true);
        java.sql.PreparedStatement psTrue = mock(java.sql.PreparedStatement.class);
        when(psTrue.executeQuery()).thenReturn(rsTrue);
        java.sql.Connection conTrue = mock(java.sql.Connection.class);
        when(conTrue.prepareStatement(anyString())).thenReturn(psTrue);
        Object r1 = m.invoke(servlet, conTrue, "Notifications");
        org.assertj.core.api.Assertions.assertThat((Boolean) r1).isTrue();

        // false branch via SQLException
        java.sql.Connection conErr = mock(java.sql.Connection.class);
        when(conErr.prepareStatement(anyString())).thenThrow(new java.sql.SQLException("err"));
        Object r2 = m.invoke(servlet, conErr, "Notifications");
        org.assertj.core.api.Assertions.assertThat((Boolean) r2).isFalse();
    }

    @Test
    @DisplayName("notifyPartnerOrderCompleted inserts when partner found and table exists")
    void notifyPartner_inserts_when_table_exists() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        java.lang.reflect.Method m = AdminReturnServlet.class.getDeclaredMethod("notifyPartnerOrderCompleted", int.class, String.class, String.class);
        m.setAccessible(true);

        java.sql.Connection con = mock(java.sql.Connection.class);
        // find partner id
        java.sql.PreparedStatement psFind = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFind = mock(java.sql.ResultSet.class);
        when(rsFind.next()).thenReturn(true);
        when(rsFind.getInt(1)).thenReturn(123);
        when(psFind.executeQuery()).thenReturn(rsFind);

        // table exists -> true
        java.sql.PreparedStatement psTable = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsTable = mock(java.sql.ResultSet.class);
        when(rsTable.next()).thenReturn(true);
        when(psTable.executeQuery()).thenReturn(rsTable);

        // insert notification
        java.sql.PreparedStatement psInsert = mock(java.sql.PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);

        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql == null) return psFind;
            String s = sql.replaceAll("\\s+"," ").trim();
            if (s.contains("FROM RentalOrders r") && s.contains("JOIN Accounts a")) return psFind;
            if (s.startsWith("SELECT 1 FROM INFORMATION_SCHEMA.TABLES")) return psTable;
            if (s.startsWith("INSERT INTO Notifications")) return psInsert;
            return psFind;
        });

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);
            m.invoke(servlet, 10, "t", "m");
            verify(psInsert, times(1)).executeUpdate();
        }
    }

    @Test
    @DisplayName("notifyPartnerOrderCompleted returns early when table missing")
    void notifyPartner_table_missing() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        java.lang.reflect.Method m = AdminReturnServlet.class.getDeclaredMethod("notifyPartnerOrderCompleted", int.class, String.class, String.class);
        m.setAccessible(true);

        java.sql.Connection con = mock(java.sql.Connection.class);

        java.sql.PreparedStatement psFind = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFind = mock(java.sql.ResultSet.class);
        when(rsFind.next()).thenReturn(true);
        when(rsFind.getInt(1)).thenReturn(123);
        when(psFind.executeQuery()).thenReturn(rsFind);

        java.sql.PreparedStatement psTable = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsTable = mock(java.sql.ResultSet.class);
        when(rsTable.next()).thenReturn(false);
        when(psTable.executeQuery()).thenReturn(rsTable);

        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql == null) return psFind;
            String s = sql.replaceAll("\\s+"," ").trim();
            if (s.contains("FROM RentalOrders r") && s.contains("JOIN Accounts a")) return psFind;
            if (s.startsWith("SELECT 1 FROM INFORMATION_SCHEMA.TABLES")) return psTable;
            return psFind;
        });

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(con);
            m.invoke(servlet, 10, "t", "m");
            // no insert executed
            verify(con, never()).prepareStatement(org.mockito.ArgumentMatchers.startsWith("\n                INSERT INTO Notifications"));
        }
    }

    @Test
    @DisplayName("upsertInspectionOnReturn insert and update branches")
    void upsertInspection_branches() throws Exception {
        AdminReturnServlet servlet = new AdminReturnServlet();
        java.lang.reflect.Method m = AdminReturnServlet.class.getDeclaredMethod("upsertInspectionOnReturn", int.class, int.class, String.class, long.class);
        m.setAccessible(true);

        // Insert branch (no existing)
        java.sql.Connection conInsert = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psFind = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFind = mock(java.sql.ResultSet.class);
        when(rsFind.next()).thenReturn(false);
        when(psFind.executeQuery()).thenReturn(rsFind);
        java.sql.PreparedStatement psInsert = mock(java.sql.PreparedStatement.class);
        when(psInsert.executeUpdate()).thenReturn(1);
        when(conInsert.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql == null) return psFind;
            String s = sql.replaceAll("\\s+"," ").trim();
            if (s.startsWith("SELECT TOP 1") && s.contains("FROM RefundInspections")) return psFind;
            if (s.startsWith("INSERT INTO RefundInspections")) return psInsert;
            return psFind;
        });

        try (org.mockito.MockedStatic<utils.DBConnection> mocked = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked.when(utils.DBConnection::getConnection).thenReturn(conInsert);
            m.invoke(servlet, 1, 2, "note", 0L);
            verify(psInsert, times(1)).executeUpdate();
        }

        // Update branch (existing found)
        java.sql.Connection conUpdate = mock(java.sql.Connection.class);
        java.sql.PreparedStatement psFind2 = mock(java.sql.PreparedStatement.class);
        java.sql.ResultSet rsFind2 = mock(java.sql.ResultSet.class);
        when(rsFind2.next()).thenReturn(true);
        when(rsFind2.getInt(1)).thenReturn(55);
        when(psFind2.executeQuery()).thenReturn(rsFind2);
        java.sql.PreparedStatement psUpdate = mock(java.sql.PreparedStatement.class);
        when(psUpdate.executeUpdate()).thenReturn(1);
        when(conUpdate.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            if (sql == null) return psFind2;
            String s = sql.replaceAll("\\s+"," ").trim();
            if (s.startsWith("SELECT TOP 1") && s.contains("FROM RefundInspections")) return psFind2;
            if (s.startsWith("UPDATE RefundInspections")) return psUpdate;
            return psFind2;
        });

        try (org.mockito.MockedStatic<utils.DBConnection> mocked2 = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
            mocked2.when(utils.DBConnection::getConnection).thenReturn(conUpdate);
            m.invoke(servlet, 1, 2, "note", 100L);
            verify(psUpdate, times(1)).executeUpdate();
        }
    }
}
