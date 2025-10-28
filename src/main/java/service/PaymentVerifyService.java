// an
package service;

import dao.IPaymentVerifyDao;
import dao.IOrderManageDao;
import dao.PaymentVerifyDao;
import dao.OrderManageDao;
import model.OrderStatusHistory;

import java.sql.SQLException;
import java.util.List;

public class PaymentVerifyService implements IPaymentVerifyService {
    private final IPaymentVerifyDao paymentDao = new PaymentVerifyDao();
    private final IOrderManageDao   orderDao   = new OrderManageDao();

    // CHỈ DÙNG để gửi thông báo CHO PARTNER
    private final INotificationService notificationService = new NotificationService();

    @Override
    public List<Object[]> getPendingPayments() {
        try {
            System.out.println("DEBUG: Fetching pending payments...");
            List<Object[]> payments = paymentDao.getPendingPayments();
            System.out.println("DEBUG: Found " + payments.size() + " pending payments");
            return payments;
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get pending payments: " + e.getMessage());
            return List.of();
        }
    }

    @Override
    public boolean verifyPayment(int paymentId, int adminId) {
        try {
            // Rule giữ nguyên
            if (adminId != 1) {
                System.err.println("=== PAYMENT VERIFICATION FAILED ===");
                System.err.println("ERROR: Invalid admin ID. Only admin_id = 1 is allowed.");
                return false;
            }

            System.out.println("=== PAYMENT VERIFICATION START ===");
            System.out.println("DEBUG: Payment ID: " + paymentId + ", Admin ID: " + adminId);

            // 1) Lấy order_id từ payment
            int orderId = paymentDao.getOrderIdByPayment(paymentId);
            if (orderId == 0) throw new SQLException("Không tìm thấy order_id cho payment: " + paymentId);
            System.out.println("DEBUG: Order ID: " + orderId);

            // 2) Cập nhật payment → 'paid' (trigger DB sẽ set RentalOrders.status = 'confirmed')
            boolean paymentUpdated = paymentDao.updatePaymentStatus(paymentId, "paid", adminId);
            if (!paymentUpdated) throw new SQLException("Không thể cập nhật payment status - payment không tồn tại hoặc đã được xử lý");
            System.out.println("DEBUG: Payment updated successfully");

            // 3) Ghi lịch sử (timeline)
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("confirmed");
            history.setAdminId(adminId);
            history.setNotes("Payment verified manually by admin");
            orderDao.addStatusHistory(history);
            System.out.println("DEBUG: History added successfully");

            // 4) GỬI THÔNG BÁO CHO PARTNER (chỉ Partner)
            try {
                int sent = notificationService.sendToPartnersByOrder(
                        orderId,
                        "Đơn #" + orderId + " đã được xác nhận",
                        "Khách đã thanh toán. Đơn có chứa xe của bạn đã được xác nhận."
                );
                System.out.println("DEBUG: Notification to partners created: " + sent);
            } catch (Exception ex) {
                System.err.println("WARN: notify partners failed: " + ex.getMessage());
            }

            System.out.println("=== PAYMENT VERIFICATION SUCCESS ===");
            return true;

        } catch (SQLException e) {
            System.err.println("=== PAYMENT VERIFICATION FAILED ===");
            System.err.println("ERROR: " + e.getMessage());
            return false;
        } finally {
            System.out.println("=== PAYMENT VERIFICATION END ===");
        }
    }
}
