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

    // ✅ Lấy danh sách tất cả review để hiển thị trên trang home
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String view = request.getParameter("view");

        try {
            List<StoreReview> reviews = reviewDao.findAll();
            request.setAttribute("reviews", reviews);

            if ("page".equalsIgnoreCase(view)) {
                // Chuyển đến trang riêng để người dùng thêm đánh giá
                request.getRequestDispatcher("/customer/store_review.jsp").forward(request, response);
            } else {
                // Hiển thị ở home.jsp
                request.getRequestDispatcher("/home.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Không thể tải danh sách đánh giá.");
            request.getRequestDispatcher("/home.jsp").forward(request, response);
        }
    }

    // ✅ Gửi đánh giá
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        Account account = (Account) session.getAttribute("account");

        // Nếu chưa đăng nhập thì quay lại login
        if (account == null || !"customer".equals(account.getRole())) {
            request.setAttribute("message", "Vui lòng đăng nhập bằng tài khoản khách hàng để đánh giá.");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
            return;
        }

        int customerId = account.getAccountId();
        int rating = Integer.parseInt(request.getParameter("rating"));
        String comment = request.getParameter("comment");

        boolean success = reviewDao.insertReview(customerId, rating, comment);
        if (success) {
            request.setAttribute("message", "Cảm ơn bạn đã gửi đánh giá!");
        } else {
            request.setAttribute("message", "Gửi đánh giá thất bại, vui lòng thử lại.");
        }

        // Sau khi gửi xong, load lại danh sách đánh giá để có thể hiển thị cùng thông báo
        try {
            List<StoreReview> reviews = reviewDao.findAll();
            request.setAttribute("reviews", reviews);
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.getRequestDispatcher("/customer/store_review.jsp").forward(request, response);
    }
}
