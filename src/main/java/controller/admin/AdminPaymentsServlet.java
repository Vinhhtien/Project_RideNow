// src/main/java/controller/AdminPaymentsServlet.java
package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IPaymentService;
import service.PaymentService;
import utils.DBConnection;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="AdminPaymentsServlet",
        urlPatterns = {"/admin/payments", "/adminpayments"})
public class AdminPaymentsServlet extends HttpServlet {

    private final IPaymentService paymentService = new PaymentService();

    /** Dòng hiển thị ở JSP */
    public static class Row {
        private int paymentId;
        private int orderId;
        private String customerName;
        private BigDecimal amount;
        private String method;
        private String status;
        private Timestamp createdAt;

        public int getPaymentId() { return paymentId; }
        public void setPaymentId(int paymentId) { this.paymentId = paymentId; }
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO: kiểm tra role admin nếu cần
        // Account acc = (Account) req.getSession().getAttribute("account");
        // if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) { resp.sendError(403); return; }

        String sql = """
            SELECT p.payment_id, p.order_id, p.amount, p.method, p.status, p.payment_date,
                   c.full_name
            FROM Payments p
            JOIN RentalOrders r ON r.order_id = p.order_id
            JOIN Customers c    ON c.customer_id = r.customer_id
            WHERE p.status = 'pending'
            ORDER BY p.payment_id DESC
        """;

        List<Row> rows = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Row r = new Row();
                r.setPaymentId(rs.getInt("payment_id"));
                r.setOrderId(rs.getInt("order_id"));
                r.setAmount(rs.getBigDecimal("amount"));
                r.setMethod(rs.getString("method"));
                r.setStatus(rs.getString("status"));
                r.setCreatedAt(rs.getTimestamp("payment_date"));
                r.setCustomerName(rs.getString("full_name"));
                rows.add(r);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

        req.setAttribute("rows", rows);
        req.getRequestDispatcher("/admin/payments.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // action=markPaid&paymentId=...
        String action = req.getParameter("action");
        if ("markPaid".equals(action)) {
            int pid = Integer.parseInt(req.getParameter("paymentId"));
            try {
                boolean ok = paymentService.markPaid(pid);
                req.getSession().setAttribute("flash",
                        ok ? "Đã xác nhận thanh toán. Đơn sẽ chuyển sang 'confirmed'."
                           : "Không thể xác nhận (có thể đã ở trạng thái 'paid').");
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        resp.sendRedirect(req.getContextPath() + "/admin/payments");
    }
}
