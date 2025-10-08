// MyOrdersServlet.java
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import dao.IOrderQueryDao;
import dao.OrderQueryDao;
import model.Account;
import model.Customer;
import service.CustomerService;
import service.ICustomerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name="MyOrdersServlet", urlPatterns={"/customerorders"})
public class MyOrdersServlet extends HttpServlet {

    private final IOrderQueryDao qdao = new OrderQueryDao();
    private final ICustomerService customerService = new CustomerService();

    /** View model cho 1 dòng đơn hàng */
    public static class OrderVM {
        private int orderId;
        private String bikeName;
        private Date start;
        private Date end;
        private BigDecimal total;
        private String status; // pending|confirmed|completed|cancelled
        private boolean hasPendingPayment;   // có payment ở trạng thái 'pending'
        private boolean paymentSubmitted;    // cờ r.order.payment_submitted (đã bấm "tôi đã chuyển")

        // ==== Getters/Setters tiêu chuẩn ====
        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }

        public String getBikeName() { return bikeName; }
        public void setBikeName(String bikeName) { this.bikeName = bikeName; }

        public Date getStart() { return start; }
        public void setStart(Date start) { this.start = start; }

        public Date getEnd() { return end; }
        public void setEnd(Date end) { this.end = end; }

        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public boolean isHasPendingPayment() { return hasPendingPayment; }
        public void setHasPendingPayment(boolean hasPendingPayment) { this.hasPendingPayment = hasPendingPayment; }

        public boolean isPaymentSubmitted() { return paymentSubmitted; }
        public void setPaymentSubmitted(boolean paymentSubmitted) { this.paymentSubmitted = paymentSubmitted; }

        // ==== Các thuộc tính tính toán cho JSP ====
        /** Được phép tick để thanh toán? */
        public boolean isCanSelectForPay() {
            return "pending".equalsIgnoreCase(status) && !hasPendingPayment && !paymentSubmitted;
        }

        /** Hiển thị nút Hủy? */
        public boolean isCanCancel() {
            return "pending".equalsIgnoreCase(status) && !hasPendingPayment; // đã gửi xác minh thì không cho hủy ở đây
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath()+"/login.jsp"); return; }

        try {
            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) { resp.sendRedirect(req.getContextPath()+"/customer/profile.jsp?need=1"); return; }

            // rows: [order_id, bike_name, start, end, total, status, has_pending_payment, payment_submitted]
            List<Object[]> rows = qdao.findOrdersOfCustomerWithPaymentStatus(c.getCustomerId());

            // Map sang OrderVM để JSP dùng thuộc tính đọc dễ hơn
            List<OrderVM> ordersVm = new ArrayList<>();
            boolean hasPendingPayments = false;

            for (Object[] r : rows) {
                OrderVM vm = new OrderVM();
                vm.setOrderId((Integer) r[0]);
                vm.setBikeName((String) r[1]);
                vm.setStart((Date) r[2]);
                vm.setEnd((Date) r[3]);
                vm.setTotal((BigDecimal) r[4]);
                vm.setStatus((String) r[5]);

                boolean pendingPay = false;
                if (r.length > 6 && r[6] != null) {
                    // r[6] có thể là Boolean, Integer hoặc Number -> ép về boolean
                    if (r[6] instanceof Boolean) pendingPay = (Boolean) r[6];
                    else if (r[6] instanceof Number) pendingPay = ((Number) r[6]).intValue() != 0;
                    else pendingPay = Boolean.parseBoolean(r[6].toString());
                }
                vm.setHasPendingPayment(pendingPay);

                boolean submitted = false;
                if (r.length > 7 && r[7] != null) {
                    if (r[7] instanceof Boolean) submitted = (Boolean) r[7];
                    else if (r[7] instanceof Number) submitted = ((Number) r[7]).intValue() != 0;
                    else submitted = Boolean.parseBoolean(r[7].toString());
                }
                vm.setPaymentSubmitted(submitted);

                if (pendingPay || submitted) hasPendingPayments = true;

                ordersVm.add(vm);
            }

            req.setAttribute("ordersVm", ordersVm);
            req.setAttribute("hasPendingPayments", hasPendingPayments);

            // Fallback cho JSP cũ nếu vẫn còn dùng "rows"
            req.setAttribute("rows", rows);

            req.getRequestDispatcher("/customer/my-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Error loading orders: " + e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath()+"/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        if ("cancel".equals(action)) {
            cancelOrder(req, resp, acc);
        } else {
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp, Account acc)
            throws ServletException, IOException {

        String orderIdParam = req.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdParam);

            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) {
                resp.sendRedirect(req.getContextPath()+"/customer/profile.jsp?need=1");
                return;
            }

            boolean success = customerService.cancelOrder(c.getCustomerId(), orderId);

            if (success) {
                req.getSession().setAttribute("flash", "Đã hủy đơn hàng #" + orderId + " thành công.");
            } else {
                req.getSession().setAttribute("flash", "Hủy đơn hàng thất bại. Đơn hàng không tồn tại hoặc không thể hủy.");
            }

            resp.sendRedirect(req.getContextPath() + "/customerorders");

        } catch (NumberFormatException e) {
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }
}
