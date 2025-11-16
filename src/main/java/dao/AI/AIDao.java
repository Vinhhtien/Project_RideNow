package dao.AI;

import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class AIDao {

    // ======== SELECT an toàn ========
    public List<Map<String, Object>> select(String sql, List<String> params) {
        if (!isSafeSelect(sql)) {
            throw new IllegalArgumentException("Only SELECT statements are allowed for AI DAO.");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Bind param thông minh
            if (params != null) {
                int idx = 1;
                for (String s : params) {
                    if (s == null) {
                        ps.setNull(idx++, Types.NVARCHAR);
                        continue;
                    }

                    // số nguyên
                    if (s.matches("^-?\\d+$")) {
                        ps.setLong(idx++, Long.parseLong(s));
                        continue;
                    }
                    // số thập phân
                    if (s.matches("^-?\\d+[\\.,]\\d+$")) {
                        ps.setBigDecimal(idx++, new BigDecimal(s.replace(',', '.')));
                        continue;
                    }
                    // yyyy-MM-dd
                    if (s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                        ps.setDate(idx++, java.sql.Date.valueOf(java.time.LocalDate.parse(s)));
                        continue;
                    }
                    // Unicode
                    ps.setNString(idx++, s);
                }
            }

            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData md = rs.getMetaData();
                int c = md.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= c; i++) {
                        String label = md.getColumnLabel(i);
                        if (label == null || label.isBlank()) label = md.getColumnName(i);
                        row.put(label, rs.getObject(i));
                    }
                    rows.add(row);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("AIDao.select SQL error: " + e.getMessage(), e);
        }
        return rows;
    }

    /**
     * Top N xe theo tên loại (Xe số / Xe ga / Phân khối lớn)
     */
    public List<Map<String, Object>> topBikesByType(String typeName, int limit) {
        int n = Math.max(1, Math.min(limit, 10));
        String sql =
                "SELECT TOP " + n + " " +
                        "M.bike_id, M.bike_name, M.price_per_day, M.description, " +
                        "M.license_plate, M.status, BT.type_name " +
                        "FROM Motorbikes AS M " +
                        "JOIN BikeTypes AS BT ON M.type_id = BT.type_id " +
                        "WHERE BT.type_name = ? " +
                        "ORDER BY M.price_per_day ASC";

        System.out.println("DEBUG: Executing SQL for type: " + typeName);
        System.out.println("DEBUG: SQL: " + sql);

        List<Map<String, Object>> result = select(sql, List.of(typeName));
        System.out.println("DEBUG: Found " + (result == null ? 0 : result.size()) + " results");

        if (result != null && !result.isEmpty()) {
            System.out.println("DEBUG: First row: " + result.get(0));
        }

        return result;
    }

    /**
     * Tìm xe với nhiều điều kiện
     */
    public List<Map<String, Object>> searchBikes(Map<String, String> conditions) {
        StringBuilder sql = new StringBuilder(
                "SELECT M.bike_id, M.bike_name, M.price_per_day, M.description, " +
                        "M.license_plate, M.status, BT.type_name " +
                        "FROM Motorbikes M JOIN BikeTypes BT ON M.type_id = BT.type_id " +
                        "WHERE 1=1 "
        );

        List<String> params = new ArrayList<>();

        // Loại xe
        if (conditions.containsKey("type")) {
            sql.append(" AND BT.type_name = ?");
            params.add(conditions.get("type"));
        }

        // Giá tối đa
        if (conditions.containsKey("max_price")) {
            sql.append(" AND M.price_per_day <= ?");
            params.add(conditions.get("max_price"));
        }

        // Giá tối thiểu  
        if (conditions.containsKey("min_price")) {
            sql.append(" AND M.price_per_day >= ?");
            params.add(conditions.get("min_price"));
        }

        // Trạng thái
        if (conditions.containsKey("status")) {
            sql.append(" AND M.status = ?");
            params.add(conditions.get("status"));
        }

        // Từ khóa
        if (conditions.containsKey("keyword")) {
            sql.append(" AND (M.bike_name LIKE ? OR M.description LIKE ?)");
            String keyword = "%" + conditions.get("keyword") + "%";
            params.add(keyword);
            params.add(keyword);
        }

        sql.append(" ORDER BY M.price_per_day ASC");

        System.out.println("DEBUG: Search SQL: " + sql.toString());
        System.out.println("DEBUG: Search params: " + params);

        return select(sql.toString(), params);
    }

    /**
     * Top N xe rẻ nhất (status = available)
     */
    public List<Map<String, Object>> findCheapestBikes(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        String sql =
                "SELECT TOP " + n + " " +
                        "M.bike_id, M.bike_name, M.price_per_day, M.description, " +
                        "M.license_plate, M.status, BT.type_name " +
                        "FROM Motorbikes M " +
                        "JOIN BikeTypes BT ON M.type_id = BT.type_id " +
                        "WHERE M.status = 'available' " +
                        "ORDER BY M.price_per_day ASC";

        System.out.println("DEBUG: findCheapestBikes SQL: " + sql);
        return select(sql, Collections.emptyList());
    }

    /**
     * Top N xe đắt nhất (status = available)
     */
    public List<Map<String, Object>> findMostExpensiveBikes(int limit) {
        int n = Math.max(1, Math.min(limit, 20));
        String sql =
                "SELECT TOP " + n + " " +
                        "M.bike_id, M.bike_name, M.price_per_day, M.description, " +
                        "M.license_plate, M.status, BT.type_name " +
                        "FROM Motorbikes M " +
                        "JOIN BikeTypes BT ON M.type_id = BT.type_id " +
                        "WHERE M.status = 'available' " +
                        "ORDER BY M.price_per_day DESC";

        System.out.println("DEBUG: findMostExpensiveBikes SQL: " + sql);
        return select(sql, Collections.emptyList());
    }

    // ======== Schema doc ========
    public String buildSchemaDoc() {
        StringBuilder sb = new StringBuilder("Database Schema:\n\n");

        String sql = """
                    SELECT 
                        t.TABLE_NAME, 
                        c.COLUMN_NAME, 
                        c.DATA_TYPE,
                        c.IS_NULLABLE,
                        COLUMNPROPERTY(OBJECT_ID(c.TABLE_SCHEMA + '.' + c.TABLE_NAME), c.COLUMN_NAME, 'IsIdentity') AS IS_IDENTITY
                    FROM INFORMATION_SCHEMA.TABLES t
                    JOIN INFORMATION_SCHEMA.COLUMNS c ON t.TABLE_NAME = c.TABLE_NAME
                    WHERE t.TABLE_TYPE = 'BASE TABLE'
                    ORDER BY t.TABLE_NAME, c.ORDINAL_POSITION
                """;

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            String currentTable = null;
            List<String> columns = new ArrayList<>();

            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String columnName = rs.getString("COLUMN_NAME");
                String dataType = rs.getString("DATA_TYPE");
                String isNullable = rs.getString("IS_NULLABLE");
                int isIdentity = rs.getInt("IS_IDENTITY");

                if (!tableName.equals(currentTable)) {
                    if (currentTable != null) {
                        sb.append("- ").append(currentTable).append(": ").append(String.join(", ", columns)).append("\n");
                    }
                    currentTable = tableName;
                    columns.clear();
                }

                String columnDesc = columnName + " " + dataType;
                if (isIdentity == 1) columnDesc += " (IDENTITY)";
                if ("NO".equals(isNullable)) columnDesc += " NOT NULL";

                columns.add(columnDesc);
            }

            if (currentTable != null) {
                sb.append("- ").append(currentTable).append(": ").append(String.join(", ", columns)).append("\n");
            }

            sb.append("\nSample Data:\n");
            sb.append("- BikeTypes: Xe số, Xe ga, Phân khối lớn\n");
            sb.append("- Motorbikes status: available, rented, maintenance\n");

        } catch (SQLException e) {
            sb.append("- Error reading schema: ").append(e.getMessage());
        }

        return sb.toString();
    }

    // ======== Guard ========
    private boolean isSafeSelect(String sql) {
        if (sql == null) return false;
        String s = sql.trim().toUpperCase(Locale.ROOT);
        if (!s.startsWith("SELECT")) return false;
        String t = s.replace("\n", " ").replace("\r", " ");
        if (t.contains(";")) return false;
        String[] banned = {"--", "/*", "*/", " EXEC ", " EXECUTE ", " INSERT ", " UPDATE ", " DELETE ", " MERGE ",
                " DROP ", " ALTER ", " CREATE ", " GRANT ", " REVOKE ", " DENY ", " TRUNCATE ", " XP_", " SP_", "SYSOBJECTS", "SYS.TABLES"};
        for (String b : banned) if (t.contains(b)) return false;
        return true;
    }
}
