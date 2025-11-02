package dao;

import model.StoreReview;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StoreReviewDao implements IStoreReviewDao {

    // =========================
    // LIST TẤT CẢ REVIEW (JOIN ĐÚNG)
    // =========================
    @Override
    public List<StoreReview> findAll() {
        List<StoreReview> reviews = new ArrayList<>();
        String sql =
            "SELECT sr.*, c.customer_id, c.account_id, c.full_name, a.username " +
            "FROM StoreReviews sr " +
            "JOIN Customers c ON sr.customer_id = c.customer_id " +
            "LEFT JOIN Accounts a ON c.account_id = a.account_id " +
            "ORDER BY sr.created_at DESC";

        System.out.println("🔍 DAO - Executing findAll query");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {

            int count = 0;
            while (rs.next()) {
                StoreReview r = new StoreReview();
                r.setStoreReviewId(rs.getInt("store_review_id"));
                r.setCustomerId(rs.getInt("customer_id"));
                r.setStoreId(rs.getInt("store_id"));
                r.setRating(rs.getInt("rating"));

                String comment = rs.getString("comment");
                if (comment != null) comment = comment.replace("hộp lý", "hợp lý");
                r.setComment(comment);

                r.setCreatedAt(rs.getTimestamp("created_at"));

                String name = rs.getString("full_name");
                if (name == null || name.isBlank()) name = rs.getString("username");
                if (name == null || name.isBlank()) name = "Khách hàng #" + r.getCustomerId();
                r.setCustomerName(name);

                reviews.add(r);
                count++;
            }

            System.out.println("✅ DAO - Found " + count + " reviews");

        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error findAll: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ DAO - Unexpected Error findAll: " + e.getMessage());
            e.printStackTrace();
        }
        return reviews;
    }

    // ==========================================
    // TÌM REVIEW THEO customerId (JOIN ĐÚNG)
    // ==========================================
    public StoreReview findByCustomerId(int customerId) {
        String sql =
            "SELECT sr.*, c.full_name, a.username " +
            "FROM StoreReviews sr " +
            "JOIN Customers c ON sr.customer_id = c.customer_id " +
            "LEFT JOIN Accounts a ON c.account_id = a.account_id " +
            "WHERE sr.customer_id = ?";

        System.out.println("🔍 DAO - Finding review for customer ID: " + customerId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, customerId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    StoreReview review = new StoreReview();
                    review.setStoreReviewId(rs.getInt("store_review_id"));
                    review.setCustomerId(rs.getInt("customer_id"));
                    review.setStoreId(rs.getInt("store_id"));
                    review.setRating(rs.getInt("rating"));

                    String comment = rs.getString("comment");
                    if (comment != null) comment = comment.replace("hộp lý", "hợp lý");
                    review.setComment(comment);

                    review.setCreatedAt(rs.getTimestamp("created_at"));

                    String name = rs.getString("full_name");
                    if (name == null || name.isBlank()) name = rs.getString("username");
                    if (name == null || name.isBlank()) name = "Khách hàng #" + review.getCustomerId();
                    review.setCustomerName(name);

                    System.out.println("✅ DAO - Found existing review for customer: " + customerId);
                    return review;
                }
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

    // ====================================================
    // INSERT THEO customerId (GIỮ LẠI HÀM BẠN ĐANG DÙNG)
    // ====================================================
    @Override
    public boolean insertReview(int customerId, int rating, String comment) {
        // Dùng NOT EXISTS theo code gốc của bạn
        String sql = "INSERT INTO StoreReviews (customer_id, store_id, rating, comment) " +
                     "SELECT ?, 1, ?, ? " +
                     "WHERE NOT EXISTS (SELECT 1 FROM StoreReviews WHERE customer_id = ?)";

        System.out.println("🆕 DAO - Attempting conditional insert for customer: " + customerId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, customerId);
            st.setInt(2, rating);
            st.setString(3, comment);
            st.setInt(4, customerId);

            int affectedRows = st.executeUpdate();
            boolean success = affectedRows > 0;

            if (success) {
                System.out.println("✅ DAO - Insert review: SUCCESS - New review created for customer " + customerId);
            } else {
                System.out.println("❌ DAO - Insert review: FAILED - Customer " + customerId + " already has a review");
            }
            return success;

        } catch (SQLException e) {
            // UNIQUE/FK
            if (e.getErrorCode() == 2627 || "23000".equals(e.getSQLState()) ||
                (e.getMessage() != null && (e.getMessage().contains("UNIQUE") || e.getMessage().contains("duplicate")))) {
                System.err.println("❌ DAO - UNIQUE/FK violation on insert: " + e.getMessage());
                return false;
            }
            System.err.println("❌ DAO - SQL Insert Error: " + e.getMessage());
            System.err.println("❌ DAO - Error Code: " + e.getErrorCode() + ", SQL State: " + e.getSQLState());
            e.printStackTrace();
            return false;
        }
    }

    // (Debug nội bộ)
    private void debugCheckCustomer(int customerId) {
        String sql = "SELECT COUNT(*) as count FROM StoreReviews WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, customerId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("🔍 DEBUG - Actual count in DB for customer " + customerId + ": " + count);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DEBUG - Error checking customer: " + e.getMessage());
        }
    }

    // =========================
    // UPDATE REVIEW
    // =========================
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

    // ===================================
    // CHECK TỒN TẠI THEO customerId
    // ===================================
    public boolean hasCustomerReviewed(int customerId) {
        String sql = "SELECT 1 FROM StoreReviews WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, customerId);
            try (ResultSet rs = st.executeQuery()) {
                boolean has = rs.next();
                System.out.println("🔍 DAO - Direct DB check - Customer " + customerId + " has reviewed: " + has);
                return has;
            }
        } catch (SQLException e) {
            System.err.println("❌ DAO - Error checking review existence: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // FIND BY ID (JOIN ĐÚNG)
    // =========================
    public StoreReview findById(int storeReviewId) {
        String sql =
            "SELECT sr.*, c.full_name, a.username " +
            "FROM StoreReviews sr " +
            "JOIN Customers c ON sr.customer_id = c.customer_id " +
            "LEFT JOIN Accounts a ON c.account_id = a.account_id " +
            "WHERE sr.store_review_id = ?";

        System.out.println("🔍 DAO - Finding review by ID: " + storeReviewId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, storeReviewId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    StoreReview review = new StoreReview();
                    review.setStoreReviewId(rs.getInt("store_review_id"));
                    review.setCustomerId(rs.getInt("customer_id"));
                    review.setStoreId(rs.getInt("store_id"));
                    review.setRating(rs.getInt("rating"));

                    String comment = rs.getString("comment");
                    if (comment != null) comment = comment.replace("hộp lý", "hợp lý");
                    review.setComment(comment);

                    review.setCreatedAt(rs.getTimestamp("created_at"));

                    String name = rs.getString("full_name");
                    if (name == null || name.isBlank()) name = rs.getString("username");
                    if (name == null || name.isBlank()) name = "Khách hàng #" + review.getCustomerId();
                    review.setCustomerName(name);

                    System.out.println("✅ DAO - Found review by ID: " + storeReviewId);
                    return review;
                }
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

    // =========================
    // DELETE REVIEW
    // =========================
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

    // ==================================================
    // CÁC HÀM THEO account_id (TRÁNH LỖI FK 547)
    // ==================================================

    // Lấy review theo account_id (JOIN qua Customers)
    public StoreReview findByAccountId(int accountId) {
        String sql =
            "SELECT sr.*, c.customer_id, c.account_id, c.full_name, a.username " +
            "FROM StoreReviews sr " +
            "JOIN Customers c ON sr.customer_id = c.customer_id " +
            "LEFT JOIN Accounts a ON c.account_id = a.account_id " +
            "WHERE c.account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, accountId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    StoreReview r = new StoreReview();
                    r.setStoreReviewId(rs.getInt("store_review_id"));
                    r.setCustomerId(rs.getInt("customer_id"));
                    r.setStoreId(rs.getInt("store_id"));
                    r.setRating(rs.getInt("rating"));

                    String comment = rs.getString("comment");
                    if (comment != null) comment = comment.replace("hộp lý", "hợp lý");
                    r.setComment(comment);
                    r.setCreatedAt(rs.getTimestamp("created_at"));

                    String name = rs.getString("full_name");
                    if (name == null || name.isBlank()) name = rs.getString("username");
                    if (name == null || name.isBlank()) name = "Khách hàng #" + r.getCustomerId();
                    r.setCustomerName(name);
                    return r;
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ DAO - SQL Error findByAccountId: " + e.getMessage());
        }
        return null;
    }

    // Kiểm tra đã review theo account_id
    public boolean hasAccountReviewed(int accountId) {
        String sql =
            "SELECT 1 " +
            "FROM StoreReviews sr " +
            "JOIN Customers c ON sr.customer_id = c.customer_id " +
            "WHERE c.account_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {
            st.setInt(1, accountId);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("❌ DAO - Error hasAccountReviewed: " + e.getMessage());
            return false;
        }
    }

    // Insert review theo account_id (INSERT ... SELECT để map sang customer_id)
    public boolean insertReviewByAccountId(int accountId, int rating, String comment) {
        String sql =
            "INSERT INTO StoreReviews (customer_id, store_id, rating, comment) " +
            "SELECT c.customer_id, 1, ?, ? " +   // TODO: thay 1 bằng store_id thực nếu cần
            "FROM Customers c " +
            "WHERE c.account_id = ?";

        System.out.println("🆕 DAO - Insert by account_id: " + accountId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement st = conn.prepareStatement(sql)) {

            st.setInt(1, rating);
            st.setString(2, comment);
            st.setInt(3, accountId);

            int n = st.executeUpdate();
            if (n == 0) {
                // Không có Customers tương ứng → servlet hiển thị message hướng dẫn bổ sung hồ sơ
                System.out.println("ℹ️ DAO - No Customers row for account_id=" + accountId);
                return false;
            }
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == 2627 || "23000".equals(e.getSQLState())) {
                // UNIQUE(customer_id) hoặc FK
                System.err.println("❌ DAO - UNIQUE/FK violation on insert (by account): " + e.getMessage());
                return false;
            }
            System.err.println("❌ DAO - SQL Insert Error (by account): " + e.getMessage());
            return false;
        }
    }
}
