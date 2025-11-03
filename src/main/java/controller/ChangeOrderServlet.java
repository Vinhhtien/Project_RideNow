package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import dao.IOrderChangeDao;
import dao.OrderChangeDao;
import java.io.IOException;
import model.Account;
import model.ChangeOrderVM;
import java.sql.Date; // THÊM IMPORT NÀY

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

            if ("cancel".equals(action)) {
                int rows = changeDao.cancelConfirmedOrderWithin30Min(orderId, acc.getAccountId()); // SỬA: getAccountId()
                req.getSession().setAttribute("flash",
                    rows>0 ? ("Đã hủy đơn #"+orderId) : "Hủy thất bại (có thể đã quá hạn hoặc không đúng quyền).");
                resp.sendRedirect(req.getContextPath()+"/customerorders");
                return;
            }

            if ("update_dates".equals(action)) {
                Date newStart = Date.valueOf(req.getParameter("start"));
                Date newEnd   = Date.valueOf(req.getParameter("end"));
                IOrderChangeDao.ChangeResult result = changeDao.updateOrderDatesWithin30Min(orderId, acc.getAccountId(), newStart, newEnd); // SỬA: ChangeResult
                switch (result) {
                    case OK -> req.getSession().setAttribute("flash","Đã đổi thời gian cho đơn #"+orderId);
                    case EXPIRED -> req.getSession().setAttribute("flash","Đã quá hạn 30 phút để đổi.");
                    case CONFLICT -> req.getSession().setAttribute("flash","Khoảng thời gian mới bị trùng lịch xe.");
                    default -> req.getSession().setAttribute("flash","Cập nhật thất bại.");
                }
                resp.sendRedirect(req.getContextPath()+"/customerorders");
                return;
            }

            resp.sendRedirect(req.getContextPath()+"/customerorders");
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash","Lỗi đổi/hủy: "+e.getMessage());
            resp.sendRedirect(req.getContextPath()+"/customerorders");
        }
    }
}