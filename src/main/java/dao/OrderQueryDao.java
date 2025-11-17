package dao;

import utils.DBConnection;
import java.sql.*;
import java.util.*;

public class OrderQueryDao implements IOrderQueryDao {

    @Override
    public List<Object[]> findOrdersOfCustomer(int customerId) throws Exception {
        String sql = "SELECT r.order_id, "
                + "STRING_AGG(b.bike_name, ', ') WITHIN GROUP (ORDER BY d.detail_id) AS bikes, "
                + "r.start_date, r.end_date, r.total_price, r.status "
                + "FROM RentalOrders r "
                + "JOIN OrderDetails d ON d.order_id = r.order_id "
                + "JOIN Motorbikes b ON b.bike_id = d.bike_id "
                + "WHERE r.customer_id = ? "
                + "GROUP BY r.order_id, r.start_date, r.end_date, r.total_price, r.status "
                + "ORDER BY r.order_id DESC";

        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Object[]> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getDate(3),
                        rs.getDate(4),
                        rs.getBigDecimal(5),
                        rs.getString(6)
                    });
                }
                return list;
            }
        }
    }
    
    @Override
public List<Object[]> findOrdersOfCustomerWithPaymentStatus(int customerId) {
    // Thêm 3 cột mới:
    //  - r.confirmed_at                          AS confirmed_at
    //  - change_remaining_min (cửa sổ 30 phút)
    //  - r.allow_customer_cancel                 AS allow_customer_cancel
    final String sql =
        "SELECT " +
        "  r.order_id, " +
        "  b.bike_name, " +
        "  r.start_date, " +
        "  r.end_date, " +
        "  r.total_price, " +
        "  r.status, " +
        "  CASE WHEN EXISTS ( " +
        "         SELECT 1 FROM Payments p2 " +
        "         WHERE p2.order_id = r.order_id AND p2.status = 'pending' " +
        "       ) THEN 1 ELSE 0 END AS has_pending_payment, " +
        "  p.method AS payment_method, " +
        "  CASE WHEN p.payment_id IS NOT NULL THEN 1 ELSE 0 END AS payment_submitted, " +
        "  d.bike_id, " +
        "  r.confirmed_at, " +
        "  CASE " +
        "    WHEN r.status = 'confirmed' AND r.confirmed_at IS NOT NULL THEN " +
        "      CASE " +
        "        WHEN (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) < 0 THEN 0 " +
        "        ELSE (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) " +
        "      END " +
        "    ELSE NULL " +
        "  END AS change_remaining_min, " +
        "  r.allow_customer_cancel AS allow_customer_cancel " +   // 🔹 THÊM DÒNG NÀY
        "FROM RentalOrders r " +
        "JOIN OrderDetails d ON d.order_id = r.order_id " +
        "JOIN Motorbikes  b ON b.bike_id   = d.bike_id " +
        "OUTER APPLY ( " +
        "  SELECT TOP 1 payment_id, method " +
        "  FROM Payments p " +
        "  WHERE p.order_id = r.order_id AND p.status <> 'refunded' " +
        "  ORDER BY p.payment_date DESC, p.payment_id DESC " +
        ") p " +
        "WHERE r.customer_id = ? " +
        "ORDER BY r.order_id DESC";

    List<Object[]> rows = new ArrayList<>();
    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, customerId);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                // MẢNG 13 PHẦN TỬ: 0..12
                Object[] row = new Object[13];
                row[0]  = rs.getInt("order_id");
                row[1]  = rs.getString("bike_name");
                row[2]  = rs.getDate("start_date");
                row[3]  = rs.getDate("end_date");
                row[4]  = rs.getBigDecimal("total_price");
                row[5]  = rs.getString("status");
                row[6]  = rs.getInt("has_pending_payment") == 1;   // Boolean
                row[7]  = rs.getString("payment_method");           // String (có thể null)
                row[8]  = rs.getInt("payment_submitted") == 1;      // Boolean
                row[9]  = rs.getInt("bike_id");                     // int
                row[10] = rs.getTimestamp("confirmed_at");          // Timestamp (có thể null)

                // change_remaining_min có thể null → dùng getObject để giữ null
                Object remain = rs.getObject("change_remaining_min");
                row[11] = (remain == null ? null : ((Number) remain).intValue()); // Integer

                // 🔹 CỜ CHO PHÉP KHÁCH HỦY ĐƠN (ADMIN BẬT)
                row[12] = rs.getBoolean("allow_customer_cancel");   // Boolean

                rows.add(row);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return rows;
}


    
    
    

//    @Override
//    public List<Object[]> findOrdersOfCustomerWithPaymentStatus(int customerId) {
//        // Thêm các cột cần thiết (p.payment_method, p.payment_submitted, d.bike_id)
//        String sql = "SELECT "
//                + "r.order_id, "
//                + "b.bike_name, "
//                + "r.start_date, "
//                + "r.end_date, "
//                + "r.total_price, "
//                + // Dùng r.total_price thay cho d.line_total nếu order chỉ có 1 xe
//                "r.status, "
//                + "CASE WHEN EXISTS (SELECT 1 FROM Payments p2 WHERE p2.order_id = r.order_id AND p2.status = 'pending') THEN 1 ELSE 0 END AS has_pending_payment, "
//                + "p.method AS payment_method, "
//                + "CASE WHEN p.payment_id IS NOT NULL THEN 1 ELSE 0 END as payment_submitted, "
//                + // p.payment_submitted (hoặc kiểm tra p.payment_id)
//                "d.bike_id "
//                + // Cột này phải là index 9 (Cột thứ 10)
//                "FROM RentalOrders r "
//                + "JOIN OrderDetails d ON d.order_id = r.order_id "
//                + "JOIN Motorbikes b ON b.bike_id = d.bike_id "
//                + "OUTER APPLY ( SELECT TOP 1 payment_id, method FROM Payments p WHERE p.order_id = r.order_id AND p.status <> 'refunded' ORDER BY payment_date DESC, payment_id DESC ) p "
//                + // LEFT JOIN thay vì chỉ JOIN pending payment
//                "WHERE r.customer_id = ? "
//                + "ORDER BY r.order_id DESC";
//
//        // Khởi tạo mảng mới với kích thước 10
//        List<Object[]> rows = new ArrayList<>();
//        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, customerId);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) {
//                    // KÍCH THƯỚC MẢNG MỚI: [0]...[9] => 10 phần tử
//                    Object[] row = new Object[10];
//                    row[0] = rs.getInt("order_id");
//                    row[1] = rs.getString("bike_name");
//                    row[2] = rs.getDate("start_date");
//                    row[3] = rs.getDate("end_date");
//                    row[4] = rs.getBigDecimal("total_price"); // Lấy total_price
//                    row[5] = rs.getString("status");
//                    row[6] = rs.getInt("has_pending_payment") == 1; // hasPendingPayment (Boolean)
//                    row[7] = rs.getString("payment_method"); // paymentMethod (String)
//                    row[8] = rs.getInt("payment_submitted") == 1; // paymentSubmitted (Boolean)
//                    row[9] = rs.getInt("bike_id"); // ✅ bikeId - Index 9
//                    rows.add(row);
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return rows;
//    }
}
