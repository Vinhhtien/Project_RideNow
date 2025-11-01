package model;

import java.math.BigDecimal;
import java.util.Date;

public class RefundInfo {
    private Integer inspectionId;
    private Integer orderId;
    private Integer adminId;
    private String bikeCondition;  // excellent | good | damaged
    private String damageNotes;
    private BigDecimal damageFee;
    private BigDecimal refundAmount;
    private String refundMethod;   // cash | wallet
    private String refundStatus;   // pending | processing | completed | cancelled | refunded
    private Date inspectedAt;

    // getters & setters
    public Integer getInspectionId() {
        return inspectionId;
    }

    public void setInspectionId(Integer inspectionId) {
        this.inspectionId = inspectionId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getBikeCondition() {
        return bikeCondition;
    }

    public void setBikeCondition(String bikeCondition) {
        this.bikeCondition = bikeCondition;
    }

    public String getDamageNotes() {
        return damageNotes;
    }

    public void setDamageNotes(String damageNotes) {
        this.damageNotes = damageNotes;
    }

    public BigDecimal getDamageFee() {
        return damageFee;
    }

    public void setDamageFee(BigDecimal damageFee) {
        this.damageFee = damageFee;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }

    public String getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(String refundStatus) {
        this.refundStatus = refundStatus;
    }

    public Date getInspectedAt() {
        return inspectedAt;
    }

    public void setInspectedAt(Date inspectedAt) {
        this.inspectedAt = inspectedAt;
    }
}
