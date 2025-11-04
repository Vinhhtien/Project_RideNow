package model.report;

import java.math.BigDecimal;

/** Tổng cho partner: totalShare = netRevenue * shareRate */
public final class PartnerStoreRevenueItem {
    private final int partnerId;
    private final String companyName;
    private final int orders;
    private final BigDecimal collected;
    private final BigDecimal refunded;
    private final BigDecimal netRevenue;
    private final BigDecimal shareRate;  // ví dụ 0.40
    private final BigDecimal totalShare;

    public PartnerStoreRevenueItem(int partnerId, String companyName, int orders,
                                   BigDecimal collected, BigDecimal refunded, BigDecimal shareRate) {
        this.partnerId = partnerId;
        this.companyName = companyName;
        this.orders = orders;
        this.collected = collected == null ? BigDecimal.ZERO : collected;
        this.refunded = refunded == null ? BigDecimal.ZERO : refunded;
        this.netRevenue = this.collected.subtract(this.refunded);
        this.shareRate = shareRate == null ? BigDecimal.ZERO : shareRate;
        this.totalShare = this.netRevenue.multiply(this.shareRate);
    }

    public int getPartnerId() { return partnerId; }
    public String getCompanyName() { return companyName; }
    public int getOrders() { return orders; }
    public BigDecimal getCollected() { return collected; }
    public BigDecimal getRefunded() { return refunded; }
    public BigDecimal getNetRevenue() { return netRevenue; }
    public BigDecimal getShareRate() { return shareRate; }
    public BigDecimal getTotalShare() { return totalShare; }
}
