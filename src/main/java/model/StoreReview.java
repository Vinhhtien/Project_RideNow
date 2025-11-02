package model;

import java.util.Date;

public class StoreReview {
    private int storeReviewId;
    private int customerId;
    private int storeId;
    private int rating;
    private String comment;
    private Date createdAt;
    private String customerName;
    private boolean canEdit; // Thêm trường mới để kiểm tra có thể chỉnh sửa không

    // Các getter methods
    public int getStoreReviewId() { return storeReviewId; }
    public int getCustomerId() { return customerId; }
    public int getStoreId() { return storeId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public Date getCreatedAt() { return createdAt; }
    public String getCustomerName() { return customerName; }
    public boolean isCanEdit() { return canEdit; } // Thêm getter cho canEdit

    // Các setter methods
    public void setStoreReviewId(int storeReviewId) { this.storeReviewId = storeReviewId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setStoreId(int storeId) { this.storeId = storeId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCanEdit(boolean canEdit) { this.canEdit = canEdit; } // Thêm setter cho canEdit
}