// src/main/java/dao/IOrderDao.java
package dao;

import java.math.BigDecimal;
import java.sql.Date;

public interface IOrderDao {

    /** Trả về giá/ngày nếu xe còn bookable (status != 'maintenance'), ngược lại null */
    BigDecimal getBikePriceIfBookable(int bikeId) throws Exception;

    /** Kiểm tra có đơn chồng chéo (pending/confirmed) với khoảng ngày không */
    boolean isOverlappingLocked(int bikeId, Date start, Date end) throws Exception;

    /** Tạo đơn pending + 1 dòng chi tiết; có ghi cả tiền cọc vào RentalOrders */
    int createPendingOrder(int customerId, int bikeId, Date start, Date end, BigDecimal pricePerDay) throws Exception;
}
