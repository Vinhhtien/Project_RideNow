package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

@WebServlet(name = "AdminReturnsServlet", urlPatterns = {"/adminreturns"})
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
    }

    public static class WithdrawalRow {
        private int withdrawalId;
        private String customerName;
        private String customerPhone;
        private BigDecimal amount;
        private Timestamp requestDate;
        private String status;
        private String bankAccount;
        private String bankName;

        public int getWithdrawalId() { return withdrawalId; }
        public void setWithdrawalId(int withdrawalId) { this.withdrawalId = withdrawalId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getCustomerPhone() { return customerPhone; }
        public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Timestamp getRequestDate() { return requestDate; }
        public void setRequestDate(Timestamp requestDate) { this.requestDate = requestDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getBankAccount() { return bankAccount; }
        public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        List<RefundOrderVM> refundOrders = new ArrayList<>();
        List<WithdrawalRow> pendingWithdrawals = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {

            System.out.println("DEBUG: Fetching refund orders...");

            // 1️⃣ SỬA QUERY: Tìm orders với deposit_status = 'held'
            String sqlRefundOrders = """
                SELECT r.order_id, c.full_name AS customer_name, c.phone AS customer_phone,
                       b.bike_name, r.end_date, r.deposit_amount, r.pickup_status, r.returned_at
                FROM RentalOrders r
                JOIN Customers c ON c.customer_id = r.customer_id
                JOIN OrderDetails d ON d.order_id = r.order_id
                JOIN Motorbikes b ON b.bike_id = d.bike_id
                WHERE r.pickup_status = 'returned'
                  AND r.deposit_status = 'held'
                ORDER BY r.returned_at DESC
            """;

            try (PreparedStatement ps = con.prepareStatement(sqlRefundOrders);
                 ResultSet rs = ps.executeQuery()) {
                
                System.out.println("DEBUG: Executing refund orders query...");
                
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
                    r.setReturnStatus(rs.getString("pickup_status"));
                    
                    Timestamp returned = rs.getTimestamp("returned_at");
                    if (returned != null)
                        r.setReturnedAt(new java.util.Date(returned.getTime()));
                    
                    refundOrders.add(r);
                    
                    System.out.println("DEBUG: Found refund order #" + r.getOrderId() + " - " + r.getCustomerName());
                }
                
                System.out.println("DEBUG: Total refund orders found: " + refundOrders.size());
            }

            // 2️⃣ Yêu cầu rút tiền - THÊM TIẾNG VIỆT CHO STATUS
            String sqlWithdrawals = """
                SELECT w.withdrawal_id, c.full_name, c.phone, 
                       w.amount, w.request_date, w.status, w.bank_account, w.bank_name
                FROM WithdrawalRequests w
                JOIN Customers c ON c.customer_id = w.customer_id
                WHERE w.status IN ('pending','processing')
                ORDER BY w.request_date DESC
            """;

            try (PreparedStatement ps = con.prepareStatement(sqlWithdrawals);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    WithdrawalRow w = new WithdrawalRow();
                    w.setWithdrawalId(rs.getInt("withdrawal_id"));
                    w.setCustomerName(rs.getString("full_name"));
                    w.setCustomerPhone(rs.getString("phone"));
                    w.setAmount(rs.getBigDecimal("amount"));
                    w.setRequestDate(rs.getTimestamp("request_date"));
                    
                    // CHUYỂN STATUS SANG TIẾNG VIỆT
                    String status = rs.getString("status");
                    w.setStatus(getVietnameseStatus(status));
                    
                    w.setBankAccount(rs.getString("bank_account"));
                    w.setBankName(rs.getString("bank_name"));
                    pendingWithdrawals.add(w);
                }
            }

        } catch (SQLException e) {
            log("[AdminReturnsServlet] SQL error: " + e.getMessage());
            throw new ServletException("Không thể tải dữ liệu hoàn cọc / rút tiền", e);
        }

        req.setAttribute("refundOrders", refundOrders);
        req.setAttribute("pendingWithdrawals", pendingWithdrawals);
        req.getRequestDispatcher("/admin/admin-returns.jsp").forward(req, resp);
    }
    
    // PHƯƠNG THỨC CHUYỂN STATUS SANG TIẾNG VIỆT
    private String getVietnameseStatus(String status) {
        switch (status) {
            case "pending": return "đang chờ";
            case "processing": return "đang xử lý";
            case "completed": return "đã hoàn thành";
            case "cancelled": return "đã hủy";
            default: return status;
        }
    }
}