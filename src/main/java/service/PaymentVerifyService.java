package service;

import dao.IPaymentVerifyDao;
import dao.IOrderManageDao;
import dao.INotificationDao;
import dao.PaymentVerifyDao;
import dao.OrderManageDao;
import dao.NotificationDao;
import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PaymentVerifyService implements IPaymentVerifyService {
    private final IPaymentVerifyDao paymentDao = new PaymentVerifyDao();
    private final IOrderManageDao orderDao = new OrderManageDao();
    private final INotificationDao notificationDao = new NotificationDao();
    
    @Override
    public List<Object[]> getPendingPayments() {
        try {
            System.out.println("DEBUG: Fetching pending payments...");
            List<Object[]> payments = paymentDao.getPendingPayments();
            System.out.println("DEBUG: Found " + payments.size() + " pending payments");
            return payments;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get pending payments: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
  
    @Override
    public boolean verifyPayment(int paymentId, int adminId) {
        Connection con = null;
        try {
            // THÊM: Kiểm tra admin_id có hợp lệ không (phải là 1)
            if (adminId != 1) {
                System.err.println("=== PAYMENT VERIFICATION FAILED ===");
                System.err.println("ERROR: Invalid admin ID. Only admin_id = 1 is allowed.");
                return false;
            }

            con = DBConnection.getConnection();
            con.setAutoCommit(false);

            System.out.println("=== PAYMENT VERIFICATION START ===");
            System.out.println("DEBUG: Payment ID: " + paymentId + ", Admin ID: " + adminId);

            // 1. Lấy order_id từ payment
            int orderId = paymentDao.getOrderIdByPayment(paymentId);
            if (orderId == 0) {
                throw new SQLException("Không tìm thấy order_id cho payment: " + paymentId);
            }
            System.out.println("DEBUG: Order ID: " + orderId);

            // 2. Cập nhật payment status thành 'paid'
            boolean paymentUpdated = paymentDao.updatePaymentStatus(paymentId, "paid", adminId);
            if (!paymentUpdated) {
                throw new SQLException("Không thể cập nhật payment status - payment không tồn tại hoặc đã được xử lý");
            }
            System.out.println("DEBUG: Payment updated successfully");

            // 3. Ghi lịch sử
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("confirmed");
            history.setAdminId(adminId);
            history.setNotes("Payment verified manually by admin");

            orderDao.addStatusHistory(history);
            System.out.println("DEBUG: History added successfully");

            // 4. Gửi thông báo cho customer
            int accountId = notificationDao.getAccountIdByOrderId(orderId);
            System.out.println("DEBUG: Customer account ID: " + accountId);

            if (accountId > 0) {
                notificationDao.createNotification(accountId,
                    "Thanh toán thành công",
                    "Đơn hàng #" + orderId + " đã được xác nhận thanh toán. Vui lòng đến nhận xe theo lịch hẹn!");
                System.out.println("DEBUG: Notification created successfully");
            }

            con.commit();
            System.out.println("=== PAYMENT VERIFICATION SUCCESS ===");
            return true;

        } catch (SQLException e) {
            System.err.println("=== PAYMENT VERIFICATION FAILED ===");
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
            System.out.println("=== PAYMENT VERIFICATION END ===");
        }
    }
    
}