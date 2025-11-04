package dao;

import model.report.PartnerReportSummary;
import model.report.PartnerBikeRevenueItem;
import model.report.PartnerStoreRevenueItem;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PartnerReportDao implements IPartnerReportDao {

    private static Timestamp ts(Date d) { return d == null ? null : new Timestamp(d.getTime()); }

    // ===== SUMMARY: anchor = inspected_at, gross = total_price + deposit_amount, split theo số dòng =====
    @Override
    public PartnerReportSummary getSummaryByPartner(int partnerId, Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id " +
            "), Orders AS ( " +
            "  SELECT r.order_id, CAST(ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0) AS decimal(18,2)) AS gross " +
            "  FROM RentalOrders r JOIN Completed c ON c.order_id = r.order_id " +
            "), Lines AS ( " +
            "  SELECT od.order_id, od.bike_id, " +
            "         CAST(1.0 / NULLIF(COUNT(*) OVER (PARTITION BY od.order_id),0) AS decimal(18,6)) AS share " +
            "  FROM OrderDetails od " +
            "), PartnerLines AS ( " +
            "  SELECT l.order_id, l.bike_id, l.share " +
            "  FROM Lines l JOIN Motorbikes b ON b.bike_id = l.bike_id " +
            "  WHERE b.partner_id = ? " +
            ") " +
            "SELECT " +
            "  COUNT(DISTINCT pl.order_id)                              AS total_orders, " +
            "  SUM(o.gross * ISNULL(pl.share,0))                        AS total_collected, " +
            "  SUM(ISNULL(c.refunded,0) * ISNULL(pl.share,0))           AS total_refunded " +
            "FROM PartnerLines pl " +
            "JOIN Orders o     ON o.order_id = pl.order_id " +
            "LEFT JOIN Completed c ON c.order_id = pl.order_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, partnerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int totalOrders = rs.getInt("total_orders");
                    BigDecimal collected = rs.getBigDecimal("total_collected");
                    BigDecimal refunded  = rs.getBigDecimal("total_refunded");
                    return new PartnerReportSummary(
                        totalOrders,
                        collected == null ? BigDecimal.ZERO : collected,
                        refunded  == null ? BigDecimal.ZERO : refunded
                    );
                }
            }
        }
        return new PartnerReportSummary(0, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    // ===== THEO XE: cùng công thức, group theo bike =====
    @Override
    public List<PartnerBikeRevenueItem> getBikeRevenueByPartner(int partnerId, Date from, Date to) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id " +
            "), Orders AS ( " +
            "  SELECT r.order_id, CAST(ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0) AS decimal(18,2)) AS gross " +
            "  FROM RentalOrders r JOIN Completed c ON c.order_id = r.order_id " +
            "), Lines AS ( " +
            "  SELECT od.order_id, od.bike_id, " +
            "         CAST(1.0 / NULLIF(COUNT(*) OVER (PARTITION BY od.order_id),0) AS decimal(18,6)) AS share " +
            "  FROM OrderDetails od " +
            "), PartnerLines AS ( " +
            "  SELECT l.order_id, l.bike_id, l.share " +
            "  FROM Lines l JOIN Motorbikes b ON b.bike_id = l.bike_id " +
            "  WHERE b.partner_id = ? " +
            ") " +
            "SELECT b.bike_id, b.bike_name, " +
            "       COUNT(DISTINCT pl.order_id)                                   AS orders, " +
            "       SUM(o.gross * ISNULL(pl.share,0))                              AS collected, " +
            "       SUM(ISNULL(c.refunded,0) * ISNULL(pl.share,0))                 AS refunded " +
            "FROM PartnerLines pl " +
            "JOIN Motorbikes b ON b.bike_id = pl.bike_id " +
            "JOIN Orders o     ON o.order_id = pl.order_id " +
            "LEFT JOIN Completed c ON c.order_id = pl.order_id " +
            "GROUP BY b.bike_id, b.bike_name " +
            "ORDER BY (SUM(o.gross * ISNULL(pl.share,0)) - SUM(ISNULL(c.refunded,0) * ISNULL(pl.share,0))) DESC";

        List<PartnerBikeRevenueItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, partnerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal collected = rs.getBigDecimal("collected");
                    BigDecimal refunded  = rs.getBigDecimal("refunded");
                    list.add(new PartnerBikeRevenueItem(
                        rs.getInt("bike_id"),
                        rs.getString("bike_name"),
                        rs.getInt("orders"),
                        collected == null ? BigDecimal.ZERO : collected,
                        refunded  == null ? BigDecimal.ZERO : refunded
                    ));
                }
            }
        }
        return list;
    }

    // ===== THEO CỬA HÀNG (đúng 1 partner), nhãn an toàn không đụng cột tên có thể thiếu =====
    @Override
    public List<PartnerStoreRevenueItem> getStoreRevenueByPartner(int partnerId, Date from, Date to, BigDecimal shareRate) throws Exception {
        String sql =
            "WITH Completed AS ( " +
            "  SELECT ri.order_id, SUM(ISNULL(ri.refund_amount,0)) AS refunded " +
            "  FROM RefundInspections ri " +
            "  WHERE ri.refund_status='completed' " +
            "    AND (? IS NULL OR ri.inspected_at >= ?) " +
            "    AND (? IS NULL OR ri.inspected_at < DATEADD(day,1,?)) " +
            "  GROUP BY ri.order_id " +
            "), Orders AS ( " +
            "  SELECT r.order_id, CAST(ISNULL(r.total_price,0) + ISNULL(r.deposit_amount,0) AS decimal(18,2)) AS gross " +
            "  FROM RentalOrders r JOIN Completed c ON c.order_id = r.order_id " +
            "), Lines AS ( " +
            "  SELECT od.order_id, od.bike_id, " +
            "         CAST(1.0 / NULLIF(COUNT(*) OVER (PARTITION BY od.order_id),0) AS decimal(18,6)) AS share " +
            "  FROM OrderDetails od " +
            "), PartnerLines AS ( " +
            "  SELECT l.order_id, l.bike_id, l.share " +
            "  FROM Lines l JOIN Motorbikes b ON b.bike_id = l.bike_id " +
            "  WHERE b.partner_id = ? " +
            ") " +
            "SELECT p.partner_id, " +
            "       COALESCE(NULLIF(p.company_name,''), CONCAT('Partner #', CAST(p.partner_id AS nvarchar(10)))) AS company_name, " +
            "       COUNT(DISTINCT pl.order_id)                                   AS orders, " +
            "       SUM(o.gross * ISNULL(pl.share,0))                              AS collected, " +
            "       SUM(ISNULL(c.refunded,0) * ISNULL(pl.share,0))                 AS refunded " +
            "FROM PartnerLines pl " +
            "JOIN Partners p ON p.partner_id = ? " + // chỉ để lấy tên an toàn
            "JOIN Orders o     ON o.order_id = pl.order_id " +
            "LEFT JOIN Completed c ON c.order_id = pl.order_id " +
            "GROUP BY p.partner_id, COALESCE(NULLIF(p.company_name,''), CONCAT('Partner #', CAST(p.partner_id AS nvarchar(10))))";

        List<PartnerStoreRevenueItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
            ps.setInt(5, partnerId);
            ps.setInt(6, partnerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BigDecimal collected = rs.getBigDecimal("collected");
                    BigDecimal refunded  = rs.getBigDecimal("refunded");
                    list.add(new PartnerStoreRevenueItem(
                        rs.getInt("partner_id"),
                        rs.getString("company_name"),
                        rs.getInt("orders"),
                        collected == null ? BigDecimal.ZERO : collected,
                        refunded  == null ? BigDecimal.ZERO : refunded,
                        shareRate == null ? BigDecimal.ZERO : shareRate
                    ));
                }
            }
        }
        return list;
    }
}
