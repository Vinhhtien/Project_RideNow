package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

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
        
        // Getters/Setters
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }
        public java.util.Date getEndDate() { return endDate; }
        public void setEndDate(java.util.Date endDate) { this.endDate = endDate; }
        public BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
        public String getReturnStatus() { return returnStatus; }
        public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }
        public java.util.Date getReturnedAt() { return returnedAt; }
        public void setReturnedAt(java.util.Date returnedAt) { this.returnedAt = returnedAt; }
        public String getDepositStatus() { return depositStatus; }
        public void setDepositStatus(String depositStatus) { this.depositStatus = depositStatus; }
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
        
        // Getters/Setters
        public int getInspectionId() { return inspectionId; }
        public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public BigDecimal getDepositAmount() { return depositAmount; }
        public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
        public Timestamp getInspectedAt() { return inspectedAt; }
        public void setInspectedAt(Timestamp inspectedAt) { this.inspectedAt = inspectedAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getRefundMethod() { return refundMethod; }
        public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
        public String getBikeCondition() { return bikeCondition; }
        public void setBikeCondition(String bikeCondition) { this.bikeCondition = bikeCondition; }
        public BigDecimal getDamageFee() { return damageFee; }
        public void setDamageFee(BigDecimal damageFee) { this.damageFee = damageFee; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        List<RefundOrderVM> refundOrders = new ArrayList<>();
        List<RefundRequestVM> refundRequests = new ArrayList<>();

        System.out.println("=== DEBUG: Starting AdminReturnsServlet ===");

        try (Connection con = DBConnection.getConnection()) {
            System.out.println("✅ Database connection successful");

            // 1. Lấy đơn hàng đã trả nhưng chưa kiểm tra
            String sqlRefundOrders = """
                SELECT 
                    ro.order_id, 
                    c.full_name AS customer_name, 
                    c.phone AS customer_phone,
                    b.bike_name, 
                    ro.end_date, 
                    ro.deposit_amount, 
                    ro.return_status, 
                    ro.returned_at,
                    ro.deposit_status
                FROM RentalOrders ro
                JOIN Customers c ON c.customer_id = ro.customer_id
                JOIN OrderDetails od ON od.order_id = ro.order_id
                JOIN Motorbikes b ON b.bike_id = od.bike_id
                WHERE ro.return_status = 'returned'
                  AND ro.deposit_status = 'held'  -- QUAN TRỌNG: phải khớp
                  AND NOT EXISTS (
                      SELECT 1 FROM RefundInspections ri 
                      WHERE ri.order_id = ro.order_id
                  )
                ORDER BY ro.returned_at DESC
                """;

            System.out.println("📊 Fetching pending inspection orders...");
            try (PreparedStatement ps = con.prepareStatement(sqlRefundOrders);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    RefundOrderVM r = new RefundOrderVM();
                    r.setOrderId(rs.getInt("order_id"));
                    r.setCustomerName(rs.getString("customer_name"));
                    r.setCustomerPhone(rs.getString("customer_phone"));
                    r.setBikeName(rs.getString("bike_name"));

                    java.sql.Date sqlDate = rs.getDate("end_date");
                    if (sqlDate != null)
                        r.setEndDate(new java.util.Date(sqlDate.getTime()));

                    r.setDepositAmount(rs.getBigDecimal("deposit_amount"));
                    r.setReturnStatus(rs.getString("return_status"));
                    
                    Timestamp returned = rs.getTimestamp("returned_at");
                    if (returned != null)
                        r.setReturnedAt(new java.util.Date(returned.getTime()));
                    
                    r.setDepositStatus(rs.getString("deposit_status"));
                    
                    refundOrders.add(r);
                    System.out.println("📝 Found pending inspection order #" + r.getOrderId());
                }
                System.out.println("✅ Total pending inspection orders: " + refundOrders.size());
            }

            // 2. Lấy yêu cầu hoàn cọc đang chờ xử lý
            String sqlRefundRequests = """
                SELECT 
                    ri.inspection_id,
                    ri.order_id,
                    c.full_name AS customer_name,
                    c.phone AS customer_phone,
                    b.bike_name,
                    ri.refund_amount,
                    ro.deposit_amount,
                    ri.inspected_at,
                    ri.refund_status,
                    ri.refund_method,
                    ri.bike_condition,
                    ri.damage_fee
                FROM RefundInspections ri
                JOIN RentalOrders ro ON ri.order_id = ro.order_id
                JOIN Customers c ON ro.customer_id = c.customer_id
                JOIN OrderDetails od ON od.order_id = ro.order_id
                JOIN Motorbikes b ON b.bike_id = od.bike_id
                WHERE ri.refund_status IN ('pending', 'processing')
                ORDER BY 
                    CASE 
                        WHEN ri.refund_status = 'pending' THEN 1
                        WHEN ri.refund_status = 'processing' THEN 2
                        ELSE 3
                    END,
                    ri.inspected_at DESC
                """;

            System.out.println("📊 Fetching refund requests...");
            try (PreparedStatement ps = con.prepareStatement(sqlRefundRequests);
                 ResultSet rs = ps.executeQuery()) {
                
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
                    System.out.println("📝 Found refund request - Inspection#" + r.getInspectionId());
                }
                System.out.println("✅ Total refund requests: " + refundRequests.size());
            }

        } catch (SQLException e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            log("[AdminReturnsServlet] SQL error: " + e.getMessage());
            throw new ServletException("Không thể tải dữ liệu hoàn cọc", e);
        }

        req.setAttribute("refundOrders", refundOrders);
        req.setAttribute("refundRequests", refundRequests);
        
        req.getRequestDispatcher("/admin/admin-returns.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String action = req.getParameter("action");
        String orderIdRaw = req.getParameter("orderId");
        String inspectionIdRaw = req.getParameter("inspectionId");
        String refundMethod = req.getParameter("refundMethod");

        System.out.println("=== DEBUG: Processing POST Request ===");
        System.out.println("🔄 Action: " + action + ", OrderId: " + orderIdRaw + 
                         ", InspectionId: " + inspectionIdRaw + ", Method: " + refundMethod);

        HttpSession session = req.getSession();
        
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            String message = "";
            boolean success = false;

            switch (action) {
                case "mark_processing":
                    success = markAsProcessing(con, inspectionIdRaw);
                    message = success ? "✅ Đã duyệt yêu cầu hoàn cọc" : "❌ Không tìm thấy yêu cầu để duyệt";
                    break;
                    
                case "complete_refund":
                    if (orderIdRaw != null && refundMethod != null) {
                        success = completeRefund(con, orderIdRaw, refundMethod);
                        message = success ? "✅ Đã hoàn tất hoàn cọc" : "❌ Không thể hoàn tất hoàn cọc";
                    }
                    break;
                    
                case "cancel":
                    success = cancelRefund(con, inspectionIdRaw);
                    message = success ? "❌ Đã từ chối yêu cầu hoàn cọc" : "❌ Không tìm thấy yêu cầu để từ chối";
                    break;
                    
                default:
                    message = "❌ Hành động không hợp lệ";
                    break;
            }

            if (success) {
                con.commit();
            } else {
                con.rollback();
            }

            System.out.println("✅ Result: " + message);
            session.setAttribute("flash", message);

        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error in doPost: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("flash", "❌ Lỗi khi xử lý: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/adminreturns");
    }

    private boolean markAsProcessing(Connection con, String inspectionIdRaw) throws SQLException {
        if (inspectionIdRaw == null || inspectionIdRaw.isBlank()) return false;
        
        String sql = "UPDATE RefundInspections SET refund_status='processing' WHERE inspection_id=? AND refund_status='pending'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(inspectionIdRaw.trim()));
            return ps.executeUpdate() > 0;
        }
    }

    private boolean completeRefund(Connection con, String orderIdRaw, String refundMethod) throws SQLException {
        if (orderIdRaw == null || orderIdRaw.isBlank()) return false;
        
        int orderId = Integer.parseInt(orderIdRaw.trim());
        
        // 1. Cập nhật trạng thái inspection
        String updateInspectionSql = """
            UPDATE RefundInspections 
            SET refund_status='completed', refund_method=?
            WHERE order_id=? AND refund_status IN ('pending', 'processing')
            """;
        
        try (PreparedStatement ps = con.prepareStatement(updateInspectionSql)) {
            ps.setString(1, refundMethod);
            ps.setInt(2, orderId);
            int inspectionUpdated = ps.executeUpdate();
            
            if (inspectionUpdated == 0) return false;
        }

        // 2. Cập nhật trạng thái đơn hàng
        String updateOrderSql = "UPDATE RentalOrders SET deposit_status='refunded' WHERE order_id=?";
        try (PreparedStatement ps = con.prepareStatement(updateOrderSql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }

        // 3. Nếu hoàn về ví, cập nhật số dư
        if ("wallet".equals(refundMethod)) {
            updateWalletBalance(con, orderId);
        }

        return true;
    }

    private boolean cancelRefund(Connection con, String inspectionIdRaw) throws SQLException {
        if (inspectionIdRaw == null || inspectionIdRaw.isBlank()) return false;
        
        String sql = "UPDATE RefundInspections SET refund_status='cancelled' WHERE inspection_id=? AND refund_status IN ('pending', 'processing')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(inspectionIdRaw.trim()));
            return ps.executeUpdate() > 0;
        }
    }

