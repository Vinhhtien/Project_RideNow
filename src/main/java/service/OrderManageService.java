package service;

import dao.IOrderManageDao;
import dao.INotificationDao;
import dao.OrderManageDao;
import dao.NotificationDao;
import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class OrderManageService implements IOrderManageService {
    private final IOrderManageDao orderDao = new OrderManageDao();
    private final INotificationDao notificationDao = new NotificationDao();
    
    @Override
    public List<Object[]> getOrdersForPickup() {
        try {
            return orderDao.getOrdersForPickup();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public List<Object[]> getActiveOrders() {
        try {
            return orderDao.getActiveOrders();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            System.out.println("=== ORDER PICKUP CONFIRMATION START ===");
            System.out.println("DEBUG: Order ID: " + orderId + ", Admin ID: " + adminId);

            // 1. Đánh dấu đã giao xe (chỉ cập nhật pickup_status, KHÔNG cập nhật status)
            boolean pickupMarked = orderDao.markOrderPickedUp(orderId, adminId);
            if (!pickupMarked) {
                throw new SQLException("Failed to mark order as picked up");
            }
            System.out.println("DEBUG: Order marked as picked up successfully");

            // 2. Ghi lịch sử - dùng status 'confirmed' thay vì 'active'
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("confirmed"); // SỬA: dùng status hợp lệ
            history.setAdminId(adminId);
            history.setNotes("Customer picked up the bike");
            orderDao.addStatusHistory(history);
            System.out.println("DEBUG: History added successfully");

            // 3. Gửi thông báo cho customer
            int accountId = notificationDao.getAccountIdByOrderId(orderId);
            if (accountId > 0) {
                notificationDao.createNotification(accountId, 
                    "Đã nhận xe thành công", 
                    "Đơn hàng #" + orderId + " đã được xác nhận nhận xe. Chúc bạn có chuyến đi an toàn!");
                System.out.println("DEBUG: Notification sent to customer");
            }

            con.commit();
            System.out.println("=== ORDER PICKUP CONFIRMATION SUCCESS ===");
            return true;

        } catch (SQLException e) {
            System.err.println("=== ORDER PICKUP CONFIRMATION FAILED ===");
            System.err.println("ERROR: " + e.getMessage());

            if (con != null) {
                try { 
                    con.rollback(); 
                    System.out.println("DEBUG: Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("ERROR: Rollback failed: " + ex.getMessage());
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                    System.out.println("DEBUG: Connection closed");
                } catch (SQLException e) {
                    System.err.println("ERROR: Connection close failed: " + e.getMessage());
                }
            }
            System.out.println("=== ORDER PICKUP CONFIRMATION END ===");
        }
    }
    
    @Override
    public boolean confirmOrderReturn(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            System.out.println("=== ORDER RETURN CONFIRMATION START ===");
            System.out.println("DEBUG: Order ID: " + orderId + ", Admin ID: " + adminId);

            // 1. Đánh dấu đã trả xe
            boolean returnMarked = orderDao.markOrderReturned(orderId, adminId);
            if (!returnMarked) {
                throw new SQLException("Failed to mark order as returned");
            }
            System.out.println("DEBUG: Order marked as returned successfully");

            // 2. Cập nhật xe về available
            updateBikeStatusToAvailable(orderId);
            System.out.println("DEBUG: Bike status updated to available");

            // 3. Cập nhật status order thành 'completed' (hợp lệ với CHECK constraint)
            boolean statusUpdated = orderDao.updateOrderStatus(orderId, "completed");
            if (!statusUpdated) {
                throw new SQLException("Failed to update order status to completed");
            }
            System.out.println("DEBUG: Order status updated to completed");

            // 4. Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("completed");
            history.setAdminId(adminId);
            history.setNotes("Bike returned and inspection completed");
            orderDao.addStatusHistory(history);
            System.out.println("DEBUG: History added successfully");

            con.commit();
            System.out.println("=== ORDER RETURN CONFIRMATION SUCCESS ===");
            return true;

        } catch (SQLException e) {
            System.err.println("=== ORDER RETURN CONFIRMATION FAILED ===");
            System.err.println("ERROR: " + e.getMessage());

            if (con != null) {
                try { 
                    con.rollback(); 
                    System.out.println("DEBUG: Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("ERROR: Rollback failed: " + ex.getMessage());
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                    System.out.println("DEBUG: Connection closed");
                } catch (SQLException e) {
                    System.err.println("ERROR: Connection close failed: " + e.getMessage());
                }
            }
            System.out.println("=== ORDER RETURN CONFIRMATION END ===");
        }
    }
    
    
    private void updateBikeStatusToAvailable(int orderId) throws SQLException {
        String sql = "UPDATE Motorbikes SET status = 'available' WHERE bike_id IN (SELECT bike_id FROM OrderDetails WHERE order_id = ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }
}