//
//package controller.admin;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//import service.IPaymentVerifyService;
//import service.PaymentVerifyService;
//import model.Account;
//import java.io.IOException;
//
//@WebServlet("/adminpaymentverify")
//public class AdminPaymentVerifyServlet extends HttpServlet {
//    private final IPaymentVerifyService paymentService = new PaymentVerifyService();
//    
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
//            throws ServletException, IOException {
//        
//        Account admin = (Account) req.getSession().getAttribute("account");
//        if (admin == null || !"admin".equals(admin.getRole())) {
//            resp.sendRedirect(req.getContextPath() + "/login");
//            return;
//        }
//        
//        req.setAttribute("payments", paymentService.getPendingPayments());
//        req.getRequestDispatcher("/admin/admin-payment-verify.jsp").forward(req, resp);
//    }
//    
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
//            throws ServletException, IOException {
//
//        Account admin = (Account) req.getSession().getAttribute("account");
//        if (admin == null || !"admin".equals(admin.getRole())) {
//            resp.sendRedirect(req.getContextPath() + "/login");
//            return;
//        }
//
//        String paymentIdStr = req.getParameter("paymentId");
//        String baseUrl = req.getScheme() + "://" + req.getServerName()
//                   + ((req.getServerPort() == 80 || req.getServerPort() == 443) ? "" : ":" + req.getServerPort())
//                   + req.getContextPath();
//
//        if (paymentIdStr != null) {
//            try {
//                int paymentId = Integer.parseInt(paymentIdStr);
//                int adminId = 1;
//
//                // 1. VERIFY PAYMENT - GIỮ NGUYÊN LOGIC CŨ
//                boolean success = paymentService.verifyPayment(paymentId, adminId);
//
//                if (success) {
//                    // 2. GỬI EMAIL SAU KHI VERIFY THÀNH CÔNG
//                    try {
//                        System.out.println("🔄 Calling sendPaymentConfirmationEmail...");
//                        paymentService.sendPaymentConfirmationEmail(paymentId, baseUrl);
//                        req.getSession().setAttribute("flash", "✅ Đã xác nhận thanh toán thành công & gửi email xác nhận!");
//                    } catch (Exception emailException) {
//                        System.err.println("❌ Email service failed: " + emailException.getMessage());
//                        req.getSession().setAttribute("flash", "✅ Đã xác nhận thanh toán thành công! (Gửi email thất bại)");
//                    }
//                } else {
//                    req.getSession().setAttribute("flash", "❌ Xác nhận thất bại!");
//                }
//
//            } catch (NumberFormatException e) {
//                req.getSession().setAttribute("flash", "❌ Mã thanh toán không hợp lệ!");
//            }
//        }
//
//        resp.sendRedirect(req.getContextPath() + "/adminpaymentverify");
//    }
//}