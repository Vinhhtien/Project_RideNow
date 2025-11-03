package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class OrderVm {
    private int orderId;
    private String status;
    private Date start;
    private Date end;
    private BigDecimal total;
    private String bikeName;
    private int bikeId;
    private String paymentMethod;
    private Timestamp confirmedAt;
    private Integer changeRemainingMin; // Dùng Integer để có thể null

    // Getter và Setter
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getBikeName() { return bikeName; }
    public void setBikeName(String bikeName) { this.bikeName = bikeName; }

    public int getBikeId() { return bikeId; }
    public void setBikeId(int bikeId) { this.bikeId = bikeId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public Timestamp getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

    public Integer getChangeRemainingMin() { return changeRemainingMin; }
    public void setChangeRemainingMin(Integer changeRemainingMin) { 
        this.changeRemainingMin = changeRemainingMin; 
    }
}