// CustomerService.java
package service;

import dao.CustomerDao;
import dao.ICustomerDao;
import model.Customer;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CustomerService implements ICustomerService {
    private final ICustomerDao dao = new CustomerDao();

    @Override
    public Customer getProfile(int accountId) throws Exception {
        return dao.findByAccountId(accountId);
    }

    @Override
    public void saveProfile(Customer c) throws Exception {
        dao.upsertByAccountId(c);
    }
    
    @Override
    public boolean changePassword(int accountId, String currentPw, String newPw) throws Exception {
        return dao.updatePassword(accountId, currentPw, newPw);
    }
    
    @Override
    public boolean cancelOrder(int customerId, int orderId) throws Exception {
        String sql = """
            UPDATE RentalOrders 
            SET status = 'cancelled' 
            WHERE order_id = ? 
            AND customer_id = ? 
            AND status = 'pending'
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ps.setInt(2, customerId);
            
            int affectedRows = ps.executeUpdate();
            
            // Nếu hủy thành công, xóa payment pending nếu có
            if (affectedRows > 0) {
                deletePendingPayment(orderId);
                return true;
            }
            
            return false;
        } catch (SQLException e) {
            throw new Exception("Lỗi khi hủy đơn hàng", e);
        }
    }

    private void deletePendingPayment(int orderId) throws SQLException {
        String sql = "DELETE FROM Payments WHERE order_id = ? AND status = 'pending'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }
    
    @Override
    public List<model.Customer> getAll() throws Exception {
        return dao.findAll();
    }
}