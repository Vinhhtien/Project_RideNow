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

@WebServlet(name = "WalletServlet", urlPatterns = {"/wallet"})
public class WalletServlet extends HttpServlet {

    public static class Tx {
        private Integer id;
        private String type;
        private String description;
        private BigDecimal amount;
        private Timestamp date;
        private Integer orderId;

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        public Timestamp getDate() {
            return date;
        }

        public void setDate(Timestamp date) {
            this.date = date;
        }

        public Integer getOrderId() {
            return orderId;
        }

        public void setOrderId(Integer orderId) {
            this.orderId = orderId;
        }
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
        List<Tx> txs = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            // Lấy lịch sử giao dịch từ Wallet_Transactions
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

                    // Xử lý giá trị amount có thể null
                    BigDecimal amount = rs.getBigDecimal("amount");
                    if (rs.wasNull()) {
                        amount = BigDecimal.ZERO;
                    }
                    tx.setAmount(amount);

                    tx.setType(rs.getString("type"));
                    tx.setDescription(rs.getString("description"));

                    // Xử lý orderId có thể null
                    int orderId = rs.getInt("order_id");
                    tx.setOrderId(rs.wasNull() ? null : orderId);

                    tx.setDate(rs.getTimestamp("created_at"));
                    txs.add(tx);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Lỗi tải ví: " + e.getMessage(), e);
        }

        // Set attributes - chỉ giữ lại số dư và lịch sử giao dịch
        req.setAttribute("txs", txs);
        req.setAttribute("balance", balance != null ? balance : BigDecimal.ZERO);

        req.getRequestDispatcher("/customer/wallet.jsp").forward(req, resp);
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
}