
package dao;

import java.util.List;
import model.Review;

public interface IReviewDao {
    public List<Review> findReviewByBikeId(int bikeId) throws Exception ;
    // LẤY TẤT CẢ REVIEW
    List<model.Review> findAll() throws Exception;
    
    // ✅ Đã sửa: Loại bỏ 'orderId' để khớp với cấu trúc DB hiện tại
    public boolean insertReview(int customerId, int bikeId, int rating, String comment) ;
}
