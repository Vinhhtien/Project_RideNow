package model.report;

import java.util.Date;

public class AdminRevenueItem {
    // ----- Payment item hiện có -----
    private int paymentId;
    private int orderId;
    private String customerName;
    private double amount;
    private String method;
    private String status;
    private Date paymentDate;
    private Date verifiedAt;
    private String reference;

    // ----- Nhóm theo đối tác/cửa hàng -----
    private Integer partnerId;
    private Integer storeId;
    private int orderCount;

    // Hiển thị tên
    private String partnerName; // ví dụ: đối tác A; fallback "Partner #ID" nếu null
    private String storeName;   // ví dụ: "RideNow"

    // ----- Doanh thu -----
    // Tổng thu thực tế = (total_price + deposit) theo nghiệp vụ báo cáo
    private double totalPaid;
    // Hoàn thực tế = RefundInspections.refund_status='completed'
    private double refundedAmount;
    // Ròng = totalPaid - refundedAmount
    private double netRevenue;

    // Chia tỉ lệ theo phương án A
    // Đơn thuộc partner: partnerShare40 = 40% net; adminShare60 = 60% net
    // Đơn thuộc cửa hàng: partnerShare40 = 0; adminShare60 = 100% net
    private double partnerShare40;
    private double adminShare60;

    // ===== NEW: dữ liệu phục vụ hiển thị "Due" trên trang Payments =====
    private double orderTotalPrice;   // tổng tiền thuê (total_price)
    private double depositAmount;     // tiền đặt cọc
    private double orderPaid;         // sum Payments.status='paid'
    private boolean inspectionVerified;
    private double dueBeforeVerify;   // (orderTotalPrice + depositAmount) - orderPaid

    // ===== Getter/Setter gốc =====
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

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Date paymentDate) { this.paymentDate = paymentDate; }

    public Date getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Date verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }

    // ===== Báo cáo theo nhóm =====
    public Integer getPartnerId() { return partnerId; }
    public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }

    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    // ===== Doanh thu =====
    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
        recomputeNetRevenue();
    }

    public double getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(double refundedAmount) {
        this.refundedAmount = refundedAmount;
        recomputeNetRevenue();
    }

    public double getNetRevenue() { return netRevenue; }
    /** Không khuyến khích set thủ công. Ưu tiên để auto-tính từ totalPaid/refundedAmount. */
    public void setNetRevenue(double netRevenue) { this.netRevenue = netRevenue; }

    public double getPartnerShare40() { return partnerShare40; }
    public void setPartnerShare40(double partnerShare40) { this.partnerShare40 = partnerShare40; }

    public double getAdminShare60() { return adminShare60; }
    public void setAdminShare60(double adminShare60) { this.adminShare60 = adminShare60; }

    // ===== Alias tiện dụng, không phá tương thích =====
    public double getTotalCollected() { return totalPaid; }
    public void setTotalCollected(double v) { setTotalPaid(v); }

    // ===== NEW: getters/setters cho "Due" =====
    public double getOrderTotalPrice() { return orderTotalPrice; }
    public void setOrderTotalPrice(double orderTotalPrice) { this.orderTotalPrice = orderTotalPrice; }

    public double getDepositAmount() { return depositAmount; }
    public void setDepositAmount(double depositAmount) { this.depositAmount = depositAmount; }

    public double getOrderPaid() { return orderPaid; }
    public void setOrderPaid(double orderPaid) { this.orderPaid = orderPaid; }

    public boolean isInspectionVerified() { return inspectionVerified; }
    public void setInspectionVerified(boolean inspectionVerified) { this.inspectionVerified = inspectionVerified; }

    public double getDueBeforeVerify() { return dueBeforeVerify; }
    public void setDueBeforeVerify(double dueBeforeVerify) { this.dueBeforeVerify = dueBeforeVerify; }

    // ===== Nội bộ =====
    private void recomputeNetRevenue() {
        this.netRevenue = this.totalPaid - this.refundedAmount;
    }
}
