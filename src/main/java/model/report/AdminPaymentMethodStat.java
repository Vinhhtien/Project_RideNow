package model.report;

public class AdminPaymentMethodStat {
    private String method;
    private double paidAmount;
    private double refundedAmount;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public double getPaidAmount() { return paidAmount; }
    public void setPaidAmount(double paidAmount) { this.paidAmount = paidAmount; }

    public double getRefundedAmount() { return refundedAmount; }
    public void setRefundedAmount(double refundedAmount) { this.refundedAmount = refundedAmount; }

    public double getNet() { return paidAmount - refundedAmount; }
}
