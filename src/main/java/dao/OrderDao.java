package dao;

import utils.DBConnection;
import model.RentalOrder;
import model.OrderDetail;
import model.OrderStatusHistory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao implements IOrderDao {

    @Override
    public RentalOrder getOrderById(int orderId) throws SQLException {
        String sql = "SELECT * FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    RentalOrder order = new RentalOrder();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStartDate(rs.getDate("start_date"));
                    order.setEndDate(rs.getDate("end_date"));
                    order.setTotalPrice(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    return order;
                }
            }
        }
        return null;
    }

    @Override
    public List<OrderDetail> getOrderDetailsByOrderId(int orderId) throws SQLException {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM OrderDetails WHERE order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = new OrderDetail();
                    detail.setDetailId(rs.getInt("order_detail_id"));
                    detail.setOrderId(rs.getInt("order_id"));
                    detail.setBikeId(rs.getInt("bike_id"));
                    detail.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setLineTotal(rs.getBigDecimal("line_total"));
                    details.add(detail);
                }
            }
        }
        return details;
    }

    @Override
    public List<RentalOrder> getOrdersByCustomerId(int customerId) throws SQLException {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM RentalOrders WHERE customer_id = ? ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentalOrder order = new RentalOrder();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStartDate(rs.getDate("start_date"));
                    order.setEndDate(rs.getDate("end_date"));
                    order.setTotalPrice(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public List<RentalOrder> getAllOrders() throws SQLException {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM RentalOrders ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                RentalOrder order = new RentalOrder();
                order.setOrderId(rs.getInt("order_id"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStartDate(rs.getDate("start_date"));
                order.setEndDate(rs.getDate("end_date"));
                order.setTotalPrice(rs.getBigDecimal("total_price"));
                order.setStatus(rs.getString("status"));
                orders.add(order);
            }
        }
        return orders;
    }

    @Override
    public List<RentalOrder> searchOrders(String keyword) throws SQLException {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT ro.* FROM RentalOrders ro " +
                "JOIN Customers c ON ro.customer_id = c.customer_id " +
                "WHERE c.full_name LIKE ? OR ro.order_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try {
                ps.setInt(2, Integer.parseInt(keyword));
            } catch (NumberFormatException e) {
                ps.setInt(2, -1);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentalOrder order = new RentalOrder();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStartDate(rs.getDate("start_date"));
                    order.setEndDate(rs.getDate("end_date"));
                    order.setTotalPrice(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public List<RentalOrder> filterByStatus(String status) throws SQLException {
        List<RentalOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM RentalOrders WHERE status = ? ORDER BY created_at DESC";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RentalOrder order = new RentalOrder();
                    order.setOrderId(rs.getInt("order_id"));
                    order.setCustomerId(rs.getInt("customer_id"));
                    order.setStartDate(rs.getDate("start_date"));
                    order.setEndDate(rs.getDate("end_date"));
                    order.setTotalPrice(rs.getBigDecimal("total_price"));
                    order.setStatus(rs.getString("status"));
                    orders.add(order);
                }
            }
        }
        return orders;
    }

    @Override
    public Object[] getOrderStatsByCustomerId(int customerId) throws SQLException {
        String sql = "SELECT " +
                "COUNT(*) as total_orders, " +
                "SUM(CASE WHEN status = 'pending' THEN 1 ELSE 0 END) as pending_orders, " +
                "SUM(CASE WHEN status = 'confirmed' THEN 1 ELSE 0 END) as confirmed_orders, " +
                "SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_orders, " +
                "SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_orders " +
                "FROM RentalOrders WHERE customer_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Object[]{
                            rs.getInt("total_orders"),
                            rs.getInt("pending_orders"),
                            rs.getInt("confirmed_orders"),
                            rs.getInt("completed_orders"),
                            rs.getInt("cancelled_orders")
                    };
                }
            }
        }
        return new Object[]{0, 0, 0, 0, 0};
    }

    // Keep your existing bike booking methods - they should work fine
    @Override
    public BigDecimal getBikePriceIfBookable(int bikeId) throws Exception {
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
                String status = rs.getString("status");
                if ("maintenance".equalsIgnoreCase(status)) return null;
                return rs.getBigDecimal("price_per_day");
            }
        }
    }

    @Override
    public boolean isOverlappingLocked(int bikeId, Date start, Date end) throws Exception {
        final String sql = """
                SELECT COUNT(1)
                FROM RentalOrders r 
                JOIN OrderDetails d ON d.order_id = r.order_id
                WHERE d.bike_id = ?
                  AND r.status = 'confirmed'
                  AND r.pickup_status = 'picked_up'
                  AND r.return_status IN ('not_returned', 'none')
                  AND NOT (r.end_date < ? OR r.start_date > ?)
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            ps.setDate(2, start);
            ps.setDate(3, end);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                int count = rs.getInt(1);
                return count > 0;
            }
        }
    }

    @Override
    public int createPendingOrder(int customerId, int bikeId, Date start, Date end, BigDecimal pricePerDay) throws Exception {
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
                // Calculate deposit based on bike type
                int typeId = getBikeTypeId(con, bikeId);
                BigDecimal deposit = calcDepositByType(typeId);

                // Final overlap check
                if (isOverlappingLocked(bikeId, start, end)) {
                    throw new IllegalStateException("Xe đã có lịch trong khoảng ngày này");
                }

                // Create order
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

                // Create order detail
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
        return (typeId == 3) ? BigDecimal.valueOf(1_000_000) : BigDecimal.valueOf(500_000);
    }

    @Override
    public boolean updateOrderStatus(int orderId, String newStatus) throws SQLException {
        String sql = "UPDATE RentalOrders SET status = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markOrderPickedUp(int orderId, int adminId) throws SQLException {
        String sql = "UPDATE RentalOrders SET pickup_status = 'picked_up', picked_up_at = GETDATE(), admin_pickup_id = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean markOrderReturned(int orderId, int adminId) throws SQLException {
        String sql = "UPDATE RentalOrders SET return_status = 'returned', returned_at = GETDATE(), admin_return_id = ? WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, adminId);
            ps.setInt(2, orderId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public void addStatusHistory(OrderStatusHistory history) throws SQLException {
        String sql = "INSERT INTO OrderStatusHistory (order_id, status, admin_id, notes) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, history.getOrderId());
            ps.setString(2, history.getStatus());
            ps.setInt(3, history.getAdminId());
            ps.setString(4, history.getNotes());
            ps.executeUpdate();
        }
    }

    @Override
    public List<Object[]> getOrdersForPickup() throws SQLException {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT ro.order_id, c.full_name, b.bike_name, ro.start_date, ro.end_date " +
                "FROM RentalOrders ro " +
                "JOIN Customers c ON ro.customer_id = c.customer_id " +
                "JOIN OrderDetails od ON ro.order_id = od.order_id " +
                "JOIN Motorbikes b ON od.bike_id = b.bike_id " +
                "WHERE ro.status = 'confirmed' AND ro.pickup_status = 'not_picked_up'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("order_id"),
                        rs.getString("full_name"),
                        rs.getString("bike_name"),
                        rs.getDate("start_date"),
                        rs.getDate("end_date")
                };
                results.add(row);
            }
        }
        return results;
    }
    
    
    @Override
public RentalOrder findCurrentAdminBookingForBike(int bikeId) throws SQLException {
    // Lấy đơn THUÊ GẦN NHẤT cho xe này, không phân biệt là admin hay customer.
    String sql = """
            SELECT TOP 1 r.*
            FROM RentalOrders r
            JOIN OrderDetails d ON d.order_id = r.order_id
            WHERE d.bike_id = ?
              AND r.status IN ('pending','confirmed')   -- đơn còn hiệu lực
              AND r.end_date >= CAST(GETDATE() AS DATE) -- chưa kết thúc
            ORDER BY r.start_date ASC                   -- lấy đơn đang/ sắp thuê gần nhất
            """;

    System.out.println("[OrderDao] findCurrentAdminBookingForBike, bikeId=" + bikeId);

    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, bikeId);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                RentalOrder order = new RentalOrder();
                order.setOrderId(rs.getInt("order_id"));
                order.setCustomerId(rs.getInt("customer_id"));
                order.setStartDate(rs.getDate("start_date"));
                order.setEndDate(rs.getDate("end_date"));
                order.setTotalPrice(rs.getBigDecimal("total_price"));
                order.setStatus(rs.getString("status"));

                System.out.println("[OrderDao] FOUND booking: orderId="
                        + order.getOrderId() + ", start=" + order.getStartDate()
                        + ", end=" + order.getEndDate());
                return order;
            } else {
                System.out.println("[OrderDao] No booking for bike " + bikeId);
            }
        }
    }
    return null;
}



}