package service;

import java.math.BigDecimal;

public interface IPaymentService {
    int createPendingForOrder(int orderId, BigDecimal amount, String reference) throws Exception;

    /**
     * Admin xác nhận đã nhận tiền (đổi payment -> paid)
     */
    boolean markPaid(int paymentId) throws Exception;
}
