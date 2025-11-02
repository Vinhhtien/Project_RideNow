package controller;

import dao.IStoreReviewDao;
import dao.StoreReviewDao;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import model.Account;
import model.StoreReview;

@WebServlet(name = "StoreReviewServlet", urlPatterns = {"/storereview", "/home"})
public class StoreReviewServlet extends HttpServlet {

    private final IStoreReviewDao reviewDao = new StoreReviewDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set encoding UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String path = request.getServletPath();
        String view = request.getParameter("view");

        System.out.println("=== STORE REVIEW SERVLET START ===");
        System.out.println("Path: " + path);
        System.out.println("View parameter: " + view);

        try {
            List<StoreReview> reviews = reviewDao.findAll();

            // DEBUG CHI TIẾT
            System.out.println("=== SERVLET DEBUG ===");
            System.out.println("Reviews size: " + (reviews != null ? reviews.size() : "null"));

            if (reviews != null && !reviews.isEmpty()) {
                System.out.println("✅ CÓ DỮ LIỆU REVIEWS:");
                for (int i = 0; i < reviews.size(); i++) {
                    StoreReview review = reviews.get(i);
                    System.out.println("Review " + i + ": " + review.getComment() + " - Rating: " + review.getRating());
                }
            } else {
                System.out.println("❌ KHÔNG CÓ REVIEWS NÀO ĐƯỢC TRẢ VỀ!");
            }

            // Đặt attribute vào request
            request.setAttribute("reviews", reviews);

            String targetPage;
            if ("/storereview".equals(path) && "page".equalsIgnoreCase(view)) {
                targetPage = "/customer/store_review.jsp";
                System.out.println("📄 Chuyển đến trang đánh giá riêng");
            } else {
                targetPage = "/home.jsp";
                System.out.println("🏠 Chuyển đến trang chủ");
            }

            System.out.println("Forwarding to: " + targetPage);
            request.getRequestDispatcher(targetPage).forward(request, response);

        } catch (Exception e) {
            System.err.println("❌ SERVLET ERROR: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Không thể tải danh sách đánh giá.");
            request.getRequestDispatcher("/home.jsp").forward(request, response);
        }
    }

    // ✅ Gửi đánh giá
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Set encoding UTF-8
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");

        // Nếu chưa đăng nhập thì quay lại login
        if (account == null || !"customer".equalsIgnoreCase(account.getRole())) {
            session.setAttribute("message", "Vui lòng đăng nhập bằng tài khoản khách hàng để đánh giá.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        int customerId = account.getAccountId();
        int rating = Integer.parseInt(request.getParameter("rating"));
        String comment = request.getParameter("comment");

        System.out.println("=== SUBMIT REVIEW ===");
        System.out.println("Customer ID: " + customerId);
        System.out.println("Rating: " + rating);
        System.out.println("Comment: " + comment);

        boolean success = reviewDao.insertReview(customerId, rating, comment);
        
        if (success) {
            session.setAttribute("message", "Cảm ơn bạn đã gửi đánh giá!");
            System.out.println("✅ Review submitted successfully");
        } else {
            session.setAttribute("message", "Gửi đánh giá thất bại, vui lòng thử lại.");
            System.out.println("❌ Failed to submit review");
        }

        // Redirect để tránh resubmit form
        String referer = request.getHeader("referer");
        if (referer != null && referer.contains("storereview")) {
            response.sendRedirect(request.getContextPath() + "/storereview?view=page");
        } else {
            response.sendRedirect(request.getContextPath() + "/home");
        }
    }
}