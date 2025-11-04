package model;

import java.util.Map;
import java.util.HashMap;

public class ReviewStats {
    private double averageRating;
    private int totalReviews;
    private Map<Integer, Integer> ratingCounts;
    private Map<Integer, Double> ratingPercentages;
    
    public ReviewStats() {
        this.averageRating = 0.0;
        this.totalReviews = 0;
        this.ratingCounts = new HashMap<>();
        this.ratingPercentages = new HashMap<>();
        
        // Khởi tạo với giá trị mặc định
        for (int i = 1; i <= 5; i++) {
            ratingCounts.put(i, 0);
            ratingPercentages.put(i, 0.0);
        }
    }
    
    // Getters and Setters
    public double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }
    
    public int getTotalReviews() {
        return totalReviews;
    }
    
    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }
    
    public Map<Integer, Integer> getRatingCounts() {
        return ratingCounts;
    }
    
    public void setRatingCounts(Map<Integer, Integer> ratingCounts) {
        this.ratingCounts = ratingCounts;
    }
    
    public Map<Integer, Double> getRatingPercentages() {
        return ratingPercentages;
    }
    
    public void setRatingPercentages(Map<Integer, Double> ratingPercentages) {
        this.ratingPercentages = ratingPercentages;
    }
    
    // Helper methods để dùng trong JSP
    public int getRatingCount(int rating) {
        return ratingCounts.getOrDefault(rating, 0);
    }
    
    public double getRatingPercentage(int rating) {
        return ratingPercentages.getOrDefault(rating, 0.0);
    }
}