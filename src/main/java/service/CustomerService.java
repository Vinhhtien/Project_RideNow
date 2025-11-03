// CustomerService.java
package service;

import dao.CustomerDao;
import dao.ICustomerDao;
import model.Customer;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import model.OrderVm;

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

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

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
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<model.Customer> getAll() throws Exception {
        return dao.findAll();
    }

    @Override
    public Customer getCustomerById(int customerId) throws Exception {
        return dao.findCustomerById(customerId); 
    }
    
     @Override
    public List<OrderVm> getOrdersByCustomer(int accountId) throws Exception {
        List<OrderVm> orders = new ArrayList<>();
        
        String sql = """
            SELECT DISTINCT
                r.order_id, r.status, r.start_date, r.end_date, 
                r.total_price, r.confirmed_at,
                m.bike_name, m.bike_id,
                p.method as payment_method
            FROM RentalOrders r
            JOIN Customers c ON r.customer_id = c.customer_id
            JOIN Accounts a ON c.account_id = a.account_id
            JOIN OrderDetails od ON r.order_id = od.order_id
            JOIN Motorbikes m ON od.bike_id = m.bike_id
            LEFT JOIN Payments p ON r.order_id = p.order_id
            WHERE a.account_id = ?
            ORDER BY r.order_id DESC
            """;

        try (Connection con = DBConnection.getConnection(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, accountId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderVm order = new OrderVm();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setStatus(rs.getString("status"));
                    order.setStart(rs.getDate("start_date"));
                    order.setEnd(rs.getDate("end_date"));
                    order.setTotal(rs.getBigDecimal("total_price"));
                    order.setConfirmedAt(rs.getTimestamp("confirmed_at"));
                    order.setBikeName(rs.getString("bike_name"));
                    order.setBikeId(rs.getInt("bike_id"));
                    order.setPaymentMethod(rs.getString("payment_method"));
                    
                    // TÍNH TOÁN changeRemainingMin cho orders confirmed
                    if ("confirmed".equals(order.getStatus())) {
                        order.setChangeRemainingMin(calculateRemainingMinutes(order.getConfirmedAt()));
                    }
                    
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    // PHƯƠNG THỨC TÍNH THỜI GIAN CÒN LẠI
    private Integer calculateRemainingMinutes(Timestamp confirmedAt) {
        if (confirmedAt == null) return 0;
        
        long confirmedTime = confirmedAt.getTime();
        long currentTime = System.currentTimeMillis();
        long elapsedMinutes = (currentTime - confirmedTime) / (60 * 1000);
        long remainingMinutes = 30 - elapsedMinutes;
        
        return remainingMinutes > 0 ? (int) remainingMinutes : 0;
    }
    
    
}
