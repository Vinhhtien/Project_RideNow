package service;

import dao.IOrderManageDao;
import dao.OrderManageDao;
import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OrderManageService implements IOrderManageService {
    private final IOrderManageDao orderDao = new OrderManageDao();
    
    @Override
    public List<Object[]> getOrdersForPickup() {
        try {
            return orderDao.getOrdersForPickup();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public List<Object[]> getActiveOrders() {
        try {
            return orderDao.getActiveOrders();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    
    @Override
    public boolean confirmOrderPickup(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            
            // 1. Mark order as picked up
            boolean success = orderDao.markOrderPickedUp(orderId, adminId);
            if (!success) {
                throw new SQLException("Failed to mark order as picked up");
            }
            
            // 2. Update bike status to rented
            orderDao.updateBikeStatus(orderId, "rented");
            
            // 3. Add status history
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("picked_up");
            history.setAdminId(adminId);
            history.setNotes("Customer picked up the bike");
            orderDao.addStatusHistory(history);
            
            con.commit();
            return true;
            
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                } catch (SQLException e) {}
            }
        }
    }
    
    @Override
    public boolean confirmOrderReturn(int orderId, int adminId) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            
            // 1. Mark order as returned - SỬA: dùng phương thức có Connection
            boolean success = orderDao.markOrderReturned(con, orderId, adminId);
            if (!success) {
                throw new SQLException("Failed to mark order as returned");
            }
            
            // 2. Update bike status to available
            orderDao.updateBikeStatus(orderId, "available");
            
            // 3. Add status history - SỬA: dùng phương thức có Connection
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("returned");
            history.setAdminId(adminId);
            history.setNotes("Customer returned the bike - waiting for inspection");
            orderDao.addStatusHistory(con, history);
            
            con.commit();
            return true;
            
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { 
                    con.setAutoCommit(true); 
                    con.close(); 
                } catch (SQLException e) {}
            }
        }
    }
}
