package dao;

import model.Motorbike;
import model.MotorbikeListItem;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MotorbikeDao implements IMotorbikeDao {

    @Override
    public List<Motorbike> findAll() throws Exception {
        String sql = "SELECT bike_id, partner_id, store_id, type_id, bike_name, license_plate, price_per_day, status, description FROM Motorbikes";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Motorbike> list = new ArrayList<>();
            while (rs.next()) list.add(mapMotorbike(rs));
            return list;
        }
    }

    @Override
    public List<Motorbike> findByTypeId(int typeId) throws Exception {
        String sql = "SELECT bike_id, partner_id, store_id, type_id, bike_name, license_plate, price_per_day, status, description FROM Motorbikes WHERE type_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, typeId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Motorbike> list = new ArrayList<>();
                while (rs.next()) list.add(mapMotorbike(rs));
                return list;
            }
        }
    }

    @Override
    public List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate,
                                          BigDecimal maxPrice, String keyword,
                                          String sort, int page, int size) throws Exception {

        StringBuilder sb = new StringBuilder();
        sb.append("""
                    SELECT b.bike_id, b.bike_name, b.license_plate, b.price_per_day, b.status, b.description,
                           t.type_name,
                           CASE WHEN b.partner_id IS NOT NULL THEN 'partner' ELSE 'store' END AS owner_type,
                           COALESCE(s.store_name, p.company_name) AS owner_name
                    FROM Motorbikes b
                    JOIN BikeTypes t ON t.type_id = b.type_id
                    LEFT JOIN Stores s   ON s.store_id   = b.store_id
                    LEFT JOIN Partners p ON p.partner_id = b.partner_id
                    WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (typeId != null) {
            sb.append(" AND b.type_id = ?");
            params.add(typeId);
        }
        if (maxPrice != null) {
            sb.append(" AND b.price_per_day <= ?");
            params.add(maxPrice);
        }
        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (b.bike_name LIKE ? OR b.license_plate LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (startDate != null && endDate != null) {
            sb.append("""
                        AND NOT EXISTS (
                          SELECT 1
                          FROM OrderDetails d
                          JOIN RentalOrders r ON r.order_id = d.order_id
                          WHERE d.bike_id = b.bike_id
                            AND r.status IN ('pending','confirmed')
                            AND NOT (r.end_date < ? OR r.start_date > ?)
                        )
                    """);
            params.add(startDate);
            params.add(endDate);
        }

        // Whitelist ORDER BY
        String orderBy;
        if ("price_asc".equalsIgnoreCase(sort)) orderBy = "b.price_per_day ASC";
        else if ("price_desc".equalsIgnoreCase(sort)) orderBy = "b.price_per_day DESC";
        else if ("name_asc".equalsIgnoreCase(sort)) orderBy = "b.bike_name ASC";
        else if ("name_desc".equalsIgnoreCase(sort)) orderBy = "b.bike_name DESC";
        else orderBy = "b.bike_id DESC";

        sb.append(" ORDER BY ").append(orderBy).append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        int offset = (Math.max(page, 1) - 1) * Math.max(size, 1);
        params.add(offset);
        params.add(size);

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<MotorbikeListItem> list = new ArrayList<>();
                while (rs.next()) list.add(mapListItem(rs));
                return list;
            }
        }
    }

    @Override
    public int count(Integer typeId, Date startDate, Date endDate,
                     BigDecimal maxPrice, String keyword) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                    SELECT COUNT(1)
                    FROM Motorbikes b
                    WHERE 1=1
                """);
        List<Object> params = new ArrayList<>();

        if (typeId != null) {
            sb.append(" AND b.type_id = ?");
            params.add(typeId);
        }
        if (maxPrice != null) {
            sb.append(" AND b.price_per_day <= ?");
            params.add(maxPrice);
        }
        if (keyword != null && !keyword.isBlank()) {
            sb.append(" AND (b.bike_name LIKE ? OR b.license_plate LIKE ?)");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
        }
        if (startDate != null && endDate != null) {
            sb.append("""
                        AND NOT EXISTS (
                          SELECT 1
                          FROM OrderDetails d
                          JOIN RentalOrders r ON r.order_id = d.order_id
                          WHERE d.bike_id = b.bike_id
                            AND r.status IN ('pending','confirmed')
                            AND NOT (r.end_date < ? OR r.start_date > ?)
                        )
                    """);
            params.add(startDate);
            params.add(endDate);
        }

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sb.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public List<Motorbike> findAllByOwnerAccount(int accountId, String role) throws Exception {
        String sqlPartner = """
                    SELECT b.bike_id, b.partner_id, b.store_id, b.type_id, b.bike_name, b.license_plate,
                           b.price_per_day, b.status, b.description
                    FROM Motorbikes b
                    JOIN Partners p ON p.partner_id = b.partner_id
                    WHERE p.account_id = ?
                """;
        String sqlAdmin = """
                    SELECT b.bike_id, b.partner_id, b.store_id, b.type_id, b.bike_name, b.license_plate,
                           b.price_per_day, b.status, b.description
                    FROM Motorbikes b
                    JOIN Stores s ON s.store_id = b.store_id
                    JOIN Admins a ON a.admin_id = s.admin_id
                    WHERE a.account_id = ?
                """;
        String sql = "partner".equalsIgnoreCase(role) ? sqlPartner : sqlAdmin;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Motorbike> list = new ArrayList<>();
                while (rs.next()) list.add(mapMotorbike(rs));
                return list;
            }
        }
    }

    @Override
    public MotorbikeListItem findDetailById(int bikeId) throws Exception {
        String sql = """
                    SELECT b.bike_id, b.bike_name, b.license_plate, b.price_per_day, b.status, b.description,
                           t.type_name,
                           CASE WHEN b.partner_id IS NOT NULL THEN 'partner' ELSE 'store' END AS owner_type,
                           COALESCE(s.store_name, p.company_name) AS owner_name
                    FROM Motorbikes b
                    JOIN BikeTypes t ON t.type_id = b.type_id
                    LEFT JOIN Stores s   ON s.store_id   = b.store_id
                    LEFT JOIN Partners p ON p.partner_id = b.partner_id
                    WHERE b.bike_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bikeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapListItem(rs);
                return null;
            }
        }
    }

    private Motorbike mapMotorbike(ResultSet rs) throws SQLException {
        Integer partnerId = rs.getObject("partner_id") == null ? null : rs.getInt("partner_id");
        Integer storeId = rs.getObject("store_id") == null ? null : rs.getInt("store_id");
        return new Motorbike(
                rs.getInt("bike_id"),
                partnerId,
                storeId,
                rs.getInt("type_id"),
                rs.getString("bike_name"),
                rs.getString("license_plate"),
                rs.getBigDecimal("price_per_day"),
                rs.getString("status"),
                rs.getString("description")
        );
    }

    private MotorbikeListItem mapListItem(ResultSet rs) throws SQLException {
        return new MotorbikeListItem(
                rs.getInt("bike_id"),
                rs.getString("bike_name"),
                rs.getString("license_plate"),
                rs.getBigDecimal("price_per_day"),
                rs.getString("status"),
                rs.getString("type_name"),
                rs.getString("owner_type"),
                rs.getString("owner_name"),
                rs.getString("description")
        );
    }

    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    @Override
    public List<Motorbike> findByPartnerId(int partnerId) throws Exception {
        String sql = """
                    SELECT bike_id, partner_id, store_id, type_id, bike_name, license_plate,
                           price_per_day, status, description
                    FROM Motorbikes
                    WHERE partner_id = ?
                """;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, partnerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Motorbike> list = new ArrayList<>();
                while (rs.next()) list.add(mapMotorbike(rs));
                return list;
            }
        }

    }
}
