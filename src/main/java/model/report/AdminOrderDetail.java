package model.report;

import java.util.Date;
import java.util.List;

public class AdminOrderDetail {
    private int orderId;
    private int customerId;
    private String customerName;
    private double totalPrice;
    private double depositAmount;
    private String status;
    private String pickupStatus;
    private String returnStatus;
    private Date createdAt;
    private double paidAmount;
    private double refundedAmount;
    private List<AdminRevenueItem> payments;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }

    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double depositAmount) { this.depositAmount = depositAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPickupStatus() { return pickupStatus; }
    public void setPickupStatus(String pickupStatus) { this.pickupStatus = pickupStatus; }

    public String getReturnStatus() { return returnStatus; }
    public void setReturnStatus(String returnStatus) { this.returnStatus = returnStatus; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(double refundedAmount) { this.refundedAmount = refundedAmount; }

    public List<AdminRevenueItem> getPayments() { return payments; }
    public void setPayments(List<AdminRevenueItem> payments) { this.payments = payments; }

    public double getNetPaid() { return paidAmount - refundedAmount; }
    public double getAmountDue() { return Math.max(0, totalPrice - getNetPaid()); }
}
