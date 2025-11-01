package dao;

import java.sql.SQLException;
import java.util.List;

public interface IPaymentVerifyDao {
    List<Object[]> getPendingPayments() throws SQLException;

    boolean updatePaymentStatus(int paymentId, String status, Integer adminId) throws SQLException;

    int getOrderIdByPayment(int paymentId) throws SQLException;
}