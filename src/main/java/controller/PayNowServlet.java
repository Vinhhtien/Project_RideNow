package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import service.IPaymentService;
import service.PaymentService;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "PayNowServlet", urlPatterns = {"/paynow"})
public class PayNowServlet extends HttpServlet {

    private final IPaymentService paymentService = new PaymentService();

    public static class Row {
        private int orderId;
        private String bikeName;
        private java.sql.Date start;
        private java.sql.Date end;
        private BigDecimal totalPrice;
        private BigDecimal deposit;
        private BigDecimal thirtyPct;
        private BigDecimal toPayNow;
        private String paymentStatus;
        private String status;

        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }
        public java.sql.Date getStart() { return start; }
        public void setStart(java.sql.Date start) { this.start = start; }
        public java.sql.Date getEnd() { return end; }
        public void setEnd(java.sql.Date end) { this.end = end; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public BigDecimal getDeposit() { return deposit; }
        public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
        public BigDecimal getThirtyPct() { return thirtyPct; }
        public void setThirtyPct(BigDecimal thirtyPct) { this.thirtyPct = thirtyPct; }
        public BigDecimal getToPayNow() { return toPayNow; }
        public void setToPayNow(BigDecimal toPayNow) { this.toPayNow = toPayNow; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("🔍 DEBUG PayNowServlet - doGet called");
        
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            System.out.println("❌ No account in session");
            resp.sendRedirect(req.getContextPath() + "/login.jsp"); 
            return; 
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        
        System.out.println("📦 Order IDs received: " + orderIds);
        
        if (orderIds.isEmpty()) {
            System.out.println("❌ No order IDs provided");
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {
            // SỬA: Query kiểm tra điều kiện thanh toán với validation tốt hơn
            String sql = """
                SELECT 
                    r.order_id,
                    COALESCE(
                        STUFF((SELECT ', ' + b2.bike_name
                               FROM OrderDetails d2
                               JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id
                               WHERE d2.order_id = r.order_id
                               FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,''),
                        'Unknown Bike'
                    ) AS bike_name,
                    r.start_date,
                    r.end_date,
                    COALESCE(r.total_price, 0) AS total_price,
                    COALESCE(r.deposit_amount, 0) AS deposit_amount,
                    r.status,
                    CASE 
                        WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.order_id = r.order_id AND p.status = 'pending') 
                        THEN 1 ELSE 0 
                    END AS has_pending_payment,
                    COALESCE(r.payment_submitted, 0) AS payment_submitted
                FROM RentalOrders r
                JOIN Customers c ON r.customer_id = c.customer_id
                WHERE r.order_id IN (%s)
                AND c.account_id = ?
                AND r.status = 'pending'
                AND r.payment_submitted = 0
            """.formatted(orderIds.stream().map(id -> "?").collect(Collectors.joining(",")));

            List<Row> rows = new ArrayList<>();
            boolean hasPending = false;
            BigDecimal grandTotal = BigDecimal.ZERO;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int paramIndex = 1;
                for (Integer orderId : orderIds) {
                    ps.setInt(paramIndex++, orderId);
                }
                ps.setInt(paramIndex, acc.getAccountId());
                
                System.out.println("🔍 Executing query with params: " + orderIds + ", account: " + acc.getAccountId());
                
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Row r = new Row();
                        r.setOrderId(rs.getInt("order_id"));
                        r.setBikeName(rs.getString("bike_name"));
                        r.setStart(rs.getDate("start_date"));
                        r.setEnd(rs.getDate("end_date"));
                        r.setStatus(rs.getString("status"));

                        BigDecimal total = rs.getBigDecimal("total_price");
                        BigDecimal deposit = rs.getBigDecimal("deposit_amount");
                        
                        // SỬA QUAN TRỌNG: Kiểm tra và tính toán tiền cọc nếu chưa có
                        if (deposit == null || deposit.compareTo(BigDecimal.ZERO) == 0) {
                            // Tính tiền cọc mặc định là 50% của tổng tiền thuê
                            deposit = total.multiply(new BigDecimal("0.5"));
                            System.out.println("⚠️ Order #" + r.getOrderId() + " has no deposit, using 50% default: " + deposit);
                        }
                        
                        r.setTotalPrice(total);
                        r.setDeposit(deposit);

                        BigDecimal thirty = total.multiply(new BigDecimal("0.30"));
                        r.setThirtyPct(thirty);
                        
                        BigDecimal toPayNow = thirty.add(deposit);
                        r.setToPayNow(toPayNow);

                        boolean pending = rs.getInt("has_pending_payment") == 1;
                        r.setPaymentStatus(pending ? "pending" : "none");
                        if (pending) hasPending = true;

                        boolean submitted = rs.getInt("payment_submitted") == 1;
                        if (submitted) {
                            System.out.println("⚠️ Order #" + r.getOrderId() + " already has payment submitted");
                        }

                        rows.add(r);
                        grandTotal = grandTotal.add(toPayNow);
                        
                        System.out.println("✅ Order #" + r.getOrderId() + " - " + r.getBikeName() + 
                                         " - Total: " + total + " - Deposit: " + deposit + 
                                         " - 30%: " + thirty + " - To Pay: " + toPayNow);
                    }
                }
            }

            if (rows.isEmpty()) {
                System.out.println("❌ No valid orders found for payment");
                flash(req, "Không có đơn hợp lệ để thanh toán. Các đơn có thể đã được xử lý hoặc không tồn tại.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            String ordersCsv = rows.stream()
                    .map(Row::getOrderId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            // Lấy số dư ví
            BigDecimal walletBalance = getWalletBalance(acc.getAccountId());
            
            req.setAttribute("rows", rows);
            req.setAttribute("grandTotal", grandTotal);
            req.setAttribute("ordersCsv", ordersCsv);
            req.setAttribute("hasPendingPayment", hasPending);
            req.setAttribute("walletBalance", walletBalance);
            req.setAttribute("qrAccountNo", "0916134642");
            req.setAttribute("qrAccountName", "Cua Hang RideNow");
            req.setAttribute("qrAddInfo", "RN" + ordersCsv + " " + System.currentTimeMillis());

            System.out.println("✅ Forwarding to paynow.jsp with " + rows.size() + " orders, total: " + grandTotal);
            req.getRequestDispatcher("/cart/paynow.jsp").forward(req, resp);

        } catch (Exception e) {
            System.err.println("❌ ERROR in PayNowServlet doGet: " + e.getMessage());
            e.printStackTrace();
            flash(req, "Lỗi hệ thống khi tải trang thanh toán: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("🔍 DEBUG PayNowServlet - doPost called");
        
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login.jsp"); 
            return; 
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        if (orderIds.isEmpty()) {
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        String paymentMethod = req.getParameter("paymentMethod");
        BigDecimal walletAmount = parseBigDecimal(req.getParameter("walletAmount"));

        System.out.println("💰 PAYMENT PROCESSING STARTED");
        System.out.println("📝 Payment Method: " + paymentMethod);
        System.out.println("📝 Wallet Amount: " + walletAmount);
        System.out.println("📝 Order IDs: " + orderIds);

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1. Kiểm tra và tính toán số tiền cần thanh toán
                Map<Integer, BigDecimal> payMap = getPayableOrders(con, orderIds, acc.getAccountId());
                if (payMap.isEmpty()) {
                    con.rollback();
                    System.out.println("❌ No payable orders found");
                    flash(req, "Các đơn đã được gửi hoặc không còn trạng thái cho phép.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                BigDecimal totalAmount = payMap.values().stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                System.out.println("💰 Total amount to pay: " + totalAmount);

                // 2. Xử lý theo phương thức thanh toán
                boolean success = processPayment(con, acc.getAccountId(), payMap, paymentMethod, 
                                               walletAmount, totalAmount, req);

                if (success) {
                    con.commit();
                    String message = getSuccessMessage(paymentMethod, walletAmount, totalAmount);
                    System.out.println("✅ PAYMENT SUCCESS: " + message);
                    flash(req, message);
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                } else {
                    con.rollback();
                    System.out.println("❌ PAYMENT FAILED");
                    flash(req, "❌ Thanh toán thất bại. Vui lòng thử lại.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                }

            } catch (Exception ex) {
                con.rollback();
                System.err.println("❌ PAYMENT ERROR: " + ex.getMessage());
                ex.printStackTrace();
                flash(req, "❌ Lỗi hệ thống: " + ex.getMessage());
                resp.sendRedirect(req.getContextPath() + "/customerorders");
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.err.println("❌ DATABASE ERROR: " + e.getMessage());
            e.printStackTrace();
            flash(req, "❌ Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    private Map<Integer, BigDecimal> getPayableOrders(Connection con, List<Integer> orderIds, int accountId) 
            throws SQLException {
        
        String qs = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String check = """
            SELECT r.order_id, r.total_price, ISNULL(r.deposit_amount,0) AS deposit
              FROM RentalOrders r
              JOIN Customers c ON c.customer_id=r.customer_id
              JOIN Accounts  a ON a.account_id=c.account_id
         LEFT JOIN Payments p ON p.order_id=r.order_id AND p.status='pending'
             WHERE r.order_id IN (%s)
               AND a.account_id=?
               AND r.status='pending'
               AND r.payment_submitted=0
               AND p.payment_id IS NULL
            """.formatted(qs);

        Map<Integer, BigDecimal> payMap = new LinkedHashMap<>();
        try (PreparedStatement ps = con.prepareStatement(check)) {
            int i=1; 
            for (Integer id: orderIds) ps.setInt(i++, id);
            ps.setInt(i, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int oid = rs.getInt("order_id");
                    BigDecimal total   = safe(rs.getBigDecimal("total_price"));
                    BigDecimal deposit = safe(rs.getBigDecimal("deposit"));
                    
                    // SỬA: Kiểm tra tiền cọc
                    if (deposit.compareTo(BigDecimal.ZERO) == 0) {
                        deposit = total.multiply(new BigDecimal("0.5"));
                        System.out.println("⚠️ Order #" + oid + " has no deposit in payable check, using 50%: " + deposit);
                    }
                    
                    BigDecimal need = total.multiply(new BigDecimal("0.30")).add(deposit);
                    payMap.put(oid, need);
                    System.out.println("💰 Order #" + oid + " needs: " + need);
                }
            }
        }
        return payMap;
    }

    private boolean processPayment(Connection con, int accountId, Map<Integer, BigDecimal> payMap,
                                 String paymentMethod, BigDecimal walletAmount, 
                                 BigDecimal totalAmount, HttpServletRequest req) 
            throws SQLException {
        
        Integer customerId = getCustomerIdByAccount(con, accountId);
        if (customerId == null) {
            System.out.println("❌ Customer not found for account: " + accountId);
            return false;
        }

        System.out.println("👤 Processing payment for customer: " + customerId);

        // 1. Xử lý thanh toán bằng ví (nếu có)
        if (("wallet".equals(paymentMethod) || "wallet_transfer".equals(paymentMethod)) 
            && walletAmount.compareTo(BigDecimal.ZERO) > 0) {
            
            if (!processWalletPayment(con, customerId, walletAmount, payMap.keySet())) {
                return false;
            }
        }

        // 2. Xử lý thanh toán chuyển khoản (nếu có)
        if ("transfer".equals(paymentMethod) || "wallet_transfer".equals(paymentMethod)) {
            BigDecimal transferAmount = totalAmount.subtract(walletAmount);
            if (transferAmount.compareTo(BigDecimal.ZERO) > 0) {
                processTransferPayment(con, payMap, transferAmount);
            }
        }

        // 3. Cập nhật trạng thái đơn hàng
        updateOrderPaymentStatus(con, payMap.keySet());
        
        return true;
    }

    private boolean processWalletPayment(Connection con, int customerId, BigDecimal walletAmount, 
                                   Set<Integer> orderIds) throws SQLException {
    
        // 1. Kiểm tra số dư ví
        BigDecimal currentBalance = getWalletBalance(con, customerId);
        if (currentBalance.compareTo(walletAmount) < 0) {
            System.out.println("❌ Insufficient wallet balance: " + currentBalance + " < " + walletAmount);
            return false;
        }

        // 2. Trừ tiền trong ví
        String updateWalletSql = "UPDATE Wallets SET balance = balance - ?, updated_at = GETDATE() WHERE customer_id = ?";
        try (PreparedStatement ps = con.prepareStatement(updateWalletSql)) {
            ps.setBigDecimal(1, walletAmount);
            ps.setInt(2, customerId);
            int updated = ps.executeUpdate();
            if (updated == 0) {
                System.out.println("❌ Failed to update wallet balance");
                return false;
            }
        }

        // 3. Ghi lại giao dịch ví
        String walletTxSql = """
            INSERT INTO Wallet_Transactions (wallet_id, amount, type, description, created_at)
            SELECT wallet_id, ?, 'payment', ?, GETDATE()
            FROM Wallets WHERE customer_id = ?
            """;

        String orderIdsStr = orderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        try (PreparedStatement ps = con.prepareStatement(walletTxSql)) {
            ps.setBigDecimal(1, walletAmount.negate());
            ps.setString(2, "Thanh toán đơn hàng #" + orderIdsStr);
            ps.setInt(3, customerId);
            ps.executeUpdate();
        }

        // 4. Tạo bản ghi thanh toán cho phần ví
        BigDecimal amountPerOrder = walletAmount.divide(new BigDecimal(orderIds.size()), 2, RoundingMode.HALF_UP);
        for (Integer orderId : orderIds) {
            // SỬA: Dùng 'wallet' (được phép bởi CK_Payments_Method)
            String paymentSql = """
                INSERT INTO Payments (order_id, amount, method, status, payment_date, reference)
                VALUES (?, ?, 'wallet', 'paid', GETDATE(), ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(paymentSql)) {
                ps.setInt(1, orderId);
                ps.setBigDecimal(2, amountPerOrder);
                ps.setString(3, "WALLET-" + orderId + "-" + (System.currentTimeMillis() % 100000));
                ps.executeUpdate();
            }
        }

        System.out.println("✅ Wallet payment processed: " + walletAmount);
        return true;
    }

    private void processTransferPayment(Connection con, Map<Integer, BigDecimal> payMap, 
                                      BigDecimal totalTransferAmount) throws SQLException {

        // Phân bổ số tiền chuyển khoản cho các đơn hàng
        BigDecimal amountPerOrder = totalTransferAmount.divide(
            new BigDecimal(payMap.size()), 2, RoundingMode.HALF_UP);

        for (Integer orderId : payMap.keySet()) {
            String ref = "TRF-" + orderId + "-" + (System.currentTimeMillis() % 100000);

            
            String paymentSql = """
                INSERT INTO Payments (order_id, amount, method, status, payment_date, reference)
                VALUES (?, ?, 'transfer', 'pending', GETDATE(), ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(paymentSql)) {
                ps.setInt(1, orderId);
                ps.setBigDecimal(2, amountPerOrder);
                ps.setString(3, ref);
                ps.executeUpdate();
            }
        }

        System.out.println("✅ Transfer payment processed: " + totalTransferAmount);
    }

    private void updateOrderPaymentStatus(Connection con, Set<Integer> orderIds) throws SQLException {
        String orderIdsStr = orderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String upd = "UPDATE RentalOrders SET payment_submitted = 1 WHERE order_id IN (" + orderIdsStr + ")";
        try (PreparedStatement ps = con.prepareStatement(upd)) {
            int updated = ps.executeUpdate();
            System.out.println("✅ Updated payment status for " + updated + " orders: " + orderIdsStr);
        }
    }

    // ===== HELPER METHODS =====

    private BigDecimal getWalletBalance(int accountId) {
        String sql = """
            SELECT ISNULL(w.balance, 0) as balance
            FROM Wallets w
            JOIN Customers c ON w.customer_id = c.customer_id
            WHERE c.account_id = ?
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            BigDecimal balance = rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
            System.out.println("💰 Wallet balance for account " + accountId + ": " + balance);
            return balance;
        } catch (SQLException e) {
            System.err.println("❌ Error getting wallet balance: " + e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getWalletBalance(Connection con, int customerId) throws SQLException {
        String sql = "SELECT balance FROM Wallets WHERE customer_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
        }
    }

    private Integer getCustomerIdByAccount(Connection con, int accountId) throws SQLException {
        String sql = "SELECT customer_id FROM Customers WHERE account_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("customer_id") : null;
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private String getSuccessMessage(String paymentMethod, BigDecimal walletAmount, BigDecimal totalAmount) {
        switch (paymentMethod) {
            case "wallet":
                return "✅ Đã thanh toán thành công " + totalAmount + " VNĐ bằng ví điện tử.";
            case "wallet_transfer":
                return "✅ Đã thanh toán " + walletAmount + " VNĐ bằng ví và " + 
                       totalAmount.subtract(walletAmount) + " VNĐ chờ chuyển khoản.";
            case "transfer":
            default:
                return "✅ Đã ghi nhận 'Tôi đã chuyển khoản'. Vui lòng chờ xác minh.";
        }
    }

    private static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        for (String s : csv.split(",")) { 
            try { ids.add(Integer.parseInt(s.trim())); } 
            catch (Exception ignore) {} 
        }
        return ids;
    }
    
    private static BigDecimal safe(BigDecimal v){ 
        return v==null?BigDecimal.ZERO:v; 
    }
    
    private static void flash(HttpServletRequest req, String msg){ 
        req.getSession().setAttribute("flash", msg); 
    }
}