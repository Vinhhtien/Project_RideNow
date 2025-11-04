package model.adminfeedback;

import java.sql.Timestamp;

public class AdminFeedbackItem {
    private FeedbackType type;
    private Integer targetId;      // store_id hoặc motorbike_id
    private String  targetCode;    // "Store#12" hoặc "Bike#7"
    private String  targetName;    // tên cửa hàng/xe (nếu join được)
    private Integer orderId;       // nullable
    private Integer customerId;    // nullable
    private String  customerName;  // nullable
    private int     rating;        // 1..5
    private String  title;         // nullable
    private String  content;       // nullable
    private Timestamp createdAt;

    public FeedbackType getType() { return type; }
    public void setType(FeedbackType type) { this.type = type; }

    public Integer getTargetId() { return targetId; }
    public void setTargetId(Integer targetId) { this.targetId = targetId; }

    public String getTargetCode() { return targetCode; }
    public void setTargetCode(String targetCode) { this.targetCode = targetCode; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Integer getCustomerId() { return customerId; }
    public void setCustomerId(Integer customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
