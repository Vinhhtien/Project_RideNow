package model;

import java.time.LocalDate;

public class ScheduleItem {
    private int orderId;
    private int bikeId;
    private String bikeName;
    private String licensePlate;
    private String ownerType;   // "store" | "partner"
    private String ownerName;   // store_name hoặc company_name
    private LocalDate startDate;
    private LocalDate endDate;
    private String orderStatus;

    public ScheduleItem() {}

    public ScheduleItem(int orderId, int bikeId, String bikeName, String licensePlate,
                        String ownerType, String ownerName, LocalDate startDate,
                        LocalDate endDate, String orderStatus) {
        this.orderId = orderId;
        this.bikeId = bikeId;
        this.bikeName = bikeName;
        this.licensePlate = licensePlate;
        this.ownerType = ownerType;
        this.ownerName = ownerName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.orderStatus = orderStatus;
    }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBikeId() { return bikeId; }
    public void setBikeId(int bikeId) { this.bikeId = bikeId; }

    public String getBikeName() { return bikeName; }
    public void setBikeName(String bikeName) { this.bikeName = bikeName; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }
}
