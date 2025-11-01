package service;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface IOrderService {
    int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception;

    boolean isBikeAvailable(int bikeId, Date start, Date end) throws SQLException;

    /**
     * Trả về danh sách các khoảng ngày (đơn đã xác nhận) đè lên [start,end] của cùng chiếc xe
     */
    List<OverlappedRange> getOverlappingRanges(int bikeId, Date start, Date end) throws SQLException;

    /**
     * Kiểm tra tính khả dụng cho admin (loại trừ các booking admin)
     */
    boolean isBikeAvailableForAdmin(int bikeId, Date start, Date end) throws SQLException;

    /**
     * Tạo booking admin để đánh dấu xe đã được thuê
     */
    boolean createAdminBooking(int bikeId, Date startDate, Date endDate, String note) throws SQLException;

    /**
     * DTO đơn giản để hiển thị
     */
    class OverlappedRange {
        public final int orderId;
        public final Date start;
        public final Date end;

        public OverlappedRange(int orderId, Date start, Date end) {
            this.orderId = orderId;
            this.start = start;
            this.end = end;
        }
    }
}