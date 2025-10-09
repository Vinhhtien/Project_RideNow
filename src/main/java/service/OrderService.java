package service;

import dao.IOrderDao;
import dao.NotificationDao;
import dao.OrderDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.OrderStatusHistory;
import utils.DBConnection;

public class OrderService implements IOrderService {
    private final IOrderDao orderDao = new OrderDao(); // SỬA: chỉ dùng orderDao
    private final NotificationDao notificationDAO = new NotificationDao();

    @Override
    public int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception {
        System.out.println("🚀 START bookOneBike - customer: " + customerId + 
                          ", bike: " + bikeId + ", dates: " + start + " to " + end);

        if (start == null || end == null) throw new IllegalArgumentException("Vui lòng chọn ngày nhận và trả xe");
        if (start.after(end)) throw new IllegalArgumentException("Ngày nhận xe phải trước hoặc bằng ngày trả xe");

        // 1. Kiểm tra giá và trạng thái xe
        System.out.println("🔍 Checking bike availability...");
        BigDecimal pricePerDay = orderDao.getBikePriceIfBookable(bikeId);
        if (pricePerDay == null) {
            throw new IllegalStateException("Xe không khả dụng để thuê hoặc đang trong quá trình bảo dưỡng");
        }
        System.out.println("✅ Bike available, price: " + pricePerDay);

        // 2. Kiểm tra chồng lịch
        System.out.println("🔍 Checking for overlapping bookings...");
        boolean hasOverlap = orderDao.isOverlappingLocked(bikeId, start, end);

        if (hasOverlap) {
            System.out.println("❌ OVERLAP DETECTED for bike " + bikeId);
            String overlapDetails = getOverlapDetails(bikeId, start, end);

            // SỬA: Thông báo KHÔNG chứa tên xe
            String professionalMessage = "Xe không khả dụng trong khoảng thời gian đã chọn. " +
                                       "Xe đang được thuê bởi các đơn hàng sau: " + overlapDetails + 
                                       ". Vui lòng chọn khoảng thời gian khác hoặc xe khác.";

            throw new IllegalStateException(professionalMessage);
        }
        System.out.println("✅ No overlapping bookings found");

        // 3. Tạo đơn pending
        System.out.println("📝 Creating pending order...");
        int orderId = orderDao.createPendingOrder(customerId, bikeId, start, end, pricePerDay);
        System.out.println("✅ SUCCESS - Created order #" + orderId);

        return orderId;
    }
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
            """;  // SỬA: ro.start_date thay vì r.start_date

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);

            ResultSet rs = ps.executeQuery();
            StringBuilder details = new StringBuilder();

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    details.append("; ");
                }
                details.append("Đơn hàng #").append(rs.getInt("order_id"))
                       .append(" (Khách hàng: ").append(rs.getString("full_name"))
                       .append(") từ ").append(rs.getDate("start_date"))
                       .append(" đến ").append(rs.getDate("end_date"));
                first = false;
            }

            if (details.length() == 0) {
                details.append("Không tìm thấy thông tin chi tiết");
            }

            return details.toString();
        }
    }

    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            
            // 1. Cập nhật trạng thái đơn hàng - SỬA: dùng orderDao thay vì orderDAO
            orderDao.updateOrderStatus(orderId, "confirmed"); // SỬA: status nên là 'confirmed' thay vì 'active'
            
            // 2. Đánh dấu đã giao xe
            orderDao.markOrderPickedUp(orderId, adminId);
            
            // 3. Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("picked_up"); // SỬA: status nên là 'picked_up'
            history.setAdminId(adminId);
            history.setNotes("Khách hàng đã nhận xe");
            orderDao.addStatusHistory(history);
            
            // 4. Gửi thông báo cho customer
            int accountId = notificationDAO.getAccountIdByOrderId(orderId);
            if (accountId > 0) {
                notificationDAO.createNotification(accountId, 
                    "Đã nhận xe thành công", 
                    "Đơn hàng #" + orderId + " đã được xác nhận nhận xe. Chúc bạn có chuyến đi an toàn!");
            }
            
            con.commit();
            return true;
            
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) {}
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) {}
        }
    }
    
    public List<Object[]> getOrdersForPickup() {
        try {
            // SỬA: Ép kiểu đúng - OrderDao implements IOrderDao nên có method getOrdersForPickup()
            return ((OrderDao) orderDao).getOrdersForPickup();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}