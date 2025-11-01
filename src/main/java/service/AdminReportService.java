package service;

import dao.AdminReportDao;
import dao.IAdminReportDao;
import model.report.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class AdminReportService implements IAdminReportService {
    private final IAdminReportDao dao = new AdminReportDao();

    private static Date atStart(LocalDate d){ return d == null ? null : Timestamp.valueOf(d.atStartOfDay()); }

    @Override public AdminReportSummary getSummary(LocalDate f, LocalDate t) throws Exception { 
        return dao.getSummary(atStart(f), atStart(t)); 
    }

    @Override public List<AdminPaymentMethodStat> getMethodStats(LocalDate f, LocalDate t) throws Exception { 
        return dao.getMethodStats(atStart(f), atStart(t)); 
    }

    @Override public List<AdminDailyRevenuePoint> getDailyRevenue(LocalDate f, LocalDate t) throws Exception { 
        return dao.getDailyRevenue(atStart(f), atStart(t)); 
    }

    @Override public List<AdminRevenueItem> getPayments(LocalDate f, LocalDate t, int page, int size) throws Exception {
        int off = Math.max(0, (page - 1) * size);
        return dao.getPayments(atStart(f), atStart(t), off, size);
    }
    @Override public int getPaymentsCount(LocalDate f, LocalDate t) throws Exception { 
        return dao.countPayments(atStart(f), atStart(t)); 
    }

    @Override public List<AdminRefundItem> getRefunds(LocalDate f, LocalDate t, int page, int size) throws Exception {
        int off = Math.max(0, (page - 1) * size);
        return dao.getRefunds(atStart(f), atStart(t), off, size);
    }
    @Override public int getRefundsCount(LocalDate f, LocalDate t) throws Exception { 
        return dao.countRefunds(atStart(f), atStart(t)); 
    }

    @Override public List<AdminTopCustomerStat> getTopCustomers(LocalDate f, LocalDate t, int limit) throws Exception {
        return dao.getTopCustomers(atStart(f), atStart(t), limit);
    }

    @Override public List<AdminOutstandingOrderItem> getOutstanding(LocalDate f, LocalDate t, int page, int size) throws Exception {
        int off = Math.max(0, (page - 1) * size);
        return dao.getOutstanding(atStart(f), atStart(t), off, size);
    }
    @Override public int getOutstandingCount(LocalDate f, LocalDate t) throws Exception { 
        return dao.countOutstanding(atStart(f), atStart(t)); 
    }

    @Override public AdminRevenueItem getPaymentById(int paymentId) throws Exception { 
        return dao.getPaymentById(paymentId); 
    }
    @Override public AdminOrderDetail getOrderDetail(int orderId) throws Exception { 
        return dao.getOrderDetail(orderId); 
    }

    @Override
    public List<AdminRevenueItem> getStoreRevenueSummary(LocalDate f, LocalDate t, Integer partnerId) throws Exception {
        return dao.getStoreRevenueSummary(atStart(f), atStart(t), partnerId);
    }

    // ===== NEW: doanh thu ròng theo ngày và drill-down theo đơn =====
    @Override
    public List<AdminRevenueItem> getDailyRevenueNet(LocalDate f, LocalDate t) throws Exception {
        return dao.findDailyRevenue(atStart(f), atStart(t));
    }

    @Override
    public List<AdminOrderDetail> getRevenueOrdersNet(LocalDate f, LocalDate t) throws Exception {
        return dao.findRevenueOrders(atStart(f), atStart(t));
    }
}
