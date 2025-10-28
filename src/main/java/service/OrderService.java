//an

package service;

import dao.IOrderDao;
import dao.OrderDao;
import model.OrderStatusHistory;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * OrderService - làm sạch, tránh lỗi, bắn notification cho Partner
 * - bookOneBike: kiểm tra khả dụng & chồng lịch, tạo đơn pending, notify Partner
 * - confirmOrderPickup: đánh dấu đã nhận xe, ghi history, notify Partner
 */
public class OrderService implements IOrderService {

    private final IOrderDao orderDao = new OrderDao();
    // Dùng Service để bắn notify đúng chuẩn, không gọi DAO trực tiếp
    private final INotificationService notificationService = new NotificationService();

    @Override
    public int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception {
        System.out.println("[OrderService] START bookOneBike - customer=" + customerId
                + ", bike=" + bikeId + ", dates=" + start + "→" + end);

        if (start == null || end == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày nhận và trả xe");
        }
        if (start.after(end)) {
            throw new IllegalArgumentException("Ngày nhận xe phải trước hoặc bằng ngày trả xe");
        }

        // 1) Kiểm tra giá & trạng thái xe
        BigDecimal pricePerDay = orderDao.getBikePriceIfBookable(bikeId);
        if (pricePerDay == null) {
            throw new IllegalStateException("Xe không khả dụng để thuê hoặc đang bảo dưỡng");
        }

        // 2) Kiểm tra chồng lịch (lock)
        boolean hasOverlap = orderDao.isOverlappingLocked(bikeId, start, end);
        if (hasOverlap) {
            String overlapDetails = getOverlapDetails(bikeId, start, end);
            String msg = "Xe không khả dụng trong khoảng thời gian đã chọn. "
                    + "Trùng với: " + overlapDetails
                    + ". Vui lòng chọn khoảng thời gian khác hoặc xe khác.";
            throw new IllegalStateException(msg);
        }

        // 3) Tạo đơn pending (DAO của bạn sẽ lo tạo Order + OrderDetails tương ứng)
        int orderId = orderDao.createPendingOrder(customerId, bikeId, start, end, pricePerDay);
        System.out.println("[OrderService] Created order #" + orderId);

        // 4) (YÊU CẦU) Notify tới các Partner có xe trong đơn này – hành động của customer
        try {
            notificationService.sendToPartnersByOrder(orderId,
                    "Đơn mới #" + orderId,
                    "Khách vừa tạo đơn thuê chứa xe của bạn. Vui lòng kiểm tra chi tiết đơn.");
        } catch (Exception ex) {
            // Không làm vỡ luồng chính
            System.err.println("[OrderService] notify partners (bookOneBike) failed: " + ex.getMessage());
        }

        return orderId;
    }

    /**
     * Lấy chi tiết chồng lịch để hiển thị chuyên nghiệp (không dùng cột không tồn tại).
     * Điều kiện:
     *  - Đơn liên quan tới bike
     *  - Trạng thái đơn đang giữ xe: status in ('pending','confirmed')
     *  - Chưa trả xe: pickup_status <> 'returned'
     *  - Khoảng thời gian có giao nhau
     */
    private String getOverlapDetails(int bikeId, Date start, Date end) throws SQLException {
        final String sql = """
            SELECT 
                ro.order_id,
                ro.start_date,
                ro.end_date
            FROM RentalOrders ro
            JOIN OrderDetails od ON ro.order_id = od.order_id
            WHERE od.bike_id = ?
              AND ro.status IN ('pending','confirmed')
              AND ro.pickup_status <> 'returned'
              AND NOT (ro.end_date < ? OR ro.start_date > ?)
            ORDER BY ro.start_date
            """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                while (rs.next()) {
                    if (!first) sb.append("; ");
                    sb.append("#").append(rs.getInt("order_id"))
                      .append(" (").append(rs.getDate("start_date"))
                      .append("→").append(rs.getDate("end_date")).append(")");
                    first = false;
                }
                return sb.length() == 0 ? "Không có bản ghi chồng lịch" : sb.toString();
            }
        }
    }

    /**
     * Admin xác nhận KHÁCH ĐÃ NHẬN XE (picked_up):
     *  - KHÔNG ép set status='confirmed' (tránh xung đột trigger thanh toán)
     *  - Đánh dấu pickup_status, ghi history, notify partner
     */
    public boolean confirmOrderPickup(int orderId, int adminId) {
        try {
            // 1) Đánh dấu đã giao xe (DAO của bạn thực hiện cập nhật pickup_status)
            orderDao.markOrderPickedUp(orderId, adminId);

            // 2) Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("picked_up"); // lịch sử mốc “đã nhận xe”
            history.setAdminId(adminId);
            history.setNotes("Khách hàng đã nhận xe");
            orderDao.addStatusHistory(history);

            // 3) Notify Partner: đơn đã được khách nhận xe
            try {
                notificationService.sendToPartnersByOrder(orderId,
                        "Đơn #" + orderId + " đã nhận xe",
                        "Khách đã nhận xe trong đơn có xe của bạn.");
            } catch (Exception ex) {
                System.err.println("[OrderService] notify partners (confirmOrderPickup) failed: " + ex.getMessage());
            }

            return true;
        } catch (SQLException e) {
            System.err.println("[OrderService] confirmOrderPickup failed: " + e.getMessage());
            return false;
        }
    }

   public List<Object[]> getOrdersForPickup() {
    try {
        // Nếu instance đúng là OrderDao thì gọi trực tiếp
        if (orderDao instanceof OrderDao) {
            return ((OrderDao) orderDao).getOrdersForPickup();
        }
        // Không phải OrderDao → trả list rỗng cho an toàn
        return new ArrayList<>();
    } catch (SQLException e) {
        System.err.println("[OrderService] getOrdersForPickup failed: " + e.getMessage());
        return new ArrayList<>();
    } catch (Exception e) { // phòng trường hợp DAO ném kiểu khác
        System.err.println("[OrderService] getOrdersForPickup unexpected: " + e.getMessage());
        return new ArrayList<>();
    }
}
}
