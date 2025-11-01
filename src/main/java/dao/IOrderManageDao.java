package dao;

import model.OrderStatusHistory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface IOrderManageDao {
    boolean updateOrderStatus(int orderId, String status) throws SQLException;

    boolean markOrderPickedUp(int orderId, int adminId) throws SQLException;

    boolean markOrderReturned(int orderId, int adminId) throws SQLException;

    boolean markOrderReturned(Connection con, int orderId, int adminId) throws SQLException; // THÊM

    void addStatusHistory(OrderStatusHistory history) throws SQLException;

    void addStatusHistory(Connection con, OrderStatusHistory history) throws SQLException; // THÊM

    List<Object[]> getOrdersForPickup() throws SQLException;

    List<Object[]> getActiveOrders() throws SQLException;

    boolean updateBikeStatus(int orderId, String status) throws SQLException;
}