package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/adminreturns")
public class AdminReturnsServlet extends HttpServlet {

    public static class RefundOrderVM {

        private int orderId;
        private String customerName;
        private String customerPhone;
        private String bikeName;
        private java.util.Date endDate;
        private BigDecimal depositAmount;
        private String returnStatus;
        private java.util.Date returnedAt;
        private String depositStatus;

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public String getBikeName() {
            return bikeName;
        }

        public void setBikeName(String bikeName) {
            this.bikeName = bikeName;
        }

        public java.util.Date getEndDate() {
            return endDate;
        }

        public void setEndDate(java.util.Date endDate) {
            this.endDate = endDate;
        }

        public BigDecimal getDepositAmount() {
            return depositAmount;
        }

        public void setDepositAmount(BigDecimal depositAmount) {
            this.depositAmount = depositAmount;
        }

        public String getReturnStatus() {
            return returnStatus;
        }

        public void setReturnStatus(String returnStatus) {
            this.returnStatus = returnStatus;
        }

        public java.util.Date getReturnedAt() {
            return returnedAt;
        }

        public void setReturnedAt(java.util.Date returnedAt) {
            this.returnedAt = returnedAt;
        }

        public String getDepositStatus() {
            return depositStatus;
        }

        public void setDepositStatus(String depositStatus) {
            this.depositStatus = depositStatus;
        }
    }

    public static class RefundRequestVM {

        private int inspectionId;
        private int orderId;
        private String customerName;
        private String customerPhone;
        private String bikeName;
        private BigDecimal refundAmount;
        private BigDecimal depositAmount;
        private Timestamp inspectedAt;
        private String status;
        private String refundMethod;
        private String bikeCondition;
        private BigDecimal damageFee;

        public int getInspectionId() {
            return inspectionId;
        }

