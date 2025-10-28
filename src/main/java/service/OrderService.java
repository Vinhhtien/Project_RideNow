package service;

import dao.IOrderDao;
import dao.NotificationDao;
import dao.OrderDao;
import model.OrderStatusHistory;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderService implements IOrderService {
    private final IOrderDao orderDao = new OrderDao();
    private final NotificationDao notificationDAO = new NotificationDao();
    private final INotificationService notificationService = new NotificationService();

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

        // 1) Kiểm tra xe còn "bookable" (status != maintenance, tồn tại, lấy được price)
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
        int orderId = orderDao.createPendingOrder(customerId, bikeId, start, end, pricePerDay);
        System.out.println("[OrderService] Created order #" + orderId);

        // 4) Gửi notification cho customer (từ service cũ)
        try {
            int accountId = notificationDAO.getAccountIdByOrderId(orderId);
            if (accountId > 0) {
                notificationDAO.createNotification(
                        accountId,
                        "Đặt xe thành công",
                        "Đơn hàng #" + orderId + " của bạn đã được tạo thành công và đang chờ xác nhận."
                );
            }
        } catch (Exception ex) {
            System.err.println("[OrderService] notify customer failed: " + ex.getMessage());
        }

        // 5) Gửi notification cho partners (từ service mới)
        try {
            notificationService.sendToPartnersByOrder(orderId,
                    "Đơn mới #" + orderId,
                    "Khách vừa tạo đơn thuê chứa xe của bạn. Vui lòng kiểm tra chi tiết đơn.");
        } catch (Exception ex) {
            System.err.println("[OrderService] notify partners (bookOneBike) failed: " + ex.getMessage());
        }

        return orderId;
    }

    /** Hiển thị chi tiết các đơn đang chồng lịch - Kết hợp logic từ cả hai phiên bản */
    private String getOverlapDetails(int bikeId, Date start, Date end) throws SQLException {
        // Sử dụng phiên bản cải tiến từ service mới nhưng giữ thông tin chi tiết từ service cũ
        String sql = """
            SELECT 
                ro.order_id, 
                c.full_name, 
                ro.start_date, 
                ro.end_date,
                ro.status
            FROM RentalOrders ro
            JOIN OrderDetails od ON ro.order_id = od.order_id
            JOIN Customers c ON ro.customer_id = c.customer_id
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
                StringBuilder details = new StringBuilder();
                boolean first = true;
                while (rs.next()) {
                    if (!first) details.append("; ");
                    details.append("Đơn hàng #").append(rs.getInt("order_id"))
                           .append(" (Khách hàng: ").append(rs.getString("full_name")).append(") từ ")
                           .append(rs.getDate("start_date")).append(" đến ").append(rs.getDate("end_date"))
                           .append(" [").append(rs.getString("status")).append("]");
                    first = false;
                }
                if (details.length() == 0) details.append("Không tìm thấy thông tin chi tiết");
                return details.toString();
            }
        }
    }

    /** Admin xác nhận đã giao xe cho khách - Kết hợp cả transaction và partner notification */
    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1) Cập nhật trạng thái đơn (từ service cũ)
            orderDao.updateOrderStatus(orderId, "confirmed");

            // 2) Đánh dấu đã giao xe (từ cả hai service)
            orderDao.markOrderPickedUp(orderId, adminId);

            // 3) Ghi lịch sử (từ cả hai service)
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("picked_up");
            history.setAdminId(adminId);
            history.setNotes("Khách hàng đã nhận xe");
            orderDao.addStatusHistory(history);

            // 4) Thông báo cho customer (từ service cũ)
            int accountId = notificationDAO.getAccountIdByOrderId(orderId);
            if (accountId > 0) {
                notificationDAO.createNotification(
                        accountId,
                        "Đã nhận xe thành công",
                        "Đơn hàng #" + orderId + " đã được xác nhận nhận xe. Chúc bạn có chuyến đi an toàn!"
                );
            }

            // 5) Thông báo cho partners (từ service mới)
            try {
                notificationService.sendToPartnersByOrder(orderId,
                        "Đơn #" + orderId + " đã nhận xe",
                        "Khách đã nhận xe trong đơn có xe của bạn.");
            } catch (Exception ex) {
                System.err.println("[OrderService] notify partners (confirmOrderPickup) failed: " + ex.getMessage());
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ignored) {}
            System.err.println("[OrderService] confirmOrderPickup failed: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                } catch (SQLException ignored) {}
            }
        }
    }

    /** Lấy danh sách đơn chờ giao xe (phục vụ UI) - Kết hợp xử lý lỗi từ cả hai */
    public List<Object[]> getOrdersForPickup() {
        try {
            if (orderDao instanceof OrderDao) {
                return ((OrderDao) orderDao).getOrdersForPickup();
            }
            return new ArrayList<>();
        } catch (SQLException e) {
            System.err.println("[OrderService] getOrdersForPickup failed: " + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[OrderService] getOrdersForPickup unexpected: " + e.getMessage());
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