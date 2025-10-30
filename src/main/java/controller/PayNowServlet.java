package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import utils.DBConnection;
import utils.EmailUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "PayNowServlet", urlPatterns = {"/paynow"})
public class PayNowServlet extends HttpServlet {

    /* ===== View Row cho paynow.jsp ===== */
    public static class Row {
        private int orderId;
        private String bikeName;
        private java.sql.Date start;
        private java.sql.Date end;
        private BigDecimal totalPrice;
        private BigDecimal deposit;
        private BigDecimal thirtyPct;
        private BigDecimal toPayNow;
        private String status;
        private String paymentStatus = "none"; // tránh JSP null

        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }
        public java.sql.Date getStart() { return start; }
        public void setStart(java.sql.Date start) { this.start = start; }
        public java.sql.Date getEnd() { return end; }
        public void setEnd(java.sql.Date end) { this.end = end; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
        public BigDecimal getDeposit() { return deposit; }
        public void setDeposit(BigDecimal deposit) { this.deposit = deposit; }
        public BigDecimal getThirtyPct() { return thirtyPct; }
        public void setThirtyPct(BigDecimal thirtyPct) { this.thirtyPct = thirtyPct; }
        public BigDecimal getToPayNow() { return toPayNow; }
        public void setToPayNow(BigDecimal toPayNow) { this.toPayNow = toPayNow; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    }

    /* ====================== GET: hiển thị trang thanh toán ====================== */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        if (orderIds.isEmpty()) {
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try (Connection con = DBConnection.getConnection()) {

            String inParams = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
            String sql = """
                SELECT 
                    r.order_id,
                    COALESCE(
                        STUFF((SELECT N', ' + b2.bike_name
                               FROM OrderDetails d2
                               JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id
                               WHERE d2.order_id = r.order_id
                               FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,''), N'Unknown Bike'
                    ) AS bike_name,
                    r.start_date, r.end_date, r.total_price, r.status
                FROM RentalOrders r
                JOIN Customers c ON r.customer_id = c.customer_id
                WHERE r.order_id IN (%s)
                  AND c.account_id = ?
                  AND r.status = 'pending'
            """.formatted(inParams);

            List<Row> rows = new ArrayList<>();
            BigDecimal grandTotal = BigDecimal.ZERO;

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int i = 1;
                for (Integer id : orderIds) ps.setInt(i++, id);
                ps.setInt(i, acc.getAccountId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Row r = new Row();
                        r.setOrderId(rs.getInt("order_id"));
                        r.setBikeName(rs.getString("bike_name"));
                        r.setStart(rs.getDate("start_date"));
                        r.setEnd(rs.getDate("end_date"));
                        r.setStatus(rs.getString("status"));

                        BigDecimal total = safe(rs.getBigDecimal("total_price"));
                        r.setTotalPrice(total);

                        // ✅ Cọc theo loại xe (Số/Ga: 500k; PKL: 1tr) × quantity
                        BigDecimal deposit = calcDepositForOrder(con, r.getOrderId());
                        r.setDeposit(deposit);

                        // ✅ 30% tổng tiền
                        BigDecimal thirty = total.multiply(new BigDecimal("0.30"))
                                .setScale(0, RoundingMode.HALF_UP);
                        r.setThirtyPct(thirty);

                        // ✅ Tổng phải trả
                        BigDecimal toPay = thirty.add(deposit);
                        r.setToPayNow(toPay);

                        grandTotal = grandTotal.add(toPay);
                        rows.add(r);
                    }
                }
            }

            if (rows.isEmpty()) {
                flash(req, "Không có đơn hợp lệ để thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            String ordersCsv = rows.stream()
                    .map(Row::getOrderId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            req.setAttribute("rows", rows);
            req.setAttribute("grandTotal", grandTotal);
            req.setAttribute("ordersCsv", ordersCsv);
            // Lấy customerId để query số dư ví
            Integer customerId = getCustomerIdByAccount(acc.getAccountId());
            if (customerId == null) {
                flash(req, "Không tìm thấy thông tin khách hàng.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            // Lấy số dư ví thực tế
            BigDecimal walletBalance = getWalletBalance(customerId);
            req.setAttribute("walletBalance", walletBalance);
            req.setAttribute("qrAccountNo", "0916134642");
            req.setAttribute("qrAccountName", "Cua Hang RideNow");
            req.setAttribute("qrAddInfo", "RN" + ordersCsv + " " + System.currentTimeMillis());

            req.getRequestDispatcher("/cart/paynow.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            flash(req, "Lỗi khi tải trang thanh toán: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    /* ====================== POST: khách bấm “Tôi đã chuyển khoản” ====================== */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        if (orderIds.isEmpty()) {
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        String paymentMethod = req.getParameter("paymentMethod"); // "transfer" | "wallet" | "wallet_transfer" | ...
        if (paymentMethod == null) paymentMethod = "transfer";

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1) Tính số tiền cần trả cho từng đơn = 30% + cọc (chỉ đơn 'pending' của chính user)
                Map<Integer, BigDecimal> payMap = getPayableOrders(con, orderIds, acc.getAccountId());
                if (payMap.isEmpty()) {
                    flash(req, "Các đơn đã được xử lý hoặc không hợp lệ.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                // 2) Ghi Payments (status='paid') cho từng đơn với số tiền chính xác
                insertPaidPayments(con, payMap, paymentMethod);

                // 3) (Dưới DB bạn đã có trigger đẩy order → confirmed khi payment 'paid')
                //    Nhưng vẫn cập nhật idempotent để chắc chắn đơn ở trạng thái 'confirmed'
                setOrdersConfirmed(con, payMap.keySet());

                // 4) ✅ Khóa xe: nếu HÔM NAY nằm trong [start_date, end_date] của các đơn vừa xác nhận
                lockBikesForActiveDates(con, payMap.keySet());

                // (Tuỳ chọn) Mở lại xe hết kỳ thuê vào 'available'
                // unlockBikesNoActiveToday(con);

                con.commit();

                // 5) Gửi mail xác nhận
                try {
                    sendOrderConfirmedEmail(con, acc.getAccountId(), payMap.keySet());
                } catch (Exception mailEx) {
                    mailEx.printStackTrace();
                }

                flash(req, "✅ Thanh toán thành công! Đơn hàng đã được XÁC NHẬN.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");

            } catch (Exception ex) {
                con.rollback();
                ex.printStackTrace();
                flash(req, "Lỗi khi xử lý thanh toán: " + ex.getMessage());
                resp.sendRedirect(req.getContextPath() + "/customerorders");
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            flash(req, "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    /* ================== Cọc theo loại xe (không đụng schema) ================== */
    /**
     * Tính tổng cọc cho 1 order:
     *  - 'Phân khối lớn'  -> 1,000,000/xe
     *  - 'Xe số' hoặc 'Xe ga' -> 500,000/xe
     *  × quantity
     */
    private BigDecimal calcDepositForOrder(Connection con, int orderId) throws SQLException {
        String sql = """
            SELECT SUM(
                     CASE 
                       WHEN bt.type_name = N'Phân khối lớn' THEN 1000000
                       WHEN bt.type_name = N'Xe số' OR bt.type_name = N'Xe ga' THEN 500000
                       ELSE 500000
                     END * d.quantity
                   ) AS deposit
            FROM OrderDetails d
            JOIN Motorbikes m ON m.bike_id = d.bike_id
            JOIN BikeTypes  bt ON bt.type_id = m.type_id
            WHERE d.order_id = ?
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal dep = rs.getBigDecimal("deposit");
                    return dep != null ? dep : BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /* ================== Payments & Confirm ================== */

    /** Ghi payments = 'paid' cho từng order đúng số tiền (30%+cọc), map phương thức hợp lệ với schema */
    private void insertPaidPayments(Connection con,
                                    Map<Integer, BigDecimal> payMap,
                                    String uiMethod) throws SQLException {

        if (payMap == null || payMap.isEmpty()) return;

        String method = ("transfer".equalsIgnoreCase(uiMethod) || "wallet_transfer".equalsIgnoreCase(uiMethod))
                ? "bank_transfer" : "cash"; // schema: 'cash' | 'bank_transfer'

        String sql = """
            INSERT INTO Payments (order_id, amount, method, status, payment_date)
            VALUES (?, ?, ?, 'paid', GETDATE())
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (Map.Entry<Integer, BigDecimal> e : payMap.entrySet()) {
                ps.setInt(1, e.getKey());
                ps.setBigDecimal(2, e.getValue().setScale(0, RoundingMode.HALF_UP));
                ps.setString(3, method);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Đổi các order → confirmed (idempotent) */
    private void setOrdersConfirmed(Connection con, Set<Integer> orderIds) throws SQLException {
        if (orderIds == null || orderIds.isEmpty()) return;
        String ids = orderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String upd = "UPDATE RentalOrders SET status='confirmed' WHERE status='pending' AND order_id IN (" + ids + ")";
        try (PreparedStatement ps = con.prepareStatement(upd)) {
            ps.executeUpdate();
        }
        // Các cờ phụ nếu có cột (không bắt buộc, ignore nếu không tồn tại)
        safeExec(con, "UPDATE RentalOrders SET pickup_status='not_picked' WHERE order_id IN (" + ids + ") AND pickup_status IS NULL");
        safeExec(con, "UPDATE RentalOrders SET return_status='none'      WHERE order_id IN (" + ids + ") AND return_status IS NULL");
    }

    private void safeExec(Connection con, String sql) {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception ignore) {
            // bỏ qua nếu cột không tồn tại
        }
    }

    /** ✅ Khóa xe thành 'rented' nếu HÔM NAY nằm trong kỳ thuê của các đơn confirmed vừa xử lý */

    private void lockBikesForActiveDates(Connection con, java.util.Set<Integer> orderIds) throws SQLException {
        if (orderIds == null || orderIds.isEmpty()) return;

        String ids = orderIds.stream()
                .map(String::valueOf)
                .collect(java.util.stream.Collectors.joining(","));

        String sql = """
            UPDATE m
            SET m.status = 'rented'
            FROM Motorbikes m
            WHERE EXISTS (
                SELECT 1
                FROM OrderDetails d
                JOIN RentalOrders r ON r.order_id = d.order_id
                WHERE d.bike_id = m.bike_id
                  AND r.status = 'confirmed'
                  AND CAST(GETDATE() AS DATE) BETWEEN r.start_date AND r.end_date
                  AND r.order_id IN (%s)
            )
            """.formatted(ids);  // << chèn biến đúng cách

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }


    /** (Tuỳ chọn) Trả xe về 'available' nếu hôm nay KHÔNG còn đơn confirmed nào bao phủ */
    private void unlockBikesNoActiveToday(Connection con) {
        String sql = """
            UPDATE m
            SET m.status = 'available'
            FROM Motorbikes m
            WHERE m.status = 'rented'
              AND NOT EXISTS (
                  SELECT 1
                  FROM OrderDetails d
                  JOIN RentalOrders r ON r.order_id = d.order_id
                  WHERE d.bike_id = m.bike_id
                    AND r.status = 'confirmed'
                    AND CAST(GETDATE() AS DATE) BETWEEN r.start_date AND r.end_date
              )
        """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        } catch (Exception ignore) {}
    }

    /* ================== Email ================== */

    private void sendOrderConfirmedEmail(Connection con, int accountId, Set<Integer> orderIds) throws SQLException {
        if (orderIds == null || orderIds.isEmpty()) return;

        String fullName = "Quý khách";
        String email = null;
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT full_name, email FROM Customers WHERE account_id = ?")) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    fullName = rs.getString("full_name");
                    email = rs.getString("email");
                }
            }
        }
        if (email == null || email.isBlank()) return;

        String ids = orderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
        String ordersSql = """
            SELECT r.order_id, r.start_date, r.end_date, r.total_price,
                   COALESCE(
                       STUFF((SELECT N', ' + b2.bike_name
                              FROM OrderDetails d2
                              JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id
                              WHERE d2.order_id = r.order_id
                              FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,''), N'Unknown Bike'
                   ) AS bike_name
            FROM RentalOrders r
            WHERE r.order_id IN (""" + ids + ")";

        List<String> lines = new ArrayList<>();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (PreparedStatement ps = con.prepareStatement(ordersSql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int oid = rs.getInt("order_id");
                java.sql.Date s = rs.getDate("start_date");
                java.sql.Date e = rs.getDate("end_date");
                BigDecimal total = safe(rs.getBigDecimal("total_price"));
                String bikeName = rs.getString("bike_name");
                lines.add("""
                    <tr>
                      <td style="padding:8px 12px">#%d</td>
                      <td style="padding:8px 12px">%s</td>
                      <td style="padding:8px 12px">%s → %s</td>
                      <td style="padding:8px 12px; text-align:right">%s đ</td>
                    </tr>
                """.formatted(oid, escape(bikeName),
                        s.toLocalDate().format(df), e.toLocalDate().format(df),
                        formatVND(total)));
            }
        }

        String html = """
            <div style="font-family:Segoe UI,Arial,sans-serif; color:#111; line-height:1.6">
              <h2 style="color:#2563eb">RideNow - Xác nhận đặt xe thành công</h2>
              <p>Xin chào <strong>%s</strong>,</p>
              <p>Chúng tôi đã nhận thanh toán của bạn và <strong>xác nhận</strong> các đơn dưới đây:</p>
              <table style="width:100%%; border-collapse:collapse; border:1px solid #e5e7eb">
                <thead>
                  <tr style="background:#f3f4f6">
                    <th style="padding:8px 12px; text-align:left">Mã đơn</th>
                    <th style="padding:8px 12px; text-align:left">Xe</th>
                    <th style="padding:8px 12px; text-align:left">Thời gian thuê</th>
                    <th style="padding:8px 12px; text-align:right">Tổng tiền</th>
                  </tr>
                </thead>
                <tbody>%s</tbody>
              </table>
              <p style="margin-top:16px">Vui lòng đến cửa hàng vào ngày nhận xe để hoàn tất thủ tục.</p>
              <p>Nếu cần hỗ trợ, bạn có thể phản hồi email này.</p>
              <hr style="border:none;border-top:1px solid #e5e7eb; margin:16px 0" />
              <p style="font-size:12px; color:#6b7280">Cảm ơn bạn đã tin tưởng RideNow.</p>
            </div>
        """.formatted(escape(fullName), String.join("", lines));

        EmailUtil.sendMailHTML(email, "[RideNow] Xác nhận đặt xe thành công", html);
    }

    /* ================== Helper Queries ================== */

    /**
     * Tính số tiền phải trả của từng đơn (30% + cọc) cho chính user,
     * chỉ lấy đơn đang 'pending'.
     */
    private Map<Integer, BigDecimal> getPayableOrders(Connection con, List<Integer> orderIds, int accountId)
            throws SQLException {
        Map<Integer, BigDecimal> payMap = new LinkedHashMap<>();

        String qs = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
        String check = """
            SELECT r.order_id, r.total_price
            FROM RentalOrders r
            JOIN Customers c ON c.customer_id = r.customer_id
            JOIN Accounts  a ON a.account_id = c.account_id
            WHERE r.order_id IN (%s)
              AND a.account_id = ?
              AND r.status = 'pending'
        """.formatted(qs);

        try (PreparedStatement ps = con.prepareStatement(check)) {
            int i = 1;
            for (Integer id : orderIds) ps.setInt(i++, id);
            ps.setInt(i, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int oid = rs.getInt("order_id");
                    BigDecimal total = safe(rs.getBigDecimal("total_price"));
                    BigDecimal thirty = total.multiply(new BigDecimal("0.30")).setScale(0, RoundingMode.HALF_UP);
                    BigDecimal deposit = calcDepositForOrder(con, oid);
                    BigDecimal need = thirty.add(deposit);
                    payMap.put(oid, need);
                }
            }
        }
        return payMap;
    }

    /* ================== Utils ================== */

    private static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        for (String s : csv.split(",")) {
            try { ids.add(Integer.parseInt(s.trim())); } catch (Exception ignored) {}
        }
        return ids;
    }

    private static BigDecimal safe(BigDecimal v){ return v==null?BigDecimal.ZERO:v; }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
    private static String formatVND(BigDecimal v) { return String.format("%,.0f", v); }

    private static void flash(HttpServletRequest req, String msg){
        req.getSession().setAttribute("flash", msg);
    }
    
    private BigDecimal getWalletBalance(int customerId) throws ServletException {
        String sql = "SELECT balance FROM Wallets WHERE customer_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getBigDecimal("balance") : BigDecimal.ZERO;
        } catch (Exception e) {
            // Nếu chưa có ví, trả về 0
            return BigDecimal.ZERO;
        }
    }
    
    
    private Integer getCustomerIdByAccount(int accountId) throws ServletException {
        String sql = "SELECT customer_id FROM Customers WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("customer_id") : null;
        } catch (Exception e) {
            throw new ServletException("Không lấy được customer_id", e);
        }
    }
    
}
