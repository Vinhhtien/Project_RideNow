
package service;


import java.util.List;
import model.Review;

public interface IReviewService {
    public List<Review> getReviewByBikeId(int bikeId) throws Exception ;
    // LẤY TẤT CẢ REVIEW
    List<model.Review> getAll() throws Exception;

}
