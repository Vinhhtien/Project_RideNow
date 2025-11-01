// dao/IPaymentDao.java
package dao;

import java.math.BigDecimal;

public interface IPaymentDao {
    int createPendingBankTransfer(int orderId, BigDecimal amount, String reference) throws Exception;

    int insertPending(int orderId, BigDecimal amount, String reference) throws Exception;

    boolean markPaid(int paymentId) throws Exception;
}
