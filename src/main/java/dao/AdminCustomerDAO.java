package dao;

import model.AdminCustomerDTO;
import utils.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminCustomerDAO implements IAdminCustomerDAO {

    @Override
    public List<AdminCustomerDTO> searchCustomers(String q, String status, String walletFilter, String sort, String dir, int page, int pageSize) {
        List<AdminCustomerDTO> customers = new ArrayList<>();

        String sql = """
                    SELECT 
                        c.customer_id as id,
                        c.full_name as fullName,
                        c.email,
                        c.phone,
                        a.status as banned,
                        a.created_at,
                        COUNT(DISTINCT ro.order_id) as orders,
                        COALESCE(w.balance, 0) as wallet
                    FROM Customers c
                    INNER JOIN Accounts a ON c.account_id = a.account_id
                    LEFT JOIN RentalOrders ro ON c.customer_id = ro.customer_id
                    LEFT JOIN Wallets w ON c.customer_id = w.customer_id
                    WHERE a.role = 'customer'
                """;

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (q != null && !q.trim().isEmpty()) {
            conditions.add("(c.full_name LIKE ? OR c.email LIKE ? OR c.phone LIKE ?)");
            String searchTerm = "%" + q.trim() + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if ("active".equals(status)) {
            conditions.add("a.status = 1");
        } else if ("banned".equals(status)) {
            conditions.add("a.status = 0");
        }

        // Thêm điều kiện lọc theo ví
        if (walletFilter != null) {
            switch (walletFilter) {
                case "has_balance":
                    conditions.add("COALESCE(w.balance, 0) > 0");
                    break;
                case "no_balance":
                    conditions.add("COALESCE(w.balance, 0) = 0");
                    break;
                case "high_balance":
                    conditions.add("COALESCE(w.balance, 0) >= 1000000");
                    break;
            }
        }

        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }

        sql += " GROUP BY c.customer_id, c.full_name, c.email, c.phone, a.status, a.created_at, w.balance";

        // Sắp xếp
        String sortColumn = getSortColumn(sort);
        String sortDirection = "desc".equalsIgnoreCase(dir) ? "DESC" : "ASC";
        sql += " ORDER BY " + sortColumn + " " + sortDirection;

        sql += " OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        int offset = (page - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AdminCustomerDTO customer = new AdminCustomerDTO();
                customer.setId(rs.getInt("id"));
                customer.setFullName(rs.getString("fullName"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setBanned(rs.getInt("banned") == 0); // 0 = banned, 1 = active
                customer.setCreatedAt(rs.getTimestamp("created_at"));
                customer.setOrders(rs.getInt("orders"));
                customer.setWallet(rs.getBigDecimal("wallet")); // Sử dụng setWallet()

                customers.add(customer);
            }
        } catch (SQLException e) {
            System.err.println("Error in searchCustomers: " + e.getMessage());
            e.printStackTrace();
        }
        return customers;
    }

    @Override
    public int countCustomers(String q, String status, String walletFilter) {
        String sql = """
                    SELECT COUNT(DISTINCT c.customer_id) as total 
                    FROM Customers c
                    INNER JOIN Accounts a ON c.account_id = a.account_id
                    LEFT JOIN Wallets w ON c.customer_id = w.customer_id
                    WHERE a.role = 'customer'
                """;

        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (q != null && !q.trim().isEmpty()) {
            conditions.add("(c.full_name LIKE ? OR c.email LIKE ? OR c.phone LIKE ?)");
            String searchTerm = "%" + q.trim() + "%";
            params.add(searchTerm);
            params.add(searchTerm);
            params.add(searchTerm);
        }

        if ("active".equals(status)) {
            conditions.add("a.status = 1");
        } else if ("banned".equals(status)) {
            conditions.add("a.status = 0");
        }

        // Thêm điều kiện lọc theo ví
        if (walletFilter != null) {
            switch (walletFilter) {
                case "has_balance":
                    conditions.add("COALESCE(w.balance, 0) > 0");
                    break;
                case "no_balance":
                    conditions.add("COALESCE(w.balance, 0) = 0");
                    break;
                case "high_balance":
                    conditions.add("COALESCE(w.balance, 0) >= 1000000");
                    break;
            }
        }

        if (!conditions.isEmpty()) {
            sql += " AND " + String.join(" AND ", conditions);
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error in countCustomers: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public AdminCustomerDTO getCustomerDetail(int customerId) {
        AdminCustomerDTO customer = null;

        String sql = """
                    SELECT 
                        c.customer_id as id,
                        c.full_name as fullName,
                        c.email,
                        c.phone,
                        a.status as banned,
                        a.email_verified,
                        a.created_at,
                        c.address,
                        NULL as dob,
                        a.last_login,
                        COALESCE(w.balance, 0) as wallet,
                        COUNT(DISTINCT ro.order_id) as totalOrders,
                        COALESCE(SUM(ro.total_price), 0) as totalSpent,
                        MAX(ro.created_at) as lastOrderAt
                    FROM Customers c
                    INNER JOIN Accounts a ON c.account_id = a.account_id
                    LEFT JOIN RentalOrders ro ON c.customer_id = ro.customer_id
                    LEFT JOIN Wallets w ON c.customer_id = w.customer_id
                    WHERE c.customer_id = ?
                    GROUP BY 
                        c.customer_id, c.full_name, c.email, c.phone, a.status, 
                        a.email_verified, a.created_at, c.address, a.last_login, w.balance
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                customer = new AdminCustomerDTO();
                customer.setId(rs.getInt("id"));
                customer.setFullName(rs.getString("fullName"));
                customer.setEmail(rs.getString("email"));
                customer.setPhone(rs.getString("phone"));
                customer.setBanned(rs.getInt("banned") == 0);
                customer.setEmailVerified(rs.getBoolean("email_verified"));
                customer.setCreatedAt(rs.getTimestamp("created_at"));
                customer.setAddress(rs.getString("address"));
                customer.setLastLogin(rs.getTimestamp("last_login"));
                customer.setWallet(rs.getBigDecimal("wallet")); // Sử dụng setWallet()
                customer.setOrders(rs.getInt("totalOrders"));
                customer.setTotalSpent(rs.getBigDecimal("totalSpent"));
                customer.setLastOrderAt(rs.getTimestamp("lastOrderAt"));

                loadRecentOrders(conn, customer, customerId);
            }
        } catch (SQLException e) {
            System.err.println("Error in getCustomerDetail: " + e.getMessage());
            e.printStackTrace();
        }
        return customer;
    }

    @Override
    public void toggleCustomerStatus(int customerId) {
        // Lấy account_id từ customer_id trước
        String getAccountIdSql = "SELECT account_id FROM Customers WHERE customer_id = ?";
        String updateSql = "UPDATE Accounts SET status = 1 - status WHERE account_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement getStmt = conn.prepareStatement(getAccountIdSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

            // Lấy account_id
            getStmt.setInt(1, customerId);
            ResultSet rs = getStmt.executeQuery();

            if (rs.next()) {
                int accountId = rs.getInt("account_id");

                // Cập nhật status trong Accounts
                updateStmt.setInt(1, accountId);
                int rows = updateStmt.executeUpdate();
                System.out.println("Toggled status for account " + accountId + " (customer " + customerId + "), rows affected: " + rows);
            }
        } catch (SQLException e) {
            System.err.println("Error in toggleCustomerStatus: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadRecentOrders(Connection conn, AdminCustomerDTO customer, int customerId) throws SQLException {
        String sql = """
                    SELECT TOP 5 
                        ro.order_id,
                        m.bike_name,
                        ro.total_price as total,
                        ro.status,
                        ro.created_at
                    FROM RentalOrders ro
                    LEFT JOIN OrderDetails od ON ro.order_id = od.order_id
                    LEFT JOIN Motorbikes m ON od.bike_id = m.bike_id
                    WHERE ro.customer_id = ?
                    ORDER BY ro.created_at DESC
                """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                AdminCustomerDTO.OrderMini order = new AdminCustomerDTO.OrderMini();
                order.setOrderId(rs.getInt("order_id"));
                order.setBikeName(rs.getString("bike_name"));
                order.setTotal(rs.getBigDecimal("total"));
                order.setStatus(rs.getString("status"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                customer.addRecentOrder(order);
            }
        }
    }

    private String getSortColumn(String sort) {
        return switch (sort) {
            case "name" -> "c.full_name";
            case "orders" -> "orders";
            case "wallet" -> "COALESCE(w.balance, 0)"; // Sắp xếp theo số dư ví
            default -> "a.created_at";
        };
    }

    // Thêm phương thức để tính tổng số dư ví
    public java.math.BigDecimal getTotalWalletBalance() {
        String sql = "SELECT COALESCE(SUM(balance), 0) as total_balance FROM Wallets";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getBigDecimal("total_balance");
            }
        } catch (SQLException e) {
            System.err.println("Error in getTotalWalletBalance: " + e.getMessage());
            e.printStackTrace();
        }
        return java.math.BigDecimal.ZERO;
    }
}