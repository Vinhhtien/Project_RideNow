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
import model.RentalOrder;

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

    /**
     * Hiển thị chi tiết các đơn đang chồng lịch - Kết hợp logic từ cả hai phiên bản
     */
    private String getOverlapDetails(int bikeId, Date start, Date end) throws SQLException {
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

    /**
     * Admin xác nhận đã giao xe cho khách - Kết hợp cả transaction và partner notification
     */
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
            if (con != null) try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            System.err.println("[OrderService] confirmOrderPickup failed: " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Lấy danh sách đơn chờ giao xe (phục vụ UI) - Kết hợp xử lý lỗi từ cả hai
     */
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

    // ===== API cho admin: check trùng lịch (loại trừ booking admin) =====
    @Override
    public boolean isBikeAvailableForAdmin(int bikeId, Date start, Date end) throws SQLException {
        // Điều kiện giao nhau giữa 2 khoảng ngày:
        // [start, end] giao với [o.start_date, o.end_date] khi:
        //   o.end_date >= start  AND  o.start_date <= end
        //
        // Ta chỉ chặn các đơn KHÔNG bị hủy (pending, confirmed, completed).
        // Nếu em muốn thêm trạng thái khác thì đưa vào IN (...)
        String sql = """
            SELECT COUNT(*) AS cnt
            FROM OrderDetails od
            JOIN RentalOrders r ON r.order_id = od.order_id
            WHERE od.bike_id = ?
              AND r.status IN ('pending', 'confirmed', 'completed')
              AND r.end_date >= ?    -- newStart
              AND r.start_date <= ?  -- newEnd
        """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, bikeId);
            ps.setDate(2, start); // newStart
            ps.setDate(3, end);   // newEnd

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cnt = rs.getInt("cnt");
                    // Nếu không có đơn nào giao ngày => xe rảnh
                    return cnt == 0;
                }
            }
        }
        // lỗi SQL hoặc không có row => coi như không rảnh (an toàn)
        return false;
    }

    // ===== Tạo booking admin để đánh dấu xe đã được thuê =====
    @Override
    public boolean createAdminBooking(int bikeId, Date startDate, Date endDate, String note) throws SQLException {
        System.out.println("[OrderService] createAdminBooking for bike " + bikeId +
                " from " + startDate + " to " + endDate);

        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            // 1. Tìm / tạo customer đặc biệt cho admin
            int adminCustomerId = findOrCreateAdminCustomer(con);

            // 2. Lấy giá xe
            BigDecimal pricePerDay = getBikePrice(bikeId, con);
            if (pricePerDay == null) {
                throw new SQLException("Không tìm thấy giá xe cho bike_id=" + bikeId);
            }

            // 3. Tính số ngày + tiền
            long days = (endDate.toLocalDate().toEpochDay() - startDate.toLocalDate().toEpochDay()) + 1;
            if (days <= 0) {
                throw new SQLException("Khoảng ngày không hợp lệ");
            }

            BigDecimal lineTotal = pricePerDay.multiply(BigDecimal.valueOf(days));
            BigDecimal orderTotal = lineTotal;

            // 4. Đặt cọc = 0 (vì là booking hệ thống)
            BigDecimal deposit = BigDecimal.ZERO;

            // 5. Tạo order (DÙNG CÙNG CỘT VỚI createPendingOrder)
            String insertOrderSQL = """
                INSERT INTO RentalOrders(
                    customer_id, start_date, end_date, total_price,
                    status, created_at, deposit_amount, deposit_status
                )
                OUTPUT INSERTED.order_id
                VALUES (?, ?, ?, ?, 'confirmed', GETDATE(), ?, 'none')
                """;

            int orderId;
            try (PreparedStatement ps = con.prepareStatement(insertOrderSQL)) {
                ps.setInt(1, adminCustomerId);
                ps.setDate(2, startDate);
                ps.setDate(3, endDate);
                ps.setBigDecimal(4, orderTotal);
                ps.setBigDecimal(5, deposit);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Không lấy được order_id cho admin booking");
                    orderId = rs.getInt(1);
                }
            }

            // 6. OrderDetails
            String insertDetailSQL = """
                INSERT INTO OrderDetails(order_id, bike_id, price_per_day, quantity, line_total)
                VALUES (?, ?, ?, 1, ?)
                """;
            try (PreparedStatement ps = con.prepareStatement(insertDetailSQL)) {
                ps.setInt(1, orderId);
                ps.setInt(2, bikeId);
                ps.setBigDecimal(3, pricePerDay);
                ps.setBigDecimal(4, lineTotal);
                ps.executeUpdate();
            }

            // 7. Payment (đánh dấu đã trả)
            String insertPaymentSQL = """
                INSERT INTO Payments(order_id, amount, method, status)
                VALUES (?, ?, 'cash', 'paid')
                """;
            try (PreparedStatement ps = con.prepareStatement(insertPaymentSQL)) {
                ps.setInt(1, orderId);
                ps.setBigDecimal(2, orderTotal);
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("✅ Admin booking created successfully - Order #" + orderId);
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            System.err.println("❌ Error creating admin booking: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }


    private int findOrCreateAdminCustomer(Connection con) throws SQLException {
        // Tìm customer admin đã tồn tại
        String findSQL = "SELECT customer_id FROM Customers WHERE email = 'admin_booking@system.com'";
        try (PreparedStatement ps = con.prepareStatement(findSQL)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("customer_id");
            }
        }

        // Nếu không tìm thấy, tạo mới
        // 1. Tạo account trước
        String insertAccountSQL = "INSERT INTO Accounts (username, password, role, status) VALUES (?, ?, 'customer', 1)";
        int accountId;
        try (PreparedStatement ps = con.prepareStatement(insertAccountSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "admin_booking");
            ps.setString(2, "system_password"); // Mật khẩu mặc định
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                accountId = rs.getInt(1);
            } else {
                throw new SQLException("Không thể tạo account cho admin booking");
            }
        }

        // 2. Tạo customer
        String insertCustomerSQL = "INSERT INTO Customers (account_id, full_name, email, phone, address, admin_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(insertCustomerSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, "Hệ thống - Admin Booking");
            ps.setString(3, "admin_booking@system.com");
            ps.setString(4, "000-000-0000");
            ps.setString(5, "Hệ thống");
            ps.setInt(6, 1); // admin_id = 1
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                throw new SQLException("Không thể tạo customer cho admin booking");
            }
        }
    }

    private BigDecimal getBikePrice(int bikeId, Connection con) throws SQLException {
        String sql = "SELECT price_per_day FROM Motorbikes WHERE bike_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("price_per_day");
            }
        }
        return null;
    }
    
    
    @Override
    public RentalOrder findCurrentAdminBookingForBike(int bikeId) throws SQLException {
        // Ủy quyền xuống DAO, không đụng tới SQL ở đây
        return orderDao.findCurrentAdminBookingForBike(bikeId);
    }

    
    @Override
public boolean reopenChangeWindowByAdmin(int orderId, int adminId) {
    try {
        boolean ok = orderDao.reopenChangeWindow(orderId);

        if (ok) {
            // Ghi lịch sử trạng thái cho đẹp
            OrderStatusHistory h = new OrderStatusHistory();
            h.setOrderId(orderId);
            h.setStatus("confirmed"); // vẫn là confirmed
            h.setAdminId(adminId);
            h.setNotes("Admin reopened 30-minute change window for customer");
            orderDao.addStatusHistory(h);
        }

        return ok;
    } catch (SQLException e) {
        System.err.println("[OrderService] reopenChangeWindowByAdmin failed: " + e.getMessage());
        return false;
    }
}


    
}