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

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String path = request.getServletPath();
        String view = request.getParameter("view");

        System.out.println("=== STORE REVIEW SERVLET START ===");
        System.out.println("Path: " + path);
        System.out.println("View parameter: " + view);

        try {
            // 1) Load tất cả reviews
            List<StoreReview> reviews = reviewDao.findAll();

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

            // 2) Kiểm tra user đã có review chưa (THEO account_id)
            HttpSession session = request.getSession();
            Account account = (Account) session.getAttribute("account");

            if (account != null && "customer".equalsIgnoreCase(account.getRole())) {
                int accountId = account.getAccountId(); // đây là account_id
                StoreReview userReview = ((StoreReviewDao) reviewDao).findByAccountId(accountId);
                if (userReview != null) {
                    userReview.setCanEdit(true);
                    request.setAttribute("userReview", userReview);
                    // gửi kèm accountId để JSP có thể đánh dấu "Đánh giá của bạn"
                    request.setAttribute("currentAccountId", accountId);
                    System.out.println("✅ USER ĐÃ CÓ REVIEW (by account): ID=" + userReview.getStoreReviewId());
                }
            }

            // 3) Set list reviews và forward
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");

        if (account == null || !"customer".equalsIgnoreCase(account.getRole())) {
            session.setAttribute("message", "Vui lòng đăng nhập bằng tài khoản khách hàng để đánh giá.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // LƯU Ý: đây là account_id (không phải customer_id)
        int accountId = account.getAccountId();

        String ratingParam = request.getParameter("rating");
        String comment = request.getParameter("comment");
        String action = request.getParameter("action"); // "create" hoặc "update"

        // Validate input
        if (ratingParam == null || ratingParam.trim().isEmpty()) {
            session.setAttribute("message", "Vui lòng chọn số sao đánh giá.");
            response.sendRedirect(request.getContextPath() + "/storereview?view=page");
            return;
        }

        if (comment == null || comment.trim().isEmpty()) {
            session.setAttribute("message", "Vui lòng nhập nội dung đánh giá.");
            response.sendRedirect(request.getContextPath() + "/storereview?view=page");
            return;
        }

        int rating;
        try {
            rating = Integer.parseInt(ratingParam);
            if (rating < 1 || rating > 5) {
                session.setAttribute("message", "Số sao đánh giá phải từ 1 đến 5.");
                response.sendRedirect(request.getContextPath() + "/storereview?view=page");
                return;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("message", "Số sao đánh giá không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/storereview?view=page");
            return;
        }

        System.out.println("=== SUBMIT REVIEW ===");
        System.out.println("Account ID (not customer_id): " + accountId);
        System.out.println("Rating: " + rating);
        System.out.println("Comment: " + comment);
        System.out.println("Action: " + action);

        boolean success = false;

        try {
            if ("update".equals(action)) {
                // Cập nhật đánh giá
                String storeReviewIdParam = request.getParameter("storeReviewId");
                if (storeReviewIdParam == null || storeReviewIdParam.trim().isEmpty()) {
                    session.setAttribute("message", "Thiếu thông tin đánh giá cần cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/storereview?view=page");
                    return;
                }

                int storeReviewId = Integer.parseInt(storeReviewIdParam);
                success = ((StoreReviewDao) reviewDao).updateReview(storeReviewId, rating, comment);
                session.setAttribute("message", success ? "Cập nhật đánh giá thành công!" : "Cập nhật đánh giá thất bại!");
                System.out.println("🔄 Update review: " + (success ? "SUCCESS" : "FAILED"));

            } else {
                // CREATE – kiểm tra theo account_id để tránh nhầm khoá
                if (((StoreReviewDao) reviewDao).hasAccountReviewed(accountId)) {
                    session.setAttribute("message", "Bạn đã đánh giá cửa hàng rồi. Bạn chỉ có thể chỉnh sửa đánh giá hiện có.");
                    System.out.println("❌ User đã có review, không thể tạo mới");
                    response.sendRedirect(request.getContextPath() + "/storereview?view=page");
                    return;
                }

                // Insert theo account_id; DAO sẽ tự map account_id -> customer_id qua JOIN Customers
                success = ((StoreReviewDao) reviewDao).insertReviewByAccountId(accountId, rating, comment);

                if (!success) {
                    // Hai trường hợp hay gặp:
                    // 1) Tài khoản chưa có bản ghi trong Customers (chưa hoàn thiện hồ sơ)
                    // 2) Vi phạm UNIQUE/FK (đã có review/khách hàng không hợp lệ)
                    session.setAttribute("message",
                        "Không thể gửi đánh giá. Có thể tài khoản của bạn chưa có hồ sơ Khách hàng. " +
                        "Vui lòng vào trang Hồ sơ để bổ sung thông tin.");
                } else {
                    session.setAttribute("message", "Cảm ơn bạn đã gửi đánh giá!");
                }

                System.out.println("🆕 Create review: " + (success ? "SUCCESS" : "FAILED"));
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR IN REVIEW SUBMISSION: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("message", "Có lỗi xảy ra khi xử lý đánh giá. Vui lòng thử lại.");
        }

        response.sendRedirect(request.getContextPath() + "/storereview?view=page");
    }
}
