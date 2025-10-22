package service;
import java.util.List;

public interface IPaymentVerifyService {
    List<Object[]> getPendingPayments();
    boolean verifyPayment(int paymentId, int adminId);
    void sendPaymentConfirmationEmail(int paymentId, String baseUrl);
}