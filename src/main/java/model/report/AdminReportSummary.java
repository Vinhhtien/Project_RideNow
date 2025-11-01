package model.report;

public class AdminReportSummary {
    private int totalOrders;
    private double totalPaid;
    private double totalRefunded;

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public double getTotalPaid() { return totalPaid; }
    public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }

    public double getTotalRefunded() { return totalRefunded; }
    public void setTotalRefunded(double totalRefunded) { this.totalRefunded = totalRefunded; }

    public double getNet() { return totalPaid - totalRefunded; }
    
}
