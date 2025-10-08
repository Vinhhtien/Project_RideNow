package dao;

import model.OrderStatusHistory;
import java.sql.SQLException;
import java.util.List;

public interface IOrderManageDao {
    boolean updateOrderStatus(int orderId, String status) throws SQLException;
    boolean markOrderPickedUp(int orderId, int adminId) throws SQLException;
    boolean markOrderReturned(int orderId, int adminId) throws SQLException;
    void addStatusHistory(OrderStatusHistory history) throws SQLException;
    List<Object[]> getOrdersForPickup() throws SQLException;
    List<Object[]> getActiveOrders() throws SQLException;
}