private void updateWalletBalance(Connection con, int orderId) throws SQLException {
    System.out.println("=== DEBUG WALLET UPDATE START ===");
    
    // Lấy thông tin refund amount và customer_id
    String selectSql = """
        SELECT ri.refund_amount, ro.customer_id, c.account_id
        FROM RefundInspections ri
        JOIN RentalOrders ro ON ri.order_id = ro.order_id
        JOIN Customers c ON ro.customer_id = c.customer_id
        WHERE ri.order_id = ? AND ri.refund_status = 'completed'
        """;

    try (PreparedStatement ps = con.prepareStatement(selectSql)) {
        ps.setInt(1, orderId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            BigDecimal refundAmount = rs.getBigDecimal("refund_amount");
            int customerId = rs.getInt("customer_id");
            int accountId = rs.getInt("account_id");
            
            System.out.println("DEBUG: Customer ID: " + customerId + ", Account ID: " + accountId + ", Refund Amount: " + refundAmount);

            // 1. Lấy wallet_id từ customer_id
            String getWalletSql = "SELECT wallet_id FROM Wallets WHERE customer_id = ?";
            Integer walletId = null;
            try (PreparedStatement psWallet = con.prepareStatement(getWalletSql)) {
                psWallet.setInt(1, customerId);
                ResultSet rsWallet = psWallet.executeQuery();
                if (rsWallet.next()) {
                    walletId = rsWallet.getInt("wallet_id");
                    System.out.println("DEBUG: Found existing wallet ID: " + walletId);
                }
            }

            // 2. Nếu chưa có wallet, tạo mới
            if (walletId == null) {
                String createWalletSql = """
                    INSERT INTO Wallets (customer_id, balance, created_at, updated_at)
                    VALUES (?, ?, GETDATE(), GETDATE());
                    SELECT SCOPE_IDENTITY();
                    """;
                try (PreparedStatement psCreate = con.prepareStatement(createWalletSql)) {
                    psCreate.setInt(1, customerId);
                    psCreate.setBigDecimal(2, refundAmount);
                    ResultSet rsCreate = psCreate.executeQuery();
                    if (rsCreate.next()) {
                        walletId = rsCreate.getInt(1);
                        System.out.println("DEBUG: Created new wallet ID: " + walletId);
                    }
                }
            } else {
                // 3. Cập nhật balance cho wallet đã tồn tại
                String updateWalletSql = "UPDATE Wallets SET balance = balance + ?, updated_at = GETDATE() WHERE wallet_id = ?";
                try (PreparedStatement psUpdate = con.prepareStatement(updateWalletSql)) {
                    psUpdate.setBigDecimal(1, refundAmount);
                    psUpdate.setInt(2, walletId);
                    int walletUpdated = psUpdate.executeUpdate();
                    System.out.println("DEBUG: Wallet updated rows: " + walletUpdated);
                }
            }

            // 4. Thêm giao dịch vào Wallet_Transactions với type='refund' (giá trị hợp lệ)
            String transactionSql = """
                INSERT INTO Wallet_Transactions (wallet_id, amount, type, order_id, description, created_at)
                VALUES (?, ?, 'refund', ?, ?, GETDATE())
                """;

            try (PreparedStatement psTrans = con.prepareStatement(transactionSql)) {
                psTrans.setInt(1, walletId);
                psTrans.setBigDecimal(2, refundAmount);
                psTrans.setInt(3, orderId);
                psTrans.setString(4, "Hoàn tiền cọc đơn hàng #" + orderId);
                int transInserted = psTrans.executeUpdate();
                System.out.println("DEBUG: Transaction inserted rows: " + transInserted);
            }
            
            System.out.println("=== DEBUG WALLET UPDATE SUCCESS ===");
        } else {
            System.out.println("DEBUG: No refund record found for order: " + orderId);
        }
    }
}

}
