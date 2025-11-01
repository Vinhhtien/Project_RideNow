package service;

import dao.IAdminDAO;
import dao.AdminDAO;

import java.util.*;

import model.OrderListItem;

public class AdminService implements IAdminService {
    private final IAdminDAO adminDAO = new AdminDAO();

    @Override
    public Map<String, Object> getKpiCards() {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalCustomers", adminDAO.countCustomers());
        m.put("totalPartners", adminDAO.countPartners());
        m.put("totalBikes", adminDAO.countBikes());
        m.put("totalOrders", adminDAO.countOrders());
        m.put("revenueToday", adminDAO.sumRevenueToday());
        m.put("revenueThisMonth", adminDAO.sumRevenueThisMonth());
        return m;
    }

    @Override
    public List<OrderListItem> getLatestOrders(int limit) {
        return adminDAO.findLatestOrders(limit);
    }

    @Override
    public List<String[]> getMaintenanceBikes(int limit) {
        return adminDAO.findBikesMaintenance(limit);
    }
}
