package service;

import java.util.List;

public interface IRefundInspectionService {
    List<Object[]> getRefundOrders();

    List<Object[]> getRefundRequests();

    boolean processRefund(int inspectionId, String action, String refundMethod);

    boolean createInspection(int orderId, int adminId, String bikeCondition,
                             String damageNotes, double damageFee, double refundAmount, String refundMethod);
}