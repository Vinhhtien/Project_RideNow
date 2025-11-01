    package dao;

import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import model.Review;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReviewDao implements IReviewDao {

    @Override
    public List<Review> findReviewByBikeId(int bikeId) throws SQLException {
        List<Review> reviews = new ArrayList<>();

        String sql = "SELECT review_id, customer_id, bike_id, rating, comment, created_at "
                + "FROM MotorbikeRentalDB.dbo.Reviews WHERE bike_id = ?";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bikeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setBikeId(rs.getInt("bike_id"));
                review.setRating(rs.getInt("rating"));
                review.setComment(rs.getString("comment"));

                Timestamp timestamp = rs.getTimestamp("created_at");
                if (timestamp != null) {
                    review.setCreatedAt(timestamp.toLocalDateTime());
                }

                reviews.add(review);
            }
        }

        return reviews;
    }

    @Override
    public List<model.Review> findAll() throws java.sql.SQLException {
        List<model.Review> reviews = new ArrayList<>();
        String sql = "SELECT review_id, customer_id, bike_id, rating, comment, created_at FROM Reviews";
        try (java.sql.Connection conn = utils.DBConnection.getConnection(); java.sql.PreparedStatement stmt = conn.prepareStatement(sql); java.sql.ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                model.Review r = new model.Review();
                r.setReviewId(rs.getInt("review_id"));
                r.setCustomerId(rs.getInt("customer_id"));
                r.setBikeId(rs.getInt("bike_id"));
                r.setRating(rs.getInt("rating"));
                r.setComment(rs.getString("comment"));

                java.sql.Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) {
                    r.setCreatedAt(ts.toLocalDateTime()); // nếu model dùng LocalDateTime
                }
                reviews.add(r);
            }
        }
        return reviews;
    }

    @Override
    public boolean insertReview(int customerId, int bikeId, int rating, String comment) {

        // ✅ Bắt buộc: LOẠI BỎ cột order_id khỏi SQL INSERT
        // Đảm bảo Reviews table có các cột: customer_id, bike_id, rating, comment, created_at
        String sql = "INSERT INTO Reviews (customer_id, bike_id, rating, comment, created_at) VALUES (?, ?, ?, ?, GETDATE())";

        try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            // Đảm bảo chỉ có 4 tham số được gán
            ps.setInt(1, customerId);
            ps.setInt(2, bikeId);
            ps.setInt(3, rating);
            ps.setString(4, comment);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace(); // Lỗi này sẽ in ra log server, giúp debug nếu vẫn lỗi.
            return false;
        }
    }
}
