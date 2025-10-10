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
        private String paymentMethod;

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
        
        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        // ==== Các thuộc tính tính toán cho JSP ====
        /** Được phép tick để thanh toán? */
        public boolean isCanSelectForPay() {
            return "pending".equalsIgnoreCase(status) && !hasPendingPayment && !paymentSubmitted;
        }

        /** Hiển thị nút Hủy? */
        public boolean isCanCancel() {
            return "pending".equalsIgnoreCase(status) && !hasPendingPayment && !paymentSubmitted;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("🔍 DEBUG MyOrdersServlet - doGet called");
        
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { 
            System.out.println("❌ No account found, redirecting to login");
            resp.sendRedirect(req.getContextPath()+"/login.jsp"); 
            return; 
        }

        try {
            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) { 
                System.out.println("❌ No customer profile found");
                resp.sendRedirect(req.getContextPath()+"/customer/profile.jsp?need=1"); 
                return; 
            }

            System.out.println("✅ Loading orders for customer: " + c.getCustomerId());
            
            // SỬA: Sử dụng query mới với validation tốt hơn
            List<Object[]> rows = getOrdersWithPaymentStatus(c.getCustomerId());

            // Map sang OrderVM để JSP dùng thuộc tính đọc dễ hơn
            List<OrderVM> ordersVm = new ArrayList<>();
            boolean hasPendingPayments = false;

            for (Object[] r : rows) {
                if (r == null || r.length < 6) {
                    System.out.println("⚠️ Skipping invalid row: " + (r == null ? "null" : "length=" + r.length));
                    continue;
                }
                
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
                    
                    // SỬA: Xử lý payment method
                    String paymentMethod = "";
                    if (r.length > 7 && r[7] != null) {
                        paymentMethod = r[7].toString();
                    }
                    vm.setPaymentMethod(paymentMethod);
                    
                    boolean submitted = false;
                    if (r.length > 8 && r[8] != null) {
                        if (r[8] instanceof Boolean) submitted = (Boolean) r[8];
                        else if (r[8] instanceof Number) submitted = ((Number) r[8]).intValue() != 0;
                        else submitted = Boolean.parseBoolean(r[8].toString());
                    }
                    vm.setPaymentSubmitted(submitted);

                    if (pendingPay || submitted) hasPendingPayments = true;

                    ordersVm.add(vm);
                    
                    System.out.println("📦 Order #" + vm.getOrderId() + " - Status: " + vm.getStatus() + 
                                     ", PendingPay: " + vm.isHasPendingPayment() + 
                                     ", Submitted: " + vm.isPaymentSubmitted() +
                                     ", Method: " + vm.getPaymentMethod());
                } catch (Exception e) {
                    System.err.println("❌ Error processing order row: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            req.setAttribute("ordersVm", ordersVm);
            req.setAttribute("hasPendingPayments", hasPendingPayments);
            req.setAttribute("rows", rows);

            System.out.println("✅ Loaded " + ordersVm.size() + " orders, hasPendingPayments: " + hasPendingPayments);
            
            req.getRequestDispatcher("/customer/my-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            System.err.println("❌ ERROR in MyOrdersServlet doGet: " + e.getMessage());
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        System.out.println("🔍 DEBUG MyOrdersServlet - doPost called");
        
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            System.out.println("❌ No account in session");
            resp.sendRedirect(req.getContextPath()+"/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        System.out.println("📝 Action parameter: " + action);
        
        if ("cancel".equals(action)) {
            cancelOrder(req, resp, acc);
        } else {
            System.out.println("❌ Unknown action: " + action);
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }

    /**
     * SỬA: Query mới với validation tốt hơn
     */
    private List<Object[]> getOrdersWithPaymentStatus(int customerId) {
        try {
            // Sử dụng DAO hiện có, nhưng thêm logging
            List<Object[]> results = qdao.findOrdersOfCustomerWithPaymentStatus(customerId);
            System.out.println("📊 Query returned " + (results != null ? results.size() : "null") + " orders");
            
            if (results != null) {
                for (int i = 0; i < results.size(); i++) {
                    Object[] row = results.get(i);
                    if (row != null) {
                        System.out.println("📋 Row " + i + ": " + java.util.Arrays.toString(row));
                    }
                }
            }
            
            return results != null ? results : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("❌ Error in getOrdersWithPaymentStatus: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void cancelOrder(HttpServletRequest req, HttpServletResponse resp, Account acc)
            throws ServletException, IOException {

        System.out.println("🚨 CANCEL ORDER REQUEST 🚨");
        
        String orderIdParam = req.getParameter("orderId");
        System.out.println("📝 Order ID parameter: " + orderIdParam);
        
        if (orderIdParam == null || orderIdParam.trim().isEmpty()) {
            System.out.println("❌ Order ID parameter is missing or empty");
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(orderIdParam);
            System.out.println("🔄 Processing cancel for order #" + orderId);

            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) {
                System.out.println("❌ Customer profile not found for account: " + acc.getAccountId());
                resp.sendRedirect(req.getContextPath()+"/customer/profile.jsp?need=1");
                return;
            }

            System.out.println("🔄 Calling customerService.cancelOrder for customer: " + c.getCustomerId() + ", order: " + orderId);
            boolean success = customerService.cancelOrder(c.getCustomerId(), orderId);

            if (success) {
                System.out.println("✅ SUCCESS: Cancelled order #" + orderId);
                req.getSession().setAttribute("flash", "Đã hủy đơn hàng #" + orderId + " thành công.");
            } else {
                System.out.println("❌ FAILED: Could not cancel order #" + orderId);
                req.getSession().setAttribute("flash", "Hủy đơn hàng thất bại. Đơn hàng không tồn tại hoặc không thể hủy.");
            }

            System.out.println("🔄 Redirecting to customerorders page");
            resp.sendRedirect(req.getContextPath() + "/customerorders");

        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid orderId format: " + orderIdParam);
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        } catch (Exception e) {
            System.err.println("❌ ERROR in cancelOrder: " + e.getMessage());
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi hệ thống: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }
}