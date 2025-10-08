package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Enumeration;

@WebServlet(name = "AdminWithdrawalsServlet", urlPatterns = {"/adminwithdrawals"})
public class AdminWithdrawalsServlet extends HttpServlet {

    public static class WithdrawalVM {
        private int withdrawalId;
        private int orderId;
        private int inspectionId;
        private String customerName;
        private String customerPhone;
        private BigDecimal amount;
        private BigDecimal refundAmount;
        private String refundMethod;
        private Timestamp requestDate;
        private Timestamp returnedAt;
        private String status;
        private String depositStatus;
        private String bikeCondition;
        private String bankAccount;
        private String bankName;

        // Getters and Setters
        public int getWithdrawalId() { return withdrawalId; }
        public void setWithdrawalId(int withdrawalId) { this.withdrawalId = withdrawalId; }
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public int getInspectionId() { return inspectionId; }
        public void setInspectionId(int inspectionId) { this.inspectionId = inspectionId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public BigDecimal getRefundAmount() { return refundAmount; }
        public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }
        public String getRefundMethod() { return refundMethod; }
        public void setRefundMethod(String refundMethod) { this.refundMethod = refundMethod; }
        public Timestamp getRequestDate() { return requestDate; }
        public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
        public Timestamp getReturnedAt() { return returnedAt; }
        public void setReturnedAt(Timestamp returnedAt) { this.returnedAt = returnedAt; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getDepositStatus() { return depositStatus; }
        public void setDepositStatus(String depositStatus) { this.depositStatus = depositStatus; }
        public String getBikeCondition() { return bikeCondition; }
        public void setBikeCondition(String bikeCondition) { this.bikeCondition = bikeCondition; }
        public String getBankAccount() { return bankAccount; }
        public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        List<WithdrawalVM> list = new ArrayList<>();
        String statusFilter = req.getParameter("status");

        System.out.println("=== DEBUG: Starting AdminWithdrawalsServlet ===");
        System.out.println("📊 Request parameters - status: " + statusFilter);

        try (Connection con = DBConnection.getConnection()) {
            System.out.println("✅ Database connection successful");

            // Test data trước khi query
            testRefundInspectionsData(con);

            // Query đơn giản hơn - tập trung vào RefundInspections
            String query = """
                SELECT 
                    ri.inspection_id as withdrawal_id,
                    ri.order_id,
                    c.full_name as customer_name,
                    c.phone as customer_phone,
                    ro.deposit_amount as amount,
                    ri.refund_amount,
                    ri.refund_method,
                    ri.refund_status as status,
                    ri.inspected_at as request_date,
                    ri.inspection_id,
                    ri.bike_condition,
                    ro.returned_at
                FROM RefundInspections ri
                INNER JOIN RentalOrders ro ON ri.order_id = ro.order_id
                INNER JOIN Customers c ON ro.customer_id = c.customer_id
                WHERE ri.refund_status IS NOT NULL
                ORDER BY ri.inspected_at DESC
                """;

            System.out.println("📊 Executing query...");

            try (PreparedStatement ps = con.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                int count = 0;
                while (rs.next()) {
                    count++;
                    WithdrawalVM w = new WithdrawalVM();

                    // Map dữ liệu từ ResultSet
                    w.setWithdrawalId(rs.getInt("withdrawal_id"));
                    w.setOrderId(rs.getInt("order_id"));
                    w.setCustomerName(rs.getString("customer_name"));
                    w.setCustomerPhone(rs.getString("customer_phone"));
                    w.setAmount(rs.getBigDecimal("amount"));
                    w.setRefundAmount(rs.getBigDecimal("refund_amount"));
                    w.setRefundMethod(rs.getString("refund_method"));
                    w.setStatus(rs.getString("status"));
                    w.setRequestDate(rs.getTimestamp("request_date"));
                    w.setInspectionId(rs.getInt("inspection_id"));
                    w.setBikeCondition(rs.getString("bike_condition"));
                    w.setReturnedAt(rs.getTimestamp("returned_at"));

                    System.out.println("📝 Found record: Order#" + w.getOrderId() + 
                                     ", Status: " + w.getStatus() + 
                                     ", Customer: " + w.getCustomerName() +
                                     ", Amount: " + w.getAmount() +
                                     ", RefundAmount: " + w.getRefundAmount() +
                                     ", Method: " + w.getRefundMethod());

                    // Apply filter
                    if (statusFilter == null || statusFilter.isEmpty() || 
                        "all".equals(statusFilter) || statusFilter.equals(w.getStatus())) {
                        list.add(w);
                    }
                }

                System.out.println("✅ Total records found: " + count);
                System.out.println("✅ Records after filtering: " + list.size());

                // Debug: hiển thị tất cả status có trong dữ liệu
                if (count > 0) {
                    System.out.println("🔍 All statuses in data:");
                    try (PreparedStatement ps2 = con.prepareStatement(
                         "SELECT DISTINCT refund_status, COUNT(*) as count FROM RefundInspections WHERE refund_status IS NOT NULL GROUP BY refund_status");
                         ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next()) {
                            System.out.println("  - " + rs2.getString("refund_status") + ": " + rs2.getInt("count") + " records");
                        }
                    }
                } else {
                    System.out.println("❌ No records found in RefundInspections");
                }
            }

            // Tính toán thống kê
            calculateAndSetStats(req, list);

        } catch (Exception e) {
            System.out.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
            log("[AdminWithdrawalsServlet] Error loading data: " + e.getMessage());
            req.setAttribute("error", "Không thể tải danh sách hoàn tiền: " + e.getMessage());
        }

