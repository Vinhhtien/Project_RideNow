package dao;

import model.report.*;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminReportDao implements IAdminReportDao {

    private static Timestamp ts(Date d) { return d == null ? null : new Timestamp(d.getTime()); }

    // ===== SUMMARY: chỉ đơn đã hoàn tiền, mốc = inspected_at =====
    @Override
    public AdminReportSummary getSummary(Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, CAST(ri.inspected_at AS date) AS completed_date, " +
            "         SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id, CAST(ri.inspected_at AS date) " +
            "), Gross AS ( " +
            "  SELECT c.order_id, ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0) AS gross " +
            "  FROM Completed c JOIN RentalOrders r ON r.order_id = c.order_id " +
            ") " +
            "SELECT COUNT(*) AS total_orders, " +
            "       ISNULL(SUM(g.gross),0) AS total_paid, " +
            "       ISNULL(SUM(c.refunded),0) AS total_refunded " +
            "FROM Completed c JOIN Gross g ON g.order_id = c.order_id";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                AdminReportSummary s = new AdminReportSummary();
                if (rs.next()) {
                    s.setTotalOrders(rs.getInt("total_orders"));
                    s.setTotalPaid(rs.getBigDecimal("total_paid")==null?0:rs.getBigDecimal("total_paid").doubleValue());
                    s.setTotalRefunded(rs.getBigDecimal("total_refunded")==null?0:rs.getBigDecimal("total_refunded").doubleValue());
                }
                return s;
            }
        }
    }

    // ===== METHOD STATS: phân bổ theo phương thức, anchor inspected_at =====
    @Override
    public List<AdminPaymentMethodStat> getMethodStats(Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id " +
            "), O AS ( " +
            "  SELECT r.order_id, ISNULL(r.total_price,0) AS total_price, ISNULL(r.deposit_amount,0) AS deposit_amount " +
            "  FROM RentalOrders r JOIN Completed c ON c.order_id = r.order_id " +
            "), P AS ( " +
            "  SELECT p.order_id, LOWER(p.method) AS method, SUM(ISNULL(p.amount,0)) AS amt " +
            "  FROM Payments p JOIN O ON O.order_id = p.order_id " +
            "  WHERE p.status='paid' " +
            "  GROUP BY p.order_id, LOWER(p.method) " +
            "), W AS ( " +
            "  SELECT wt.order_id, SUM(CASE WHEN wt.type='payment' THEN -ISNULL(wt.amount,0) ELSE 0 END) AS amt " +
            "  FROM Wallet_Transactions wt JOIN O ON O.order_id = wt.order_id " +
            "  GROUP BY wt.order_id " +
            "), AGG AS ( " +
            "  SELECT O.order_id, O.total_price, O.deposit_amount, " +
            "         ISNULL(SUM(CASE WHEN P.method='bank_transfer' THEN P.amt END),0) AS bank_amt, " +
            "         ISNULL(SUM(CASE WHEN P.method='transfer'      THEN P.amt END),0) AS transfer_amt, " +
            "         ISNULL(SUM(CASE WHEN P.method='cash'          THEN P.amt END),0) AS cash_amt, " +
            "         ISNULL(W.amt,0) AS wallet_amt " +
            "  FROM O LEFT JOIN P ON P.order_id=O.order_id " +
            "         LEFT JOIN W ON W.order_id=O.order_id " +
            "  GROUP BY O.order_id, O.total_price, O.deposit_amount, W.amt " +
            "), TAKE AS ( " +
            "  SELECT order_id, " +
            "         CAST(ISNULL(total_price,0) + ISNULL(deposit_amount,0) AS decimal(18,2)) AS revenue_needed, " +
            "         CAST(ISNULL(bank_amt,0) + ISNULL(transfer_amt,0) + ISNULL(wallet_amt,0) + ISNULL(cash_amt,0) AS decimal(18,2)) AS method_sum, " +
            "         CAST(ISNULL(bank_amt,0)     AS decimal(18,2)) AS bank_amt, " +
            "         CAST(ISNULL(transfer_amt,0) AS decimal(18,2)) AS transfer_amt, " +
            "         CAST(ISNULL(wallet_amt,0)   AS decimal(18,2)) AS wallet_amt, " +
            "         CAST(ISNULL(cash_amt,0)     AS decimal(18,2)) AS cash_amt " +
            "  FROM AGG " +
            "), PAID_METHOD AS ( " +
            "  SELECT 'bank_transfer' AS method, SUM(CASE WHEN method_sum=0 THEN 0 ELSE (bank_amt/NULLIF(method_sum,0))*revenue_needed END) AS amt_paid FROM TAKE " +
            "  UNION ALL SELECT 'transfer',      SUM(CASE WHEN method_sum=0 THEN 0 ELSE (transfer_amt/NULLIF(method_sum,0))*revenue_needed END) FROM TAKE " +
            "  UNION ALL SELECT 'wallet',        SUM(CASE WHEN method_sum=0 THEN 0 ELSE (wallet_amt/NULLIF(method_sum,0))*revenue_needed END) FROM TAKE " +
            "  UNION ALL SELECT 'cash',          SUM(CASE WHEN method_sum=0 THEN revenue_needed ELSE (cash_amt/NULLIF(method_sum,0))*revenue_needed END) FROM TAKE " +
            "), REF AS ( " +
            "  SELECT LOWER(ri.refund_method) AS method, SUM(ISNULL(ri.refund_amount,0)) AS amt_refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY LOWER(ri.refund_method) " +
            ") " +
            "SELECT COALESCE(PM.method, REF.method) AS method, " +
            "       ISNULL(PM.amt_paid,0)  AS amt_paid, " +
            "       ISNULL(REF.amt_refunded,0) AS amt_refunded " +
            "FROM PAID_METHOD PM FULL OUTER JOIN REF ON REF.method = PM.method " +
            "ORDER BY method";
        List<AdminPaymentMethodStat> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setTimestamp(5, ts(from)); ps.setTimestamp(6, ts(from));
            ps.setTimestamp(7, ts(to));   ps.setTimestamp(8, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminPaymentMethodStat it = new AdminPaymentMethodStat();
                    it.setMethod(rs.getString("method"));
                    it.setPaidAmount(rs.getBigDecimal("amt_paid")==null?0:rs.getBigDecimal("amt_paid").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("amt_refunded")==null?0:rs.getBigDecimal("amt_refunded").doubleValue());
                    out.add(it);
                }
            }
        }
        return out;
    }

    // ===== MONTHLY for overview “Theo tháng”, anchor inspected_at =====
    @Override
    public List<AdminDailyRevenuePoint> getDailyRevenue(Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, ri.inspected_at, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id, ri.inspected_at " +
            "), Gross AS ( " +
            "  SELECT c.order_id, DATEFROMPARTS(YEAR(c.inspected_at), MONTH(c.inspected_at), 1) AS m, " +
            "         ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0) AS gross, c.refunded " +
            "  FROM Completed c JOIN RentalOrders r ON r.order_id = c.order_id " +
            ") " +
            "SELECT m, SUM(ISNULL(gross,0)) AS amt_collected, SUM(ISNULL(refunded,0)) AS amt_refunded " +
            "FROM Gross GROUP BY m ORDER BY m";
        List<AdminDailyRevenuePoint> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminDailyRevenuePoint it = new AdminDailyRevenuePoint();
                    it.setDay(rs.getDate("m"));
                    it.setPaidAmount(rs.getBigDecimal("amt_collected")==null?0:rs.getBigDecimal("amt_collected").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("amt_refunded")==null?0:rs.getBigDecimal("amt_refunded").doubleValue());
                    out.add(it);
                }
            }
        }
        return out;
    }

    // ===== PAYMENTS =====
    @Override
    public List<AdminRevenueItem> getPayments(Date from, Date to, int offset, int pageSize) throws Exception {
        String sql =
            "SELECT p.payment_id, p.order_id, p.amount, p.method, p.status, " +
            "       p.payment_date, p.verified_at, p.reference, c.full_name AS customer_name, " +
            "       ro.total_price, COALESCE(ro.deposit_amount,0) AS deposit_amount, " +
            "       COALESCE(agg.paid_sum,0) AS paid_sum, " +
            "       CASE WHEN EXISTS (SELECT 1 FROM RefundInspections ri WHERE ri.order_id = p.order_id AND ri.refund_status = 'completed') THEN 1 ELSE 0 END AS inspection_verified, " +
            "       CAST(ISNULL(ro.total_price,0) + ISNULL(ro.deposit_amount,0) AS decimal(18,2)) AS totalPaid, " +
            "       CAST(ISNULL(rf.refundedAmount,0) AS decimal(18,2)) AS refundedAmount " +
            "FROM Payments p " +
            "JOIN RentalOrders ro ON ro.order_id = p.order_id " +
            "JOIN Customers c ON c.customer_id = ro.customer_id " +
            "LEFT JOIN (SELECT order_id, SUM(amount) AS paid_sum FROM Payments WHERE status='paid' GROUP BY order_id) agg ON agg.order_id = p.order_id " +
            "LEFT JOIN ( " +
            "   SELECT order_id, SUM(CASE WHEN refund_status='completed' THEN ISNULL(refund_amount,0) ELSE 0 END) AS refundedAmount " +
            "   FROM RefundInspections GROUP BY order_id " +
            ") rf ON rf.order_id = p.order_id " +
            "WHERE (? IS NULL OR COALESCE(p.verified_at, p.payment_date) >= ?) " +
            "  AND (? IS NULL OR COALESCE(p.verified_at, p.payment_date) < DATEADD(day,1,?)) " +
            "  AND p.status='paid' " +
            "ORDER BY COALESCE(p.verified_at, p.payment_date) DESC, p.payment_id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminRevenueItem> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, Math.max(0, offset));
            ps.setInt(6, Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminRevenueItem it = new AdminRevenueItem();
                    it.setPaymentId(rs.getInt("payment_id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setAmount(rs.getBigDecimal("amount").doubleValue());
                    it.setMethod(rs.getString("method"));
                    it.setStatus(rs.getString("status"));
                    it.setPaymentDate(rs.getTimestamp("payment_date"));
                    it.setVerifiedAt(rs.getTimestamp("verified_at"));
                    it.setReference(rs.getString("reference"));
                    it.setCustomerName(rs.getString("customer_name"));

                    double totalPrice = rs.getBigDecimal("total_price")==null?0:rs.getBigDecimal("total_price").doubleValue();
                    double deposit    = rs.getBigDecimal("deposit_amount")==null?0:rs.getBigDecimal("deposit_amount").doubleValue();
                    double paidSum    = rs.getBigDecimal("paid_sum")==null?0:rs.getBigDecimal("paid_sum").doubleValue();
                    boolean verified  = rs.getInt("inspection_verified")==1;

                    it.setOrderTotalPrice(totalPrice);
                    it.setDepositAmount(deposit);
                    it.setOrderPaid(paidSum);
                    it.setInspectionVerified(verified);
                    it.setDueBeforeVerify((totalPrice + deposit) - paidSum);

                    // Bổ sung mapping hiển thị
                    it.setTotalPaid(rs.getBigDecimal("totalPaid")==null?0:rs.getBigDecimal("totalPaid").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("refundedAmount")==null?0:rs.getBigDecimal("refundedAmount").doubleValue());
                    // netRevenue tự tính từ setters trên

                    out.add(it);
                }
            }
        }
        return out;
    }

    @Override
    public int countPayments(Date from, Date to) throws Exception {
        String sql =
            "SELECT COUNT(*) AS cnt FROM Payments " +
            "WHERE (? IS NULL OR COALESCE(verified_at, payment_date) >= ?) " +
            "  AND (? IS NULL OR COALESCE(verified_at, payment_date) < DATEADD(day,1,?)) " +
            "  AND status='paid'";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt("cnt") : 0; }
        }
    }

    // ===== REFUNDS =====
    @Override
    public List<AdminRefundItem> getRefunds(Date from, Date to, int offset, int pageSize) throws Exception {
        String sql =
            "SELECT ri.inspection_id, ri.order_id, c.full_name AS customer_name, " +
            "       ri.refund_amount AS amount, ri.refund_method AS method, " +
            "       ri.inspected_at AS payment_date, ri.inspected_at AS verified_at, " +
            "       CAST(NULL AS nvarchar(64)) AS reference " +
            "FROM RefundInspections ri " +
            "JOIN RentalOrders ro ON ro.order_id = ri.order_id " +
            "JOIN Customers c     ON c.customer_id = ro.customer_id " +
            "WHERE (? IS NULL OR ri.inspected_at >= ?) " +
            "  AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  AND (ri.refund_status IS NULL OR ri.refund_status='completed') " +
            "ORDER BY ri.inspected_at DESC, ri.inspection_id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminRefundItem> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, Math.max(0, offset)); ps.setInt(6, Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminRefundItem it = new AdminRefundItem();
                    it.setPaymentId(rs.getInt("inspection_id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setCustomerName(rs.getString("customer_name"));
                    it.setAmount(rs.getBigDecimal("amount").doubleValue());
                    it.setMethod(rs.getString("method"));
                    it.setPaymentDate(rs.getTimestamp("payment_date"));
                    it.setVerifiedAt(rs.getTimestamp("verified_at"));
                    it.setReference(rs.getString("reference"));
                    out.add(it);
                }
            }
        }
        return out;
    }

    @Override
    public int countRefunds(Date from, Date to) throws Exception {
        String sql =
            "SELECT COUNT(*) AS cnt FROM RefundInspections " +
            "WHERE (? IS NULL OR inspected_at >= ?) " +
            "  AND (? IS NULL OR inspected_at < DATEADD(day,1,?)) " +
            "  AND (refund_status IS NULL OR refund_status='completed')";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt("cnt") : 0; }
        }
    }

    // ===== TOP CUSTOMERS =====
    @Override
    public List<AdminTopCustomerStat> getTopCustomers(Date from, Date to, int limit) throws Exception {
        int safeLimit = Math.max(1, Math.min(100, limit));
        String sql = "SELECT TOP " + safeLimit + " ro.customer_id, c.full_name, " +
                     "SUM(CASE WHEN p.status='paid' THEN p.amount ELSE 0 END) AS total_paid " +
                     "FROM Payments p " +
                     "JOIN RentalOrders ro ON ro.order_id = p.order_id " +
                     "JOIN Customers c ON c.customer_id = ro.customer_id " +
                     "WHERE (? IS NULL OR COALESCE(p.verified_at, p.payment_date) >= ?) " +
                     "  AND (? IS NULL OR COALESCE(p.verified_at, p.payment_date) < DATEADD(day,1,?)) " +
                     "  AND p.status='paid' " +
                     "GROUP BY ro.customer_id, c.full_name " +
                     "ORDER BY total_paid DESC";
        List<AdminTopCustomerStat> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminTopCustomerStat it = new AdminTopCustomerStat();
                    it.setCustomerId(rs.getInt("customer_id"));
                    it.setCustomerName(rs.getString("full_name"));
                    it.setTotalPaid(rs.getBigDecimal("total_paid")==null?0:rs.getBigDecimal("total_paid").doubleValue());
                    out.add(it);
                }
            }
        }
        return out;
    }

    // ===== OUTSTANDING =====
    @Override
    public List<AdminOutstandingOrderItem> getOutstanding(Date from, Date to, int offset, int pageSize) throws Exception {
        String sql =
            "WITH pay AS ( " +
            "  SELECT order_id, SUM(CASE WHEN status='paid' THEN amount ELSE 0 END) - SUM(CASE WHEN status='refunded' THEN amount ELSE 0 END) AS paid_amount " +
            "  FROM Payments " +
            "  WHERE (? IS NULL OR COALESCE(verified_at, payment_date) >= ?) " +
            "    AND (? IS NULL OR COALESCE(verified_at, payment_date) < DATEADD(day,1,?)) " +
            "    AND status IN ('paid','refunded') " +
            "  GROUP BY order_id " +
            ") " +
            "SELECT ro.order_id, ro.customer_id, c.full_name, ro.total_price, COALESCE(pay.paid_amount,0) AS paid_amount " +
            "FROM RentalOrders ro " +
            "JOIN Customers c ON c.customer_id = ro.customer_id " +
            "LEFT JOIN pay ON pay.order_id = ro.order_id " +
            "WHERE (ro.total_price - COALESCE(pay.paid_amount,0)) > 0 " +
            "ORDER BY (ro.total_price - COALESCE(pay.paid_amount,0)) DESC, ro.order_id DESC " +
            "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<AdminOutstandingOrderItem> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, Math.max(0, offset)); ps.setInt(6, Math.max(1, pageSize));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminOutstandingOrderItem it = new AdminOutstandingOrderItem();
                    it.setOrderId(rs.getInt("order_id"));
                    it.setCustomerId(rs.getInt("customer_id"));
                    it.setCustomerName(rs.getString("full_name"));
                    it.setTotalPrice(rs.getBigDecimal("total_price").doubleValue());
                    it.setPaidAmount(rs.getBigDecimal("paid_amount")==null?0:rs.getBigDecimal("paid_amount").doubleValue());
                    out.add(it);
                }
            }
        }
        return out;
    }

    @Override
    public int countOutstanding(Date from, Date to) throws Exception {
        String sql =
            "WITH pay AS ( " +
            "  SELECT order_id, SUM(CASE WHEN status='paid' THEN amount ELSE 0 END) - SUM(CASE WHEN status='refunded' THEN amount ELSE 0 END) AS paid_amount " +
            "  FROM Payments " +
            "  WHERE (? IS NULL OR COALESCE(verified_at, payment_date) >= ?) " +
            "    AND (? IS NULL OR COALESCE(verified_at, payment_date) < DATEADD(day,1,?)) " +
            "    AND status IN ('paid','refunded') " +
            "  GROUP BY order_id " +
            ") " +
            "SELECT COUNT(*) AS cnt FROM RentalOrders ro LEFT JOIN pay ON pay.order_id = ro.order_id WHERE (ro.total_price - COALESCE(pay.paid_amount,0)) > 0";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt("cnt") : 0; }
        }
    }

    // ===== PAYMENT BY ID =====
    @Override
    public AdminRevenueItem getPaymentById(int paymentId) throws Exception {
        String sql =
            "SELECT p.payment_id, p.order_id, p.amount, p.method, p.status, p.payment_date, p.verified_at, p.reference, " +
            "       c.full_name AS customer_name, ro.total_price, COALESCE(ro.deposit_amount,0) AS deposit_amount, " +
            "       COALESCE(agg.paid_sum,0) AS paid_sum, " +
            "       CASE WHEN EXISTS (SELECT 1 FROM RefundInspections ri WHERE ri.order_id = p.order_id AND ri.refund_status = 'completed') THEN 1 ELSE 0 END AS inspection_verified, " +
            "       CAST(ISNULL(ro.total_price,0) + ISNULL(ro.deposit_amount,0) AS decimal(18,2)) AS totalPaid, " +
            "       CAST(ISNULL(rf.refundedAmount,0) AS decimal(18,2)) AS refundedAmount " +
            "FROM Payments p " +
            "JOIN RentalOrders ro ON ro.order_id = p.order_id " +
            "JOIN Customers c ON c.customer_id = ro.customer_id " +
            "LEFT JOIN (SELECT order_id, SUM(amount) AS paid_sum FROM Payments WHERE status='paid' GROUP BY order_id) agg ON agg.order_id = p.order_id " +
            "LEFT JOIN ( " +
            "   SELECT order_id, SUM(CASE WHEN refund_status='completed' THEN ISNULL(refund_amount,0) ELSE 0 END) AS refundedAmount " +
            "   FROM RefundInspections GROUP BY order_id " +
            ") rf ON rf.order_id = p.order_id " +
            "WHERE p.payment_id = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    AdminRevenueItem it = new AdminRevenueItem();
                    it.setPaymentId(rs.getInt("payment_id"));
                    it.setOrderId(rs.getInt("order_id"));
                    it.setAmount(rs.getBigDecimal("amount").doubleValue());
                    it.setMethod(rs.getString("method"));
                    it.setStatus(rs.getString("status"));
                    it.setPaymentDate(rs.getTimestamp("payment_date"));
                    it.setVerifiedAt(rs.getTimestamp("verified_at"));
                    it.setReference(rs.getString("reference"));
                    it.setCustomerName(rs.getString("customer_name"));

                    double totalPrice = rs.getBigDecimal("total_price")==null?0:rs.getBigDecimal("total_price").doubleValue();
                    double deposit    = rs.getBigDecimal("deposit_amount")==null?0:rs.getBigDecimal("deposit_amount").doubleValue();
                    double paidSum    = rs.getBigDecimal("paid_sum")==null?0:rs.getBigDecimal("paid_sum").doubleValue();
                    boolean verified  = rs.getInt("inspection_verified")==1;

                    it.setOrderTotalPrice(totalPrice);
                    it.setDepositAmount(deposit);
                    it.setOrderPaid(paidSum);
                    it.setInspectionVerified(verified);
                    it.setDueBeforeVerify((totalPrice + deposit) - paidSum);

                    // Bổ sung mapping hiển thị
                    it.setTotalPaid(rs.getBigDecimal("totalPaid")==null?0:rs.getBigDecimal("totalPaid").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("refundedAmount")==null?0:rs.getBigDecimal("refundedAmount").doubleValue());
                    // netRevenue tự tính

                    return it;
                }
            }
        }
        return null;
    }

    // ===== ORDER DETAIL =====
    @Override
    public AdminOrderDetail getOrderDetail(int orderId) throws Exception {
        String qOrder =
            "SELECT ro.order_id, ro.customer_id, c.full_name, ro.total_price, ro.deposit_amount, ro.status, ro.pickup_status, ro.return_status, ro.created_at " +
            "FROM RentalOrders ro JOIN Customers c ON c.customer_id = ro.customer_id WHERE ro.order_id = ?";

        String qPayAgg =
            "SELECT (SELECT SUM(amount) FROM Payments WHERE order_id = ? AND status='paid') AS paid_amt, " +
            "       (SELECT SUM(refund_amount) FROM RefundInspections WHERE order_id = ? AND (refund_status IS NULL OR refund_status='completed')) AS refunded_amt";

        String qPayList =
            "SELECT p.payment_id, p.order_id, p.amount, p.method, p.status, p.payment_date, p.verified_at, p.reference, c.full_name AS customer_name " +
            "FROM Payments p JOIN RentalOrders ro ON ro.order_id = p.order_id JOIN Customers c ON c.customer_id = ro.customer_id " +
            "WHERE p.order_id = ? ORDER BY COALESCE(p.verified_at, p.payment_date) DESC, p.payment_id DESC";

        try (Connection cn = DBConnection.getConnection()) {
            AdminOrderDetail od = null;
            try (PreparedStatement ps = cn.prepareStatement(qOrder)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        od = new AdminOrderDetail();
                        od.setOrderId(rs.getInt("order_id"));
                        od.setCustomerId(rs.getInt("customer_id"));
                        od.setCustomerName(rs.getString("full_name"));
                        od.setTotalPrice(rs.getBigDecimal("total_price").doubleValue());
                        od.setDepositAmount(rs.getBigDecimal("deposit_amount")==null?0:rs.getBigDecimal("deposit_amount").doubleValue());
                        od.setStatus(rs.getString("status"));
                        od.setPickupStatus(rs.getString("pickup_status"));
                        od.setReturnStatus(rs.getString("return_status"));
                        od.setCreatedAt(rs.getTimestamp("created_at"));
                    }
                }
            }
            if (od == null) return null;

            try (PreparedStatement ps = cn.prepareStatement(qPayAgg)) {
                ps.setInt(1, orderId); ps.setInt(2, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        od.setPaidAmount(rs.getBigDecimal("paid_amt")==null?0:rs.getBigDecimal("paid_amt").doubleValue());
                        od.setRefundedAmount(rs.getBigDecimal("refunded_amt")==null?0:rs.getBigDecimal("refunded_amt").doubleValue());
                    }
                }
            }

            List<AdminRevenueItem> list = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(qPayList)) {
                ps.setInt(1, orderId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        AdminRevenueItem it = new AdminRevenueItem();
                        it.setPaymentId(rs.getInt("payment_id"));
                        it.setOrderId(rs.getInt("order_id"));
                        it.setAmount(rs.getBigDecimal("amount").doubleValue());
                        it.setMethod(rs.getString("method"));
                        it.setStatus(rs.getString("status"));
                        it.setPaymentDate(rs.getTimestamp("payment_date"));
                        it.setVerifiedAt(rs.getTimestamp("verified_at"));
                        it.setReference(rs.getString("reference"));
                        it.setCustomerName(rs.getString("customer_name"));
                        list.add(it);
                    }
                }
            }
            od.setPayments(list);
            return od;
        }
    }

    // ===== STORE REVENUE SUMMARY: 1 hàng Store + các hàng Partner; anchor returned_at =====
    @Override
    public List<AdminRevenueItem> getStoreRevenueSummary(Date from, Date to, Integer partnerId) throws Exception {
        String sql =
            // O: chia đều giá trị theo số dòng của mỗi order để tránh nhân đôi giữa nhiều xe
            "WITH O AS ( " +
            "  SELECT r.order_id, r.total_price, r.deposit_amount, r.returned_at, " +
            "         m.partner_id, m.store_id, " +
            "         1.0 / NULLIF(COUNT(*) OVER (PARTITION BY r.order_id),0) AS share " +
            "  FROM RentalOrders r " +
            "  JOIN OrderDetails od ON od.order_id = r.order_id " +
            "  JOIN Motorbikes  m  ON m.bike_id   = od.bike_id " +
            "  WHERE r.returned_at IS NOT NULL " +
            "    AND (? IS NULL OR r.returned_at >= ?) " +
            "    AND (? IS NULL OR r.returned_at < DATEADD(day,1,?)) " +
            "), RF AS ( " +
            "  SELECT ri.order_id, SUM(CASE WHEN ri.refund_status='completed' THEN ISNULL(ri.refund_amount,0) ELSE 0 END) AS refunded " +
            "  FROM RefundInspections ri GROUP BY ri.order_id " +
            "), L AS ( " +
            "  SELECT O.order_id, O.partner_id, O.store_id, " +
            "         CAST((ISNULL(O.total_price,0) + ISNULL(O.deposit_amount,0)) * O.share AS decimal(18,2)) AS gross_alloc, " +
            "         CAST(ISNULL(RF.refunded,0) * O.share AS decimal(18,2))                   AS refund_alloc " +
            "  FROM O LEFT JOIN RF ON RF.order_id = O.order_id " +
            "), AGG_P AS ( " +
            "  SELECT l.partner_id, " +
            "         COUNT(DISTINCT l.order_id) AS order_count, " +
            "         SUM(l.gross_alloc)        AS total_paid, " +
            "         SUM(l.refund_alloc)       AS refunded_amount, " +
            "         SUM(l.gross_alloc) - SUM(l.refund_alloc) AS net_revenue " +
            "  FROM L l WHERE l.partner_id IS NOT NULL GROUP BY l.partner_id " +
            "), AGG_S AS ( " +
            "  SELECT COUNT(DISTINCT l.order_id) AS order_count, " +
            "         SUM(l.gross_alloc)        AS total_paid, " +
            "         SUM(l.refund_alloc)       AS refunded_amount, " +
            "         SUM(l.gross_alloc) - SUM(l.refund_alloc) AS net_revenue " +
            "  FROM L l WHERE l.partner_id IS NULL " +
            ") " +
            // Hàng STORE (toàn bộ xe công ty)
            "SELECT 'store' AS kind, " +
            "       CAST(NULL AS int) AS partner_id, CAST(NULL AS nvarchar(200)) AS partner_name, " +
            "       CAST(NULL AS int) AS store_id,  CAST(N'Cửa hàng' AS nvarchar(200)) AS store_name, " +
            "       ISNULL(s.order_count,0)   AS order_count, " +
            "       ISNULL(s.total_paid,0)    AS total_paid, " +
            "       ISNULL(s.refunded_amount,0) AS refunded_amount, " +
            "       ISNULL(s.net_revenue,0)   AS net_revenue " +
            "FROM AGG_S s " +
            "UNION ALL " +
            // Các hàng PARTNER, liệt kê đủ partner
            "SELECT 'partner' AS kind, p.partner_id, p.company_name, " +
            "       CAST(NULL AS int) AS store_id, CAST(NULL AS nvarchar(200)) AS store_name, " +
            "       ISNULL(ap.order_count,0), ISNULL(ap.total_paid,0), ISNULL(ap.refunded_amount,0), ISNULL(ap.net_revenue,0) " +
            "FROM Partners p " +
            "LEFT JOIN AGG_P ap ON ap.partner_id = p.partner_id " +
            "WHERE (? IS NULL OR p.partner_id = ?) " +
            "ORDER BY kind DESC, partner_id";

        List<AdminRevenueItem> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            int i = 1;
            ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
            ps.setTimestamp(i++, ts(to));   ps.setTimestamp(i++, ts(to));
            if (partnerId == null) {
                ps.setNull(i++, Types.INTEGER);
                ps.setNull(i++, Types.INTEGER);
            } else {
                ps.setInt(i++, partnerId);
                ps.setInt(i++, partnerId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminRevenueItem it = new AdminRevenueItem();

                    int pid = rs.getInt("partner_id");
                    if (rs.wasNull()) it.setPartnerId(null); else it.setPartnerId(pid);
                    it.setPartnerName(rs.getString("partner_name"));

                    int sid = rs.getInt("store_id");
                    if (rs.wasNull()) it.setStoreId(null); else it.setStoreId(sid);
                    it.setStoreName(rs.getString("store_name"));

                    it.setOrderCount(rs.getInt("order_count"));
                    it.setTotalPaid(rs.getBigDecimal("total_paid")==null?0:rs.getBigDecimal("total_paid").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("refunded_amount")==null?0:rs.getBigDecimal("refunded_amount").doubleValue());
                    it.setNetRevenue(rs.getBigDecimal("net_revenue")==null?0:rs.getBigDecimal("net_revenue").doubleValue());

                    out.add(it);
                }
            }
        }
        return out;
    }

    // ===== DAILY theo ngày, anchor inspected_at =====
    @Override
    public List<AdminRevenueItem> findDailyRevenue(Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, CAST(ri.inspected_at AS date) AS d, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id, CAST(ri.inspected_at AS date) " +
            "), Gross AS ( " +
            "  SELECT c.d, c.order_id, (ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0)) AS gross, c.refunded " +
            "  FROM Completed c JOIN RentalOrders r ON r.order_id = c.order_id " +
            ") " +
            "SELECT d AS report_date, SUM(gross) AS total_collected, SUM(refunded) AS total_refunded, " +
            "       SUM(gross) - SUM(refunded) AS net_revenue " +
            "FROM Gross GROUP BY d ORDER BY report_date";
        List<AdminRevenueItem> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminRevenueItem it = new AdminRevenueItem();
                    java.util.Date d = rs.getDate("report_date");
                    if (d != null) it.setPaymentDate(new Timestamp(d.getTime()));
                    it.setTotalPaid(rs.getBigDecimal("total_collected")==null?0:rs.getBigDecimal("total_collected").doubleValue());
                    it.setRefundedAmount(rs.getBigDecimal("total_refunded")==null?0:rs.getBigDecimal("total_refunded").doubleValue());
                    it.setNetRevenue(rs.getBigDecimal("net_revenue")==null?0:rs.getBigDecimal("net_revenue").doubleValue());
                    out.add(it);
                }
            }
        }
        return out;
    }

    @Override
    public List<AdminOrderDetail> findRevenueOrders(Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id " +
            ") " +
            "SELECT r.order_id, r.customer_id, c.full_name, " +
            "       ISNULL(r.total_price,0) AS total_price, ISNULL(r.deposit_amount,0) AS deposit_amount, " +
            "       ISNULL(comp.refunded,0) AS total_refunded " +
            "FROM RentalOrders r JOIN Customers c ON c.customer_id = r.customer_id " +
            "JOIN Completed comp ON comp.order_id = r.order_id " +
            "ORDER BY r.order_id DESC";
        List<AdminOrderDetail> out = new ArrayList<>();
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AdminOrderDetail d = new AdminOrderDetail();
                    d.setOrderId(rs.getInt("order_id"));
                    d.setCustomerId(rs.getInt("customer_id"));
                    d.setCustomerName(rs.getString("full_name"));
                    double price   = rs.getBigDecimal("total_price")==null?0:rs.getBigDecimal("total_price").doubleValue();
                    double deposit = rs.getBigDecimal("deposit_amount")==null?0:rs.getBigDecimal("deposit_amount").doubleValue();
                    double refunded= rs.getBigDecimal("total_refunded")==null?0:rs.getBigDecimal("total_refunded").doubleValue();
                    d.setTotalPrice(price);
                    d.setDepositAmount(deposit);
                    d.setPaidAmount(price + deposit);
                    d.setRefundedAmount(refunded);
                    out.add(d);
                }
            }
        }
        return out;
    }
}
