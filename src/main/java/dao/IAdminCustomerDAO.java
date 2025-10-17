package dao;

import model.AdminCustomerDTO;
import java.math.BigDecimal;
import java.util.List;

public interface IAdminCustomerDAO {
    List<AdminCustomerDTO> searchCustomers(String q, String status, String walletFilter, String sort, String dir, int page, int pageSize);
    int countCustomers(String q, String status, String walletFilter);
    AdminCustomerDTO getCustomerDetail(int customerId);
    void toggleCustomerStatus(int customerId);
    BigDecimal getTotalWalletBalance(); // Thêm phương thức mới
}