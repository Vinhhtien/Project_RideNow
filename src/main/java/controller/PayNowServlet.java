// controller/PayNowServlet.java
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import service.IPaymentService;
import service.PaymentService;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "PayNowServlet", urlPatterns = {"/paynow"})
public class PayNowServlet extends HttpServlet {

    private final IPaymentService paymentService = new PaymentService();

    public static class Row {
        private int orderId;
        private String bikeName;
        private java.sql.Date start;
        private java.sql.Date end;
        private BigDecimal totalPrice;     // tổng tiền thuê của đơn
        private BigDecimal deposit;        // tiền cọc ở đơn
        private BigDecimal thirtyPct;      // 30% của totalPrice
        private BigDecimal toPayNow;       // thirtyPct + deposit
        private String    paymentStatus;   // "pending" | "none"
        private String status;

        // getters/setters...
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
        public String getPaymentStatus() { return paymentStatus; }
        public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String action = req.getParameter("action");
        
        // XỬ LÝ HỦY ĐƠN HÀNG
        if ("cancel".equals(action)) {
            handleCancelOrder(req, resp);
            return;
        }

        // PHẦN XỬ LÝ THANH TOÁN BAN ĐẦU
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { 
            resp.sendRedirect(req.getContextPath() + "/"); 
            return; 
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        
        System.out.println("DEBUG PayNowServlet - orderIds received: " + orderIds);
        
        if (orderIds.isEmpty()) {
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }
        orderIds = orderIds.stream().distinct().collect(Collectors.toList());

        try (Connection con = DBConnection.getConnection()) {
            String qs = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
            
            String sql = ("""
                SELECT r.order_id,
                       STUFF((SELECT ', ' + b2.bike_name
                              FROM OrderDetails d2
                              JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id
                              WHERE d2.order_id = r.order_id
                              FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,'') AS bikes,
                       r.start_date, r.end_date,
                       r.total_price,
                       ISNULL(r.deposit_amount,0) AS deposit_amount,
                       CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.order_id=r.order_id AND p.status='pending')
                                 OR r.payment_submitted=1
                            THEN 1 ELSE 0 END AS has_pending,
                       r.status
                  FROM RentalOrders r
                  JOIN Customers c ON c.customer_id = r.customer_id
                  JOIN Accounts  a ON a.account_id  = c.account_id
                 WHERE r.order_id IN (%s)
                   AND a.account_id = ?
            """).formatted(qs);

            List<Row> rows = new ArrayList<>();
            boolean hasPending = false;
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                int i = 1;
                for (Integer id : orderIds) ps.setInt(i++, id);
                ps.setInt(i, acc.getAccountId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Row r = new Row();
                        r.setOrderId(rs.getInt("order_id"));
                        r.setBikeName(rs.getString("bikes"));
                        r.setStart(rs.getDate("start_date"));
                        r.setEnd(rs.getDate("end_date"));
                        r.setStatus(rs.getString("status"));

                        BigDecimal total   = safe(rs.getBigDecimal("total_price"));
                        BigDecimal deposit = safe(rs.getBigDecimal("deposit_amount"));
                        r.setTotalPrice(total);
                        r.setDeposit(deposit);

                        BigDecimal thirty = total.multiply(new BigDecimal("0.30"));
                        r.setThirtyPct(thirty);
                        r.setToPayNow(thirty.add(deposit));

                        boolean pending = rs.getInt("has_pending") == 1;
                        r.setPaymentStatus(pending ? "pending" : "none");
                        if (pending) hasPending = true;

                        rows.add(r);
                    }
                }
            }

