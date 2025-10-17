package service;

import dao.AdminCustomerDAO;
import dao.IAdminCustomerDAO;
import model.AdminCustomerDTO;

import java.math.BigDecimal;
import java.util.List;

public class AdminCustomerService implements IAdminCustomerService {
    private final IAdminCustomerDAO dao = new AdminCustomerDAO();

    @Override
    public List<AdminCustomerDTO> searchCustomers(String q, String status, String walletFilter, String sort, String dir, int page, int pageSize) {
        return dao.searchCustomers(q, status, walletFilter, sort, dir, page, pageSize);
    }

    @Override
    public int countCustomers(String q, String status, String walletFilter) {
        return dao.countCustomers(q, status, walletFilter);
    }

    @Override
    public AdminCustomerDTO getCustomerDetail(int customerId) {
        return dao.getCustomerDetail(customerId);
    }

    @Override
    public void toggleCustomerStatus(int customerId) {
        dao.toggleCustomerStatus(customerId);
    }

    @Override
    public BigDecimal getTotalWalletBalance() {
        return dao.getTotalWalletBalance();
    }
}