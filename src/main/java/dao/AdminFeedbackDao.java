package dao;

import model.adminfeedback.*;
import utils.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdminFeedbackDao implements IAdminFeedbackDao {

    private static Timestamp ts(Date d) { return d == null ? null : new Timestamp(d.getTime()); }

    @Override
    public AdminFeedbackSummary getSummary(Date from, Date to) throws Exception {
        try (Connection cn = DBConnection.getConnection()) {
            boolean hasStore = tableExists(cn, "StoreReviews");

            // Chọn bảng đánh giá xe + cột id đúng theo schema
            String bikeTable = null;
            String bikeIdCol = null;
            if (tableExists(cn, "MotorbikeReviews")) {
                bikeTable = "MotorbikeReviews";
                bikeIdCol = "motorbike_id";
            } else if (tableExists(cn, "Reviews")) {
                bikeTable = "Reviews";
                bikeIdCol = "bike_id";
            }

            AdminFeedbackSummary sum = new AdminFeedbackSummary();
            sum.setFrom(from); sum.setTo(to);

            int storeCount = 0, bikeCount = 0;
            BigDecimal storeAvg = null, bikeAvg = null;
            int[] histS = new int[6], histB = new int[6];

            // ===== STORE =====
            if (hasStore) {
                String sql =
                    "SELECT COUNT(1) cnt, " +
                    "       CAST(AVG(CAST(r.rating AS DECIMAL(10,4))) AS DECIMAL(10,2)) avg, " +
                    "       SUM(CASE WHEN r.rating=1 THEN 1 ELSE 0 END) h1, " +
                    "       SUM(CASE WHEN r.rating=2 THEN 1 ELSE 0 END) h2, " +
                    "       SUM(CASE WHEN r.rating=3 THEN 1 ELSE 0 END) h3, " +
                    "       SUM(CASE WHEN r.rating=4 THEN 1 ELSE 0 END) h4, " +
                    "       SUM(CASE WHEN r.rating=5 THEN 1 ELSE 0 END) h5 " +
                    "FROM StoreReviews r " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?))";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
                    ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            storeCount = rs.getInt("cnt");
                            storeAvg = rs.getBigDecimal("avg");
                            histS[1] = rs.getInt("h1");
                            histS[2] = rs.getInt("h2");
                            histS[3] = rs.getInt("h3");
                            histS[4] = rs.getInt("h4");
                            histS[5] = rs.getInt("h5");
                        }
                    }
                }

                String tops =
                    "SELECT TOP 5 r.store_id AS id, " +
                    "       ISNULL(s.store_name, CONCAT('Store #', r.store_id)) AS name, " +
                    "       CAST(AVG(CAST(r.rating AS DECIMAL(10,4))) AS DECIMAL(10,2)) AS avg_rating, " +
                    "       COUNT(1) AS cnt " +
                    "FROM StoreReviews r " +
                    "LEFT JOIN Stores s ON s.store_id = r.store_id " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    "GROUP BY r.store_id, s.store_name " +
                    "ORDER BY avg_rating DESC, cnt DESC";
                try (PreparedStatement ps = cn.prepareStatement(tops)) {
                    ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
                    ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
                    try (ResultSet rs = ps.executeQuery()) {
                        List<TopRow> topS = new ArrayList<>();
                        while (rs.next()) {
                            topS.add(new TopRow(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getBigDecimal("avg_rating"),
                                rs.getInt("cnt")));
                        }
                        sum.setTopStores(topS);
                    }
                }
            }

            // ===== BIKE =====
            if (bikeTable != null) {
                String sql =
                    "SELECT COUNT(1) cnt, " +
                    "       CAST(AVG(CAST(r.rating AS DECIMAL(10,4))) AS DECIMAL(10,2)) avg, " +
                    "       SUM(CASE WHEN r.rating=1 THEN 1 ELSE 0 END) h1, " +
                    "       SUM(CASE WHEN r.rating=2 THEN 1 ELSE 0 END) h2, " +
                    "       SUM(CASE WHEN r.rating=3 THEN 1 ELSE 0 END) h3, " +
                    "       SUM(CASE WHEN r.rating=4 THEN 1 ELSE 0 END) h4, " +
                    "       SUM(CASE WHEN r.rating=5 THEN 1 ELSE 0 END) h5 " +
                    "FROM " + bikeTable + " r " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?))";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
                    ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            bikeCount = rs.getInt("cnt");
                            bikeAvg = rs.getBigDecimal("avg");
                            histB[1] = rs.getInt("h1");
                            histB[2] = rs.getInt("h2");
                            histB[3] = rs.getInt("h3");
                            histB[4] = rs.getInt("h4");
                            histB[5] = rs.getInt("h5");
                        }
                    }
                }

                String tops =
                    "SELECT TOP 5 r." + bikeIdCol + " AS id, " +
                    "       ISNULL(m.bike_name, CONCAT('Bike #', r." + bikeIdCol + ")) AS name, " +
                    "       CAST(AVG(CAST(r.rating AS DECIMAL(10,4))) AS DECIMAL(10,2)) AS avg_rating, " +
                    "       COUNT(1) AS cnt " +
                    "FROM " + bikeTable + " r " +
                    "LEFT JOIN Motorbikes m ON m.bike_id = r." + bikeIdCol + " " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    "GROUP BY r." + bikeIdCol + ", m.bike_name " +
                    "ORDER BY avg_rating DESC, cnt DESC";
                try (PreparedStatement ps = cn.prepareStatement(tops)) {
                    ps.setTimestamp(1, ts(from)); ps.setTimestamp(2, ts(from));
                    ps.setTimestamp(3, ts(to));   ps.setTimestamp(4, ts(to));
                    try (ResultSet rs = ps.executeQuery()) {
                        List<TopRow> topB = new ArrayList<>();
                        while (rs.next()) {
                            topB.add(new TopRow(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getBigDecimal("avg_rating"),
                                rs.getInt("cnt")));
                        }
                        sum.setTopBikes(topB);
                    }
                }
            }

            // ===== OVERALL =====
            sum.setCountStore(storeCount);
            sum.setCountBike(bikeCount);
            sum.setCountOverall(storeCount + bikeCount);
            sum.setAvgStore(storeAvg);
            sum.setAvgBike(bikeAvg);

            int totalCnt = storeCount + bikeCount;
            if (totalCnt > 0) {
                BigDecimal s = (storeAvg != null) ? storeAvg.multiply(BigDecimal.valueOf(storeCount)) : BigDecimal.ZERO;
                BigDecimal b = (bikeAvg  != null) ? bikeAvg .multiply(BigDecimal.valueOf(bikeCount )) : BigDecimal.ZERO;
                BigDecimal overall = s.add(b).divide(BigDecimal.valueOf(totalCnt), 2, RoundingMode.HALF_UP);
                sum.setAvgOverall(overall);
            } else {
                sum.setAvgOverall(null);
            }

            sum.setHistStore(histS);
            sum.setHistBike(histB);
            return sum;
        }
    }

    @Override
    public List<AdminFeedbackItem> findAll(Date from, Date to,
                                           FeedbackType type, Integer star,
                                           int offset, int limit) throws Exception {
        try (Connection cn = DBConnection.getConnection()) {
            boolean hasStore = tableExists(cn, "StoreReviews");

            String bikeTable = null;
            String bikeIdCol = null;
            if (tableExists(cn, "MotorbikeReviews")) {
                bikeTable = "MotorbikeReviews";
                bikeIdCol = "motorbike_id";
            } else if (tableExists(cn, "Reviews")) {
                bikeTable = "Reviews";
                bikeIdCol = "bike_id";
            }

            List<AdminFeedbackItem> out = new ArrayList<>();

            // ===== STORE only =====
            if (type == FeedbackType.STORE || (bikeTable == null && hasStore)) {
                String sql =
                    "SELECT r.store_id AS target_id, " +
                    "       ISNULL(s.store_name, CONCAT('Store #', r.store_id)) AS target_name, " +
                    "       r.rating, NULL AS title, " +
                    "       r.comment AS content, r.created_at, " +
                    "       NULL AS order_id, r.customer_id, c.full_name AS customer_name " +
                    "FROM StoreReviews r " +
                    "LEFT JOIN Stores s    ON s.store_id = r.store_id " +
                    "LEFT JOIN Customers c ON c.customer_id = r.customer_id " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    (star != null ? "  AND r.rating = ? " : "") +
                    "ORDER BY r.created_at DESC, r.store_id DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to));   ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    ps.setInt(i++, Math.max(0, offset));
                    ps.setInt(i,   Math.max(1, limit));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            AdminFeedbackItem it = new AdminFeedbackItem();
                            it.setType(FeedbackType.STORE);
                            int id = rs.getInt("target_id");
                            it.setTargetId(id);
                            it.setTargetCode("Store#" + id);
                            it.setTargetName(rs.getString("target_name"));
                            it.setRating(rs.getInt("rating"));
                            it.setTitle(rs.getString("title")); // null
                            it.setContent(rs.getString("content"));
                            it.setCreatedAt(rs.getTimestamp("created_at"));
                            it.setOrderId(null);
                            int cid = rs.getInt("customer_id"); it.setCustomerId(rs.wasNull()? null: cid);
                            it.setCustomerName(rs.getString("customer_name"));
                            out.add(it);
                        }
                    }
                }
                return out;
            }

            // ===== BIKE only =====
            if (type == FeedbackType.BIKE || (!hasStore && bikeTable != null)) {
                String sql =
                    "SELECT r." + bikeIdCol + " AS target_id, " +
                    "       ISNULL(m.bike_name, CONCAT('Bike #', r." + bikeIdCol + ")) AS target_name, " +
                    "       r.rating, NULL AS title, " +
                    "       r.comment AS content, r.created_at, " +
                    "       NULL AS order_id, r.customer_id, c.full_name AS customer_name " +
                    "FROM " + bikeTable + " r " +
                    "LEFT JOIN Motorbikes m ON m.bike_id = r." + bikeIdCol + " " +
                    "LEFT JOIN Customers c   ON c.customer_id = r.customer_id " +
                    "WHERE (? IS NULL OR r.created_at >= ?) " +
                    "  AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    (star != null ? "  AND r.rating = ? " : "") +
                    "ORDER BY r.created_at DESC, r." + bikeIdCol + " DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    int i = 1;
                    ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to));   ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    ps.setInt(i++, Math.max(0, offset));
                    ps.setInt(i,   Math.max(1, limit));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            AdminFeedbackItem it = new AdminFeedbackItem();
                            it.setType(FeedbackType.BIKE);
                            int id = rs.getInt("target_id");
                            it.setTargetId(id);
                            it.setTargetCode("Bike#" + id);
                            it.setTargetName(rs.getString("target_name"));
                            it.setRating(rs.getInt("rating"));
                            it.setTitle(rs.getString("title")); // null
                            it.setContent(rs.getString("content"));
                            it.setCreatedAt(rs.getTimestamp("created_at"));
                            it.setOrderId(null);
                            int cid = rs.getInt("customer_id"); it.setCustomerId(rs.wasNull()? null: cid);
                            it.setCustomerName(rs.getString("customer_name"));
                            out.add(it);
                        }
                    }
                }
                return out;
            }

            // ===== BOTH → UNION ALL =====
            if (hasStore && bikeTable != null) {
                String sql =
                    "WITH S AS ( " +
                    "  SELECT 'STORE' AS t, r.created_at, r.store_id AS target_id, " +
                    "         ISNULL(s.store_name, CONCAT('Store #', r.store_id)) AS target_name, " +
                    "         r.rating, NULL AS title, r.comment AS content, " +
                    "         NULL AS order_id, r.customer_id, c.full_name AS customer_name " +
                    "  FROM StoreReviews r " +
                    "  LEFT JOIN Stores s    ON s.store_id = r.store_id " +
                    "  LEFT JOIN Customers c ON c.customer_id = r.customer_id " +
                    "  WHERE (? IS NULL OR r.created_at >= ?) " +
                    "    AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    (star != null ? "    AND r.rating = ? " : "") +
                    "), B AS ( " +
                    "  SELECT 'BIKE' AS t, r.created_at, r." + bikeIdCol + " AS target_id, " +
                    "         ISNULL(m.bike_name, CONCAT('Bike #', r." + bikeIdCol + ")) AS target_name, " +
                    "         r.rating, NULL AS title, r.comment AS content, " +
                    "         NULL AS order_id, r.customer_id, c.full_name AS customer_name " +
                    "  FROM " + bikeTable + " r " +
                    "  LEFT JOIN Motorbikes m ON m.bike_id = r." + bikeIdCol + " " +
                    "  LEFT JOIN Customers c   ON c.customer_id = r.customer_id " +
                    "  WHERE (? IS NULL OR r.created_at >= ?) " +
                    "    AND (? IS NULL OR r.created_at < DATEADD(day,1,?)) " +
                    (star != null ? "    AND r.rating = ? " : "") +
                    ") " +
                    "SELECT t, created_at, target_id, target_name, rating, title, content, order_id, customer_id, customer_name " +
                    "FROM (SELECT * FROM S UNION ALL SELECT * FROM B) X " +
                    "ORDER BY created_at DESC, target_id DESC " +
                    "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";

                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    int i = 1;
                    // S params
                    ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to));   ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    // B params
                    ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to));   ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    // paging
                    ps.setInt(i++, Math.max(0, offset));
                    ps.setInt(i,   Math.max(1, limit));
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String t = rs.getString("t");
                            AdminFeedbackItem it = new AdminFeedbackItem();
                            it.setType("STORE".equals(t) ? FeedbackType.STORE : FeedbackType.BIKE);
                            int id = rs.getInt("target_id");
                            it.setTargetId(id);
                            it.setTargetCode(("STORE".equals(t) ? "Store#" : "Bike#") + id);
                            it.setTargetName(rs.getString("target_name"));
                            it.setRating(rs.getInt("rating"));
                            it.setTitle(rs.getString("title")); // null
                            it.setContent(rs.getString("content"));
                            it.setCreatedAt(rs.getTimestamp("created_at"));
                            it.setOrderId(null);
                            int cid = rs.getInt("customer_id"); it.setCustomerId(rs.wasNull()? null: cid);
                            it.setCustomerName(rs.getString("customer_name"));
                            out.add(it);
                        }
                    }
                }
                return out;
            }

            return out;
        }
    }

    @Override
    public int countAll(Date from, Date to, FeedbackType type, Integer star) throws Exception {
        try (Connection cn = DBConnection.getConnection()) {
            boolean hasStore = tableExists(cn, "StoreReviews");

            String bikeTable = null;
            if (tableExists(cn, "MotorbikeReviews")) {
                bikeTable = "MotorbikeReviews";
            } else if (tableExists(cn, "Reviews")) {
                bikeTable = "Reviews";
            }

            int total = 0;

            if ((type == null || type == FeedbackType.STORE) && hasStore) {
                String sql = "SELECT COUNT(1) FROM StoreReviews r " +
                        "WHERE (? IS NULL OR r.created_at >= ?) AND (? IS NULL OR r.created_at < DATEADD(day,1,?))" +
                        (star != null ? " AND r.rating=?" : "");
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    int i=1; ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to)); ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total += rs.getInt(1); }
                }
            }

            if ((type == null || type == FeedbackType.BIKE) && bikeTable != null) {
                String sql = "SELECT COUNT(1) FROM " + bikeTable + " r " +
                        "WHERE (? IS NULL OR r.created_at >= ?) AND (? IS NULL OR r.created_at < DATEADD(day,1,?))" +
                        (star != null ? " AND r.rating=?" : "");
                try (PreparedStatement ps = cn.prepareStatement(sql)) {
                    int i=1; ps.setTimestamp(i++, ts(from)); ps.setTimestamp(i++, ts(from));
                    ps.setTimestamp(i++, ts(to)); ps.setTimestamp(i++, ts(to));
                    if (star != null) ps.setInt(i++, star);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) total += rs.getInt(1); }
                }
            }

            return total;
        }
    }

    // ===== helpers =====
    private boolean tableExistsAny(Connection cn, String... names) throws SQLException {
        for (String n : names) if (tableExists(cn, n)) return true;
        return false;
    }
    private boolean tableExists(Connection cn, String name) throws SQLException {
        String sql = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }
}
