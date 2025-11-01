package service;

import java.util.List;
import java.util.Map;

import model.OrderListItem;

public interface IAdminService {
    Map<String, Object> getKpiCards();

    List<OrderListItem> getLatestOrders(int limit);

    List<String[]> getMaintenanceBikes(int limit);
}