            if (rows.isEmpty()) {
                flash(req, "Không có đơn hợp lệ để thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            BigDecimal grandTotal = rows.stream()
                    .map(Row::getToPayNow)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String ordersCsv = rows.stream()
                    .map(Row::getOrderId)
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            req.setAttribute("rows", rows);
            req.setAttribute("grandTotal", grandTotal);
            req.setAttribute("ordersCsv", ordersCsv);
            req.setAttribute("hasPendingPayment", hasPending);
            req.setAttribute("qrAccountNo",   "0916134642");
            req.setAttribute("qrAccountName", "Cua Hang RideNow");
            req.setAttribute("qrAddInfo",     "RN " + ordersCsv);

            req.getRequestDispatcher("/cart/paynow.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }

        List<Integer> orderIds = parseIds(req.getParameter("orders"));
        if (orderIds.isEmpty()) {
            flash(req, "Vui lòng chọn đơn để thanh toán.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }
        orderIds = orderIds.stream().distinct().collect(Collectors.toList());

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                String qs = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
                String check = ("""
                    SELECT r.order_id, r.total_price, ISNULL(r.deposit_amount,0) AS deposit
                      FROM RentalOrders r
                      JOIN Customers c ON c.customer_id=r.customer_id
                      JOIN Accounts  a ON a.account_id=c.account_id
                 LEFT JOIN Payments p ON p.order_id=r.order_id AND p.status='pending'
                     WHERE r.order_id IN (%s)
                       AND a.account_id=?
                       AND r.status='pending'
                       AND r.payment_submitted=0
                       AND p.payment_id IS NULL
                """).formatted(qs);

                Map<Integer, BigDecimal> payMap = new LinkedHashMap<>();
                try (PreparedStatement ps = con.prepareStatement(check)) {
                    int i=1; for (Integer id: orderIds) ps.setInt(i++, id);
                    ps.setInt(i, acc.getAccountId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int oid = rs.getInt("order_id");
                            BigDecimal total   = safe(rs.getBigDecimal("total_price"));
                            BigDecimal deposit = safe(rs.getBigDecimal("deposit"));
                            BigDecimal need = total.multiply(new BigDecimal("0.30")).add(deposit);
                            payMap.put(oid, need);
                        }
                    }
                }
                if (payMap.isEmpty()) {
                    con.rollback();
                    flash(req, "Các đơn đã được gửi hoặc không còn trạng thái cho phép.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                for (Map.Entry<Integer, BigDecimal> e : payMap.entrySet()) {
                    String ref = "RN-" + e.getKey() + "-" + (System.currentTimeMillis() % 100000);
                    paymentService.createPendingForOrder(e.getKey(), e.getValue(), ref);
                }

                String upd = "UPDATE RentalOrders SET payment_submitted=1 WHERE order_id IN (" +
                        payMap.keySet().stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
                try (PreparedStatement ps = con.prepareStatement(upd)) { ps.executeUpdate(); }

                con.commit();
                flash(req, "Đã ghi nhận 'Tôi đã chuyển khoản'. Vui lòng chờ xác minh.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * XỬ LÝ HỦY ĐƠN HÀNG
     */
    private void handleCancelOrder(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            resp.sendRedirect(req.getContextPath() + "/login"); 
            return; 
        }

        // SỬA LỖI: Dùng orderId thay vì orderld
        String orderIdStr = req.getParameter("orderId");
        if (orderIdStr == null) {
            // Fallback: kiểm tra cả tham số cũ (nếu có)
            orderIdStr = req.getParameter("orderld");
        }
        
        if (orderIdStr == null || orderIdStr.trim().isEmpty()) {
            flash(req, "Không tìm thấy đơn hàng để hủy.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdStr.trim());
            
            // KIỂM TRA VÀ HỦY ĐƠN HÀNG
            boolean cancelSuccess = cancelOrder(orderId, acc.getAccountId());
            
            if (cancelSuccess) {
                flash(req, "Đã hủy đơn hàng #" + orderId + " thành công.");
            } else {
                flash(req, "Không thể hủy đơn hàng #" + orderId + ". Đơn hàng không tồn tại hoặc không thuộc quyền sở hữu của bạn.");
            }
            
        } catch (NumberFormatException e) {
            flash(req, "Mã đơn hàng không hợp lệ.");
        } catch (Exception e) {
            flash(req, "Có lỗi xảy ra khi hủy đơn hàng.");
            e.printStackTrace();
        }
        
        resp.sendRedirect(req.getContextPath() + "/customerorders");
    }

    /**
     * HỦY ĐƠN HÀNG TRONG DATABASE
     */
    private boolean cancelOrder(int orderId, int accountId) {
        String sql = "UPDATE RentalOrders SET status = 'cancelled' " +
                     "WHERE order_id = ? " +
                     "AND customer_id IN (SELECT customer_id FROM Customers WHERE account_id = ?) " +
                     "AND status = 'pending'";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        for (String s : csv.split(",")) { 
            try { ids.add(Integer.parseInt(s.trim())); } 
            catch (Exception ignore) {} 
        }
        return ids;
    }
    
    private static BigDecimal safe(BigDecimal v){ 
        return v==null?BigDecimal.ZERO:v; 
    }
    
    private static void flash(HttpServletRequest req, String msg){ 
        req.getSession().setAttribute("flash", msg); 
    }
}