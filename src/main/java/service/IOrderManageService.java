package service;

import java.util.List;

public interface IOrderManageService {
    List<Object[]> getOrdersForPickup();
    boolean confirmOrderPickup(int orderId, int adminId);
    List<Object[]> getActiveOrders();
    boolean confirmOrderReturn(int orderId, int adminId);
}