package dao;

import model.ChangeOrderVM;

import java.math.BigDecimal;
import java.sql.Date;

public interface IOrderChangeDao {

    // 🔥 Thêm LIMIT_REACHED để báo vượt quá số lần đổi cho phép
    enum ChangeResult {
        OK,          // Đổi thành công
        EXPIRED,     // Hết hạn 30'
        CONFLICT,    // Trùng lịch
        FAIL,        // Lỗi khác
        LIMIT_REACHED // Đơn này đã đổi quá 3 lần
    }

    ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception;

    int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception;

    ChangeResult updateOrderDatesWithin30Min(int orderId,
                                             int accountId,
                                             Date newStart,
                                             Date newEnd) throws Exception;

    boolean checkDateConflict(int excludeOrderId,
                              int bikeId,
                              Date newStart,
                              Date newEnd);

    // Các phương thức hỗ trợ khác
    BigDecimal getDepositAmount(int orderId) throws Exception;

    BigDecimal getTotalAmount(int orderId) throws Exception;
    
    int getCancelCountByAccount(int accountId) throws Exception;
}
