package service;

import model.report.*;
import java.time.LocalDate;
import java.util.List;

public interface IAdminReportService {
    AdminReportSummary getSummary(LocalDate from, LocalDate to) throws Exception;
    List<AdminPaymentMethodStat> getMethodStats(LocalDate from, LocalDate to) throws Exception;
    List<AdminDailyRevenuePoint> getDailyRevenue(LocalDate from, LocalDate to) throws Exception;

    List<AdminRevenueItem> getPayments(LocalDate from, LocalDate to, int page, int size) throws Exception;
    int getPaymentsCount(LocalDate from, LocalDate to) throws Exception;

    List<AdminRefundItem> getRefunds(LocalDate from, LocalDate to, int page, int size) throws Exception;
    int getRefundsCount(LocalDate from, LocalDate to) throws Exception;

    List<AdminTopCustomerStat> getTopCustomers(LocalDate from, LocalDate to, int limit) throws Exception;

    // Giữ để không vỡ compile dù UI đã ẩn
    List<AdminOutstandingOrderItem> getOutstanding(LocalDate from, LocalDate to, int page, int size) throws Exception;
    int getOutstandingCount(LocalDate from, LocalDate to) throws Exception;

    AdminRevenueItem getPaymentById(int paymentId) throws Exception;
    AdminOrderDetail getOrderDetail(int orderId) throws Exception;

    // NEW: báo cáo theo cửa hàng/đối tác
    List<AdminRevenueItem> getStoreRevenueSummary(LocalDate from, LocalDate to, Integer partnerId) throws Exception;

    // NEW: doanh thu ròng theo ngày (Payments + Ví) và drill-down theo đơn
    List<AdminRevenueItem> getDailyRevenueNet(LocalDate from, LocalDate to) throws Exception;
    List<AdminOrderDetail> getRevenueOrdersNet(LocalDate from, LocalDate to) throws Exception;
}
