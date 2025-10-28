package service;

import model.Motorbike;
import model.BikeType;
import model.Partner;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MotorbikeAdminService implements IMotorbikeAdminService {

    @Override
public List<Motorbike> getAllMotorbikes() {
    List<Motorbike> motorbikes = new ArrayList<>();
    String sql =
        "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, m.description, " +
        "       m.partner_id, m.store_id, m.type_id, " +
        "       p.company_name AS partner_name, s.store_name, bt.type_name, " +
        "       CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END AS owner_type " +
        "FROM Motorbikes m " +
        "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
        "LEFT JOIN Stores   s ON m.store_id   = s.store_id " +
        "LEFT JOIN BikeTypes bt ON m.type_id  = bt.type_id " +   // ✅ đổi sang LEFT JOIN
        "ORDER BY m.bike_id DESC";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Motorbike b = new Motorbike();
            b.setBikeId(rs.getInt("bike_id"));
            b.setBikeName(rs.getString("bike_name"));
            b.setLicensePlate(rs.getString("license_plate"));
            b.setPricePerDay(rs.getBigDecimal("price_per_day"));
            b.setStatus(rs.getString("status"));
            b.setDescription(rs.getString("description"));

            // giữ lại id gốc (có thể null)
            b.setPartnerId((Integer) rs.getObject("partner_id"));
            b.setStoreId((Integer) rs.getObject("store_id"));
            b.setTypeId((Integer) rs.getObject("type_id"));

            b.setTypeName(rs.getString("type_name")); // có thể null nếu type_id không hợp lệ

            String ownerType = rs.getString("owner_type");
            b.setOwnerType(ownerType);
            String partnerName = rs.getString("partner_name");
            String storeName   = rs.getString("store_name");
            b.setOwnerName("Partner".equals(ownerType) ? partnerName : storeName);

            motorbikes.add(b);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return motorbikes;
}


    @Override
    public Motorbike getMotorbikeById(int bikeId) {
        Motorbike motorbike = null;
        String sql =
            "SELECT bike_id, partner_id, store_id, type_id, bike_name, license_plate, " +
            "       price_per_day, status, description " +
            "FROM Motorbikes WHERE bike_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, bikeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    motorbike = new Motorbike();
                    motorbike.setBikeId(rs.getInt("bike_id"));

                    Integer partnerId = (Integer) rs.getObject("partner_id");
                    Integer storeId   = (Integer) rs.getObject("store_id");
                    motorbike.setPartnerId(partnerId);
                    motorbike.setStoreId(storeId);

                    motorbike.setTypeId(rs.getInt("type_id"));
                    motorbike.setBikeName(rs.getString("bike_name"));
                    motorbike.setLicensePlate(rs.getString("license_plate"));
                    motorbike.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    motorbike.setStatus(rs.getString("status"));
                    motorbike.setDescription(rs.getString("description"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbike;
    }

    @Override
public boolean addMotorbike(Motorbike motorbike) {
    String sql = "INSERT INTO Motorbikes " +
                 "(partner_id, store_id, type_id, bike_name, license_plate, price_per_day, status, description) " +
                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        System.out.println("DEBUG: Database connection successful");
        
        // Set parameters với debug
        if (motorbike.getPartnerId() != null) {
            stmt.setInt(1, motorbike.getPartnerId());
            System.out.println("DEBUG: Setting partner_id: " + motorbike.getPartnerId());
        } else {
            stmt.setNull(1, Types.INTEGER);
            System.out.println("DEBUG: Setting partner_id: NULL");
        }

        if (motorbike.getStoreId() != null) {
            stmt.setInt(2, motorbike.getStoreId());
            System.out.println("DEBUG: Setting store_id: " + motorbike.getStoreId());
        } else {
            stmt.setNull(2, Types.INTEGER);
            System.out.println("DEBUG: Setting store_id: NULL");
        }

        stmt.setInt(3, motorbike.getTypeId());
        stmt.setString(4, motorbike.getBikeName());
        stmt.setString(5, motorbike.getLicensePlate());
        stmt.setBigDecimal(6, motorbike.getPricePerDay());
        stmt.setString(7, motorbike.getStatus());
        stmt.setString(8, motorbike.getDescription());

        System.out.println("DEBUG: Executing SQL: " + sql);
        int rows = stmt.executeUpdate();
        System.out.println("DEBUG: Rows affected: " + rows);

        if (rows == 0) {
            System.err.println("ERROR: No rows affected");
            return false;
        }

        try (ResultSet keys = stmt.getGeneratedKeys()) {
            if (keys.next()) {
                int newId = keys.getInt(1);
                motorbike.setBikeId(newId);
                System.out.println("DEBUG: Generated Bike ID: " + newId);
            } else {
                System.err.println("ERROR: No generated keys returned");
            }
        }
        return true;

    } catch (SQLException e) {
        System.err.println("SQL ERROR: " + e.getMessage());
        System.err.println("SQL State: " + e.getSQLState());
        System.err.println("Error Code: " + e.getErrorCode());
        e.printStackTrace();
        return false;
    }
}

    @Override
    public boolean updateMotorbike(Motorbike motorbike) {
        String sql = "UPDATE Motorbikes SET bike_name = ?, license_plate = ?, price_per_day = ?, " +
                     "status = ?, description = ?, type_id = ? WHERE bike_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, motorbike.getBikeName());
            stmt.setString(2, motorbike.getLicensePlate());
            stmt.setBigDecimal(3, motorbike.getPricePerDay());
            stmt.setString(4, motorbike.getStatus());
            stmt.setString(5, motorbike.getDescription());
            stmt.setInt(6, motorbike.getTypeId());
            stmt.setInt(7, motorbike.getBikeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            // có thể thêm xử lý dup biển số như trên nếu cần
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteMotorbike(int bikeId) {
        String sql = "DELETE FROM Motorbikes WHERE bike_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, bikeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<BikeType> getAllBikeTypes() {
        List<BikeType> bikeTypes = new ArrayList<>();
        String sql = "SELECT type_id, type_name FROM BikeTypes ORDER BY type_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                BikeType t = new BikeType();
                t.setTypeId(rs.getInt("type_id"));
                t.setTypeName(rs.getString("type_name"));
                bikeTypes.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bikeTypes;
    }

    @Override
    public List<Partner> getAllPartners() {
        List<Partner> partners = new ArrayList<>();
        // SỬA QUERY: lấy company_name và map đúng vào fullName
        String sql = "SELECT partner_id, company_name, address, phone, admin_id FROM Partners ORDER BY partner_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Partner p = new Partner();
                p.setPartnerId(rs.getInt("partner_id"));
                p.setFullname(rs.getString("company_name")); // DÙNG company_name TRỰC TIẾP
                p.setAddress(rs.getString("address"));
                p.setPhone(rs.getString("phone"));
                p.setAdminId((Integer)rs.getObject("admin_id"));
                partners.add(p);

                // DEBUG
                System.out.println("DEBUG Partner: " + p.getPartnerId() + " - " + p.getFullname());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.println("DEBUG: getAllPartners() returned " + partners.size() + " partners");
        return partners;
    }

    @Override
    public List<Motorbike> getMotorbikesByOwner(String ownerType) {
        List<Motorbike> motorbikes = new ArrayList<>();
        String condition = "partner".equalsIgnoreCase(ownerType)
                ? "m.partner_id IS NOT NULL"
                : "m.store_id IS NOT NULL";

        String sql =
            "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, m.description, " +
            "       p.company_name AS partner_name, s.store_name, bt.type_name, " +
            "       CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END AS owner_type " +
            "FROM Motorbikes m " +
            "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
            "LEFT JOIN Stores   s ON m.store_id   = s.store_id " +
            "JOIN  BikeTypes   bt ON m.type_id    = bt.type_id " +
            "WHERE " + condition + " " +
            "ORDER BY m.bike_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Motorbike b = new Motorbike();
                b.setBikeId(rs.getInt("bike_id"));
                b.setBikeName(rs.getString("bike_name"));
                b.setLicensePlate(rs.getString("license_plate"));
                b.setPricePerDay(rs.getBigDecimal("price_per_day"));
                b.setStatus(rs.getString("status"));
                b.setDescription(rs.getString("description"));
                b.setTypeName(rs.getString("type_name"));

                String ot = rs.getString("owner_type");
                b.setOwnerType(ot);
                b.setOwnerName("Partner".equals(ot)
                        ? rs.getString("partner_name")
                        : rs.getString("store_name"));

                motorbikes.add(b);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbikes;
    }

    @Override
    public List<Motorbike> getMotorbikesByStatus(String status) {
        List<Motorbike> motorbikes = new ArrayList<>();
        String sql =
            "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, m.description, " +
            "       p.company_name AS partner_name, s.store_name, bt.type_name, " +
            "       CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END AS owner_type " +
            "FROM Motorbikes m " +
            "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
            "LEFT JOIN Stores   s ON m.store_id   = s.store_id " +
            "JOIN  BikeTypes   bt ON m.type_id    = bt.type_id " +
            "WHERE m.status = ? " +
            "ORDER BY m.bike_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Motorbike b = new Motorbike();
                    b.setBikeId(rs.getInt("bike_id"));
                    b.setBikeName(rs.getString("bike_name"));
                    b.setLicensePlate(rs.getString("license_plate"));
                    b.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    b.setStatus(rs.getString("status"));
                    b.setDescription(rs.getString("description"));
                    b.setTypeName(rs.getString("type_name"));

                    String ot = rs.getString("owner_type");
                    b.setOwnerType(ot);
                    b.setOwnerName("Partner".equals(ot)
                            ? rs.getString("partner_name")
                            : rs.getString("store_name"));

                    motorbikes.add(b);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbikes;
    }
}
