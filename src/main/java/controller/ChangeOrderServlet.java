package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.IOrderChangeDao;
import dao.OrderChangeDao;
import java.io.IOException;
import java.math.BigDecimal;
import model.Account;
import model.ChangeOrderVM;
import java.sql.Date;

@WebServlet(name="ChangeOrderServlet", urlPatterns={"/change-order"})
public class ChangeOrderServlet extends HttpServlet {

    private final IOrderChangeDao changeDao = new OrderChangeDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("=== CHANGE ORDER SERVLET - START ===");
        System.out.println("[ChangeOrder][GET] HIT uri=" + req.getRequestURI()
            + " q=" + req.getQueryString());

        Account acc = (Account) req.getSession().getAttribute("account");
        System.out.println("[ChangeOrder] Session account: " + (acc != null ? acc.getAccountId() : "NULL"));
        
        if (acc == null) { 
            System.out.println("[ChangeOrder] no session account → redirect login");
            resp.sendRedirect(req.getContextPath()+"/login.jsp"); 
            return; 
        }

        String sid = req.getParameter("orderId");
        System.out.println("[ChangeOrder] orderId param = " + sid);
        if (sid == null) { 
            System.out.println("[ChangeOrder] No orderId parameter → redirect");
            resp.sendRedirect(req.getContextPath()+"/customerorders"); 
            return; 
        }

        try {
            int orderId = Integer.parseInt(sid);
            System.out.println("[ChangeOrder] Parsed orderId: " + orderId);
            
            System.out.println("[ChangeOrder] Calling changeDao.loadChangeOrderVM...");
            ChangeOrderVM vm = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());
            
            System.out.println("[ChangeOrder] VM result: " + (vm != null ? "FOUND" : "NULL"));
            
            // Kiểm tra vm null trước
            if (vm == null) {
                System.out.println("[ChangeOrder] VM is null - order not found or no permission");
                req.getSession().setAttribute("flash",
                    "Không tìm thấy đơn hoặc bạn không có quyền truy cập.");
                resp.sendRedirect(req.getContextPath()+"/customerorders");
                return;
            }
            
            System.out.println("[ChangeOrder] VM details - Status: " + vm.getStatus() + 
                ", BikeId: " + vm.getBikeId() +
                ", RentalDays: " + vm.getOriginalRentalDays() +
                ", RemainingMinutes: " + vm.getRemainingMinutes() +
                ", ConfirmedAt: " + vm.getConfirmedAt());
            
            // Kiểm tra thời gian
            if (!vm.isWithin30Min()) {
                System.out.println("[ChangeOrder] Outside 30min window - remaining: " + vm.getRemainingMinutes());
                req.getSession().setAttribute("flash",
                    "Đã quá 30 phút sau khi xác nhận, không thể đổi/hủy đơn.");
                resp.sendRedirect(req.getContextPath()+"/customerorders");
                return;
            }

