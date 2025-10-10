
package dao;

import java.util.List;
import model.Review;

public interface IReviewDao {
    public List<Review> findReviewByBikeId(int bikeId) throws Exception ;
    // LẤY TẤT CẢ REVIEW
    List<model.Review> findAll() throws Exception;

}
