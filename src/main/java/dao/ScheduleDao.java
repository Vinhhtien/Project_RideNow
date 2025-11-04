package dao;

import model.ScheduleItem;
import utils.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScheduleDao implements IScheduleDao {

    private static final String SQL = """
        SELECT
            r.order_id,
            m.bike_id,
            m.bike_name,
            m.license_plate,
            CASE 
              WHEN m.store_id IS NOT NULL THEN 'store' 
              ELSE 'partner' 
            END AS owner_type,
            COALESCE(s.store_name, p.company_name) AS owner_name,
            r.start_date,
            r.end_date,
            r.status AS order_status
        FROM RentalOrders r
        JOIN OrderDetails d   ON d.order_id = r.order_id
        JOIN Motorbikes  m    ON m.bike_id = d.bike_id
        LEFT JOIN Stores   s  ON s.store_id = m.store_id
        LEFT JOIN Partners p  ON p.partner_id = m.partner_id
        WHERE
            -- thuộc admin:
            ( (m.store_id IS NOT NULL AND s.admin_id = ?)
              OR (m.partner_id IS NOT NULL AND p.admin_id = ?) )
            -- giao thoa khoảng ngày:
            AND r.end_date >= ? AND r.start_date <= ?
            -- chỉ lấy đơn có ý nghĩa với lịch:
            AND r.status IN ('pending','confirmed','completed')
        ORDER BY r.start_date ASC, r.order_id ASC
        """;

    @Override
    public List<ScheduleItem> findByAdminAndDateRange(int adminId, LocalDate from, LocalDate to) {
        List<ScheduleItem> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {

            ps.setInt(1, adminId);
            ps.setInt(2, adminId);
            ps.setDate(3, Date.valueOf(from));
            ps.setDate(4, Date.valueOf(to));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScheduleItem it = new ScheduleItem(
                            rs.getInt("order_id"),
                            rs.getInt("bike_id"),
                            rs.getString("bike_name"),
                            rs.getString("license_plate"),
                            rs.getString("owner_type"),
                            rs.getString("owner_name"),
                            rs.getDate("start_date").toLocalDate(),
                            rs.getDate("end_date").toLocalDate(),
                            rs.getString("order_status")
                    );
                    list.add(it);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load schedule", e);
        }
        return list;
    }
}
