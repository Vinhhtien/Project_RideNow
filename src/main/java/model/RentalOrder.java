
package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class RentalOrder {
    private int orderId;
    private int customerId;
    private Date startDate;
    private Date endDate;
    private BigDecimal totalPrice;
    private String status;
    private Timestamp createdAt;
    private BigDecimal depositAmount;
    private String depositStatus;
    private boolean paymentSubmitted;
    private String returnStatus;
    private String pickupStatus;
    private Timestamp pickedUpAt;
    private Timestamp returnedAt;
    private Integer adminPickupId;
    private Integer adminReturnId;

    public RentalOrder() {}

    public RentalOrder(int orderId, int customerId, Date startDate, Date endDate,
                       BigDecimal totalPrice, String status, Timestamp createdAt,
                       BigDecimal depositAmount, String depositStatus, boolean paymentSubmitted,
                       String returnStatus, String pickupStatus, Timestamp pickedUpAt,
                       Timestamp returnedAt, Integer adminPickupId, Integer adminReturnId) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.depositAmount = depositAmount;
        this.depositStatus = depositStatus;
        this.paymentSubmitted = paymentSubmitted;
        this.returnStatus = returnStatus;
        this.pickupStatus = pickupStatus;
        this.pickedUpAt = pickedUpAt;
        this.returnedAt = returnedAt;
        this.adminPickupId = adminPickupId;
        this.adminReturnId = adminReturnId;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }

    public String getDepositStatus() { return depositStatus; }
    public void setDepositStatus(String depositStatus) { this.depositStatus = depositStatus; }

    public boolean isPaymentSubmitted() { return paymentSubmitted; }
    public void setPaymentSubmitted(boolean paymentSubmitted) { this.paymentSubmitted = paymentSubmitted; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public String getPickupStatus() { return pickupStatus; }
    public void setPickupStatus(String pickupStatus) { this.pickupStatus = pickupStatus; }

    public Timestamp getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(Timestamp pickedUpAt) { this.pickedUpAt = pickedUpAt; }

    public Timestamp getReturnedAt() { return returnedAt; }
    public void setReturnedAt(Timestamp returnedAt) { this.returnedAt = returnedAt; }

    public Integer getAdminPickupId() { return adminPickupId; }
    public void setAdminPickupId(Integer adminPickupId) { this.adminPickupId = adminPickupId; }

    public Integer getAdminReturnId() { return adminReturnId; }
    public void setAdminReturnId(Integer adminReturnId) { this.adminReturnId = adminReturnId; }
     @Override
    public String toString() {
        return "RentalOrder{" +
                "orderId=" + orderId +
                ", customerId=" + customerId +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", depositAmount=" + depositAmount +
                ", depositStatus='" + depositStatus + '\'' +
                ", paymentSubmitted=" + paymentSubmitted +
                ", returnStatus='" + returnStatus + '\'' +
                ", pickupStatus='" + pickupStatus + '\'' +
                ", pickedUpAt=" + pickedUpAt +
                ", returnedAt=" + returnedAt +
                ", adminPickupId=" + adminPickupId +
                ", adminReturnId=" + adminReturnId +
                '}';
    }
}