            req.setAttribute("vm", vm);
            System.out.println("[ChangeOrder] Forwarding to /customer/change-order.jsp");
            req.getRequestDispatcher("/customer/change-order.jsp").forward(req, resp);
            
        } catch (NumberFormatException e) {
            System.out.println("[ChangeOrder] Invalid orderId format: " + sid);
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath()+"/customerorders");
        } catch (Exception e) {
            System.out.println("[ChangeOrder] Exception: " + e.getMessage());
            e.printStackTrace();
            req.getSession().setAttribute("flash","Lỗi khi tải trang đổi đơn: "+e.getMessage());
            resp.sendRedirect(req.getContextPath()+"/customerorders");
        }
        
        System.out.println("=== CHANGE ORDER SERVLET - END ===");
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
        String sid = req.getParameter("orderId");
        if (sid == null) { 
            resp.sendRedirect(req.getContextPath()+"/customerorders"); 
            return; 
        }

        try {
            int orderId = Integer.parseInt(sid);

            // Trong phần xử lý hủy đơn của ChangeOrderServlet
if ("cancel".equals(action)) {
    // Load thông tin đơn trước khi hủy để tính toán số tiền
    ChangeOrderVM vmBeforeCancel = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());
    if (vmBeforeCancel == null) {
        req.getSession().setAttribute("flash", "Không tìm thấy đơn hàng hoặc không có quyền truy cập.");
        resp.sendRedirect(req.getContextPath()+"/customerorders");
        return;
    }
    
    // Tính toán số tiền hoàn sẽ nhận (để hiển thị)
    BigDecimal expectedRefund = vmBeforeCancel.getRefundAmount();
    
    System.out.println("[ChangeOrderServlet] Cancelling order #" + orderId + 
        ", expected refund: " + expectedRefund);
    
    int rows = changeDao.cancelConfirmedOrderWithin30Min(orderId, acc.getAccountId());
    if (rows > 0) {
        req.getSession().setAttribute("flash", 
            "Đã hủy đơn #" + orderId + " thành công. Số tiền " + 
            String.format("%,d", expectedRefund.intValue()) + " đ (cọc + 30%) đã được hoàn vào ví.");
        
        // Ghi log chi tiết
        System.out.println("[ChangeOrderServlet] Successfully cancelled order #" + orderId + 
            ", refund amount: " + expectedRefund);
    } else {
        req.getSession().setAttribute("flash", 
            "Hủy thất bại (có thể đã quá hạn 30 phút hoặc không đúng quyền).");
        System.out.println("[ChangeOrderServlet] Failed to cancel order #" + orderId);
    }
    resp.sendRedirect(req.getContextPath()+"/customerorders");
    return;
}

            if ("update_dates".equals(action)) {
                Date newStart = Date.valueOf(req.getParameter("start"));
                Date newEnd = Date.valueOf(req.getParameter("end")); // Lấy từ form thay vì tự tính

                // Load thông tin đơn hàng để kiểm tra
                ChangeOrderVM vm = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());
                if (vm == null || !vm.isWithin30Min()) {
                    req.getSession().setAttribute("flash", "Không thể đổi đơn: đơn không tồn tại hoặc đã quá hạn.");
                    resp.sendRedirect(req.getContextPath()+"/customerorders");
                    return;
                }

                // Kiểm tra số ngày thuê có giữ nguyên không
                int newRentalDays = (int) ((newEnd.getTime() - newStart.getTime()) / (1000 * 60 * 60 * 24)) + 1;
                if (newRentalDays != vm.getOriginalRentalDays()) {
                    req.getSession().setAttribute("flash", 
                        "Số ngày thuê phải giữ nguyên (" + vm.getOriginalRentalDays() + " ngày). " +
                        "Bạn đã chọn " + newRentalDays + " ngày.");
                    resp.sendRedirect(req.getContextPath()+"/change-order?orderId=" + orderId);
                    return;
                }

                // Kiểm tra ngày mới có hợp lệ không
                if (newStart.after(newEnd)) {
                    req.getSession().setAttribute("flash", "Ngày bắt đầu không thể sau ngày kết thúc.");
                    resp.sendRedirect(req.getContextPath()+"/change-order?orderId=" + orderId);
                    return;
                }

                // Kiểm tra ngày bắt đầu không trong quá khứ
                Date today = new Date(System.currentTimeMillis());
                if (newStart.before(today)) {
                    req.getSession().setAttribute("flash", "Không thể chọn ngày trong quá khứ.");
                    resp.sendRedirect(req.getContextPath()+"/change-order?orderId=" + orderId);
                    return;
                }

                // Thực hiện đổi đơn - DAO sẽ kiểm tra trùng lịch tự động
                IOrderChangeDao.ChangeResult result = changeDao.updateOrderDatesWithin30Min(
                    orderId, acc.getAccountId(), newStart, newEnd);

                switch (result) {
                    case OK -> req.getSession().setAttribute("flash", 
                        "Đã đổi thời gian cho đơn #"+orderId + " (" + vm.getOriginalRentalDays() + " ngày)");
                    case EXPIRED -> req.getSession().setAttribute("flash", "Đã quá hạn 30 phút để đổi.");
                    case CONFLICT -> req.getSession().setAttribute("flash", 
                        "Khoảng thời gian này đã có người đặt. Vui lòng chọn ngày khác.");
                    case FAIL -> req.getSession().setAttribute("flash", 
                        "Cập nhật thất bại. Vui lòng kiểm tra lại thông tin.");
                    default -> req.getSession().setAttribute("flash", "Cập nhật thất bại.");
                }
                resp.sendRedirect(req.getContextPath()+"/customerorders");
                return;
            }

            resp.sendRedirect(req.getContextPath()+"/customerorders");
        } catch (IllegalArgumentException e) {
            // Xử lý lỗi định dạng ngày
            req.getSession().setAttribute("flash", "Định dạng ngày không hợp lệ. Vui lòng chọn lại.");
            resp.sendRedirect(req.getContextPath()+"/change-order?orderId=" + sid);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash", "Lỗi đổi/hủy: "+e.getMessage());
            resp.sendRedirect(req.getContextPath()+"/customerorders");
        }
    }
}