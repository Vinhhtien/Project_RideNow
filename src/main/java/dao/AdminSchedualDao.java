package dao;

import utils.DBConnection;
import java.sql.*;

public class AdminSchedualDao implements IAdminSchedualDao {
    private static final String SQL = "SELECT admin_id FROM Admins WHERE account_id = ?";

    @Override
    public Integer findAdminIdByAccountId(int accountId) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(SQL)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Cannot map account -> admin", e);
        }
    }
}
