package dao;

import java.math.BigDecimal;
import java.util.List;
import model.OrderListItem;

public interface IAdminDAO {
    int countCustomers();
    int countPartners();
    int countBikes();
    int countOrders();
    BigDecimal sumRevenueToday();      // tổng tiền đã thanh toán hôm nay (Payments.status='paid')
    BigDecimal sumRevenueThisMonth();  // tổng tiền đã thanh toán trong tháng hiện tại
    List<OrderListItem> findLatestOrders(int limit); // đơn mới nhất
    List<String[]> findBikesMaintenance(int limit);  // gợi ý: xe đang maintenance (model, plate)
}
