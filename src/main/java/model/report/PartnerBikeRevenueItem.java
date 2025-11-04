package model.report;

import java.math.BigDecimal;

/** Per-bike: netRevenue = collected - refunded */
public final class PartnerBikeRevenueItem {
    private final int bikeId;
    private final String bikeName;
    private final int orders;
    private final BigDecimal collected;
    private final BigDecimal refunded;
    private final BigDecimal netRevenue;

    public PartnerBikeRevenueItem(int bikeId, String bikeName, int orders,
                                  BigDecimal collected, BigDecimal refunded) {
        this.bikeId = bikeId;
        this.bikeName = bikeName;
        this.orders = orders;
        this.collected = collected == null ? BigDecimal.ZERO : collected;
        this.refunded = refunded == null ? BigDecimal.ZERO : refunded;
        this.netRevenue = this.collected.subtract(this.refunded);
    }

    public int getBikeId() { return bikeId; }
    public String getBikeName() { return bikeName; }
    public int getOrders() { return orders; }
    public BigDecimal getCollected() { return collected; }
    public BigDecimal getRefunded() { return refunded; }
    public BigDecimal getNetRevenue() { return netRevenue; }
}
