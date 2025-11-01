
package service;

import dao.ReviewDao;
import model.Review;

import java.util.List;


public class ReviewService implements IReviewService {

    private ReviewDao reviewDao;

    public ReviewService() {
        this.reviewDao = new ReviewDao();
    }

    @Override
    public List<Review> getReviewByBikeId(int bikeId) throws Exception {
        return reviewDao.findReviewByBikeId(bikeId);
    }

    @Override
    public List<model.Review> getAll() throws Exception {
        return reviewDao.findAll();
    }

}