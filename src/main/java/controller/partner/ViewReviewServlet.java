//  An + Quy
package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import model.Account;
import model.Review;
import service.IReviewService;
import service.ReviewService;

@WebServlet(name = "ViewReviewServlet", urlPatterns = {"/viewreviewservlet", "/partner/reviews"})
public class ViewReviewServlet extends HttpServlet {

    private final IReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Chỉ cho đối tác
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        List<Review> list;
        String bikeIdRaw = req.getParameter("bikeId");

        try {
            if (bikeIdRaw != null && !bikeIdRaw.trim().isEmpty()) {
                int bikeId = Integer.parseInt(bikeIdRaw.trim());
                list = reviewService.getReviewByBikeId(bikeId);
            } else {
                list = reviewService.getAll(); // method mới đã add-only theo hướng dẫn trước
            }
        } catch (Exception e) {
            // Tránh sập trang: log nhẹ & trả list rỗng
            e.printStackTrace();
            list = Collections.emptyList();
            req.setAttribute("error", "Không tải được danh sách đánh giá.");
        }

        // Gán cả 2 tên attribute để tương thích mọi JSP teammate có thể dùng
        req.setAttribute("reviews", list);
        req.setAttribute("reviewList", list);

        req.getRequestDispatcher("/partners/reviewlist.jsp").forward(req, resp);
    }
}
