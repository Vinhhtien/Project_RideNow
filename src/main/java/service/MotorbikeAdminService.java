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
        String sql = "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, " +
                    "m.description, p.company_name as partner_name, s.store_name, bt.type_name, " +
                    "CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END as owner_type " +
                    "FROM Motorbikes m " +
                    "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
                    "LEFT JOIN Stores s ON m.store_id = s.store_id " +
                    "JOIN BikeTypes bt ON m.type_id = bt.type_id " +
                    "ORDER BY m.bike_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Motorbike bike = new Motorbike();
                bike.setBikeId(rs.getInt("bike_id"));
                bike.setBikeName(rs.getString("bike_name"));
                bike.setLicensePlate(rs.getString("license_plate"));
                bike.setPricePerDay(rs.getBigDecimal("price_per_day"));
                bike.setStatus(rs.getString("status"));
                bike.setDescription(rs.getString("description"));
                // Set additional display fields
                bike.setOwnerName(rs.getString("owner_type").equals("Partner") ? 
                    rs.getString("partner_name") : rs.getString("store_name"));
                bike.setOwnerType(rs.getString("owner_type"));
                bike.setTypeName(rs.getString("type_name"));
                motorbikes.add(bike);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbikes;
    }

    @Override
    public Motorbike getMotorbikeById(int bikeId) {
        Motorbike motorbike = null;
        String sql = "SELECT bike_id, partner_id, store_id, type_id, bike_name, license_plate, " +
                    "price_per_day, status, description FROM Motorbikes WHERE bike_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, bikeId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    motorbike = new Motorbike();
                    motorbike.setBikeId(rs.getInt("bike_id"));
                    motorbike.setPartnerId(rs.getInt("partner_id"));
                    if (rs.wasNull()) motorbike.setPartnerId(null);
                    motorbike.setStoreId(rs.getInt("store_id"));
                    if (rs.wasNull()) motorbike.setStoreId(null);
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
        String sql = "INSERT INTO Motorbikes (partner_id, store_id, type_id, bike_name, license_plate, " +
                    "price_per_day, status, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, motorbike.getPartnerId(), Types.INTEGER);
            stmt.setObject(2, motorbike.getStoreId(), Types.INTEGER);
            stmt.setInt(3, motorbike.getTypeId());
            stmt.setString(4, motorbike.getBikeName());
            stmt.setString(5, motorbike.getLicensePlate());
            stmt.setBigDecimal(6, motorbike.getPricePerDay());
            stmt.setString(7, motorbike.getStatus());
            stmt.setString(8, motorbike.getDescription());
            
            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
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
            
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
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
            int rowsDeleted = stmt.executeUpdate();
            return rowsDeleted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<BikeType> getAllBikeTypes() {
        List<BikeType> bikeTypes = new ArrayList<>();
        String sql = "SELECT type_id, type_name FROM BikeTypes";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                BikeType type = new BikeType();
                type.setTypeId(rs.getInt("type_id"));
                type.setTypeName(rs.getString("type_name"));
                bikeTypes.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bikeTypes;
    }

    @Override
    public List<Partner> getAllPartners() {
        List<Partner> partners = new ArrayList<>();
        String sql = "SELECT partner_id, company_name FROM Partners";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Partner partner = new Partner();
                partner.setPartnerId(rs.getInt("partner_id"));
                partner.setFullname(rs.getString("company_name")); // Sửa thành company_name
                partners.add(partner);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return partners;
    }

    @Override
    public List<Motorbike> getMotorbikesByOwner(String ownerType) {
        List<Motorbike> motorbikes = new ArrayList<>();
        String sql = "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, " +
                    "m.description, p.company_name as partner_name, s.store_name, bt.type_name, " +
                    "CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END as owner_type " +
                    "FROM Motorbikes m " +
                    "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
                    "LEFT JOIN Stores s ON m.store_id = s.store_id " +
                    "JOIN BikeTypes bt ON m.type_id = bt.type_id " +
                    "WHERE " + (ownerType.equals("partner") ? "m.partner_id IS NOT NULL" : "m.store_id IS NOT NULL") +
                    " ORDER BY m.bike_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Motorbike bike = new Motorbike();
                bike.setBikeId(rs.getInt("bike_id"));
                bike.setBikeName(rs.getString("bike_name"));
                bike.setLicensePlate(rs.getString("license_plate"));
                bike.setPricePerDay(rs.getBigDecimal("price_per_day"));
                bike.setStatus(rs.getString("status"));
                bike.setDescription(rs.getString("description"));
                bike.setOwnerName(rs.getString("owner_type").equals("Partner") ? 
                    rs.getString("partner_name") : rs.getString("store_name"));
                bike.setOwnerType(rs.getString("owner_type"));
                bike.setTypeName(rs.getString("type_name"));
                motorbikes.add(bike);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbikes;
    }

    @Override
    public List<Motorbike> getMotorbikesByStatus(String status) {
        List<Motorbike> motorbikes = new ArrayList<>();
        String sql = "SELECT m.bike_id, m.bike_name, m.license_plate, m.price_per_day, m.status, " +
                    "m.description, p.company_name as partner_name, s.store_name, bt.type_name, " +
                    "CASE WHEN m.partner_id IS NOT NULL THEN 'Partner' ELSE 'Admin' END as owner_type " +
                    "FROM Motorbikes m " +
                    "LEFT JOIN Partners p ON m.partner_id = p.partner_id " +
                    "LEFT JOIN Stores s ON m.store_id = s.store_id " +
                    "JOIN BikeTypes bt ON m.type_id = bt.type_id " +
                    "WHERE m.status = ? ORDER BY m.bike_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Motorbike bike = new Motorbike();
                    bike.setBikeId(rs.getInt("bike_id"));
                    bike.setBikeName(rs.getString("bike_name"));
                    bike.setLicensePlate(rs.getString("license_plate"));
                    bike.setPricePerDay(rs.getBigDecimal("price_per_day"));
                    bike.setStatus(rs.getString("status"));
                    bike.setDescription(rs.getString("description"));
                    bike.setOwnerName(rs.getString("owner_type").equals("Partner") ? 
                        rs.getString("partner_name") : rs.getString("store_name"));
                    bike.setOwnerType(rs.getString("owner_type"));
                    bike.setTypeName(rs.getString("type_name"));
                    motorbikes.add(bike);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return motorbikes;
    }
}