
package dao;

import model.OrderDetail;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailDao implements IOrderDetailDao {
    private Connection connection;

    public OrderDetailDao(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<OrderDetail> findAll() throws Exception {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT detail_id, order_id, bike_id, price_per_day, quantity, line_total FROM OrderDetails";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                details.add(mapResultSetToOrderDetail(rs));
            }
        }

        return details;
    }

    // Helper method to map ResultSet to OrderDetail
    private OrderDetail mapResultSetToOrderDetail(ResultSet rs) throws SQLException {
        OrderDetail detail = new OrderDetail();
        detail.setDetailId(rs.getInt("detail_id"));
        detail.setOrderId(rs.getInt("order_id"));
        detail.setBikeId(rs.getInt("bike_id"));
        detail.setPricePerDay(rs.getBigDecimal("price_per_day"));
        detail.setQuantity(rs.getInt("quantity"));
        detail.setLineTotal(rs.getBigDecimal("line_total"));
        return detail;
    }
}