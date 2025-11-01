package dao;

import model.Partner;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartnerAdminDAO implements IPartnerAdminDAO {

    @Override
    public boolean existsUsername(Connection c, String username) {
        String sql = "SELECT 1 FROM Accounts WHERE username = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kiểm tra username: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertAccountPartner(Connection c, String username, String rawPassword) {
        String sql = "INSERT INTO Accounts (username, password, role, status) VALUES (?, ?, 'partner', 1)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, rawPassword);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy account_id sau khi insert");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo account: " + e.getMessage(), e);
        }
    }

    @Override
    public int insertPartner(Connection c, int accountId, String companyName, String address, String phone, int adminId) {
        String sql = "INSERT INTO Partners (account_id, company_name, address, phone, admin_id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, accountId);
            ps.setString(2, companyName);
            ps.setString(3, address);
            ps.setString(4, phone);
            ps.setInt(5, adminId);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Không thể lấy partner_id sau khi insert");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi tạo partner: " + e.getMessage(), e);
        }
    }

    @Override
    public int getAdminIdByAccountId(Connection c, int accountId) {
        String sql = "SELECT admin_id FROM Admins WHERE account_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("admin_id");
                } else {
                    throw new RuntimeException("Không tìm thấy admin_id cho account_id: " + accountId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy admin_id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Partner> getAllPartners(Connection c) {
        String sql = "SELECT p.*, a.username, a.password " +
                "FROM Partners p " +
                "JOIN Accounts a ON p.account_id = a.account_id " +
                "ORDER BY p.partner_id DESC";
        List<Partner> partners = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Partner partner = new Partner();
                partner.setPartnerId(rs.getInt("partner_id"));
                partner.setAccountId(rs.getInt("account_id"));
                partner.setFullname(rs.getString("company_name")); // Map company_name -> fullName
                partner.setAddress(rs.getString("address"));
                partner.setPhone(rs.getString("phone"));

                // Xử lý admin_id có thể null
                int adminId = rs.getInt("admin_id");
                partner.setAdminId(rs.wasNull() ? null : adminId);

                partners.add(partner);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách partners: " + e.getMessage(), e);
        }
        return partners;
    }

    @Override
    public int getAccountIdByPartnerId(Connection c, int partnerId) {
        String sql = "SELECT account_id FROM Partners WHERE partner_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partnerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("account_id");
                } else {
                    throw new RuntimeException("Không tìm thấy partner với ID: " + partnerId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy account_id từ partner_id: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletePartner(Connection c, int partnerId) {
        String sql = "DELETE FROM Partners WHERE partner_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, partnerId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa partner: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteAccount(Connection c, int accountId) {
        String sql = "DELETE FROM Accounts WHERE account_id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            int affectedRows = ps.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi xóa account: " + e.getMessage(), e);
        }
    }
}