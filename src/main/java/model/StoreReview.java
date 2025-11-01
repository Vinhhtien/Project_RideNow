package model;

import java.time.LocalDateTime;

public class StoreReview {
    private int storeReviewId;
    private int customerId;
    private int storeId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
    private String customerName; 

    // Constructors (Phần này chỉ là tùy chọn, nhưng nên thêm)
    public StoreReview() {}

    // ... (Giữ nguyên các constructor khác)

    // Getters and Setters
    public int getStoreReviewId() { return storeReviewId; }
    public void setStoreReviewId(int storeReviewId) { this.storeReviewId = storeReviewId; }

    // ⭐️ Phương thức bạn báo lỗi "can't find symbol"
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getStoreId() { return storeId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Phương thức cho customerName (Bạn đã thêm trước đó)
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // ... (Phần toString() nếu bạn muốn)
}