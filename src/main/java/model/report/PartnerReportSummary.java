package model.report;

import java.math.BigDecimal;

/** netRevenue = totalCollected - totalRefunded */
public final class PartnerReportSummary {
    private final int totalOrders;
    private final BigDecimal totalCollected;
    private final BigDecimal totalRefunded;
    private final BigDecimal netRevenue;

    public PartnerReportSummary(int totalOrders, BigDecimal totalCollected, BigDecimal totalRefunded) {
        this.totalOrders = totalOrders;
        this.totalCollected = totalCollected == null ? BigDecimal.ZERO : totalCollected;
        this.totalRefunded = totalRefunded == null ? BigDecimal.ZERO : totalRefunded;
        this.netRevenue = this.totalCollected.subtract(this.totalRefunded);
    }
    public int getTotalOrders() { return totalOrders; }
    public BigDecimal getTotalCollected() { return totalCollected; }
    public BigDecimal getTotalRefunded() { return totalRefunded; }
    public BigDecimal getNetRevenue() { return netRevenue; }
}
