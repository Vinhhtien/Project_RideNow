package dao;

import model.RentalOrder;
import model.OrderDetail;
import model.OrderStatusHistory;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IOrderDao {
    // Basic order operations
    RentalOrder getOrderById(int orderId) throws SQLException;
    List<OrderDetail> getOrderDetailsByOrderId(int orderId) throws SQLException;
    List<RentalOrder> getOrdersByCustomerId(int customerId) throws SQLException;
    List<RentalOrder> getAllOrders() throws SQLException;
    List<RentalOrder> searchOrders(String keyword) throws SQLException;
    List<RentalOrder> filterByStatus(String status) throws SQLException;
    
    // Bike booking methods
    BigDecimal getBikePriceIfBookable(int bikeId) throws Exception;
    boolean isOverlappingLocked(int bikeId, Date start, Date end) throws Exception;
    int createPendingOrder(int customerId, int bikeId, Date start, Date end, BigDecimal pricePerDay) throws Exception;
    
    // Order management methods
    boolean updateOrderStatus(int orderId, String newStatus) throws SQLException;
    boolean markOrderPickedUp(int orderId, int adminId) throws SQLException;
    boolean markOrderReturned(int orderId, int adminId) throws SQLException;
    void addStatusHistory(OrderStatusHistory history) throws SQLException;
    List<Object[]> getOrdersForPickup() throws SQLException;
    
    // Statistics method
    Object[] getOrderStatsByCustomerId(int customerId) throws SQLException;
}