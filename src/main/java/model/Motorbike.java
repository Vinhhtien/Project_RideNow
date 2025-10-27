//package model;
//
//import java.math.BigDecimal;
//
//public class Motorbike {
//    private int bikeId;
//    private Integer partnerId;
//    private Integer storeId;
//    private int typeId;
//    private String bikeName;
//    private String licensePlate;
//    private BigDecimal pricePerDay;
//    private String status;
//    private String description;
//
//    public Motorbike() {}
//
//    public Motorbike(int bikeId, Integer partnerId, Integer storeId, int typeId, String bikeName,
//                     String licensePlate, BigDecimal pricePerDay, String status, String description) {
//        this.bikeId = bikeId;
//        this.partnerId = partnerId;
//        this.storeId = storeId;
//        this.typeId = typeId;
//        this.bikeName = bikeName;
//        this.licensePlate = licensePlate;
//        this.pricePerDay = pricePerDay;
//        this.status = status;
//        this.description = description;
//    }
//
//    public int getBikeId() { return bikeId; }
//    public void setBikeId(int bikeId) { this.bikeId = bikeId; }
//    public Integer getPartnerId() { return partnerId; }
//    public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }
//    public Integer getStoreId() { return storeId; }
//    public void setStoreId(Integer storeId) { this.storeId = storeId; }
//    public int getTypeId() { return typeId; }
//    public void setTypeId(int typeId) { this.typeId = typeId; }
//    public String getBikeName() { return bikeName; }
//    public void setBikeName(String bikeName) { this.bikeName = bikeName; }
//    public String getLicensePlate() { return licensePlate; }
//    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
//    public BigDecimal getPricePerDay() { return pricePerDay; }
//    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
//    public String getStatus() { return status; }
//    public void setStatus(String status) { this.status = status; }
//    public String getDescription() { return description; }
//    public void setDescription(String description) { this.description = description; }
//}


package model;

import java.math.BigDecimal;

public class Motorbike {
    private int bikeId;
    private Integer partnerId;
    private Integer storeId;
    private int typeId;
    private String bikeName;
    private String licensePlate;
    private BigDecimal pricePerDay;
    private String status;
    private String description;
    
    // Additional fields for display (không lưu trong database)
    private String ownerName;
    private String ownerType;
    private String typeName;

    public Motorbike() {}

    public Motorbike(int bikeId, Integer partnerId, Integer storeId, int typeId, String bikeName,
                     String licensePlate, BigDecimal pricePerDay, String status, String description) {
        this.bikeId = bikeId;
        this.partnerId = partnerId;
        this.storeId = storeId;
        this.typeId = typeId;
        this.bikeName = bikeName;
        this.licensePlate = licensePlate;
        this.pricePerDay = pricePerDay;
        this.status = status;
        this.description = description;
    }

    // Getters and Setters
    public int getBikeId() { return bikeId; }
    public void setBikeId(int bikeId) { this.bikeId = bikeId; }
    public Integer getPartnerId() { return partnerId; }
    public void setPartnerId(Integer partnerId) { this.partnerId = partnerId; }
    public Integer getStoreId() { return storeId; }
    public void setStoreId(Integer storeId) { this.storeId = storeId; }
    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    public String getBikeName() { return bikeName; }
    public void setBikeName(String bikeName) { this.bikeName = bikeName; }
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    // Additional getters and setters for display fields
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getOwnerType() { return ownerType; }
    public void setOwnerType(String ownerType) { this.ownerType = ownerType; }
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
}