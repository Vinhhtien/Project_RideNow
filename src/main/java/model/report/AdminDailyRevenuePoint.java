package model.report;

import java.util.Date;

public class AdminDailyRevenuePoint {
    private Date day;
    private double paidAmount;
    private double refundedAmount;

    public Date getDay() { return day; }
    public void setDay(Date day) { this.day = day; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(double refundedAmount) { this.refundedAmount = refundedAmount; }

    public double getNet() { return paidAmount - refundedAmount; }
}
