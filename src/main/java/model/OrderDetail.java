package model;

import java.math.BigDecimal;

public class OrderDetail {
    private int detailId;
    private int orderId;
    private int bikeId;
    private BigDecimal pricePerDay;
    private int quantity;
    private BigDecimal lineTotal;

    public OrderDetail() {
    }

    public OrderDetail(int detailId, int orderId, int bikeId, BigDecimal pricePerDay, int quantity, BigDecimal lineTotal) {
        this.detailId = detailId;
        this.orderId = orderId;
        this.bikeId = bikeId;
        this.pricePerDay = pricePerDay;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    // Constructor khi tạo mới (detailId do DB tự sinh)
    public OrderDetail(int orderId, int bikeId, BigDecimal pricePerDay, int quantity, BigDecimal lineTotal) {
        this.orderId = orderId;
        this.bikeId = bikeId;
        this.pricePerDay = pricePerDay;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    // Getters & Setters
    public int getDetailId() {
        return detailId;
    }

    public void setDetailId(int detailId) {
        this.detailId = detailId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    @Override
    public String toString() {
        return "OrderDetail{" +
                "detailId=" + detailId +
                ", orderId=" + orderId +
                ", bikeId=" + bikeId +
                ", pricePerDay=" + pricePerDay +
                ", quantity=" + quantity +
                ", lineTotal=" + lineTotal +
                '}';
    }
}
