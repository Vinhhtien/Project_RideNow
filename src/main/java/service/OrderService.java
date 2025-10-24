package service;

import dao.IOrderDao;
import dao.NotificationDao;
import dao.OrderDao;
import model.OrderStatusHistory;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;          // ✅ dùng java.sql.Date
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements IOrderService {
    private final IOrderDao orderDao = new OrderDao();
    private final NotificationDao notificationDAO = new NotificationDao();

    @Override
    public int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception {
        System.out.println("🚀 START bookOneBike - customer: " + customerId +
                ", bike: " + bikeId + ", dates: " + start + " to " + end);

        // Validate đầu vào
        if (start == null || end == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày nhận và trả xe");
        }
        if (start.after(end)) {
            throw new IllegalArgumentException("Ngày nhận xe phải trước hoặc bằng ngày trả xe");
        }

        // 1) Kiểm tra xe còn “bookable” (status != maintenance, tồn tại, lấy được price)
        BigDecimal pricePerDay = orderDao.getBikePriceIfBookable(bikeId);
        if (pricePerDay == null) {
            throw new IllegalStateException("Xe không khả dụng để thuê hoặc đang trong quá trình bảo dưỡng");
        }

        // 2) Kiểm tra chồng lịch với các đơn đã CONFIRMED (khóa logic trong DAO để tránh race)
        boolean hasOverlap = orderDao.isOverlappingLocked(bikeId, start, end);
        if (hasOverlap) {
            String overlapDetails = getOverlapDetails(bikeId, start, end);
            String professionalMessage =
                    "Xe không khả dụng trong khoảng thời gian đã chọn. "
                  + "Xe đang được thuê bởi các đơn hàng sau: " + overlapDetails
                  + ". Vui lòng chọn khoảng thời gian khác hoặc xe khác.";
            throw new IllegalStateException(professionalMessage);
        }

        // 3) Tạo đơn PENDING (Order + OrderDetails). Triển khai trên OrderDao chạy trong 1 transaction
        return orderDao.createPendingOrder(customerId, bikeId, start, end, pricePerDay);
    }

    /** Hiển thị chi tiết các đơn đang chồng lịch (đã confirmed & đang hiệu lực theo rule bạn đặt). */
    private String getOverlapDetails(int bikeId, Date start, Date end) throws SQLException {
        String sql = """
            SELECT 
                ro.order_id, c.full_name, ro.start_date, ro.end_date
            FROM RentalOrders ro
            JOIN OrderDetails od ON ro.order_id = od.order_id
            JOIN Customers c ON ro.customer_id = c.customer_id
            WHERE od.bike_id = ?
              AND ro.status = 'confirmed'
              AND ro.pickup_status = 'picked_up'
              AND ro.return_status IN ('not_returned', 'none')
              AND NOT (ro.end_date < ? OR ro.start_date > ?)
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);

            try (ResultSet rs = ps.executeQuery()) {
                StringBuilder details = new StringBuilder();
                boolean first = true;
                while (rs.next()) {
                    if (!first) details.append("; ");
                    details.append("Đơn hàng #").append(rs.getInt("order_id"))
                           .append(" (Khách hàng: ").append(rs.getString("full_name")).append(") từ ")
                           .append(rs.getDate("start_date")).append(" đến ").append(rs.getDate("end_date"));
                    first = false;
                }
                if (details.length() == 0) details.append("Không tìm thấy thông tin chi tiết");
                return details.toString();
            }
        }
    }

    /** Admin xác nhận đã giao xe cho khách. */
    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1) Cập nhật trạng thái đơn
            orderDao.updateOrderStatus(orderId, "confirmed");

            // 2) Đánh dấu đã giao xe
            orderDao.markOrderPickedUp(orderId, adminId);

            // 3) Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("picked_up");
            history.setAdminId(adminId);
            history.setNotes("Khách hàng đã nhận xe");
            orderDao.addStatusHistory(history);

            // 4) Thông báo cho customer
            int accountId = notificationDAO.getAccountIdByOrderId(orderId);
            if (accountId > 0) {
                notificationDAO.createNotification(
                        accountId,
                        "Đã nhận xe thành công",
                        "Đơn hàng #" + orderId + " đã được xác nhận nhận xe. Chúc bạn có chuyến đi an toàn!"
                );
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
        }
    }

    /** Lấy danh sách đơn chờ giao xe (phục vụ UI). */
    public List<Object[]> getOrdersForPickup() {
        try {
            return ((OrderDao) orderDao).getOrdersForPickup();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // ===== API cho giỏ hàng: check trùng lịch trước khi add/checkout =====
    @Override
    public boolean isBikeAvailable(int bikeId, Date start, Date end) throws SQLException {
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM RentalOrders r
            JOIN OrderDetails d ON d.order_id = r.order_id
            WHERE d.bike_id = ?
              AND r.status = 'confirmed'
              AND NOT (r.end_date < ? OR r.start_date > ?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt("cnt") == 0; // 0 = không đụng lịch
            }
        }
    }
    
    
    
    
     @Override
    public List<OverlappedRange> getOverlappingRanges(int bikeId, Date start, Date end) throws SQLException {
        String sql = """
            SELECT r.order_id, r.start_date, r.end_date
            FROM RentalOrders r
            JOIN OrderDetails d ON d.order_id = r.order_id
            WHERE d.bike_id = ?
              AND r.status = 'confirmed'
              AND NOT (r.end_date < ? OR r.start_date > ?)
            ORDER BY r.start_date
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);
            List<OverlappedRange> list = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new OverlappedRange(
                        rs.getInt("order_id"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date")
                    ));
                }
            }
            return list;
        }
    }
    
}
