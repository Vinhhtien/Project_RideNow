package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="WalletServlet", urlPatterns={"/wallet"})
public class WalletServlet extends HttpServlet {

    public static class Tx {
        private Integer id;
        private String type;
        private String description;
        private BigDecimal amount;
        private Timestamp date;
        private Integer orderId;

        // Getters and Setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public Timestamp getDate() { return date; }
        public void setDate(Timestamp date) { this.date = date; }
        public Integer getOrderId() { return orderId; }
        public void setOrderId(Integer orderId) { this.orderId = orderId; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { 
            resp.sendRedirect(req.getContextPath() + "/home.jsp"); 
            return; 
        }

        Integer customerId = getCustomerIdByAccount(acc.getAccountId());
        if (customerId == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }

        // Lấy số dư ví
        BigDecimal balance = getWalletBalance(customerId);
        BigDecimal availableBalance = balance;
        BigDecimal lockedBalance = BigDecimal.ZERO;
        int totalTransactions = 0;
        int pendingWithdrawCount = 0;

        List<Tx> txs = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            // Lấy lịch sử giao dịch từ Wallet_Transactions
            // Trong doGet method của WalletServlet
String transactionSql = """
    SELECT wt.tx_id, wt.amount, wt.type, wt.description, wt.order_id, wt.created_at
    FROM Wallet_Transactions wt
    JOIN Wallets w ON wt.wallet_id = w.wallet_id
    WHERE w.customer_id = ?
    ORDER BY wt.created_at DESC
    """;
                
            try (PreparedStatement ps = con.prepareStatement(transactionSql)) {
                ps.setInt(1, customerId);
                ResultSet rs = ps.executeQuery();
                
                while (rs.next()) {
                    Tx tx = new Tx();
                    tx.setId(rs.getInt("tx_id"));
                    tx.setAmount(rs.getBigDecimal("amount"));
                    tx.setType(rs.getString("type"));
                    tx.setDescription(rs.getString("description"));
                    tx.setOrderId(rs.getInt("order_id"));
                    tx.setDate(rs.getTimestamp("created_at"));
                    txs.add(tx);
                }
                totalTransactions = txs.size();
            }

            // Lấy yêu cầu rút tiền đang chờ
            String withdrawSql = """
                SELECT COUNT(*) as pending_count, COALESCE(SUM(amount), 0) as pending_total
                FROM WithdrawalRequests 
                WHERE customer_id = ? AND status IN ('pending', 'processing')
                """;
                
            try (PreparedStatement ps = con.prepareStatement(withdrawSql)) {
                ps.setInt(1, customerId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    pendingWithdrawCount = rs.getInt("pending_count");
                    lockedBalance = rs.getBigDecimal("pending_total");
                    availableBalance = balance.subtract(lockedBalance);
                    if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
                        availableBalance = BigDecimal.ZERO;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Lỗi tải ví: " + e.getMessage(), e);
        }

        // Set attributes
        req.setAttribute("txs", txs);
        req.setAttribute("balance", balance);
        req.setAttribute("availableBalance", availableBalance);
        req.setAttribute("lockedBalance", lockedBalance);
        req.setAttribute("totalTransactions", totalTransactions);
        req.setAttribute("pendingRefunds", pendingWithdrawCount);

        req.getRequestDispatcher("/cus/tomer/wallet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { 
            resp.sendRedirect(req.getContextPath() + "/home.jsp"); 
            return; 
        }

        Integer customerId = getCustomerIdByAccount(acc.getAccountId());
        if (customerId == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }

        String action = req.getParameter("action");

        try (Connection con = DBConnection.getConnection()) {
            if ("keep_deposit".equalsIgnoreCase(action)) {
                req.getSession().setAttribute("flash", "✅ Đã giữ tiền cọc trong ví để dùng cho lần thuê tiếp theo.");
            } else if ("request_refund".equalsIgnoreCase(action)) {
                // Tính số dư khả dụng
                BigDecimal balance = getWalletBalance(customerId);
                BigDecimal pendingWithdraw = getPendingWithdrawTotal(customerId);
                BigDecimal availableBalance = balance.subtract(pendingWithdraw);
                if (availableBalance.compareTo(BigDecimal.ZERO) < 0) {
                    availableBalance = BigDecimal.ZERO;
                }

                BigDecimal requestAmount = parseBigDecimal(req.getParameter("amount"));
                if (requestAmount.compareTo(BigDecimal.ZERO) <= 0 || 
                    requestAmount.compareTo(availableBalance) > 0) {
                    requestAmount = availableBalance;
                }

                if (requestAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    req.getSession().setAttribute("flash", "❌ Không có số dư khả dụng để rút.");
                    resp.sendRedirect(req.getContextPath() + "/wallet");
                    return;
                }

                String bankAccount = req.getParameter("bankAccount");
                String bankName = req.getParameter("bankName");

                // Tạo yêu cầu rút tiền
                String insertSql = """
                    INSERT INTO WithdrawalRequests (customer_id, amount, bank_account, bank_name, status, request_date)
                    VALUES (?, ?, ?, ?, 'pending', GETDATE())
                    """;
                    
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, customerId);
                    ps.setBigDecimal(2, requestAmount);
                    ps.setString(3, bankAccount != null ? bankAccount : "");
                    ps.setString(4, bankName != null ? bankName : "");
                    ps.executeUpdate();
                }

                req.getSession().setAttribute("flash", 
                    "✅ Đã tạo yêu cầu rút " + requestAmount + " VNĐ. Admin sẽ xử lý trong thời gian sớm nhất.");
            }

            resp.sendRedirect(req.getContextPath() + "/wallet");

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "❌ Lỗi khi xử lý: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/wallet");
        }
    }

    // ===== Helper Methods =====
    
    private Integer getCustomerIdByAccount(int accountId) throws ServletException {
        String sql = "SELECT customer_id FROM Customers WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("customer_id") : null;
        } catch (Exception e) {
            throw new ServletException("Không lấy được customer_id", e);
        }
    }

    private BigDecimal getWalletBalance(int customerId) throws ServletException {
        String sql = "SELECT balance FROM Wallets WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
        } catch (Exception e) {
            // Nếu chưa có ví, trả về 0
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getPendingWithdrawTotal(int customerId) throws ServletException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total 
            FROM WithdrawalRequests 
            WHERE customer_id = ? AND status IN ('pending', 'processing')
            """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
        } catch (Exception e) {
            throw new ServletException("Không lấy được tổng pending withdraw", e);
        }
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}