//package service;
//
//import dao.IOrderManageDao;
//import dao.OrderManageDao;
//import model.OrderStatusHistory;
//import utils.DBConnection;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.List;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.time.LocalDate;
//
//public class OrderManageService implements IOrderManageService {
//    private final IOrderManageDao orderDao = new OrderManageDao();
//    
//    @Override
//    public List<Object[]> getOrdersForPickup() {
//        try {
//            return orderDao.getOrdersForPickup();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return List.of();
//        }
//    }
//    
//    @Override
//    public List<Object[]> getActiveOrders() {
//        try {
//            return orderDao.getActiveOrders();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return List.of();
//        }
//    }
//    
//    @Override
//    public boolean confirmOrderPickup(int orderId, int adminId) {
//        Connection con = null;
//        try {
//            con = DBConnection.getConnection();
//            con.setAutoCommit(false);
//            
//            // 1. Mark order as picked up
//            boolean success = orderDao.markOrderPickedUp(orderId, adminId);
//            if (!success) {
//                throw new SQLException("Failed to mark order as picked up");
//            }
//            
//            // 2. Update bike status to rented
//            orderDao.updateBikeStatus(orderId, "rented");
//            
//            // 3. Add status history
//            OrderStatusHistory history = new OrderStatusHistory();
//            history.setOrderId(orderId);
//            history.setStatus("picked_up");
//            history.setAdminId(adminId);
//            history.setNotes("Customer picked up the bike");
//            orderDao.addStatusHistory(history);
//            
//            con.commit();
//            return true;
//            
//        } catch (SQLException e) {
//            if (con != null) {
//                try { con.rollback(); } catch (SQLException ex) {}
//            }
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (con != null) {
//                try { 
//                    con.setAutoCommit(true); 
//                    con.close(); 
//                } catch (SQLException e) {}
//            }
//        }
//    }
//    
//    @Override
//    public boolean confirmOrderReturn(int orderId, int adminId) {
//        Connection con = null;
//        try {
//            con = DBConnection.getConnection();
//            con.setAutoCommit(false);
//            
//            // 1. Mark order as returned - SỬA: dùng phương thức có Connection
//            boolean success = orderDao.markOrderReturned(con, orderId, adminId);
//            if (!success) {
//                throw new SQLException("Failed to mark order as returned");
//            }
//            
//            // 2. Update bike status to available
//            orderDao.updateBikeStatus(orderId, "available");
//            
//            // 3. Add status history - SỬA: dùng phương thức có Connection
//            OrderStatusHistory history = new OrderStatusHistory();
//            history.setOrderId(orderId);
//            history.setStatus("returned");
//            history.setAdminId(adminId);
//            history.setNotes("Customer returned the bike - waiting for inspection");
//            orderDao.addStatusHistory(con, history);
//            
//            con.commit();
//            return true;
//            
//        } catch (SQLException e) {
//            if (con != null) {
//                try { con.rollback(); } catch (SQLException ex) {}
//            }
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (con != null) {
//                try { 
//                    con.setAutoCommit(true); 
//                    con.close(); 
//                } catch (SQLException e) {}
//            }
//        }
//    }
//    
//    @Override
//    public boolean canPickupOrder(int orderId) {
//        String sql = "SELECT start_date FROM Orders WHERE order_id = ?";
//        try (Connection conn = DBConnection.getConnection();
//             PreparedStatement ps = conn.prepareStatement(sql)) {
//
//            ps.setInt(1, orderId);
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) {
//                LocalDate startDate = rs.getDate("start_date").toLocalDate();
//                LocalDate today = LocalDate.now();
//
//                // Chỉ cho phép nhận xe nếu hôm nay là ngày bắt đầu thuê hoặc sau đó
//                return !today.isBefore(startDate);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false;
//    }    
//}

package service;

import dao.IOrderManageDao;
import dao.OrderManageDao;
import model.OrderStatusHistory;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
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
    
    @Override
    public boolean canPickupOrder(int orderId) {
        String sql = "SELECT start_date FROM Orders WHERE order_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate today = LocalDate.now();
                
                return !today.isBefore(startDate);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    @Override
    public boolean markOrderAsNotGiven(int orderId, int adminId, String notes) {
        Connection con = null;
        try {
            con = DBConnection.getConnection();
            con.setAutoCommit(false);
            
            // Thêm ghi chú vào lịch sử trạng thái
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrderId(orderId);
            history.setStatus("pending_pickup"); // Giữ nguyên trạng thái chờ nhận
            history.setAdminId(adminId);
            history.setNotes("Xe chưa được giao cho khách. " + (notes != null ? notes : ""));
            orderDao.addStatusHistory(history);
            
            // Có thể thêm logic khác như gửi thông báo, email, etc.
            
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