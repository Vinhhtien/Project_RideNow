package service;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IOrderService {
    int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception;
    boolean isBikeAvailable(int bikeId, Date start, Date end) throws SQLException;
    
    /** Trả về danh sách các khoảng ngày (đơn đã xác nhận) đè lên [start,end] của cùng chiếc xe */
    List<OverlappedRange> getOverlappingRanges(int bikeId, Date start, Date end) throws SQLException;

    /** DTO đơn giản để hiển thị */
    class OverlappedRange {
        public final int orderId;
        public final Date start;
        public final Date end;
        public OverlappedRange(int orderId, Date start, Date end) {
            this.orderId = orderId; this.start = start; this.end = end;
        }
    }
}
