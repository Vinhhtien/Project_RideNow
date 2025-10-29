package service;

import dao.AdminOrderDAO;
import dao.IAdminOrderDAO;
import model.OrderSummary;
import model.OrderDetailItem;
import model.PaymentInfo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class AdminOrderService implements IAdminOrderService {
    private final IAdminOrderDAO dao = new AdminOrderDAO();

    @Override
    public List<OrderSummary> findOrders(String status, String kw, Date from, Date to, int page, int pageSize) {
        try { return dao.findOrders(status, kw, from, to, page, pageSize); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public int countOrders(String status, String kw, Date from, Date to) {
        try { return dao.countOrders(status, kw, from, to); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<OrderSummary> findOrderHeader(int orderId) {
        try { return dao.findOrderHeader(orderId); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public List<OrderDetailItem> findOrderItems(int orderId) {
        try { return dao.findOrderItems(orderId); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    @Override
    public List<PaymentInfo> findPayments(int orderId) {
        try { return dao.findPayments(orderId); }
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
