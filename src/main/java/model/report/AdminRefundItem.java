package model.report;

import java.util.Date;

public class AdminRefundItem {
    private int paymentId;
    private int orderId;
    private String customerName;
    private double amount;
    private String method;
    private Date paymentDate;
    private Date verifiedAt;
    private String reference;

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Date getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Date verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
}
