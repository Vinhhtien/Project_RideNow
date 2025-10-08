package service;

import dao.IPaymentDao;
import dao.PaymentDao;

import java.math.BigDecimal;

public class PaymentService implements IPaymentService {
    private final IPaymentDao dao = new PaymentDao();

    @Override
    public int createPendingForOrder(int orderId, BigDecimal amount, String reference) throws Exception {
        if (orderId <= 0) throw new IllegalArgumentException("orderId không hợp lệ");
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Số tiền không hợp lệ");
        return dao.insertPending(orderId, amount, reference);
    }

    @Override
    public boolean markPaid(int paymentId) throws Exception {
        if (paymentId <= 0) throw new IllegalArgumentException("paymentId không hợp lệ");
        return dao.markPaid(paymentId);
    }
}
