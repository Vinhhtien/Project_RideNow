package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import model.Account;
import model.Customer;
import model.Review;
import service.ReviewService;
import service.IReviewService;
import service.CustomerService;
import service.ICustomerService;
import dao.ReviewDao;
import dao.IReviewDao;

@WebServlet(name = "ReviewServlet", urlPatterns = {"/review"})
public class ReviewServlet extends HttpServlet {

    private final IReviewService reviewService = new ReviewService();
    private final IReviewDao reviewDao = new ReviewDao();
    private final ICustomerService customerService = new CustomerService();

    public static class ReviewWithCustomer {
        private Review review;
        private String customerName;

        public Review getReview() {
            return review;
        }

        public void setReview(Review review) {
            this.review = review;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String bikeIdStr = req.getParameter("bikeId");
        String orderIdStr = req.getParameter("orderId");

        if (bikeIdStr == null || bikeIdStr.trim().isEmpty()) {
            req.setAttribute("error", "Lỗi: Không xác định được ID xe để đánh giá.");
            req.getRequestDispatcher("/customer/review_list.jsp").forward(req, resp);
            return;
        }

        try {
            int bikeId = Integer.parseInt(bikeIdStr);
            int orderId = (orderIdStr != null && !orderIdStr.isEmpty())
                    ? Integer.parseInt(orderIdStr) : 0;

            if (bikeId <= 0) {
                req.setAttribute("error", "Lỗi dữ liệu: ID xe không hợp lệ (" + bikeId + "). Vui lòng kiểm tra lại đơn hàng.");
                req.getRequestDispatcher("/customer/review_list.jsp").forward(req, resp);
                return;
            }

            // 🔹 Lấy danh sách đánh giá hiện có cho xe này
            List<Review> rawReviews = reviewService.getReviewByBikeId(bikeId);
            List<ReviewWithCustomer> reviews = new ArrayList<>();

            for (Review r : rawReviews) {
                Customer c = customerService.getCustomerById(r.getCustomerId());
                ReviewWithCustomer rwc = new ReviewWithCustomer();
                rwc.setReview(r);
                rwc.setCustomerName(c != null ? c.getFullName() : "Ẩn danh");
                reviews.add(rwc);
            }

            req.setAttribute("bikeId", bikeId);
            req.setAttribute("orderId", orderId);
            req.setAttribute("reviews", reviews);

            HttpSession session = req.getSession(false);
            if (session != null && session.getAttribute("message") != null) {
                req.setAttribute("message", session.getAttribute("message"));
                session.removeAttribute("message");
            }
            if (session != null && session.getAttribute("error") != null) {
                req.setAttribute("error", session.getAttribute("error"));
                session.removeAttribute("error");
            }

            req.getRequestDispatcher("/customer/review_list.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Lỗi: Tham số ID xe không phải là số.");
            req.getRequestDispatcher("/customer/review_list.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Không thể tải trang đánh giá: " + e.getMessage());
            req.getRequestDispatcher("/customer/review_list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String bikeIdStr = req.getParameter("bikeId");
        String orderIdStr = req.getParameter("orderId");
        String ratingStr = req.getParameter("rating");
        String comment = req.getParameter("comment");

        if (bikeIdStr == null || ratingStr == null || bikeIdStr.isEmpty() || ratingStr.isEmpty() || comment == null) {
            session.setAttribute("error", "Vui lòng chọn số sao và nhập nhận xét trước khi gửi.");
            resp.sendRedirect(req.getContextPath() + "/review?bikeId=" + (bikeIdStr != null ? bikeIdStr : "0") + "&orderId=" + (orderIdStr != null ? orderIdStr : "0"));
            return;
        }

        int bikeId = 0;
        int rating = 0;
        int orderId = 0;

        try {
            bikeId = Integer.parseInt(bikeIdStr);
            rating = Integer.parseInt(ratingStr);
            orderId = (orderIdStr != null && !orderIdStr.isEmpty()) ? Integer.parseInt(orderIdStr) : 0;

            if (rating < 1 || rating > 5 || bikeId <= 0) {
                session.setAttribute("error", "Thông tin đánh giá (số sao/ID) không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/review?bikeId=" + bikeId + "&orderId=" + orderId);
                return;
            }

            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                session.setAttribute("error", "Không tìm thấy hồ sơ khách hàng. Vui lòng đăng nhập lại.");
                resp.sendRedirect(req.getContextPath() + "/login");
                return;
            }

            boolean inserted = reviewDao.insertReview(customer.getCustomerId(), bikeId, rating, comment);

            if (inserted) {
                session.setAttribute("message", "Cảm ơn bạn đã gửi đánh giá!");
            } else {
                session.setAttribute("error", "Lỗi: Không thể thêm đánh giá vào cơ sở dữ liệu. Vui lòng kiểm tra log server.");
            }

            resp.sendRedirect(req.getContextPath() + "/review?bikeId=" + bikeId + "&orderId=" + orderId);

        } catch (NumberFormatException e) {
            session.setAttribute("error", "Thông tin đánh giá (số sao/ID) không phải là số.");
            resp.sendRedirect(req.getContextPath() + "/review?bikeId=" + bikeId + "&orderId=" + orderId);
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Lỗi hệ thống khi gửi đánh giá: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/review?bikeId=" + bikeId + "&orderId=" + orderId);
        }
    }
}