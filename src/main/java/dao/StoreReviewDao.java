package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.StoreReview;
import utils.DBConnection;

public class StoreReviewDao implements IStoreReviewDao {

    // Trong file dao/StoreReviewDao.java

@Override
public List<StoreReview> findAll() throws SQLException {
    List<StoreReview> reviews = new ArrayList<>();
    
    // ⭐️ SỬA: Liệt kê rõ ràng tất cả các cột từ StoreReviews và Accounts
    String sql = "SELECT SR.store_review_id, SR.customer_id, SR.store_id, SR.rating, SR.comment, SR.created_at, A.username AS customerName " 
               + "FROM StoreReviews SR "
               + "LEFT JOIN Accounts A ON SR.customer_id = A.account_id " 
               + "ORDER BY SR.created_at DESC"; 

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            StoreReview review = new StoreReview();
            
            review.setStoreReviewId(rs.getInt("store_review_id"));
            review.setCustomerId(rs.getInt("customer_id"));
            review.setStoreId(rs.getInt("store_id"));
            review.setRating(rs.getInt("rating"));
            review.setComment(rs.getString("comment"));

            review.setCustomerName(rs.getString("customerName")); 

            Timestamp ts = rs.getTimestamp("created_at");
            if (ts != null) {
                review.setCreatedAt(ts.toLocalDateTime());
            }

            reviews.add(review);
        }
    }
    return reviews;
}

    @Override
public boolean insertReview(int customerId, int rating, String comment) {
    String sql = "INSERT INTO StoreReviews (customer_id, store_id, rating, comment, created_at) VALUES (?, ?, ?, ?, GETDATE())";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, customerId);
        ps.setInt(2, 1); // store_id luôn là 1
        ps.setInt(3, rating);
        ps.setString(4, comment);

        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        e.printStackTrace();
    }

    return false;
}
}