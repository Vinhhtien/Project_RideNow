package service;

import model.OrderSummary;
import model.OrderDetailItem;
import model.PaymentInfo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IAdminOrderService {
    List<OrderSummary> findOrders(String status, String kw, Date from, Date to, int page, int pageSize);
    int countOrders(String status, String kw, Date from, Date to);

    Optional<OrderSummary> findOrderHeader(int orderId);
    List<OrderDetailItem> findOrderItems(int orderId);
    List<PaymentInfo> findPayments(int orderId);
}
