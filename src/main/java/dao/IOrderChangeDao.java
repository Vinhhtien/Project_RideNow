package dao;

import model.ChangeOrderVM;
import java.sql.Date;

public interface IOrderChangeDao {
    ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception;
    int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception;
    ChangeResult updateOrderDatesWithin30Min(int orderId, int accountId, Date newStart, Date newEnd) throws Exception;
    
    enum ChangeResult {
        OK, EXPIRED, CONFLICT, FAIL
    }
}