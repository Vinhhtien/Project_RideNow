package dao;

import model.Customer;
import utils.DBConnection;

import java.sql.*;

public class CustomerDao implements ICustomerDao {

    @Override
    public Customer findByAccountId(int accountId) throws Exception {
        String sql = "SELECT customer_id, account_id, full_name, email, phone, address " +
                     "FROM Customers WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Customer c = new Customer();
                    c.setCustomerId(rs.getInt("customer_id"));
                    c.setAccountId(rs.getInt("account_id"));
                    c.setFullName(rs.getString("full_name"));
                    c.setEmail(rs.getString("email"));
                    c.setPhone(rs.getString("phone"));
                    c.setAddress(rs.getString("address"));
                    return c;
                }
            }
        }
        return null;
    }

    @Override
    public void upsertByAccountId(Customer c) throws Exception {
        String fullName = nz(c.getFullName());
        String email    = nz(c.getEmail());
        String phone    = c.getPhone() == null ? "" : c.getPhone().trim();
        String address  = c.getAddress() == null ? "" : c.getAddress().trim();

        if (fullName.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException("Họ tên và Email là bắt buộc");
        }

        String checkSql = "SELECT COUNT(*) FROM Customers WHERE account_id = ?";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement check = con.prepareStatement(checkSql)) {
            check.setInt(1, c.getAccountId());
            boolean existed;
            try (ResultSet rs = check.executeQuery()) {
                rs.next();
                existed = rs.getInt(1) > 0;
            }

            if (existed) {
                String upd = "UPDATE Customers SET full_name=?, email=?, phone=?, address=? WHERE account_id=?";
                try (PreparedStatement ps = con.prepareStatement(upd)) {
                    ps.setString(1, fullName);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.setString(4, address);
                    ps.setInt(5, c.getAccountId());
                    ps.executeUpdate();
                }
            } else {
                String ins = "INSERT INTO Customers(account_id, full_name, email, phone, address) VALUES(?,?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(ins)) {
                    ps.setInt(1, c.getAccountId());
                    ps.setString(2, fullName);
                    ps.setString(3, email);
                    ps.setString(4, phone);
                    ps.setString(5, address);
                    ps.executeUpdate();
                }
            }
        }
    }

    @Override
    public boolean updatePassword(int accountId, String currentPw, String newPw) throws Exception {
        // 1) Lấy hash hiện tại
        String sqlGet = "SELECT password FROM Accounts WHERE account_id = ?";
        String stored;
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sqlGet)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                stored = rs.getString("password");
            }
        }

        // 2) Kiểm tra currentPw khớp?
        if (!utils.PasswordUtil.matches(currentPw, stored)) {
            return false;
        }

        // 3) Cập nhật bằng hash mới
        String sqlUpd = "UPDATE Accounts SET password = ? WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps2 = con.prepareStatement(sqlUpd)) {
            ps2.setString(1, utils.PasswordUtil.hashPassword(newPw));
            ps2.setInt(2, accountId);
            return ps2.executeUpdate() > 0;
        }
    }


    private static String nz(String s) { return s == null ? "" : s.trim(); }
}
