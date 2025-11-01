package model;

import java.math.BigDecimal;

public class MotorbikeListItem {
    private int bikeId;
    private Integer typeId;
    private String bikeName;
    private String licensePlate;
    private BigDecimal pricePerDay;
    private String status;
    private String typeName;
    private String ownerType;
    private String ownerName;
    private String description;

    public MotorbikeListItem() {
    }

    public MotorbikeListItem(int bikeId, String bikeName, String licensePlate, BigDecimal pricePerDay,
                             String status, String typeName, String ownerType, String ownerName, String description) {
        this.bikeId = bikeId;
        this.bikeName = bikeName;
        this.licensePlate = licensePlate;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.typeName = typeName;
        this.ownerType = ownerType;
        this.ownerName = ownerName;
        this.description = description;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public int getBikeId() {
        return bikeId;
    }

    public void setBikeId(int bikeId) {
        this.bikeId = bikeId;
    }

    public String getBikeName() {
        return bikeName;
    }

    public void setBikeName(String bikeName) {
        this.bikeName = bikeName;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(String ownerType) {
        this.ownerType = ownerType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