        public void setInspectionId(int inspectionId) {
            this.inspectionId = inspectionId;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerPhone() {
            return customerPhone;
        }

        public void setCustomerPhone(String customerPhone) {
            this.customerPhone = customerPhone;
        }

        public String getBikeName() {
            return bikeName;
        }

        public void setBikeName(String bikeName) {
            this.bikeName = bikeName;
        }

        public BigDecimal getRefundAmount() {
            return refundAmount;
        }

        public void setRefundAmount(BigDecimal refundAmount) {
            this.refundAmount = refundAmount;
        }

        public BigDecimal getDepositAmount() {
            return depositAmount;
        }

        public void setDepositAmount(BigDecimal depositAmount) {
            this.depositAmount = depositAmount;
        }

        public Timestamp getInspectedAt() {
            return inspectedAt;
        }

        public void setInspectedAt(Timestamp inspectedAt) {
            this.inspectedAt = inspectedAt;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getRefundMethod() {
            return refundMethod;
        }

        public void setRefundMethod(String refundMethod) {
            this.refundMethod = refundMethod;
        }

        public String getBikeCondition() {
            return bikeCondition;
        }

        public void setBikeCondition(String bikeCondition) {
            this.bikeCondition = bikeCondition;
        }

        public BigDecimal getDamageFee() {
            return damageFee;
        }

        public void setDamageFee(BigDecimal damageFee) {
            this.damageFee = damageFee;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        List<RefundOrderVM> refundOrders = new ArrayList<>();
        List<RefundRequestVM> refundRequests = new ArrayList<>();

        dbg("=== GET /adminreturns - START ===");

        try (Connection con = DBConnection.getConnection()) {
            dbg("DB connected successfully");

            // DEBUG: Kiểm tra tất cả đơn hàng đã trả
            dbg("=== DEBUG: All returned orders with inspections ===");
            String debugAllReturned = """
                SELECT 
                    ro.order_id,
                    ro.return_status,
                    ro.deposit_status,
                    ro.deposit_amount,
                    ro.returned_at,
                    ri.inspection_id,
                    ri.inspected_at,
                    ri.refund_status,
                    ri.refund_amount,
                    ri.damage_fee
                FROM RentalOrders ro
                LEFT JOIN RefundInspections ri ON ri.order_id = ro.order_id
                WHERE ro.return_status = 'returned'
                ORDER BY ro.returned_at DESC
            """;

            try (PreparedStatement ps = con.prepareStatement(debugAllReturned); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dbg(String.format(
                            "Order: ID=%d, ReturnStatus=%s, DepositStatus=%s, DepositAmount=%s, ReturnedAt=%s, "
                            + "InspectionID=%s, InspectedAt=%s, RefundStatus=%s, RefundAmount=%s, DamageFee=%s",
                            rs.getInt("order_id"),
                            rs.getString("return_status"),
                            rs.getString("deposit_status"),
                            rs.getBigDecimal("deposit_amount"),
                            rs.getTimestamp("returned_at"),
                            rs.getObject("inspection_id"),
                            rs.getTimestamp("inspected_at"),
                            rs.getString("refund_status"),
                            rs.getBigDecimal("refund_amount"),
                            rs.getBigDecimal("damage_fee")
                    ));
                }
            }

            // A) ĐƠN CHỜ KIỂM TRA - FIXED QUERY
            final String sqlRefundOrders = """
                SELECT 
                    ro.order_id,
                    c.full_name       AS customer_name,
                    c.phone           AS customer_phone,
                    b.bike_name,
                    ro.end_date,
                    ro.deposit_amount,
                    ro.return_status,
                    ro.returned_at,
                    ro.deposit_status,
                    rix.inspection_id AS last_inspection_id,
                    rix.inspected_at  AS last_inspected_at,
                    rix.refund_status AS last_refund_status
                FROM RentalOrders ro
                JOIN Customers   c  ON c.customer_id = ro.customer_id
                JOIN OrderDetails od ON od.order_id   = ro.order_id
                JOIN Motorbikes  b  ON b.bike_id      = od.bike_id
                OUTER APPLY (
                    SELECT TOP 1 ri.inspection_id, ri.inspected_at, ri.refund_status
                    FROM RefundInspections ri
                    WHERE ri.order_id = ro.order_id
                    ORDER BY ri.inspected_at DESC, ri.inspection_id DESC
                ) rix
                WHERE ro.return_status = 'returned'
                  AND ro.deposit_status = 'held'
                  AND (rix.inspection_id IS NULL OR rix.inspected_at IS NULL 
                       OR rix.refund_status IN ('pending', 'processing'))
                ORDER BY ro.returned_at DESC
            """;

            dbg("=== Executing refundOrders query ===");
            try (PreparedStatement ps = con.prepareStatement(sqlRefundOrders); ResultSet rs = ps.executeQuery()) {
                int row = 0;
                while (rs.next()) {
                    RefundOrderVM r = new RefundOrderVM();
                    r.setOrderId(rs.getInt("order_id"));
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setCustomerPhone(rs.getString("customer_phone"));
                    r.setBikeName(rs.getString("bike_name"));

                    Date endDate = rs.getDate("end_date");
                    if (endDate != null) {
                        r.setEndDate(new java.util.Date(endDate.getTime()));
                    }

                    r.setDepositAmount(rs.getBigDecimal("deposit_amount"));
                    r.setReturnStatus(rs.getString("return_status"));

                    Timestamp returned = rs.getTimestamp("returned_at");
                    if (returned != null) {
                        r.setReturnedAt(new java.util.Date(returned.getTime()));
                    }

                    r.setDepositStatus(rs.getString("deposit_status"));
                    refundOrders.add(r);

                    row++;
                    dbg(String.format(
                            "[ORDERS#%d] orderId=%d, lastInspectionId=%s, lastInspectedAt=%s, refundStatus=%s",
                            row,
                            r.getOrderId(),
                            rs.getObject("last_inspection_id"),
                            rs.getObject("last_inspected_at"),
                            rs.getString("last_refund_status")
                    ));
                }
            }
            dbg("Pending-inspection orders count = " + refundOrders.size());

            // B) YÊU CẦU HOÀN CỌC - FIXED QUERY
            final String sqlRefundRequests = """
                SELECT 
                    ri.inspection_id,
                    ro.order_id,
                    c.full_name     AS customer_name,
                    c.phone         AS customer_phone,
                    b.bike_name,
                    ri.refund_amount,
                    ro.deposit_amount,
                    ri.inspected_at,
                    ri.refund_status,
                    ri.refund_method,
                    ri.bike_condition,
                    ri.damage_fee
                FROM RefundInspections ri
                JOIN RentalOrders ro ON ro.order_id = ri.order_id
                JOIN Customers    c  ON ro.customer_id = c.customer_id
                JOIN OrderDetails od ON od.order_id    = ro.order_id
                JOIN Motorbikes   b  ON b.bike_id      = od.bike_id
                WHERE ri.refund_status IN ('pending','processing')
                  AND ri.inspected_at IS NOT NULL
                ORDER BY 
                    CASE 
                        WHEN ri.refund_status = 'pending'    THEN 1
                        WHEN ri.refund_status = 'processing' THEN 2
                        ELSE 3
                    END,
                    ri.inspected_at DESC
            """;

            dbg("=== Executing refundRequests query ===");
            try (PreparedStatement ps = con.prepareStatement(sqlRefundRequests); ResultSet rs = ps.executeQuery()) {
                int row = 0;
                while (rs.next()) {
                    RefundRequestVM r = new RefundRequestVM();
                    r.setInspectionId(rs.getInt("inspection_id"));
                    r.setOrderId(rs.getInt("order_id"));
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setCustomerPhone(rs.getString("customer_phone"));
                    r.setBikeName(rs.getString("bike_name"));
                    r.setRefundAmount(rs.getBigDecimal("refund_amount"));
                    r.setDepositAmount(rs.getBigDecimal("deposit_amount"));
                    r.setInspectedAt(rs.getTimestamp("inspected_at"));
                    r.setStatus(rs.getString("refund_status"));
                    r.setRefundMethod(rs.getString("refund_method"));
                    r.setBikeCondition(rs.getString("bike_condition"));
                    r.setDamageFee(rs.getBigDecimal("damage_fee"));
                    refundRequests.add(r);

                    row++;
                    dbg(String.format(
                            "[REQUESTS#%d] inspectionId=%d, orderId=%d, status=%s, refundAmount=%s, inspectedAt=%s",
                            row, r.getInspectionId(), r.getOrderId(), r.getStatus(),
                            r.getRefundAmount(), r.getInspectedAt()
                    ));
                }
            }
            dbg("Refund requests (pending/processing) count = " + refundRequests.size());

        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException("Không thể tải dữ liệu hoàn cọc", e);
        }

        // Tổng tiền đang chờ
        BigDecimal totalPendingAmount = refundRequests.stream()
                .map(x -> x.getRefundAmount() == null ? BigDecimal.ZERO : x.getRefundAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        req.setAttribute("refundOrders", refundOrders);
        req.setAttribute("refundRequests", refundRequests);
        req.setAttribute("totalPendingAmount", totalPendingAmount);

        dbg("=== GET /adminreturns - COMPLETE ===");
        req.getRequestDispatcher("/admin/admin-returns.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String orderIdRaw = req.getParameter("orderId");
        String inspectionIdRaw = req.getParameter("inspectionId");
        String refundMethod = req.getParameter("refundMethod");

        dbg("=== POST /adminreturns === action=" + action
                + ", orderId=" + orderIdRaw
                + ", inspectionId=" + inspectionIdRaw
                + ", method=" + refundMethod);

        HttpSession session = req.getSession();

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            boolean ok = false;
            String msg = "❌ Hành động không hợp lệ";

            if ("mark_processing".equals(action)) {
                ok = markAsProcessing(con, inspectionIdRaw);
                msg = ok ? "✅ Đã duyệt yêu cầu hoàn cọc" : "❌ Không tìm thấy yêu cầu để duyệt";

            } else if ("complete_refund".equals(action)) {
                if (orderIdRaw != null && refundMethod != null) {
                    ok = completeRefund(con, orderIdRaw, refundMethod);
                    msg = ok ? "✅ Đã hoàn tất hoàn cọc" : "❌ Không thể hoàn tất hoàn cọc";
                }

            } else if ("cancel".equals(action)) {
                ok = cancelRefund(con, inspectionIdRaw);
                msg = ok ? "⛔ Đã từ chối yêu cầu hoàn cọc" : "❌ Không tìm thấy yêu cầu để từ chối";
            }

            if (ok) {
                con.commit();
                dbg("TRANSACTION COMMIT");
            } else {
                con.rollback();
                dbg("TRANSACTION ROLLBACK");
            }
            session.setAttribute("flash", msg);

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flash", "❌ Lỗi khi xử lý: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/adminreturns");
    }

    private boolean markAsProcessing(Connection con, String inspectionIdRaw) throws SQLException {
        if (inspectionIdRaw == null || inspectionIdRaw.isBlank()) {
            return false;
        }
        final String sql = """
            UPDATE RefundInspections
               SET refund_status='processing', updated_at=GETDATE()
             WHERE inspection_id=? AND refund_status='pending'
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(inspectionIdRaw.trim()));
            int n = ps.executeUpdate();
            dbg("markAsProcessing -> rows=" + n + " (inspectionId=" + inspectionIdRaw + ")");
            return n > 0;
        }
    }

    private boolean completeRefund(Connection con, String orderIdRaw, String refundMethod) throws SQLException {
        if (orderIdRaw == null || orderIdRaw.isBlank()) {
            return false;
        }
        int orderId = Integer.parseInt(orderIdRaw.trim());
        String method = "cash".equalsIgnoreCase(refundMethod) ? "cash" : "wallet";

        dbg("Starting completeRefund for order " + orderId + " with method " + method);

        // 1️⃣ Lấy dữ liệu cần thiết: customer_id + refund_amount
        BigDecimal refundAmount = BigDecimal.ZERO;
        Integer customerId = null;
        try (PreparedStatement ps = con.prepareStatement("""
        SELECT TOP 1 ri.refund_amount, ro.customer_id
          FROM RefundInspections ri
          JOIN RentalOrders ro ON ro.order_id = ri.order_id
         WHERE ri.order_id=? AND ri.refund_status IN ('pending','processing')
         ORDER BY ri.inspected_at DESC
    """)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    refundAmount = rs.getBigDecimal("refund_amount");
                    customerId = rs.getInt("customer_id");
                }
            }
        }

        if (customerId == null) {
            dbg("❌ Không tìm thấy inspection phù hợp cho order " + orderId);
            return false;
        }
        if (refundAmount == null) {
            refundAmount = BigDecimal.ZERO;
        }

        // 2️⃣ Cập nhật RefundInspections -> completed + phương thức hoàn
        try (PreparedStatement ps = con.prepareStatement("""
        UPDATE RefundInspections
           SET refund_status='completed',
               refund_method=?,
               updated_at=GETDATE()
         WHERE order_id=? AND refund_status IN ('pending','processing')
    """)) {
            ps.setString(1, method);
            ps.setInt(2, orderId);
            int n = ps.executeUpdate();
            dbg("completeRefund: update inspection -> rows=" + n + ", method=" + method);
            if (n == 0) {
                return false;
            }
        }

        // 3️⃣ Cập nhật trạng thái cọc
        try (PreparedStatement ps = con.prepareStatement("""
        UPDATE RentalOrders
           SET deposit_status='refunded'
         WHERE order_id=?
    """)) {
            ps.setInt(1, orderId);
            int n = ps.executeUpdate();
            dbg("completeRefund: update order.deposit_status -> rows=" + n);
        }

        // 4️⃣ Nếu hoàn ví -> cộng vào Wallets + ghi giao dịch
        if ("wallet".equalsIgnoreCase(method)) {
            dbg("→ Processing wallet refund for customerId=" + customerId);
            Integer walletId = null;

            // 4a) Kiểm tra ví
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT wallet_id FROM Wallets WHERE customer_id=?")) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        walletId = rs.getInt(1);
                    }
                }
            }

            // 4b) Tạo ví nếu chưa có
            if (walletId == null) {
                try (PreparedStatement ps = con.prepareStatement("""
                INSERT INTO Wallets(customer_id, balance, created_at, updated_at)
                VALUES (?, 0, GETDATE(), GETDATE());
                SELECT SCOPE_IDENTITY();
            """)) {
                    ps.setInt(1, customerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            walletId = rs.getInt(1);
                        }
                    }
                }
                dbg("Created new wallet: id=" + walletId);
            }

            // 4c) Cộng tiền
            try (PreparedStatement ps = con.prepareStatement("""
            UPDATE Wallets
               SET balance = balance + ?, updated_at=GETDATE()
             WHERE wallet_id=?
        """)) {
                ps.setBigDecimal(1, refundAmount);
                ps.setInt(2, walletId);
                ps.executeUpdate();
            }

            // 4d) Ghi log giao dịch ví
            try (PreparedStatement ps = con.prepareStatement("""
            INSERT INTO Wallet_Transactions(wallet_id, amount, type, order_id, description, created_at)
            VALUES (?, ?, 'refund', ?, ?, GETDATE())
        """)) {
                ps.setInt(1, walletId);
                ps.setBigDecimal(2, refundAmount);
                ps.setInt(3, orderId);
                ps.setString(4, "Hoàn tiền cọc đơn #" + orderId);
                ps.executeUpdate();
            }

            dbg("✅ Ví đã được hoàn tiền thành công");
        }

        // 5️⃣ Ghi log vào bảng Payments (nếu có)
        try (PreparedStatement ps = con.prepareStatement("""
        INSERT INTO Payments(order_id, amount, method, status, payment_date)
        VALUES (?, ?, ?, 'refunded', GETDATE())
    """)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, refundAmount);
            // Map method để khớp CHECK constraint CK_Payments_Method
            ps.setString(3, "wallet".equalsIgnoreCase(method) ? "cash" : method);
            ps.executeUpdate();
            dbg("✅ Ghi log hoàn tiền vào Payments thành công");
        } catch (SQLException ex) {
            dbg("⚠️ Bỏ qua ghi log Payments (bảng có thể chưa tồn tại): " + ex.getMessage());
        }

        // 6️⃣ Cập nhật trạng thái đơn cuối
        maybeUpdateOrderCompleted(con, orderId);

        dbg("✅ Hoàn tất completeRefund cho orderId=" + orderId);
        return true;
    }

    private boolean cancelRefund(Connection con, String inspectionIdRaw) throws SQLException {
        if (inspectionIdRaw == null || inspectionIdRaw.isBlank()) {
            return false;
        }
        final String sql = """
            UPDATE RefundInspections
               SET refund_status='cancelled', updated_at=GETDATE()
             WHERE inspection_id=? AND refund_status IN ('pending','processing')
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(inspectionIdRaw.trim()));
            int n = ps.executeUpdate();
            dbg("cancelRefund -> rows=" + n + " (inspectionId=" + inspectionIdRaw + ")");
            return n > 0;
        }
    }

    private boolean updateWalletBalance(Connection con, int orderId) throws SQLException {
        dbg("=== WALLET UPDATE START (orderId=" + orderId + ") ===");
//        final String q = """
//            SELECT ri.refund_amount, ro.customer_id
//              FROM RefundInspections ri
//              JOIN RentalOrders ro ON ro.order_id = ri.order_id
//             WHERE ri.order_id=? AND ri.refund_status='completed'
//        """;
        final String q = """
                    SELECT TOP 1 ri.refund_amount, ro.customer_id
                    FROM RefundInspections ri
                    JOIN RentalOrders ro ON ro.order_id = ri.order_id
                    WHERE ri.order_id=? AND ri.refund_status='completed'
                    ORDER BY ri.inspected_at DESC
                """;
        BigDecimal refundAmount = null;
        Integer customerId = null;

        try (PreparedStatement ps = con.prepareStatement(q)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    refundAmount = rs.getBigDecimal("refund_amount");
                    customerId = rs.getInt("customer_id");
                }
            }
        }

