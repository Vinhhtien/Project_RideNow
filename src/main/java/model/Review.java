package model;

import java.time.LocalDateTime;

public class Review {
    private int reviewId;
    private int customerId;
    private int bikeId;
    private int orderId;           // 👈 NEW: gắn với đơn hàng
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // 👈 NEW: lần chỉnh sửa cuối

    public Review() {
    }

    // Full constructor (dùng khi map từ DB)
    public Review(int reviewId,
                  int customerId,
                  int bikeId,
                  int orderId,
                  int rating,
                  String comment,
                  LocalDateTime createdAt,
                  LocalDateTime updatedAt) {
        this.reviewId = reviewId;
        this.customerId = customerId;
        this.bikeId = bikeId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructor cho insert (DB tự sinh reviewId, createdAt, updatedAt)
    public Review(int customerId,
                  int bikeId,
                  int orderId,
                  int rating,
                  String comment) {
        this.customerId = customerId;
        this.bikeId = bikeId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters & Setters
    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public int getOrderId() {        // 👈 NEW
        return orderId;
    }

    public void setOrderId(int orderId) {   // 👈 NEW
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {   // 👈 NEW
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {  // 👈 NEW
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Review{" +
                "reviewId=" + reviewId +
                ", customerId=" + customerId +
                ", bikeId=" + bikeId +
                ", orderId=" + orderId +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
