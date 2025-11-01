package service;

import java.util.List;

public interface IOrderManageService {
    List<Object[]> getOrdersForPickup();

    List<Object[]> getActiveOrders();

    boolean confirmOrderPickup(int orderId, int adminId);

    boolean confirmOrderReturn(int orderId, int adminId);

    boolean canPickupOrder(int orderId);

    boolean markOrderAsNotGiven(int orderId, int adminId, String notes); // NEW

    boolean canReturnOrder(int orderId);

    boolean confirmOverdueReturn(int orderId, int adminId, String lateFee, String notes);

    boolean markOrderAsNotReturned(int orderId, int adminId, String notes);
}