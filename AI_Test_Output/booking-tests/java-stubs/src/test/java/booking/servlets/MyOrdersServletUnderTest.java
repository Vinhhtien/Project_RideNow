package booking.servlets;

import booking.stubs.dao.IOrderQueryDao;
import booking.stubs.model.Account;
import booking.stubs.model.Customer;
import booking.stubs.service.ICustomerService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class MyOrdersServletUnderTest extends HttpServlet {
    private final IOrderQueryDao qdao;
    private final ICustomerService customerService;

    public MyOrdersServletUnderTest(IOrderQueryDao qdao, ICustomerService customerService) {
        this.qdao = qdao;
        this.customerService = customerService;
    }

    public static class OrderVM {
        private int orderId;
        private String bikeName;
        private Date start;
        private Date end;
        private BigDecimal total;
        private String status;
        private boolean hasPendingPayment;
        private boolean paymentSubmitted;
        private String paymentMethod;

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
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
        public boolean isCanSelectForPay() { return "pending".equalsIgnoreCase(status); }
        public boolean isCanCancel() { return "pending".equalsIgnoreCase(status); }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath()+"/login.jsp");
            return;
        }
        try {
            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) {
                resp.sendRedirect(req.getContextPath()+"/customer/profile.jsp?need=1");
                return;
            }
            List<Object[]> rows = getOrdersWithPaymentStatus(c.getCustomerId());
            List<OrderVM> ordersVm = new ArrayList<>();
            for (Object[] r : rows) {
                if (r == null || r.length < 6) continue;
                OrderVM vm = new OrderVM();
                try {
                    vm.setOrderId((Integer) r[0]);
                    vm.setBikeName((String) r[1]);
                    vm.setStart((Date) r[2]);
                    vm.setEnd((Date) r[3]);
                    vm.setTotal((BigDecimal) r[4]);
                    vm.setStatus((String) r[5]);
                    boolean pendingPay = false;
                    if (r.length > 6 && r[6] != null) {
                        if (r[6] instanceof Boolean) pendingPay = (Boolean) r[6];
                        else if (r[6] instanceof Number) pendingPay = ((Number) r[6]).intValue() != 0;
                        else pendingPay = Boolean.parseBoolean(r[6].toString());
                    }
                    vm.setHasPendingPayment(pendingPay);
                    String paymentMethod = "";
                    if (r.length > 7 && r[7] != null) paymentMethod = r[7].toString();
                    vm.setPaymentMethod(paymentMethod);
                    boolean submitted = false;
                    if (r.length > 8 && r[8] != null) {
                        if (r[8] instanceof Boolean) submitted = (Boolean) r[8];
                        else if (r[8] instanceof Number) submitted = ((Number) r[8]).intValue() != 0;
                        else submitted = Boolean.parseBoolean(r[8].toString());
                    }
                    vm.setPaymentSubmitted(submitted);
                    ordersVm.add(vm);
                } catch (Exception ignored) {}
            }
            req.setAttribute("ordersVm", ordersVm);
            req.setAttribute("hasPendingPayments", false);
            req.setAttribute("rows", rows);
            RequestDispatcher rd = req.getRequestDispatcher("/customer/my-orders.jsp");
            rd.forward(req, resp);
        } catch (Exception e) {
            req.getSession().setAttribute("flash", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    // Bridge for tests
    public void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }

    private List<Object[]> getOrdersWithPaymentStatus(int customerId) {
        try {
            List<Object[]> results = qdao.findOrdersOfCustomerWithPaymentStatus(customerId);
            return results != null ? results : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