        dbg("Wallet data -> refundAmount=" + refundAmount + ", customerId=" + customerId);
        if (refundAmount == null || customerId == null) {
            dbg("ERROR: No refund amount or customerId found");
            return false;
        }

        // Kiểm tra xem customer có ví chưa, nếu chưa thì tạo mới
        Integer walletId = null;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT wallet_id FROM Wallets WHERE customer_id=?")) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    walletId = rs.getInt(1);
                }
            }
        }

        if (walletId == null) {
            final String create = """
                INSERT INTO Wallets(customer_id, balance, created_at, updated_at)
                VALUES (?, 0, GETDATE(), GETDATE());
                SELECT SCOPE_IDENTITY();
            """;
            try (PreparedStatement ps = con.prepareStatement(create)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        walletId = rs.getInt(1);
                    }
                }
            }
            dbg("Created new wallet with walletId=" + walletId);
        } else {
            dbg("Found existing wallet with walletId=" + walletId);
        }

        // Cập nhật số dư ví
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE Wallets SET balance = balance + ?, updated_at=GETDATE() WHERE wallet_id=?")) {
            ps.setBigDecimal(1, refundAmount);
            ps.setInt(2, walletId);
            int n = ps.executeUpdate();
            dbg("wallet balance updated -> rows=" + n);
        }

        // Ghi log giao dịch
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Wallet_Transactions(wallet_id, amount, type, order_id, description, created_at) "
                + "VALUES (?, ?, 'refund', ?, ?, GETDATE())")) {
            ps.setInt(1, walletId);
            ps.setBigDecimal(2, refundAmount);
            ps.setInt(3, orderId);
            ps.setString(4, "Hoàn tiền cọc đơn #" + orderId);
            int n = ps.executeUpdate();
            dbg("wallet transaction inserted -> rows=" + n);
        }

        dbg("=== WALLET UPDATE SUCCESS ===");
        return true;
    }

    private void maybeUpdateOrderCompleted(Connection con, int orderId) throws SQLException {
        String sql = """
        UPDATE RentalOrders
           SET status = 'completed'
         WHERE order_id = ?
           AND deposit_status = 'refunded'
           AND return_status IN ('verified', 'returned')
           AND status <> 'completed'
    """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            int n = ps.executeUpdate();
            dbg("maybeUpdateOrderCompleted: cập nhật trạng thái completed cho order_id=" + orderId + " -> rows=" + n);
        }
    }

    private boolean tableExists(Connection con, String name) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean tableHasColumn(Connection con, String table, String column) {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME=? AND COLUMN_NAME=?")) {
            ps.setString(1, table);
            ps.setString(2, column);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private static void dbg(String msg) {
        System.out.println("[AdminReturnsServlet] " + msg);
    }
}
