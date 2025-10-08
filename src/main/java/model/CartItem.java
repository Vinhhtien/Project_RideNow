package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class CartItem {
    private int bikeId;
    private String bikeName;
    private BigDecimal pricePerDay;
    private String typeName; // "Xe số" | "Xe ga" | "Phân khối lớn"
    private Date startDate;
    private Date endDate;

    public CartItem(int bikeId, String bikeName, BigDecimal pricePerDay,
                    String typeName, Date startDate, Date endDate) {
        this.bikeId = bikeId;
        this.bikeName = bikeName;
        this.pricePerDay = pricePerDay;
        this.typeName = typeName;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /** Số ngày thuê (tính cả ngày cuối) */
    public int getDays() {
        if (startDate == null || endDate == null) return 0;
        long d = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
        return (int) Math.max(d, 1);
    }

    /** Thành tiền cho item = giá/ngày * số ngày */
    public BigDecimal getSubtotal() {
        return pricePerDay.multiply(BigDecimal.valueOf(getDays()));
    }

    /** Tiền cọc: PKL = 1tr, còn lại = 500k */
    public BigDecimal getDeposit() {
        if (typeName != null) {
            String t = typeName.toLowerCase();
            if (t.contains("phân khối") || t.contains("pkl")) {
                return BigDecimal.valueOf(1_000_000);
            }
        }
        return BigDecimal.valueOf(500_000);
    }

    // === getters & setters ===
    public int getBikeId() { return bikeId; }
    public String getBikeName() { return bikeName; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public String getTypeName() { return typeName; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }

    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
}
