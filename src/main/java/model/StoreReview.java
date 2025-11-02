package model;

import java.util.Date;

public class StoreReview {
    private int storeReviewId;
    private int customerId;
    private int storeId;
    private int rating;
    private String comment;
    private Date createdAt;
    private String customerName; // THÊM TRƯỜNG NÀY

    // Các getter methods - QUAN TRỌNG: JSP sử dụng các getter này
    public int getStoreReviewId() { return storeReviewId; }
    public int getCustomerId() { return customerId; }
    public int getStoreId() { return storeId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Date getCreatedAt() { return createdAt; }
    public String getCustomerName() { return customerName; } // THÊM GETTER

    // Các setter methods
    public void setStoreReviewId(int storeReviewId) { this.storeReviewId = storeReviewId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setCustomerName(String customerName) { this.customerName = customerName; } // THÊM SETTER
}