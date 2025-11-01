package service;

import dao.IPaymentVerifyDao;
import dao.IOrderManageDao;
import dao.INotificationDao;
import dao.PaymentVerifyDao;
import dao.OrderManageDao;
import dao.NotificationDao;
import model.OrderStatusHistory;
import utils.DBConnection;
import utils.EmailUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.util.List;

public class PaymentVerifyService implements IPaymentVerifyService {
    private final IPaymentVerifyDao paymentDao = new PaymentVerifyDao();
    private final IOrderManageDao orderDao = new OrderManageDao();
    private final INotificationDao notificationDao = new NotificationDao();
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
            e.printStackTrace();
            return List.of();
        }
    }

    @Override
    public boolean verifyPayment(int paymentId, int adminId) {
        Connection con = null;
        try {
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

            // 4. Gửi thông báo cho customer (từ service cũ)
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

            // 5. Gửi thông báo cho partners (từ service mới) - SAU KHI COMMIT
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

            // 6. Gửi email xác nhận (từ service cũ) - SAU KHI COMMIT
            try {
                sendPaymentConfirmationEmail(paymentId, "https://your-domain.com");
            } catch (Exception ex) {
                System.err.println("WARN: Email sending failed: " + ex.getMessage());
            }

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

    @Override
    public void sendPaymentConfirmationEmail(int paymentId, String baseUrl) {
        try {
            System.out.println("=== START SENDING PAYMENT CONFIRMATION EMAIL ===");
            System.out.println("Payment ID: " + paymentId);

            PaymentMailDTO mailInfo = getPaymentMailInfo(paymentId);
            if (mailInfo == null) {
                System.out.println("❌ No mail info found for payment: " + paymentId);
                return;
            }

            System.out.println("📧 Customer Email: " + mailInfo.customerEmail);
            System.out.println("👤 Customer Name: " + mailInfo.customerName);
            System.out.println("💰 Amount: " + mailInfo.amount);

            if (mailInfo.customerEmail == null || mailInfo.customerEmail.trim().isEmpty()) {
                System.out.println("❌ Customer email is empty");
                return;
            }

            // GỬI EMAIL
            sendVerificationEmail(mailInfo, baseUrl);
            System.out.println("✅ Email sent successfully to: " + mailInfo.customerEmail);

        } catch (Exception e) {
            System.err.println("❌ Email sending failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private PaymentMailDTO getPaymentMailInfo(int paymentId) throws SQLException {
        String sql = "SELECT p.order_id, c.full_name, c.email, p.amount, p.method, " +
                "r.total_price, r.start_date, r.end_date, p.payment_date " +
                "FROM Payments p " +
                "JOIN RentalOrders r ON r.order_id = p.order_id " +
                "JOIN Customers c ON c.customer_id = r.customer_id " +
                "WHERE p.payment_id = ?";

        System.out.println("🔍 Executing SQL for payment: " + paymentId);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("✅ Found customer: " + rs.getString("email"));

                    PaymentMailDTO dto = new PaymentMailDTO();
                    dto.orderId = rs.getInt("order_id");
                    dto.customerName = rs.getString("full_name");
                    dto.customerEmail = rs.getString("email");
                    dto.amount = rs.getBigDecimal("amount");
                    dto.method = rs.getString("method");
                    dto.orderTotal = rs.getBigDecimal("total_price");
                    dto.startDate = rs.getDate("start_date").toLocalDate();
                    dto.endDate = rs.getDate("end_date").toLocalDate();
                    dto.paymentDate = rs.getTimestamp("payment_date") != null ?
                            rs.getTimestamp("payment_date").toLocalDateTime() : null;
                    return dto;
                } else {
                    System.out.println("❌ No data found for payment: " + paymentId);
                }
            }
        }
        return null;
    }

    private void sendVerificationEmail(PaymentMailDTO dto, String baseUrl) throws Exception {
        String subject = "RideNow – Đơn #" + dto.orderId + " đã được xác nhận";
        String link = baseUrl + "/myorders?orderId=" + dto.orderId;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

        String htmlContent = """
                    <html>
                    <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px;">
                            <div style="text-align: center; margin-bottom: 20px;">
                                <h1 style="color: #2563eb; margin: 0;">RideNow</h1>
                                <p style="color: #6b7280; margin: 5px 0 0 0;">Xác nhận thanh toán thành công</p>
                            </div>
                
                            <p>Chào <strong>%s</strong>,</p>
                            <p>Đơn thuê xe <strong>#%d</strong> của bạn đã được xác nhận thanh toán thành công.</p>
                
                            <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd; background-color: #f9fafb;"><strong>Khoảng thuê</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">%s → %s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd; background-color: #f9fafb;"><strong>Số tiền đã thanh toán</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">%s (%s)</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd; background-color: #f9fafb;"><strong>Tổng giá trị đơn</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                </tr>
                                <tr>
                                    <td style="padding: 10px; border: 1px solid #ddd; background-color: #f9fafb;"><strong>Thời điểm thanh toán</strong></td>
                                    <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                                </tr>
                            </table>
                
                            <div style="text-align: center; margin: 30px 0;">
                                <a href="%s" style="background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; display: inline-block;">
                                    Xem chi tiết đơn hàng
                                </a>
                            </div>
                
                            <div style="margin-top: 30px; padding-top: 20px; border-top: 1px solid #ddd;">
                                <p style="margin: 0; color: #6b7280;">
                                    Trân trọng,<br>
                                    <strong>Đội ngũ RideNow</strong>
                                </p>
                            </div>
                        </div>
                    </body>
                    </html>
                """.formatted(
                dto.customerName,
                dto.orderId,
                dto.startDate.format(dateFormatter),
                dto.endDate.format(dateFormatter),
                dto.amount, dto.method,
                dto.orderTotal,
                dto.paymentDate != null ? dto.paymentDate.format(dateTimeFormatter) : "N/A",
                link
        );

        System.out.println("📧 Sending email to: " + dto.customerEmail);
        EmailUtil.sendMailHTML(dto.customerEmail, subject, htmlContent);
    }

    private static class PaymentMailDTO {
        int orderId;
        String customerName;
        String customerEmail;
        BigDecimal amount;
        String method;
        BigDecimal orderTotal;
        LocalDate startDate;
        LocalDate endDate;
        LocalDateTime paymentDate;
    }
}