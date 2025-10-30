package dao;

import model.OrderSummary;
import model.OrderDetailItem;
import model.PaymentInfo;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdminOrderDAO implements IAdminOrderDAO {

    // CHÚ Ý: dùng java.util.Date ở chữ ký
    private String buildWhere(String status, String kw, java.util.Date from, java.util.Date to, List<Object> params) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1 ");
        if (status != null && !status.isBlank()) {
            sb.append(" AND s.order_status = ? ");
            params.add(status);
        }
        if (kw != null && !kw.isBlank()) {
            sb.append(" AND s.customer_name LIKE ? ");
            params.add("%" + kw.trim() + "%");
        }
        if (from != null) {
            sb.append(" AND s.order_created_at >= ? ");
            params.add(new Timestamp(from.getTime())); // java.sql.Timestamp
        }
        if (to != null) {
            sb.append(" AND s.order_created_at < DATEADD(DAY,1,?) ");
            params.add(new Timestamp(to.getTime()));
        }
        return sb.toString();
    }

    @Override
    public List<OrderSummary> findOrders(String status, String kw, java.util.Date from, java.util.Date to, int page, int pageSize) throws Exception {
        List<Object> ps = new ArrayList<>();
        String where = buildWhere(status, kw, from, to, ps);

        String sql =
            "SELECT * FROM (" +
            "  SELECT s.order_id, s.customer_id, s.customer_name, s.start_date, s.end_date, " +
            "         s.order_total, s.order_status, s.order_created_at, s.total_paid, s.payment_count, s.last_paid_at, s.amount_due, " +
            "         ROW_NUMBER() OVER (ORDER BY s.order_created_at DESC) rn " +
            "  FROM vw_OrderPaymentSummary s " + where +
            ") t WHERE t.rn BETWEEN ? AND ?";

        int start = (page - 1) * pageSize + 1;
        int end   = page * pageSize;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            int idx = 1;
            for (Object p : ps) st.setObject(idx++, p);
            st.setInt(idx++, start);
            st.setInt(idx, end);

            try (ResultSet rs = st.executeQuery()) {
                List<OrderSummary> list = new ArrayList<>();
                while (rs.next()) {
                    OrderSummary o = new OrderSummary();
                    o.setOrderId(rs.getInt("order_id"));
                    o.setCustomerId(rs.getInt("customer_id"));
                    o.setCustomerName(rs.getString("customer_name"));
                    o.setStartDate(rs.getDate("start_date"));      // OK
                    o.setEndDate(rs.getDate("end_date"));          // OK
                    o.setOrderTotal(rs.getBigDecimal("order_total"));
                    o.setOrderStatus(rs.getString("order_status"));
                    o.setCreatedAt(rs.getTimestamp("order_created_at"));
                    o.setTotalPaid(rs.getBigDecimal("total_paid"));
                    o.setPaymentCount(rs.getInt("payment_count"));
                    o.setLastPaidAt(rs.getTimestamp("last_paid_at"));
                    o.setAmountDue(rs.getBigDecimal("amount_due"));
                    list.add(o);
                }
                return list;
            }
        }
    }

    @Override
    public int countOrders(String status, String kw, java.util.Date from, java.util.Date to) throws Exception {
        List<Object> ps = new ArrayList<>();
        String where = buildWhere(status, kw, from, to, ps);
        String sql = "SELECT COUNT(1) cnt FROM vw_OrderPaymentSummary s " + where;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            int idx = 1;
            for (Object p : ps) st.setObject(idx++, p);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next() ? rs.getInt("cnt") : 0;
            }
        }
    }

    @Override
    public Optional<OrderSummary> findOrderHeader(int orderId) throws Exception {
        String sql = "SELECT * FROM vw_OrderPaymentSummary WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                OrderSummary o = new OrderSummary();
                o.setOrderId(rs.getInt("order_id"));
                o.setCustomerId(rs.getInt("customer_id"));
                o.setCustomerName(rs.getString("customer_name"));
                o.setStartDate(rs.getDate("start_date"));
                o.setEndDate(rs.getDate("end_date"));
                o.setOrderTotal(rs.getBigDecimal("order_total"));
                o.setOrderStatus(rs.getString("order_status"));
                o.setCreatedAt(rs.getTimestamp("order_created_at"));
                o.setTotalPaid(rs.getBigDecimal("total_paid"));
                o.setPaymentCount(rs.getInt("payment_count"));
                o.setLastPaidAt(rs.getTimestamp("last_paid_at"));
                o.setAmountDue(rs.getBigDecimal("amount_due"));
                return Optional.of(o);
            }
        }
    }

    @Override
    public List<model.OrderDetailItem> findOrderItems(int orderId) throws Exception {
        String sql =
            "SELECT d.detail_id, d.bike_id, m.bike_name, m.license_plate, d.price_per_day, d.quantity, d.line_total " +
            "FROM OrderDetails d JOIN Motorbikes m ON m.bike_id = d.bike_id " +
            "WHERE d.order_id = ? ORDER BY d.detail_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                List<model.OrderDetailItem> list = new ArrayList<>();
                while (rs.next()) {
                    model.OrderDetailItem i = new model.OrderDetailItem();
                    i.setDetailId(rs.getInt("detail_id"));
                    i.setBikeId(rs.getInt("bike_id"));
                    i.setBikeName(rs.getString("bike_name"));
                    i.setLicensePlate(rs.getString("license_plate"));
                    i.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    i.setQuantity(rs.getInt("quantity"));
                    i.setLineTotal(rs.getBigDecimal("line_total"));
                    list.add(i);
                }
                return list;
            }
        }
    }

    @Override
    public List<model.PaymentInfo> findPayments(int orderId) throws Exception {
        String sql =
            "SELECT payment_id, payment_date, amount, method, status, verified_by, verified_at " +
            "FROM Payments WHERE order_id = ? ORDER BY payment_date DESC, payment_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                List<model.PaymentInfo> list = new ArrayList<>();
                while (rs.next()) {
                    model.PaymentInfo p = new model.PaymentInfo();
                    p.setPaymentId(rs.getInt("payment_id"));
                    p.setPaymentDate(rs.getTimestamp("payment_date"));
                    p.setAmount(rs.getBigDecimal("amount"));
                    p.setMethod(rs.getString("method"));
                    p.setStatus(rs.getString("status"));
                    int vb = rs.getInt("verified_by");
                    p.setVerifiedBy(rs.wasNull() ? null : vb);
                    p.setVerifiedAt(rs.getTimestamp("verified_at"));
                    list.add(p);
                }
                return list;
            }
        }
    }
    @Override
    public Optional<model.RefundInfo> findLatestRefund(int orderId) throws Exception {
        String sql =
            "SELECT TOP 1 inspection_id, order_id, admin_id, bike_condition, damage_notes, " +
            "       damage_fee, refund_amount, refund_method, refund_status, inspected_at " +
            "FROM RefundInspections WHERE order_id = ? " +
            "ORDER BY inspected_at DESC, inspection_id DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement st = con.prepareStatement(sql)) {
            st.setInt(1, orderId);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                model.RefundInfo r = new model.RefundInfo();
                r.setInspectionId(rs.getInt("inspection_id"));
                r.setOrderId(rs.getInt("order_id"));
                r.setAdminId(rs.getInt("admin_id"));
                r.setBikeCondition(rs.getString("bike_condition"));
                r.setDamageNotes(rs.getString("damage_notes"));
                r.setDamageFee(rs.getBigDecimal("damage_fee"));
                r.setRefundAmount(rs.getBigDecimal("refund_amount"));
                r.setRefundMethod(rs.getString("refund_method"));
                r.setRefundStatus(rs.getString("refund_status"));
                r.setInspectedAt(rs.getTimestamp("inspected_at"));
                return Optional.of(r);
            }
        }
    }

}
