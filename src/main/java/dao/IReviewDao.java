package dao;

import java.util.List;
import model.Review;

public interface IReviewDao {

    // Lấy danh sách review của 1 xe
    List<Review> findReviewByBikeId(int bikeId) throws Exception;

    // Lấy tất cả review
    List<Review> findAll() throws Exception;

    // 🔹 Tìm review của 1 customer cho 1 đơn cụ thể (order)
    Review findByCustomerAndOrder(int customerId, int orderId) throws Exception;

    // 🔹 Insert review lần đầu cho 1 đơn hàng
    boolean insertReview(int customerId, int bikeId, int orderId,
                         int rating, String comment) throws Exception;

    // 🔹 Cập nhật review cũ (edit)
    boolean updateReview(int reviewId, int rating, String comment) throws Exception;
}
