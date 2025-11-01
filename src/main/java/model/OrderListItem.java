package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.sql.Timestamp;

public class OrderListItem {
    private int orderId;
    private String customerName;
    private String status;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;

    // getters & setters
    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Date getCreatedAtDate() {
        return (createdAt == null) ? null : Timestamp.valueOf(createdAt);
    }
}
