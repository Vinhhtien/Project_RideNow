package dao;

import model.Review;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao implements IReviewDao {

    @Override
    public List<Review> findReviewByBikeId(int bikeId) throws Exception {
        List<Review> reviews = new ArrayList<>();

        String sql = """
            SELECT review_id, customer_id, bike_id, order_id,
                   rating, comment, created_at, updated_at
            FROM Reviews
            WHERE bike_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bikeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapRowToReview(rs));
                }
            }
        }

        return reviews;
    }

    @Override
    public List<Review> findAll() throws Exception {
        List<Review> reviews = new ArrayList<>();

        String sql = """
            SELECT review_id, customer_id, bike_id, order_id,
                   rating, comment, created_at, updated_at
            FROM Reviews
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reviews.add(mapRowToReview(rs));
            }
        }

        return reviews;
    }

    @Override
    public Review findByCustomerAndOrder(int customerId, int orderId) throws Exception {
        String sql = """
            SELECT TOP 1 review_id, customer_id, bike_id, order_id,
                   rating, comment, created_at, updated_at
            FROM Reviews
            WHERE customer_id = ? AND order_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            stmt.setInt(2, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToReview(rs);
                }
            }
        }

        return null;
    }

    @Override
    public boolean insertReview(int customerId, int bikeId, int orderId,
                                int rating, String comment) throws Exception {

        String sql = """
            INSERT INTO Reviews
              (customer_id, bike_id, order_id, rating, comment, created_at)
            VALUES (?, ?, ?, ?, ?, GETDATE())
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);
            ps.setInt(2, bikeId);
            ps.setInt(3, orderId);
            ps.setInt(4, rating);
            ps.setString(5, comment);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();  // debug nếu dính UNIQUE (customer + order)
            throw e;
        }
    }

    @Override
    public boolean updateReview(int reviewId, int rating, String comment) throws Exception {
        String sql = """
            UPDATE Reviews
            SET rating = ?,
                comment = ?,
                updated_at = GETDATE()
            WHERE review_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, rating);
            ps.setString(2, comment);
            ps.setInt(3, reviewId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    // =========================================
    // Helper: map 1 dòng ResultSet -> Review
    // =========================================
    private Review mapRowToReview(ResultSet rs) throws SQLException {
        Review review = new Review();

        review.setReviewId(rs.getInt("review_id"));
        review.setCustomerId(rs.getInt("customer_id"));
        review.setBikeId(rs.getInt("bike_id"));

        int orderId = rs.getInt("order_id");
        if (!rs.wasNull()) {
            review.setOrderId(orderId);
        }

        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));

        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            review.setCreatedAt(createdTs.toLocalDateTime());
        }

        Timestamp updatedTs = null;
        try {
            updatedTs = rs.getTimestamp("updated_at");
        } catch (SQLException ignore) {
            // phòng khi cột updated_at chưa tồn tại
        }
        if (updatedTs != null) {
            review.setUpdatedAt(updatedTs.toLocalDateTime());
        }

        return review;
    }
}
