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
//
    // View model cho 1 dòng giao dịch hiển thị
    public static class Tx {
        private String type;       // REFUND | ADJUST | PENALTY | WITHDRAW
        private String status;     // completed | pending | processing | cancelled
        private BigDecimal amount; // dương/âm (PENALTY âm)
        private Timestamp date;    // created_at / request_date
        private Integer orderId;   // có thể null
        private Integer id;        // transaction_id hoặc withdrawal_id

        public String getType() { return type; }
        public String getStatus() { return status; }
        public BigDecimal getAmount() { return amount; }
        public Timestamp getDate() { return date; }
        public Integer getOrderId() { return orderId; }
        public Integer getId() { return id; }

        public void setType(String type) { this.type = type; }
        public void setStatus(String status) { this.status = status; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public void setDate(Timestamp date) { this.date = date; }
        public void setOrderId(Integer orderId) { this.orderId = orderId; }
        public void setId(Integer id) { this.id = id; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { resp.sendRedirect(req.getContextPath()+"/home.jsp"); return; }

        Integer customerId = getCustomerIdByAccount(acc.getAccountId());
        if (customerId == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

        // Số liệu ví
        BigDecimal sumDeposCompleted = BigDecimal.ZERO;     // tổng deposit giao dịch đã hoàn tất
        BigDecimal sumWithdrawCompleted = BigDecimal.ZERO;  // tổng rút đã hoàn tất
        BigDecimal sumWithdrawPending = BigDecimal.ZERO;    // tổng rút đang chờ/đang xử lý

        int pendingWithdrawCount = 0;

        List<Tx> txs = new ArrayList<>();

        try (Connection con = DBConnection.getConnection()) {
            // 1) Giao dịch ví (DepositTransactions)
            String qDepo = """
                SELECT transaction_id, order_id, amount, method, status, created_at
                FROM DepositTransactions
                WHERE customer_id = ?
                ORDER BY transaction_id DESC
            """;
            try (PreparedStatement ps = con.prepareStatement(qDepo)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Tx t = new Tx();
                        t.setId(rs.getInt("transaction_id"));
                        int oid = rs.getInt("order_id");
                        t.setOrderId(rs.wasNull() ? null : oid);
                        t.setAmount(rs.getBigDecimal("amount"));  // PENALTY nên là số âm (đã insert âm)
                        t.setType(rs.getString("method"));        // REFUND | ADJUST | PENALTY
                        t.setStatus(rs.getString("status"));      // completed | pending
                        t.setDate(rs.getTimestamp("created_at"));
                        txs.add(t);

                        // Chỉ "completed" mới tính vào số dư thực
                        if ("completed".equalsIgnoreCase(t.getStatus())) {
                            sumDeposCompleted = sumDeposCompleted.add(t.getAmount());
                        }
                    }
                }
            }

            // 2) Yêu cầu rút (WithdrawalRequests)
            String qWith = """
                SELECT withdrawal_id, amount, status, request_date
                FROM WithdrawalRequests
                WHERE customer_id = ?
                ORDER BY withdrawal_id DESC
            """;
            try (PreparedStatement ps = con.prepareStatement(qWith)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Tx t = new Tx();
                        t.setId(rs.getInt("withdrawal_id"));
                        t.setOrderId(null);
                        t.setAmount(rs.getBigDecimal("amount").negate()); // rút ra khỏi ví: thể hiện số âm
                        String st = rs.getString("status");                // pending | processing | completed | cancelled
                        t.setStatus(st);
                        t.setType("WITHDRAW");
                        t.setDate(rs.getTimestamp("request_date"));
                        txs.add(t);

                        if ("completed".equalsIgnoreCase(st)) {
                            sumWithdrawCompleted = sumWithdrawCompleted.add(rs.getBigDecimal("amount"));
                        } else if ("pending".equalsIgnoreCase(st) || "processing".equalsIgnoreCase(st)) {
                            sumWithdrawPending = sumWithdrawPending.add(rs.getBigDecimal("amount"));
                            pendingWithdrawCount++;
                        }
                    }
                }
            }

        } catch (Exception e) {
            throw new ServletException("Lỗi tải ví: " + e.getMessage(), e);
        }

        // Số dư đã thực nhận (đã hoàn tất - rút hoàn tất)
        BigDecimal balance = sumDeposCompleted.subtract(sumWithdrawCompleted);
        if (balance == null) balance = BigDecimal.ZERO;

        // Phần đang bị khóa (yêu cầu rút đang chờ/đang xử lý)
        BigDecimal lockedBalance = sumWithdrawPending;
        if (lockedBalance == null) lockedBalance = BigDecimal.ZERO;

        // Số có thể rút (hoặc dùng): balance - locked (không âm)
        BigDecimal availableBalance = balance.subtract(lockedBalance);
        if (availableBalance.compareTo(BigDecimal.ZERO) < 0) availableBalance = BigDecimal.ZERO;

        // Sắp xếp lịch sử: đã theo id DESC mỗi bảng, nhưng gộp 2 nguồn thì thứ tự gần đúng theo time
        // (nếu muốn chuẩn hơn: sort txs theo date DESC)
        txs.sort((a,b) -> b.getDate().compareTo(a.getDate()));

        req.setAttribute("txs", txs);
        req.setAttribute("balance", balance);
        req.setAttribute("availableBalance", availableBalance);
        req.setAttribute("lockedBalance", lockedBalance);
        req.setAttribute("totalTransactions", txs.size());
        req.setAttribute("pendingRefunds", pendingWithdrawCount); // đặt tên y như JSP bạn đang dùng

        req.getRequestDispatcher("/customer/wallet.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { resp.sendRedirect(req.getContextPath()+"/home.jsp"); return; }

        Integer customerId = getCustomerIdByAccount(acc.getAccountId());
        if (customerId == null) { resp.sendRedirect(req.getContextPath()+"/login"); return; }

        String action = nvl(req.getParameter("action"));

        try (Connection con = DBConnection.getConnection()) {
            if ("keep_deposit".equalsIgnoreCase(action)) {
                // Không cần thao tác DB: tiền đã ở DepositTransactions (REFUND completed)
                req.getSession().setAttribute("flash", "Đã giữ tiền cọc trong ví để dùng cho lần thuê tiếp theo.");
                resp.sendRedirect(req.getContextPath()+"/wallet");
                return;
            }

            if ("request_refund".equalsIgnoreCase(action)) {
                // Rút tiền mặt (tạo WithdrawalRequests pending)
                // Tính lại availableBalance như GET (đảm bảo an toàn)
                BigDecimal balance = getWalletBalance(customerId);
                BigDecimal pendingW = getPendingWithdrawTotal(customerId);
                BigDecimal available = balance.subtract(pendingW);
                if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;

                // Số tiền yêu cầu rút (nếu form gửi amount; nếu không → rút hết available)
                BigDecimal reqAmount = toMoney(req.getParameter("amount"));
                if (reqAmount.compareTo(BigDecimal.ZERO) <= 0 || reqAmount.compareTo(available) > 0) {
                    reqAmount = available;
                }

                if (reqAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    req.getSession().setAttribute("flash", "Không có số dư khả dụng để rút.");
                    resp.sendRedirect(req.getContextPath()+"/wallet");
                    return;
                }

                String bankAccount = nvl(req.getParameter("bankAccount"));
                String bankName = nvl(req.getParameter("bankName"));

                try (PreparedStatement ps = con.prepareStatement("""
                    INSERT INTO WithdrawalRequests(customer_id, amount, bank_account, bank_name, status)
                    VALUES (?,?,?,?, 'pending')
                """)) {
                    ps.setInt(1, customerId);
                    ps.setBigDecimal(2, reqAmount);
                    ps.setString(3, bankAccount.isEmpty() ? null : bankAccount);
                    ps.setString(4, bankName.isEmpty() ? null : bankName);
                    ps.executeUpdate();
                }

                req.getSession().setAttribute("flash", "Đã tạo yêu cầu rút " + reqAmount + " đ. Admin sẽ xác nhận trong thời gian sớm nhất.");
                resp.sendRedirect(req.getContextPath()+"/wallet");
                return;
            }

            // Mặc định: quay lại
            resp.sendRedirect(req.getContextPath()+"/wallet");

        } catch (Exception e) {
            throw new ServletException("Lỗi xử lý ví: " + e.getMessage(), e);
        }
    }

    // ===== Helpers =====

    private Integer getCustomerIdByAccount(int accountId) throws ServletException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT customer_id FROM Customers WHERE account_id=?")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            throw new ServletException("Không lấy được customer_id", e);
        }
        return null;
    }

    // Số dư thực: tổng DepositTransactions (completed) – tổng WithdrawalRequests (completed)
    private BigDecimal getWalletBalance(int customerId) throws ServletException {
        BigDecimal deposits = BigDecimal.ZERO;
        BigDecimal withdraws = BigDecimal.ZERO;

        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement("""
                SELECT ISNULL(SUM(amount),0)
                FROM DepositTransactions
                WHERE customer_id=? AND status='completed'
            """)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) deposits = rs.getBigDecimal(1);
                }
            }
            try (PreparedStatement ps = con.prepareStatement("""
                SELECT ISNULL(SUM(amount),0)
                FROM WithdrawalRequests
                WHERE customer_id=? AND status='completed'
            """)) {
                ps.setInt(1, customerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) withdraws = rs.getBigDecimal(1);
                }
            }
        } catch (Exception e) {
            throw new ServletException("Không tính được số dư ví", e);
        }

        return deposits.subtract(withdraws);
    }

    private BigDecimal getPendingWithdrawTotal(int customerId) throws ServletException {
        BigDecimal pending = BigDecimal.ZERO;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("""
                SELECT ISNULL(SUM(amount),0)
                FROM WithdrawalRequests
                WHERE customer_id=? AND status IN ('pending','processing')
             """)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) pending = rs.getBigDecimal(1);
            }
        } catch (Exception e) {
            throw new ServletException("Không tính được pending withdraw", e);
        }
        return pending;
    }

    private static String nvl(String s) { return s == null ? "" : s.trim(); }

    private static BigDecimal toMoney(String raw) {
        if (raw == null || raw.isBlank()) return BigDecimal.ZERO;
        try { return new BigDecimal(raw.trim()); } catch (Exception ignore) { return BigDecimal.ZERO; }
    }
}
