package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import dao.IOrderQueryDao;
import dao.OrderQueryDao;
import jakarta.servlet.RequestDispatcher;
import model.Account;
import model.Customer;
import model.Review;
import service.CustomerService;
import service.ICustomerService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import dao.IReviewDao;
import dao.ReviewDao;

@WebServlet(name = "MyOrdersServlet", urlPatterns = {"/customerorders"})
public class MyOrdersServlet extends HttpServlet {

    private final IOrderQueryDao qdao = new OrderQueryDao();
    private final ICustomerService customerService = new CustomerService();
    private final IReviewDao reviewDao = new ReviewDao();

    // ViewModel hiển thị đơn hàng
    public static class OrderVM {
        private int orderId;
        private int bikeId;
        private String bikeName;
        private Date start;
        private Date end;
        private BigDecimal total;
        private String status;
        private boolean hasPendingPayment;
        private boolean paymentSubmitted;
        private String paymentMethod;

        // đổi đơn trong 30'
        private Timestamp confirmedAt;          // r[10]
        private Integer changeRemainingMin;     // r[11]

        // đã review đơn này chưa
        private boolean hasReview;

        public int getOrderId() { return orderId; }
        public void setOrderId(int orderId) { this.orderId = orderId; }

        public int getBikeId() { return bikeId; }
        public void setBikeId(int bikeId) { this.bikeId = bikeId; }

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

        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

        public Timestamp getConfirmedAt() { return confirmedAt; }
        public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

        public Integer getChangeRemainingMin() { return changeRemainingMin; }
        public void setChangeRemainingMin(Integer changeRemainingMin) { this.changeRemainingMin = changeRemainingMin; }

        public boolean isHasReview() { return hasReview; }
        public void setHasReview(boolean hasReview) { this.hasReview = hasReview; }

        public boolean isCanSelectForPay() {
            return "pending".equalsIgnoreCase(status);
        }

        public boolean isCanCancel() {
            return "pending".equalsIgnoreCase(status);
        }

        public boolean isCanReview() {
            return "completed".equalsIgnoreCase(status) && bikeId > 0;
        }

        // điều kiện hiển thị nút "Đổi đơn"
        public boolean isCanChange() {
            return "confirmed".equalsIgnoreCase(status)
                    && confirmedAt != null
                    && changeRemainingMin != null
                    && changeRemainingMin > 0;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        try {
            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                resp.sendRedirect(req.getContextPath() + "/customer/profile.jsp?need=1");
                return;
            }

            // Lấy danh sách đơn hàng kèm trạng thái thanh toán
            List<Object[]> rows = qdao.findOrdersOfCustomerWithPaymentStatus(customer.getCustomerId());
            List<OrderVM> ordersVm = new ArrayList<>();

            System.out.println("[MyOrdersServlet] Found " + rows.size()
                    + " orders for customer " + customer.getCustomerId());

            for (Object[] r : rows) {
                if (r == null || r.length < 6) continue;

                OrderVM vm = new OrderVM();
                vm.setOrderId((Integer) r[0]);
                vm.setBikeName((String) r[1]);
                vm.setStart((Date) r[2]);
                vm.setEnd((Date) r[3]);
                vm.setTotal((BigDecimal) r[4]);
                vm.setStatus((String) r[5]);

                if (r.length > 6 && r[6] != null)
                    vm.setHasPendingPayment(parseBoolean(r[6]));

                if (r.length > 7 && r[7] != null)
                    vm.setPaymentMethod(r[7].toString());

                if (r.length > 8 && r[8] != null)
                    vm.setPaymentSubmitted(parseBoolean(r[8]));

                // bikeId
                if (r.length > 9 && r[9] != null) {
                    vm.setBikeId(((Number) r[9]).intValue());
                } else {
                    vm.setBikeId(0);
                }

                // confirmed_at (index 10)
                if (r.length > 10 && r[10] != null) {
                    vm.setConfirmedAt((Timestamp) r[10]);
                }

                // change_remaining_min (index 11)
                if (r.length > 11 && r[11] != null) {
                    vm.setChangeRemainingMin(((Number) r[11]).intValue());
                } else {
                    vm.setChangeRemainingMin(null);
                }

                // 🔹 Kiểm tra đã review đơn này chưa (theo customer + order)
                boolean hasReview = false;
                try {
                    if (vm.isCanReview()) {
                        Review existing = reviewDao.findByCustomerAndOrder(
                                customer.getCustomerId(),
                                vm.getOrderId()
                        );
                        hasReview = (existing != null);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                vm.setHasReview(hasReview);

                ordersVm.add(vm);
            }

            req.setAttribute("ordersVm", ordersVm);

            RequestDispatcher rd = req.getRequestDispatcher("/customer/my-orders.jsp");
            if (rd != null) {
                rd.forward(req, resp);
            } else {
                resp.sendRedirect(req.getContextPath() + "/customerorders");
            }

        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");

        if ("cancel".equals(action)) {
            handleCancelOrder(req, resp, acc);
        } else if ("review".equals(action)) {
            handleRedirectToReview(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    // ========== HELPER METHODS ==========

    private void handleCancelOrder(HttpServletRequest req, HttpServletResponse resp, Account acc)
            throws IOException {

        String orderIdParam = req.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            req.getSession().setAttribute("flash",
                    "Không thể hủy đơn hàng: mã đơn không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdParam);
            Customer customer = customerService.getProfile(acc.getAccountId());

            if (customer == null) {
                resp.sendRedirect(req.getContextPath() + "/customer/profile.jsp?need=1");
                return;
            }

            boolean success = customerService.cancelOrder(customer.getCustomerId(), orderId);
            if (success) {
                req.getSession().setAttribute("flash",
                        "Đơn hàng #" + orderId + " đã được hủy thành công.");
            } else {
                req.getSession().setAttribute("flash",
                        "Không thể hủy đơn hàng. Vui lòng thử lại.");
            }

            resp.sendRedirect(req.getContextPath() + "/customerorders");
        } catch (NumberFormatException e) {
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    private void handleRedirectToReview(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        String orderId = req.getParameter("orderId");
        String bikeId = req.getParameter("bikeId");

        if (orderId == null || bikeId == null || orderId.isEmpty()
                || bikeId.isEmpty() || "0".equals(bikeId)) {
            req.getSession().setAttribute("flash",
                    "Không thể tạo đánh giá: thiếu thông tin đơn hàng hoặc xe.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        // ReviewServlet map ở /review
        resp.sendRedirect(req.getContextPath()
                + "/review?orderId=" + orderId + "&bikeId=" + bikeId);
    }

    private boolean parseBoolean(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).intValue() != 0;
        return Boolean.parseBoolean(o.toString());
    }
}
