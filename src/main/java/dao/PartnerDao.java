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
    public boolean updateAccountInfo(Partner partner) throws Exception {
        final String sql =
            "UPDATE Partners SET company_name = ?, address = ?, phone = ? WHERE account_id = ?"; // KHÔNG có email

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // LƯU Ý: đổi setCompanyName/setFullname cho đúng model của bạn
            // Nếu model có setCompanyName:
            // ps.setString(1, partner.getCompanyName());
            // Nếu model hiện tại đang là setFullname (như code trước):
            ps.setString(1, partner.getFullname());

            ps.setString(2, partner.getAddress());
            ps.setString(3, partner.getPhone());
            ps.setInt(4, partner.getAccountId());

            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Partner getByAccountId(int accountId) throws Exception {
        final String sql =
            "SELECT partner_id, account_id, company_name, address, phone, admin_id " + // KHÔNG có email
            "FROM Partners WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, accountId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                return null;
            }
        }
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
