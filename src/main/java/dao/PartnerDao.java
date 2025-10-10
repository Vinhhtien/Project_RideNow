
package dao;

import model.Partner;
import utils.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PartnerDao implements IPartnerDao {

    @Override
    public Partner login(String username, String password) throws Exception {
        final String sql =
            "SELECT p.partner_id, p.account_id, p.company_name, p.address, p.phone, p.admin_id " +  // KHÔNG có email
            "FROM Partners p " +
            "JOIN Accounts a ON p.account_id = a.account_id " +
            "WHERE a.username = ? AND a.password = ? AND a.role = 'partner' AND a.status = 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
    }

    @Override
    public boolean updateAccountInfo(Partner p) {
        String sql = "UPDATE Partners SET company_name = ?, address = ?, phone = ? WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getFullname());   // lưu ý method đang là getFullname()
            ps.setString(2, p.getAddress());
            ps.setString(3, p.getPhone());
            ps.setInt(4, p.getAccountId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public boolean updateAccountName(int accountId, String accountName) {
        String sql = "UPDATE Accounts SET username = ? WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, accountName);
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    @Override
    public boolean updatePassword(int accountId, String newPassword) {
        String sql = "UPDATE Accounts SET password = ? WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);     // nếu có hashing, thay bằng hash
            ps.setInt(2, accountId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    


   @Override
public Partner getByAccountId(int accountId) {
    String sql = "SELECT partner_id, account_id, company_name, address, phone, admin_id " +
                 "FROM Partners WHERE account_id = ?";
    try (Connection con = DBConnection.getConnection();
         PreparedStatement ps = con.prepareStatement(sql)) {
        ps.setInt(1, accountId);
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Partner p = new Partner();
                p.setPartnerId(rs.getInt("partner_id"));
                p.setAccountId(rs.getInt("account_id"));
                p.setFullname(rs.getString("company_name"));
                p.setAddress(rs.getString("address"));
                p.setPhone(rs.getString("phone"));
                int adminId = rs.getInt("admin_id");
                if (rs.wasNull()) {
                    p.setAdminId(null);
                } else {
                    p.setAdminId(adminId);
                }
                return p;
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
    return null;
}


    // --- Helper ---
    private Partner mapRow(ResultSet rs) throws SQLException {
        Partner p = new Partner();
        p.setPartnerId(rs.getInt("partner_id"));
        p.setAccountId(rs.getInt("account_id"));

        // Tùy model Partner của bạn:
        // Nếu có companyName:
        // p.setCompanyName(rs.getString("company_name"));
        // Nếu hiện tại bạn đang dùng fullname như code trước:
        p.setFullname(rs.getString("company_name"));

        p.setAddress(rs.getString("address"));
        p.setPhone(rs.getString("phone"));

        // Nếu model có trường adminId (vì bạn đã ALTER TABLE thêm admin_id):
        try {
            p.setAdminId(rs.getInt("admin_id"));
        } catch (Exception ignore) {
            // nếu model chưa có field này thì tạm bỏ qua
        }
        return p;
    }
}
