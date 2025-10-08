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

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }
        if (!"customer".equalsIgnoreCase(acc.getRole())) { resp.sendRedirect(req.getContextPath() + "/"); return; }

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
            // MỖI ĐƠN 1 DÒNG, gộp tên xe, dùng total_price của đơn
//            String sql = ("""
//                SELECT r.order_id,
//                       STUFF((SELECT ', ' + b2.bike_name
//                              FROM OrderDetails d2
//                              JOIN Motorbikes b2 ON b2.bike_id = d2.bike_id
//                              WHERE d2.order_id = r.order_id
//                              FOR XML PATH(''), TYPE).value('.','NVARCHAR(MAX)'),1,2,'') AS bikes,
//                       r.start_date, r.end_date,
//                       r.total_price,
//                       ISNULL(r.deposit_amount,0) AS deposit_amount,
//                       CASE WHEN EXISTS (SELECT 1 FROM Payments p WHERE p.order_id=r.order_id AND p.status='pending')
//                                 OR r.payment_submitted=1
//                            THEN 1 ELSE 0 END AS has_pending
//                  FROM RentalOrders r
//                  JOIN Customers c ON c.customer_id = r.customer_id
//                  JOIN Accounts  a ON a.account_id  = c.account_id
//                 WHERE r.order_id IN (%s)
//                   AND a.account_id = ?
//                   AND r.status = 'pending'
//            """).formatted(qs);

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
                       r.status  -- ← THÊM status để kiểm tra
                  FROM RentalOrders r
                  JOIN Customers c ON c.customer_id = r.customer_id
                  JOIN Accounts  a ON a.account_id  = c.account_id
                 WHERE r.order_id IN (%s)
                   AND a.account_id = ?
                -- BỎ điều kiện r.status = 'pending' để xem tất cả đơn được chọn
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

                        BigDecimal thirty = total.multiply(new BigDecimal("0.30")); // làm tròn nếu muốn
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
                flash(req, "Không có đơn hợp lệ để thanh toán (đơn đã gửi xác minh / không còn pending).");
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

            // QR info (demo)
            req.setAttribute("rows", rows);
            req.setAttribute("grandTotal", grandTotal);
            req.setAttribute("ordersCsv", ordersCsv);
            req.setAttribute("hasPendingPayment", hasPending);     // ← bổ sung cho JSP
            req.setAttribute("qrAccountNo",   "0916134642");
            req.setAttribute("qrAccountName", "Cua Hang RideNow");
            req.setAttribute("qrAddInfo",     "RN " + ordersCsv);

            // CHỌN ĐÚNG ĐƯỜNG DẪN TỚI FILE JSP CỦA BẠN
            //req.getRequestDispatcher("/paynow.jsp").forward(req, resp);
             req.getRequestDispatcher("/cart/paynow.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

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
                // Chốt lại đơn đủ điều kiện
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

                // tạo payment pending + mark submitted
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

    private static List<Integer> parseIds(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        for (String s : csv.split(",")) { try { ids.add(Integer.parseInt(s.trim())); } catch (Exception ignore) {} }
        return ids;
    }
    private static BigDecimal safe(BigDecimal v){ return v==null?BigDecimal.ZERO:v; }
    private static void flash(HttpServletRequest req, String msg){ req.getSession().setAttribute("flash", msg); }
}
