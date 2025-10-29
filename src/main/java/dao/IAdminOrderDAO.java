package dao;

import model.OrderSummary;
import model.OrderDetailItem;
import model.PaymentInfo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IAdminOrderDAO {
    List<OrderSummary> findOrders(String status, String customerKeyword, Date from, Date to, int page, int pageSize) throws Exception;
    int countOrders(String status, String customerKeyword, Date from, Date to) throws Exception;

    Optional<OrderSummary> findOrderHeader(int orderId) throws Exception;
    List<OrderDetailItem> findOrderItems(int orderId) throws Exception;
    List<PaymentInfo> findPayments(int orderId) throws Exception;
}
