package service;

import dao.IOrderDao;
import dao.NotificationDao;
import dao.OrderDao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import model.OrderStatusHistory;
import utils.DBConnection;

public class OrderService implements IOrderService {
    private final IOrderDao dao = new OrderDao();
    private final OrderDao orderDAO = new OrderDao();
    private final NotificationDao notificationDAO = new NotificationDao();

    @Override
    public int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception {
        if (start == null || end == null) throw new IllegalArgumentException("Thiếu ngày nhận/trả");
        if (start.after(end)) throw new IllegalArgumentException("Ngày nhận phải ≤ ngày trả");

        // Đọc giá & check trạng thái xe
        BigDecimal pricePerDay = dao.getBikePriceIfBookable(bikeId);
        if (pricePerDay == null) {
            throw new IllegalStateException("Xe không khả dụng hoặc đang bảo dưỡng");
        }
        if (pricePerDay.signum() <= 0) {
            throw new IllegalStateException("Giá thuê không hợp lệ");
        }

        // Khóa logic chống race: kiểm tra chồng chéo trong cùng transaction (dao sẽ dùng UPDLOCK/HOLDLOCK)
        if (dao.isOverlappingLocked(bikeId, start, end)) {
            throw new IllegalStateException("Xe đã có lịch trong khoảng ngày này");
        }

        // Tạo đơn pending
        return dao.createPendingOrder(customerId, bikeId, start, end, pricePerDay);
    }

    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            
            // 1. Cập nhật trạng thái đơn hàng
            orderDAO.updateOrderStatus(orderId, "active");
            
            // 2. Đánh dấu đã giao xe
            orderDAO.markOrderPickedUp(orderId, adminId);
            
            // 3. Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("active");
            history.setAdminId(adminId);
            history.setNotes("Customer picked up the bike");
            orderDAO.addStatusHistory(history);
            
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
            return (List<Object[]>) orderDAO.getOrdersForPickup();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
