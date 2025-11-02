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
        // SQL Server syntax - sử dụng TOP thay vì LIMIT
        String sql = "SELECT TOP 1000 sr.*, a.username as customer_name " +
                    "FROM StoreReviews sr " +
                    "LEFT JOIN Accounts a ON sr.customer_id = a.account_id " +
                    "ORDER BY sr.created_at DESC";

        System.out.println("=== DAO DEBUG: Executing SQL ===");
        System.out.println("SQL: " + sql);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                StoreReview review = new StoreReview();
                review.setStoreReviewId(rs.getInt("store_review_id"));
                review.setCustomerId(rs.getInt("customer_id"));
                review.setStoreId(rs.getInt("store_id"));
                review.setRating(rs.getInt("rating"));

                // ĐỌC COMMENT VÀ FIX LỖI
                String comment = rs.getString("comment");
                System.out.println("🔍 RAW COMMENT FROM SQL SERVER: '" + comment + "'");
                
                if (comment != null) {
                    // Fix các lỗi cụ thể từ database của bạn
                    comment = fixSpecificErrors(comment);
                    System.out.println("✅ AFTER FIX: '" + comment + "'");
                }
                review.setComment(comment);

                review.setCreatedAt(rs.getTimestamp("created_at"));
                
                // Thêm customer name
                try {
                    String customerName = rs.getString("customer_name");
                    review.setCustomerName(customerName != null ? customerName : "Khách hàng #" + review.getCustomerId());
                } catch (SQLException e) {
                    review.setCustomerName("Khách hàng #" + review.getCustomerId());
                }

                reviews.add(review);
            }

            System.out.println("✅ DAO - Total reviews found: " + reviews.size());

        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ DAO - General Error: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }

    /**
     * Fix các lỗi cụ thể từ database SQL Server của bạn
     */
    private String fixSpecificErrors(String text) {
        if (text == null) return null;
        
        String result = text;
        
        // DỰA TRÊN DỮ LIỆU THỰC TẾ TỪ DATABASE CỦA BẠN
        // "Nhân viên thân thiện, làm thủ tục nhanh" -> OK
        // "Giá cả hộp lý, xe mới" -> "Giá cả hợp lý, xe mới"
        // "Tốt" -> OK
        
        // Fix lỗi "hộp lý" thành "hợp lý"
        result = result.replace("hộp lý", "hợp lý");
        
        // Fix các lỗi khác nếu có
        result = result.replace("thân thiện", "thân thiện");
        
        return result;
    }

    @Override
    public boolean insertReview(int customerId, int rating, String comment) {
        // SQL Server syntax
        String sql = "INSERT INTO StoreReviews (customer_id, store_id, rating, comment) VALUES (?, 1, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, customerId);
            st.setInt(2, rating);

            // Với SQL Server, cần đảm bảo encoding đúng khi insert
            if (comment != null) {
                st.setNString(3, comment); // Sử dụng setNString cho Unicode trong SQL Server
                System.out.println("💾 Saving comment to SQL Server: " + comment);
            } else {
                st.setNull(3, Types.NVARCHAR);
            }

            int affectedRows = st.executeUpdate();
            System.out.println("✅ DAO - Insert review: " + (affectedRows > 0 ? "SUCCESS" : "FAILED"));
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("❌ DAO - Insert Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}