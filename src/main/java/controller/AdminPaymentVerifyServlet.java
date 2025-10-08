package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import service.IPaymentVerifyService;
import service.PaymentVerifyService;
import model.Account;
import java.io.IOException;

@WebServlet("/adminpaymentverify")
public class AdminPaymentVerifyServlet extends HttpServlet {
    private final IPaymentVerifyService paymentService = new PaymentVerifyService();
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        // Check admin authentication
        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        
        req.setAttribute("payments", paymentService.getPendingPayments());
        req.getRequestDispatcher("/admin/admin-payment-verify.jsp").forward(req, resp);
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {

        Account admin = (Account) req.getSession().getAttribute("account");
        if (admin == null || !"admin".equals(admin.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String paymentIdStr = req.getParameter("paymentId");

        if (paymentIdStr != null) {
            try {
                int paymentId = Integer.parseInt(paymentIdStr);

                // SỬA: Dùng account_id từ bảng Admins (là 1) thay vì account_id từ bảng Accounts
                int adminId = 1; // Vì trong database admin_id = 1

                boolean success = paymentService.verifyPayment(paymentId, adminId);

                if (success) {
                    req.getSession().setAttribute("flash", "✅ Đã xác nhận thanh toán thành công!");
                } else {
                    req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
                }

            } catch (NumberFormatException e) {
                req.getSession().setAttribute("flash", "❌ Mã thanh toán không hợp lệ!");
            }
        }

        resp.sendRedirect(req.getContextPath() + "/adminpaymentverify");
    }
}
