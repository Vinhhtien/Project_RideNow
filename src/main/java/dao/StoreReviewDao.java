package dao;

import model.StoreReview;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreReviewDao implements IStoreReviewDao {

    @Override
    public List<StoreReview> findAll() {
        List<StoreReview> reviews = new ArrayList<>();
        String sql = "SELECT sr.*, a.username as customer_name " +
                    "FROM StoreReviews sr " +
                    "LEFT JOIN Accounts a ON sr.customer_id = a.account_id " +
                    "ORDER BY sr.created_at DESC";

        System.out.println("🔍 DAO - Executing findAll query");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                StoreReview review = new StoreReview();
                review.setStoreReviewId(rs.getInt("store_review_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setStoreId(rs.getInt("store_id"));
                review.setRating(rs.getInt("rating"));

                String comment = rs.getString("comment");
                if (comment != null) {
                    comment = comment.replace("hộp lý", "hợp lý");
                }
                review.setComment(comment);

                review.setCreatedAt(rs.getTimestamp("created_at"));
                
                String customerName = rs.getString("customer_name");
                if (customerName != null && !customerName.trim().isEmpty()) {
                    review.setCustomerName(customerName);
                } else {
                    review.setCustomerName("Khách hàng #" + review.getCustomerId());
                }

                reviews.add(review);
                count++;
            }

            System.out.println("✅ DAO - Found " + count + " reviews");

        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error in findAll: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Error in findAll: " + e.getMessage());
            e.printStackTrace();
        }
        
        return reviews;
    }

    /**
     * Tìm đánh giá theo customerId
     */
    public StoreReview findByCustomerId(int customerId) {
        String sql = "SELECT sr.*, a.username as customer_name " +
                    "FROM StoreReviews sr " +
                    "LEFT JOIN Accounts a ON sr.customer_id = a.account_id " +
                    "WHERE sr.customer_id = ?";
        
        System.out.println("🔍 DAO - Finding review for customer ID: " + customerId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setInt(1, customerId);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                StoreReview review = new StoreReview();
                review.setStoreReviewId(rs.getInt("store_review_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setStoreId(rs.getInt("store_id"));
                review.setRating(rs.getInt("rating"));

                String comment = rs.getString("comment");
                if (comment != null) {
                    comment = comment.replace("hộp lý", "hợp lý");
                }
                review.setComment(comment);

                review.setCreatedAt(rs.getTimestamp("created_at"));
                
                String customerName = rs.getString("customer_name");
                if (customerName != null && !customerName.trim().isEmpty()) {
                    review.setCustomerName(customerName);
                } else {
                    review.setCustomerName("Khách hàng #" + review.getCustomerId());
                }
                
                System.out.println("✅ DAO - Found existing review for customer: " + customerId);
                return review;
            }
            
            System.out.println("ℹ️ DAO - No review found for customer: " + customerId);
            
        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error in findByCustomerId: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Error in findByCustomerId: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean insertReview(int customerId, int rating, String comment) {
        // Kiểm tra trước để tránh lỗi UNIQUE constraint
        if (hasCustomerReviewed(customerId)) {
            System.err.println("❌ DAO - Customer " + customerId + " already has a review. Cannot insert.");
            return false;
        }

        String sql = "INSERT INTO StoreReviews (customer_id, store_id, rating, comment) VALUES (?, 1, ?, ?)";

        System.out.println("🆕 DAO - Inserting review for customer: " + customerId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, customerId);
            st.setInt(2, rating);
            st.setString(3, comment);

            int affectedRows = st.executeUpdate();
            boolean success = affectedRows > 0;
            
            System.out.println("✅ DAO - Insert review: " + (success ? "SUCCESS" : "FAILED"));
            return success;

        } catch (SQLException e) {
            // Kiểm tra xem lỗi có phải do vi phạm ràng buộc UNIQUE không
            if (e.getErrorCode() == 2627 || e.getSQLState().equals("23000") || e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate")) {
                System.err.println("❌ DAO - UNIQUE constraint violation: Customer " + customerId + " already has a review");
                return false;
            }
            System.err.println("❌ DAO - SQL Insert Error: " + e.getMessage());
            System.err.println("❌ DAO - Error Code: " + e.getErrorCode() + ", SQL State: " + e.getSQLState());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Insert Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật đánh giá
     */
    public boolean updateReview(int storeReviewId, int rating, String comment) {
        String sql = "UPDATE StoreReviews SET rating = ?, comment = ? WHERE store_review_id = ?";
        
        System.out.println("🔄 DAO - Updating review ID: " + storeReviewId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setInt(1, rating);
            st.setString(2, comment);
            st.setInt(3, storeReviewId);
            
            int affectedRows = st.executeUpdate();
            boolean success = affectedRows > 0;
            
            System.out.println("✅ DAO - Update review: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Update Error: " + e.getMessage());
            System.err.println("❌ DAO - Error Code: " + e.getErrorCode() + ", SQL State: " + e.getSQLState());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Update Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Kiểm tra xem customer đã đánh giá chưa
     */
    public boolean hasCustomerReviewed(int customerId) {
        boolean hasReviewed = findByCustomerId(customerId) != null;
        System.out.println("🔍 DAO - Customer " + customerId + " has reviewed: " + hasReviewed);
        return hasReviewed;
    }

    /**
     * Lấy đánh giá theo ID
     */
    public StoreReview findById(int storeReviewId) {
        String sql = "SELECT sr.*, a.username as customer_name " +
                    "FROM StoreReviews sr " +
                    "LEFT JOIN Accounts a ON sr.customer_id = a.account_id " +
                    "WHERE sr.store_review_id = ?";
        
        System.out.println("🔍 DAO - Finding review by ID: " + storeReviewId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setInt(1, storeReviewId);
            ResultSet rs = st.executeQuery();
            
            if (rs.next()) {
                StoreReview review = new StoreReview();
                review.setStoreReviewId(rs.getInt("store_review_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setStoreId(rs.getInt("store_id"));
                review.setRating(rs.getInt("rating"));

                String comment = rs.getString("comment");
                if (comment != null) {
                    comment = comment.replace("hộp lý", "hợp lý");
                }
                review.setComment(comment);

                review.setCreatedAt(rs.getTimestamp("created_at"));
                
                String customerName = rs.getString("customer_name");
                if (customerName != null && !customerName.trim().isEmpty()) {
                    review.setCustomerName(customerName);
                } else {
                    review.setCustomerName("Khách hàng #" + review.getCustomerId());
                }
                
                System.out.println("✅ DAO - Found review by ID: " + storeReviewId);
                return review;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error in findById: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Error in findById: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("ℹ️ DAO - No review found with ID: " + storeReviewId);
        return null;
    }

    /**
     * Xóa đánh giá (nếu cần cho tính năng future)
     */
    public boolean deleteReview(int storeReviewId) {
        String sql = "DELETE FROM StoreReviews WHERE store_review_id = ?";
        
        System.out.println("🗑️ DAO - Deleting review ID: " + storeReviewId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            
            st.setInt(1, storeReviewId);
            
            int affectedRows = st.executeUpdate();
            boolean success = affectedRows > 0;
            
            System.out.println("✅ DAO - Delete review: " + (success ? "SUCCESS" : "FAILED"));
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Delete Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Delete Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}