//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import utils.DBConnection;
//
//import java.math.BigDecimal;
//import java.sql.*;
//import java.util.List;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminReturnsServletTest {
//
//    @BeforeAll
//    static void initEnv() {
//        Locale.setDefault(Locale.US);
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Mock private HttpServletRequest req;
//    @Mock private HttpServletResponse resp;
//    @Mock private HttpSession session;
//    @Mock private Connection con;
//    @Mock private PreparedStatement ps1;
//    @Mock private PreparedStatement ps2;
//    @Mock private PreparedStatement ps3;
//    @Mock private ResultSet rs1;
//    @Mock private ResultSet rs2;
//    @Mock private ResultSet rs3;
//
//    @Captor private ArgumentCaptor<List<AdminReturnsServlet.RefundOrderVM>> refundOrdersCaptor;
//    @Captor private ArgumentCaptor<List<AdminReturnsServlet.RefundRequestVM>> refundRequestsCaptor;
//    @Captor private ArgumentCaptor<BigDecimal> totalPendingAmountCaptor;
//
//    private AdminReturnsServlet servlet;
//
//    @Test
//    void doGet_WithValidData_ShouldSetAttributesAndForward() throws Exception {
//        // Setup
//        setupCommonMocks();
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            setupResultSetsWithData();
//
//            // Execute
//            new AdminReturnsServlet().doGet(req, resp);
//
//            // Verify
//            verify(req).setAttribute(eq("refundOrders"), refundOrdersCaptor.capture());
//            verify(req).setAttribute(eq("refundRequests"), refundRequestsCaptor.capture());
//            verify(req).setAttribute(eq("totalPendingAmount"), totalPendingAmountCaptor.capture());
//
//            // Assert
//            List<AdminReturnsServlet.RefundOrderVM> refundOrders = refundOrdersCaptor.getValue();
//            List<AdminReturnsServlet.RefundRequestVM> refundRequests = refundRequestsCaptor.getValue();
//            BigDecimal totalPendingAmount = totalPendingAmountCaptor.getValue();
//
//            assertThat(refundOrders).hasSize(1);
//            assertThat(refundRequests).hasSize(1);
//            assertThat(totalPendingAmount).isEqualByComparingTo("30.00");
//
//            AdminReturnsServlet.RefundOrderVM order = refundOrders.get(0);
//            assertThat(order.getOrderId()).isEqualTo(11);
//            assertThat(order.getCustomerName()).isEqualTo("Alice");
//
//            AdminReturnsServlet.RefundRequestVM request = refundRequests.get(0);
//            assertThat(request.getInspectionId()).isEqualTo(99);
//            assertThat(request.getRefundAmount()).isEqualByComparingTo("30.00");
//
//            verify(rd).forward(req, resp);
//        }
//    }
//
//    @Test
//    void doGet_WithEmptyData_ShouldSetEmptyAttributes() throws Exception {
//        // Setup
//        setupCommonMocks();
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            setupEmptyResultSets();
//
//            // Execute
//            new AdminReturnsServlet().doGet(req, resp);
//
//            // Verify
//            verify(req).setAttribute(eq("refundOrders"), refundOrdersCaptor.capture());
//            verify(req).setAttribute(eq("refundRequests"), refundRequestsCaptor.capture());
//            verify(req).setAttribute(eq("totalPendingAmount"), totalPendingAmountCaptor.capture());
//
//            // Assert
//            assertThat(refundOrdersCaptor.getValue()).isEmpty();
//            assertThat(refundRequestsCaptor.getValue()).isEmpty();
//            assertThat(totalPendingAmountCaptor.getValue()).isEqualByComparingTo(BigDecimal.ZERO);
//            verify(rd).forward(req, resp);
//        }
//    }
//
//    @Test
//    void doGet_WithDatabaseException_ShouldThrowServletException() throws Exception {
//        // Setup
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenThrow(new SQLException("Database connection failed"));
//
//            // Execute & Verify
//            Assertions.assertThatThrownBy(() -> new AdminReturnsServlet().doGet(req, resp))
//                    .isInstanceOf(jakarta.servlet.ServletException.class)
//                    .hasMessageContaining("Không thể tải dữ liệu hoàn cọc")
//                    .hasCauseInstanceOf(SQLException.class);
//        }
//    }
//
//    @Test
//    void doPost_MarkAsProcessing_ShouldUpdateStatusAndRedirect() throws Exception {
//        // Setup
//        setupPostMocks("mark_processing", "11", "99", null);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            setupUpdateSuccess();
//
//            // Execute
//            new AdminReturnsServlet().doPost(req, resp);
//
//            // Verify
//            verify(session).setAttribute("flash", "✅ Đã duyệt yêu cầu hoàn cọc");
//            verify(resp).sendRedirect("/ctx/adminreturns");
//            verify(con).commit();
//        }
//    }
//
//    @Test
//    void doPost_CompleteRefund_ShouldProcessRefundAndRedirect() throws Exception {
//        // Setup
//        setupPostMocks("complete_refund", "11", null, "wallet");
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            setupCompleteRefundSuccess();
//
//            // Execute
//            new AdminReturnsServlet().doPost(req, resp);
//
//            // Verify
//            verify(session).setAttribute("flash", "✅ Đã hoàn tất hoàn cọc");
//            verify(resp).sendRedirect("/ctx/adminreturns");
//            verify(con).commit();
//        }
//    }
//
//    @Test
//    void doPost_CancelRefund_ShouldCancelAndRedirect() throws Exception {
//        // Setup
//        setupPostMocks("cancel", "11", "99", null);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            setupUpdateSuccess();
//
//            // Execute
//            new AdminReturnsServlet().doPost(req, resp);
//
//            // Verify
//            verify(session).setAttribute("flash", "⛔ Đã từ chối yêu cầu hoàn cọc");
//            verify(resp).sendRedirect("/ctx/adminreturns");
//            verify(con).commit();
//        }
//    }
//
//    @Test
//    void doPost_WithInvalidAction_ShouldShowError() throws Exception {
//        // Setup
//        setupPostMocks("invalid_action", "11", "99", null);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//
//            // Execute
//            new AdminReturnsServlet().doPost(req, resp);
//
//            // Verify
//            verify(session).setAttribute("flash", "❌ Hành động không hợp lệ");
//            verify(resp).sendRedirect("/ctx/adminreturns");
//            verify(con).rollback();
//        }
//    }
//
//    @Test
//    void doPost_WithDatabaseException_ShouldShowError() throws Exception {
//        // Setup
//        setupPostMocks("mark_processing", "11", "99", null);
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenThrow(new SQLException("DB error"));
//
//            // Execute
//            new AdminReturnsServlet().doPost(req, resp);
//
//            // Verify
//            verify(session).setAttribute(eq("flash"), contains("❌ Lỗi khi xử lý"));
//            verify(resp).sendRedirect("/ctx/adminreturns");
//        }
//    }
//
//    // Helper methods
//    private void setupCommonMocks() {
//        when(req.getContextPath()).thenReturn("/ctx");
//    }
//
//    private void setupPostMocks(String action, String orderId, String inspectionId, String refundMethod) {
//        when(req.getParameter("action")).thenReturn(action);
//        when(req.getParameter("orderId")).thenReturn(orderId);
//        when(req.getParameter("inspectionId")).thenReturn(inspectionId);
//        when(req.getParameter("refundMethod")).thenReturn(refundMethod);
//        when(req.getSession()).thenReturn(session);
//        when(req.getContextPath()).thenReturn("/ctx");
//    }
//
//    private void setupResultSetsWithData() throws SQLException {
//        when(con.prepareStatement(anyString())).thenReturn(ps1, ps2, ps3);
//
//        // Debug query
//        when(ps1.executeQuery()).thenReturn(rs1);
//        when(rs1.next()).thenReturn(false);
//
//        // Refund orders
//        when(ps2.executeQuery()).thenReturn(rs2);
//        when(rs2.next()).thenReturn(true, false);
//        setupRefundOrderResultSet(rs2);
//
//        // Refund requests
//        when(ps3.executeQuery()).thenReturn(rs3);
//        when(rs3.next()).thenReturn(true, false);
//        setupRefundRequestResultSet(rs3);
//    }
//
//    private void setupEmptyResultSets() throws SQLException {
//        when(con.prepareStatement(anyString())).thenReturn(ps1, ps2, ps3);
//
//        when(ps1.executeQuery()).thenReturn(rs1);
//        when(rs1.next()).thenReturn(false);
//
//        when(ps2.executeQuery()).thenReturn(rs2);
//        when(rs2.next()).thenReturn(false);
//
//        when(ps3.executeQuery()).thenReturn(rs3);
//        when(rs3.next()).thenReturn(false);
//    }
//
//    private void setupRefundOrderResultSet(ResultSet rs) throws SQLException {
//        when(rs.getInt("order_id")).thenReturn(11);
//        when(rs.getString("customer_name")).thenReturn("Alice");
//        when(rs.getString("customer_phone")).thenReturn("0123");
//        when(rs.getString("bike_name")).thenReturn("Honda");
//        when(rs.getDate("end_date")).thenReturn(new java.sql.Date(0));
//        when(rs.getBigDecimal("deposit_amount")).thenReturn(new BigDecimal("100.00"));
//        when(rs.getString("return_status")).thenReturn("returned");
//        when(rs.getTimestamp("returned_at")).thenReturn(new Timestamp(0));
//        when(rs.getString("deposit_status")).thenReturn("held");
//    }
//
//    private void setupRefundRequestResultSet(ResultSet rs) throws SQLException {
//        when(rs.getInt("inspection_id")).thenReturn(99);
//        when(rs.getInt("order_id")).thenReturn(11);
//        when(rs.getString("customer_name")).thenReturn("Alice");
//        when(rs.getString("customer_phone")).thenReturn("0123");
//        when(rs.getString("bike_name")).thenReturn("Honda");
//        when(rs.getBigDecimal("refund_amount")).thenReturn(new BigDecimal("30.00"));
//        when(rs.getBigDecimal("deposit_amount")).thenReturn(new BigDecimal("100.00"));
//        when(rs.getTimestamp("inspected_at")).thenReturn(new Timestamp(0));
//        when(rs.getString("refund_status")).thenReturn("pending");
//        when(rs.getString("refund_method")).thenReturn("wallet");
//        when(rs.getString("bike_condition")).thenReturn("good");
//        when(rs.getBigDecimal("damage_fee")).thenReturn(new BigDecimal("0.00"));
//    }
//
//    private void setupUpdateSuccess() throws SQLException {
//        when(con.prepareStatement(anyString())).thenReturn(ps1);
//        when(ps1.executeUpdate()).thenReturn(1);
//    }
//
//    private void setupCompleteRefundSuccess() throws SQLException {
//        when(con.prepareStatement(anyString())).thenReturn(ps1, ps2, ps3, ps1, ps1);
//        when(ps1.executeUpdate()).thenReturn(1); // updInspection
//        when(ps2.executeUpdate()).thenReturn(1); // update order deposit_status
//        when(ps3.executeQuery()).thenReturn(rs1); // wallet query
//
//        when(rs1.next()).thenReturn(true);
//        when(rs1.getBigDecimal("refund_amount")).thenReturn(new BigDecimal("30.00"));
//        when(rs1.getInt("customer_id")).thenReturn(1);
//
//        // Wallet exists check
//        PreparedStatement psWallet = mock(PreparedStatement.class);
//        ResultSet rsWallet = mock(ResultSet.class);
//        when(con.prepareStatement(contains("Wallets"))).thenReturn(psWallet);
//        when(psWallet.executeQuery()).thenReturn(rsWallet);
//        when(rsWallet.next()).thenReturn(true);
//        when(rsWallet.getInt(1)).thenReturn(1);
//    }
//}