package dao;

import model.ChangeOrderVM;
import java.sql.Date;
import java.math.BigDecimal;

public interface IOrderChangeDao {
    enum ChangeResult { OK, EXPIRED, CONFLICT, FAIL }
    
    ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception;
    int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception;
    ChangeResult updateOrderDatesWithin30Min(int orderId, int accountId, Date newStart, Date newEnd) throws Exception;
    boolean checkDateConflict(int excludeOrderId, int bikeId, Date newStart, Date newEnd);
    
    // Các phương thức cho tính năng hủy đơn
    BigDecimal getDepositAmount(int orderId) throws Exception;
    BigDecimal getTotalAmount(int orderId) throws Exception;
    
    // XÓA các phương thức không dùng:
    // boolean isWithinCancellationPeriod(int orderId) throws Exception;
    // int cancelOrderAndProcessRefund(int orderId, int accountId, BigDecimal refundAmount) throws Exception;
}