// src/main/java/dao/OrderDao.java
package dao;

import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.OrderStatusHistory;

public class OrderDao implements IOrderDao {

    @Override
    public BigDecimal getBikePriceIfBookable(int bikeId) throws Exception {
        // Chỉ chặn khi 'maintenance'; 'rented' vẫn cho tạo đơn pending cho tương lai (tùy chính sách)
        final String sql = """
            SELECT price_per_day, status
            FROM Motorbikes WITH (READCOMMITTEDLOCK)
            WHERE bike_id = ?
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                String status = rs.getString("status"); // available | rented | maintenance
                if ("maintenance".equalsIgnoreCase(status)) return null;
                return rs.getBigDecimal("price_per_day");
            }
        }
    }

    @Override
    public boolean isOverlappingLocked(int bikeId, Date start, Date end) throws Exception {
        // Dò đơn (pending/confirmed) chồng chéo: NOT (end < start_in_DB OR start > end_in_DB)
        // Hint READCOMMITTEDLOCK đủ dùng; nếu cần cực chặt có thể dùng (UPDLOCK, HOLDLOCK) trong cùng transaction tạo đơn.
        final String sql = """
            SELECT COUNT(1)
            FROM RentalOrders r WITH (READCOMMITTEDLOCK)
            JOIN OrderDetails d WITH (READCOMMITTEDLOCK) ON d.order_id = r.order_id
            WHERE d.bike_id = ?
              AND r.status IN ('pending','confirmed')
              AND NOT (r.end_date < ? OR r.start_date > ?)
        """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // ===== helpers =====
    private int getBikeTypeId(Connection con, int bikeId) throws Exception {
        final String sql = "SELECT type_id FROM Motorbikes WHERE bike_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Không tìm thấy type_id của bike_id=" + bikeId);
    }

    private BigDecimal calcDepositByType(int typeId) {
        // Theo seed DB: type_id=3 là PKL
        return (typeId == 3) ? BigDecimal.valueOf(1_000_000) : BigDecimal.valueOf(500_000);
    }

    @Override
    public int createPendingOrder(int customerId, int bikeId,
                                  Date start, Date end,
                                  BigDecimal pricePerDay) throws Exception {
        long days = java.time.temporal.ChronoUnit.DAYS
                .between(start.toLocalDate(), end.toLocalDate()) + 1;
        if (days <= 0) throw new IllegalArgumentException("Khoảng ngày không hợp lệ");

        BigDecimal lineTotal = pricePerDay.multiply(BigDecimal.valueOf(days));
        BigDecimal orderTotal = lineTotal;

        final String insOrder = """
            INSERT INTO RentalOrders(customer_id, start_date, end_date, total_price, status, created_at, deposit_amount, deposit_status)
            OUTPUT INSERTED.order_id
            VALUES (?, ?, ?, ?, 'pending', GETDATE(), ?, 'none')
        """;
        final String insDetail = """
            INSERT INTO OrderDetails(order_id, bike_id, price_per_day, quantity, line_total)
            VALUES (?, ?, ?, 1, ?)
        """;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Tính cọc theo type_id của xe
                int typeId = getBikeTypeId(con, bikeId);
                BigDecimal deposit = calcDepositByType(typeId);

                // (Tùy chọn) Kiểm tra chồng chéo lần cuối bằng hint mạnh hơn trong cùng transaction
                // Có thể dùng truy vấn với WITH (UPDLOCK, HOLDLOCK) nếu muốn.
                if (isOverlappingLocked(bikeId, start, end)) {
                    throw new IllegalStateException("Xe đã có lịch trong khoảng ngày này");
                }

                // Tạo order
                int orderId;
                try (PreparedStatement psOrder = con.prepareStatement(insOrder)) {
                    psOrder.setInt(1, customerId);
                    psOrder.setDate(2, start);
                    psOrder.setDate(3, end);
                    psOrder.setBigDecimal(4, orderTotal);
                    psOrder.setBigDecimal(5, deposit);
                    try (ResultSet rs = psOrder.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Không lấy được order_id");
                        orderId = rs.getInt(1);
                    }
                }

                // Dòng chi tiết
                try (PreparedStatement psDetail = con.prepareStatement(insDetail)) {
                    psDetail.setInt(1, orderId);
                    psDetail.setInt(2, bikeId);
                    psDetail.setBigDecimal(3, pricePerDay);
                    psDetail.setBigDecimal(4, lineTotal);
                    psDetail.executeUpdate();
                }

                con.commit();
                return orderId;
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }
    
    
    //7/10/2025
    // Cập nhật trạng thái đơn hàng
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE RentalOrders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }
    
    // Đánh dấu đã giao xe
    public boolean markOrderPickedUp(int orderId, int adminId) throws SQLException {
        String sql = "UPDATE RentalOrders SET pickup_status = 'picked_up', picked_up_at = GETDATE(), admin_pickup_id = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }
    
    // Đánh dấu đã trả xe
    public boolean markOrderReturned(int orderId, int adminId) throws SQLException {
        String sql = "UPDATE RentalOrders SET pickup_status = 'returned', returned_at = GETDATE(), admin_return_id = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }
    
    // Ghi lịch sử trạng thái
    public void addStatusHistory(OrderStatusHistory history) throws SQLException {
        String sql = "INSERT INTO OrderStatusHistory (order_id, status, admin_id, notes) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, history.getOrderId());
            ps.setString(2, history.getStatus());
            ps.setObject(3, history.getAdminId());
            ps.setString(4, history.getNotes());
            ps.executeUpdate();
        }
    }
    
    // Lấy đơn hàng chờ giao xe
    public List<Object[]> getOrdersForPickup() throws SQLException {
        List<Object[]> orders = new ArrayList<>();
        String sql = """
            SELECT r.order_id, c.full_name, c.phone, c.email,
                   STUFF((SELECT ', ' + b2.bike_name 
                          FROM OrderDetails d2 
                          JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id 
                          WHERE d2.order_id = r.order_id 
                          FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,'') AS bikes,
                   r.start_date, r.end_date, r.total_price, r.deposit_amount,
                   r.created_at, r.pickup_status
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            WHERE r.status = 'confirmed' AND r.pickup_status = 'not_picked_up'
            ORDER BY r.created_at DESC
            """;
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Object[] order = new Object[10];
                order[0] = rs.getInt("order_id");
                order[1] = rs.getString("full_name");
                order[2] = rs.getString("phone");
                order[3] = rs.getString("email");
                order[4] = rs.getString("bikes");
                order[5] = rs.getDate("start_date");
                order[6] = rs.getDate("end_date");
                order[7] = rs.getBigDecimal("total_price");
                order[8] = rs.getBigDecimal("deposit_amount");
                order[9] = rs.getString("pickup_status");
                orders.add(order);
            }
        }
        return orders;
    }
}
