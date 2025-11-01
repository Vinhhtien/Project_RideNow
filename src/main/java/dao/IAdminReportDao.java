package dao;

import java.util.Date;
import java.util.List;
import model.report.*;

public interface IAdminReportDao {
    AdminReportSummary getSummary(Date from, Date to) throws Exception;
    List<AdminPaymentMethodStat> getMethodStats(Date from, Date to) throws Exception;
    List<AdminDailyRevenuePoint> getDailyRevenue(Date from, Date to) throws Exception;
    List<AdminRevenueItem> getPayments(Date from, Date to, int offset, int pageSize) throws Exception;
    int countPayments(Date from, Date to) throws Exception;
    List<AdminRefundItem> getRefunds(Date from, Date to, int offset, int pageSize) throws Exception;
    int countRefunds(Date from, Date to) throws Exception;
    List<AdminTopCustomerStat> getTopCustomers(Date from, Date to, int limit) throws Exception;
    List<AdminOutstandingOrderItem> getOutstanding(Date from, Date to, int offset, int pageSize) throws Exception;
    int countOutstanding(Date from, Date to) throws Exception;
    AdminRevenueItem getPaymentById(int paymentId) throws Exception;
    AdminOrderDetail getOrderDetail(int orderId) throws Exception;
    List<AdminRevenueItem> getStoreRevenueSummary(Date from, Date to, Integer partnerId) throws Exception;

    // ===== Bổ sung cho báo cáo doanh thu ròng theo nghiệp vụ 30% + cọc + 70% - hoàn =====
    /** Tổng hợp theo ngày: Tổng thu, Hoàn, Doanh thu ròng */
    List<AdminRevenueItem> findDailyRevenue(Date from, Date to) throws Exception;

    /** Drill-down từng đơn: Phần trả trước, Tổng thu, Hoàn, Doanh thu ròng */
    List<AdminOrderDetail> findRevenueOrders(Date from, Date to) throws Exception;
}
