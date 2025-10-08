package model;

import java.sql.Timestamp;

public class OrderStatusHistory {
    private int historyId;
    private int orderId;
    private String status;
    private Integer adminId;
    private String notes;
    private Timestamp createdAt;
    
    // Constructors
    public OrderStatusHistory() {}
    
    public OrderStatusHistory(int orderId, String status, Integer adminId, String notes) {
        this.orderId = orderId;
        this.status = status;
        this.adminId = adminId;
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getHistoryId() { return historyId; }
    public void setHistoryId(int historyId) { this.historyId = historyId; }
    
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}