        req.setAttribute("rows", list);
        req.setAttribute("totalRecords", list.size());
        
        System.out.println("🎯 Forwarding to JSP with " + list.size() + " records");
        
        req.getRequestDispatcher("/admin/admin-withdrawals.jsp").forward(req, resp);
    }

    private void testRefundInspectionsData(Connection con) throws SQLException {
        System.out.println("🧪 Testing RefundInspections data...");
        
        // Kiểm tra tổng số bản ghi
        String countQuery = "SELECT COUNT(*) as total FROM RefundInspections WHERE refund_status IS NOT NULL";
        try (PreparedStatement ps = con.prepareStatement(countQuery);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                System.out.println("📊 Total records in RefundInspections: " + rs.getInt("total"));
            }
        }
        
        // Kiểm tra dữ liệu mẫu
        String sampleQuery = "SELECT TOP 5 inspection_id, order_id, refund_status, refund_method, refund_amount FROM RefundInspections WHERE refund_status IS NOT NULL ORDER BY inspection_id DESC";
        try (PreparedStatement ps = con.prepareStatement(sampleQuery);
             ResultSet rs = ps.executeQuery()) {
            System.out.println("📋 Sample RefundInspections data:");
            boolean hasData = false;
            while (rs.next()) {
                hasData = true;
                System.out.println("  - ID: " + rs.getInt("inspection_id") + 
                                 ", Order: " + rs.getInt("order_id") + 
                                 ", Status: " + rs.getString("refund_status") +
                                 ", Method: " + rs.getString("refund_method") +
                                 ", Amount: " + rs.getBigDecimal("refund_amount"));
            }
            if (!hasData) {
                System.out.println("  ❌ No sample data found!");
            }
        }
    }

   private void calculateAndSetStats(HttpServletRequest req, List<WithdrawalVM> withdrawals) {
        int totalPending = 0;
        int totalProcessing = 0;
        BigDecimal totalPendingAmount = BigDecimal.ZERO;

        System.out.println("📊 Calculating stats for " + withdrawals.size() + " withdrawals");

        for (WithdrawalVM w : withdrawals) {
            String status = w.getStatus();
            System.out.println("📊 Processing stat for order " + w.getOrderId() + " with status: " + status);

            if ("pending".equals(status)) {
                totalPending++;
                BigDecimal amount = w.getRefundAmount() != null ? w.getRefundAmount() : w.getAmount();
                if (amount != null) {
                    totalPendingAmount = totalPendingAmount.add(amount);
                    System.out.println("  💰 Added " + amount + " to pending amount");
                }
            } else if ("processing".equals(status)) {
                totalProcessing++;
                BigDecimal amount = w.getRefundAmount() != null ? w.getRefundAmount() : w.getAmount();
                if (amount != null) {
                    totalPendingAmount = totalPendingAmount.add(amount);
                    System.out.println("  💰 Added " + amount + " to pending amount");
                }
            }
        }

        req.setAttribute("totalPending", totalPending);
        req.setAttribute("totalProcessing", totalProcessing);
        req.setAttribute("totalPendingAmount", totalPendingAmount);

        System.out.println("📊 Final Stats - Pending: " + totalPending + 
                         ", Processing: " + totalProcessing + 
                         ", Amount: " + totalPendingAmount);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Debug tất cả parameters
        System.out.println("=== DEBUG: Processing POST Request ===");
        System.out.println("🔍 All parameters received:");
        Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String paramValue = req.getParameter(paramName);
            System.out.println("  - " + paramName + ": " + paramValue);
        }

        String action = req.getParameter("action");
        String orderIdRaw = req.getParameter("orderId");
        String withdrawalIdRaw = req.getParameter("withdrawalId");

        System.out.println("🔄 Processing action: " + action + ", orderId: " + orderIdRaw + ", withdrawalId: " + withdrawalIdRaw);

        HttpSession session = req.getSession();
        
        try (Connection con = DBConnection.getConnection()) {
            String message = "";
            int rowsUpdated = 0;
            
            // Validate parameters
            if (action == null || action.trim().isEmpty()) {
                message = "❌ Lỗi: Thiếu hành động";
                System.out.println("❌ Error: Missing action parameter");
            } else {
                switch (action) {
                    case "mark_processing":
                        if (withdrawalIdRaw != null && !withdrawalIdRaw.isBlank()) {
                            // Update RefundInspections instead of WithdrawalRequests
                            String sql = "UPDATE RefundInspections SET refund_status='processing' WHERE inspection_id=? AND refund_status='pending'";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, Integer.parseInt(withdrawalIdRaw.trim()));
                                rowsUpdated = ps.executeUpdate();
                                System.out.println("✅ Updated " + rowsUpdated + " rows in RefundInspections");
                            }
                            message = rowsUpdated > 0 ? "✅ Đã chuyển sang trạng thái đang xử lý" : "❌ Không tìm thấy yêu cầu để xử lý";
                        } else {
                            message = "❌ Thiếu withdrawalId";
                        }
                        break;
                        
                    case "complete_refund":
                        if (orderIdRaw != null && !orderIdRaw.isBlank()) {
                            // Cập nhật refund_status trong RefundInspections
                            String sql = "UPDATE RefundInspections SET refund_status='completed' WHERE order_id=? AND refund_status IN ('pending', 'processing')";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, Integer.parseInt(orderIdRaw.trim()));
                                rowsUpdated = ps.executeUpdate();
                                System.out.println("✅ Updated " + rowsUpdated + " rows in RefundInspections to completed");
                            }
                            message = rowsUpdated > 0 ? "✅ Đã hoàn tất hoàn tiền" : "❌ Không thể hoàn tiền - kiểm tra trạng thái đơn hàng";
                        } else {
                            message = "❌ Thiếu orderId";
                        }
                        break;
                        
                    case "process_refund":
                        if (orderIdRaw != null && !orderIdRaw.isBlank()) {
                            // Cập nhật refund_status từ held sang pending
                            String sql = "UPDATE RefundInspections SET refund_status='pending' WHERE order_id=? AND refund_status='held'";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, Integer.parseInt(orderIdRaw.trim()));
                                rowsUpdated = ps.executeUpdate();
                                System.out.println("✅ Updated " + rowsUpdated + " rows in RefundInspections from held to pending");
                            }
                            message = rowsUpdated > 0 ? "✅ Đã xác nhận xe trả và bắt đầu hoàn tiền" : "❌ Không thể xác nhận xe trả - kiểm tra trạng thái đơn hàng";
                        } else {
                            message = "❌ Thiếu orderId";
                        }
                        break;
                        
                    case "cancel":
                        if (withdrawalIdRaw != null && !withdrawalIdRaw.isBlank()) {
                            String sql = "UPDATE RefundInspections SET refund_status='cancelled' WHERE inspection_id=? AND refund_status IN ('pending', 'processing')";
                            try (PreparedStatement ps = con.prepareStatement(sql)) {
                                ps.setInt(1, Integer.parseInt(withdrawalIdRaw.trim()));
                                rowsUpdated = ps.executeUpdate();
                                System.out.println("✅ Updated " + rowsUpdated + " rows in RefundInspections to cancelled");
                            }
                            message = rowsUpdated > 0 ? "❌ Đã hủy yêu cầu hoàn tiền" : "❌ Không tìm thấy yêu cầu để hủy";
                        } else {
                            message = "❌ Thiếu withdrawalId";
                        }
                        break;
                        
                    default:
                        message = "❌ Hành động không hợp lệ: " + action;
                        System.out.println("❌ Invalid action: " + action);
                        break;
                }
            }

            System.out.println("✅ Final result: " + rowsUpdated + " rows updated, message: " + message);
            session.setAttribute("flash", message);

        } catch (SQLException | NumberFormatException e) {
            System.out.println("❌ Error in doPost: " + e.getMessage());
            e.printStackTrace();
            log("[AdminWithdrawalsServlet] Error processing action: " + e.getMessage());
            session.setAttribute("flash", "❌ Lỗi khi xử lý: " + e.getMessage());
        }
        
        resp.sendRedirect(req.getContextPath() + "/adminwithdrawals");
    }
}