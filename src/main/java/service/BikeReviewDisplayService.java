package service;

import dao.ReviewDao;
import model.Review;
import model.Customer; 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BikeReviewDisplayService {
    private ReviewDao reviewDao;
    private CustomerService customerService;

    public BikeReviewDisplayService() {
        this.reviewDao = new ReviewDao();
        this.customerService = new CustomerService();
    }

    /**
     * Lấy danh sách review để hiển thị public (không có thông tin nhạy cảm)
     */
    public List<Map<String, Object>> getPublicReviewsForBike(int bikeId) {
        List<Map<String, Object>> publicReviews = new ArrayList<>();
        
        try {
            List<Review> reviews = reviewDao.findReviewByBikeId(bikeId);
            System.out.println("Raw reviews from DAO: " + reviews.size());
            
            for (Review review : reviews) {
                Map<String, Object> publicReview = new HashMap<>();
                publicReview.put("rating", review.getRating());
                publicReview.put("comment", review.getComment());
                publicReview.put("createdAt", review.getCreatedAt());
                
                // Xử lý tên khách hàng an toàn
                String displayName = getSafeDisplayName(review.getCustomerId());
                publicReview.put("customerName", displayName);
                
                publicReviews.add(publicReview);
                System.out.println("Added review: " + review.getRating() + " stars by " + displayName);
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy reviews public: " + e.getMessage());
            e.printStackTrace();
        }
        
        return publicReviews;
    }

    /**
     * Lấy thống kê reviews cho hiển thị public
     */
    public Map<String, Object> getReviewStatsForBike(int bikeId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            List<Review> reviews = reviewDao.findReviewByBikeId(bikeId);
            
            if (reviews == null || reviews.isEmpty()) {
                stats.put("totalReviews", 0);
                stats.put("averageRating", 0.0);
                stats.put("ratingCounts", createDefaultRatingCounts());
                stats.put("ratingPercentages", createDefaultRatingPercentages());
                return stats;
            }

            // Tính toán thống kê
            int totalReviews = reviews.size();
            double totalRating = 0;
            Map<Integer, Integer> ratingCounts = createDefaultRatingCounts();
            Map<Integer, Double> ratingPercentages = new HashMap<>();

            // Đếm số lượng từng rating
            for (Review review : reviews) {
                int rating = review.getRating();
                totalRating += rating;
                ratingCounts.put(rating, ratingCounts.get(rating) + 1);
            }

            // Tính phần trăm
            for (int i = 1; i <= 5; i++) {
                double percentage = (double) ratingCounts.get(i) / totalReviews;
                ratingPercentages.put(i, percentage);
            }

            // Tính điểm trung bình
            double averageRating = Math.round((totalRating / totalReviews) * 10.0) / 10.0;

            stats.put("totalReviews", totalReviews);
            stats.put("averageRating", averageRating);
            stats.put("ratingCounts", ratingCounts);
            stats.put("ratingPercentages", ratingPercentages);

            System.out.println("Calculated stats - Total: " + totalReviews + ", Avg: " + averageRating);

        } catch (Exception e) {
            System.err.println("Lỗi khi tính thống kê reviews: " + e.getMessage());
            e.printStackTrace();
            // Trả về stats mặc định nếu có lỗi
            stats.put("totalReviews", 0);
            stats.put("averageRating", 0.0);
            stats.put("ratingCounts", createDefaultRatingCounts());
            stats.put("ratingPercentages", createDefaultRatingPercentages());
        }
        
        return stats;
    }

    /**
     * Lấy tên hiển thị an toàn (bảo vệ thông tin cá nhân)
     */
    private String getSafeDisplayName(int customerId) {
        try {
            // SỬA: Sử dụng Customer thay vì model.Customer
            Customer customer = customerService.getCustomerById(customerId);
            if (customer != null && customer.getFullName() != null) {
                String fullName = customer.getFullName().trim();
                if (fullName.contains(" ")) {
                    String[] parts = fullName.split(" ");
                    return parts[0] + "***"; // Chỉ hiển thị họ
                } else {
                    return fullName.charAt(0) + "***"; // Chỉ hiển thị ký tự đầu
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên khách hàng: " + e.getMessage());
        }
        return "Khách hàng";
    }

    /**
     * Tạo rating counts mặc định
     */
    private Map<Integer, Integer> createDefaultRatingCounts() {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            counts.put(i, 0);
        }
        return counts;
    }

    /**
     * Tạo rating percentages mặc định
     */
    private Map<Integer, Double> createDefaultRatingPercentages() {
        Map<Integer, Double> percentages = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            percentages.put(i, 0.0);
        }
        return percentages;
    }
}