package dao;

import model.Account;
import utils.DBConnection;
import utils.PasswordUtil;

import java.sql.*;
import java.util.Optional;

public class AccountDao implements IAccountDao {

    @Override
    public Optional<Account> login(String usernameOrEmail, String password) throws Exception {
        // Lấy account theo username hoặc email; KHÔNG để password trong WHERE
        String sql =
            "SELECT a.account_id, a.username, a.password, a.role, a.status, a.email_verified " +
            "FROM Accounts a " +
            "LEFT JOIN Customers c ON c.account_id = a.account_id " +
            "WHERE (a.username = ? OR c.email = ?) " +
            "  AND a.status = 1";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql,
                     ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE)) {

            ps.setString(1, usernameOrEmail);
            ps.setString(2, usernameOrEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password");
                    if (!PasswordUtil.matches(password, stored)) {
                        // Sai mật khẩu
                        return Optional.empty();
                    }

                    // Nếu DB đang lưu plaintext -> “upgrade” sang BCrypt ngay
                    if (!PasswordUtil.isBCrypt(stored)) {
                        String newHash = PasswordUtil.hashPassword(password);
                        try (PreparedStatement up = con.prepareStatement(
                                "UPDATE Accounts SET password=? WHERE account_id=?")) {
                            up.setString(1, newHash);
                            up.setInt(2, rs.getInt("account_id"));
                            up.executeUpdate();
                        }
                    }

                    Account acc = mapRow(rs);
                    return Optional.of(acc);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Account> findByUsername(String username) throws Exception {
        String sql =
            "SELECT a.account_id, a.username, a.password, a.role, a.status, a.email_verified " +
            "FROM Accounts a WHERE a.username = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        Account acc = new Account(
            rs.getInt("account_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getBoolean("status")
        );
        acc.setEmailVerified(rs.getBoolean("email_verified"));
        return acc;
    }
}
