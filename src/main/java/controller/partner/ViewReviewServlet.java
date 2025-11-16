package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import model.Account;
import model.Review;
import service.IReviewService;
import service.ReviewService;
import utils.DBConnection;

@WebServlet(name = "ViewReviewServlet", urlPatterns = {"/viewreviewservlet", "/partner/reviews"})
public class ViewReviewServlet extends HttpServlet {

    private final IReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        // 1) Chặn không phải partner
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || acc.getRole() == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Lấy partnerId
        Integer partnerId = resolvePartnerId(req, acc);
        if (partnerId == null) {
            resp.sendError(400, "Không xác định được partnerId");
            return;
        }

        // 3) Lọc theo bikeId (nếu có)
        Integer bikeFilter = null;
        String bikeIdRaw = req.getParameter("bikeId");
        if (bikeIdRaw != null && bikeIdRaw.trim().matches("\\d+")) {
            bikeFilter = Integer.valueOf(bikeIdRaw.trim());
        }

        // 4) Lấy TẤT CẢ reviews của partner - SỬ DỤNG SERVICE
        List<Review> allReviews = new ArrayList<>();
        try {
            if (bikeFilter != null) {
                // Lọc theo bikeId cụ thể
                allReviews = reviewService.getReviewByBikeId(bikeFilter);
                // Verify bike thuộc partner
                allReviews = filterReviewsByPartner(allReviews, partnerId);
            } else {
                // Lấy tất cả reviews của partner
                allReviews = getAllReviewsForPartner(partnerId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Không tải được danh sách đánh giá: " + e.getMessage());
        }

        // 5) Sort theo createdAt (mới nhất trước)
        allReviews.sort((a, b) -> {
            LocalDateTime timeA = a.getCreatedAt();
            LocalDateTime timeB = b.getCreatedAt();
            if (timeA == null && timeB == null) return 0;
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        // 6) Đếm số lượng và lấy bikeIds
        Set<Integer> myBikeIds = new HashSet<>();
        for (Review rv : allReviews) {
            if (rv.getBikeId() != 0) {
                myBikeIds.add(rv.getBikeId());
            }
        }

        // 7) Set attributes cho JSP
        req.setAttribute("partnerId", partnerId);
        req.setAttribute("myBikeIds", myBikeIds);
        req.setAttribute("reviews", allReviews);
        req.setAttribute("reviewList", allReviews);

        System.out.println("DEBUG: Sending " + allReviews.size() + " reviews to JSP");
        for (Review r : allReviews) {
            System.out.println("Review: " + r);
        }

        req.getRequestDispatcher("/partners/reviewlist.jsp").forward(req, resp);
    }

    // ===== PHƯƠNG THỨC CHÍNH: Lấy reviews theo partner =====
    private List<Review> getAllReviewsForPartner(int partnerId) {
        List<Review> reviews = new ArrayList<>();

        String sql = "SELECT r.review_id, r.customer_id, r.bike_id, r.rating, r.comment, r.created_at " +
                "FROM Reviews r " +
                "INNER JOIN Motorbikes m ON r.bike_id = m.bike_id " +
                "WHERE m.partner_id = ? " +
                "ORDER BY r.created_at DESC";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, partnerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Review review = mapResultSetToReview(rs);
                    reviews.add(review);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lấy reviews cho partner " + partnerId + ": " + e.getMessage());
        }

        System.out.println("Lấy được " + reviews.size() + " reviews cho partner " + partnerId);
        return reviews;
    }

    // ===== MAP ResultSet sang Review object =====
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setCustomerId(rs.getInt("customer_id"));
        review.setBikeId(rs.getInt("bike_id"));
        review.setRating(rs.getInt("rating"));
        review.setComment(rs.getString("comment"));

        // Xử lý createdAt: chuyển từ Timestamp sang LocalDateTime
        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            review.setCreatedAt(timestamp.toLocalDateTime());
        }

        return review;
    }

    // ===== Lọc reviews theo partner (cho trường hợp lọc theo bikeId) =====
    private List<Review> filterReviewsByPartner(List<Review> reviews, int partnerId) {
        if (reviews == null || reviews.isEmpty()) {
            return reviews;
        }

        List<Review> filtered = new ArrayList<>();
        Set<Integer> partnerBikeIds = getBikeIdsByPartner(partnerId);

        for (Review review : reviews) {
            if (partnerBikeIds.contains(review.getBikeId())) {
                filtered.add(review);
            }
        }

        System.out.println("Lọc từ " + reviews.size() + " xuống " + filtered.size() + " reviews thuộc partner");
        return filtered;
    }

    // ===== Lấy tất cả bike_ids của partner =====
    private Set<Integer> getBikeIdsByPartner(int partnerId) {
        Set<Integer> bikeIds = new HashSet<>();

        String sql = "SELECT bike_id FROM Motorbikes WHERE partner_id = ?";

        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setInt(1, partnerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    bikeIds.add(rs.getInt("bike_id"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lấy bike_ids cho partner " + partnerId);
        }

        System.out.println("Partner " + partnerId + " có " + bikeIds.size() + " xe");
        return bikeIds;
    }

    // ===== Helpers =====
    private static Integer resolvePartnerId(HttpServletRequest req, Account acc) {
        try {
            // Thử từ session trước
            Integer partnerId = (Integer) req.getSession().getAttribute("partnerId");
            if (partnerId != null) {
                System.out.println("Lấy partnerId từ session: " + partnerId);
                return partnerId;
            }

            // Thử từ parameter
            String pidParam = req.getParameter("partnerId");
            if (pidParam != null && pidParam.matches("\\d+")) {
                partnerId = Integer.valueOf(pidParam);
                req.getSession().setAttribute("partnerId", partnerId);
                System.out.println("Lấy partnerId từ parameter: " + partnerId);
                return partnerId;
            }

            // Lấy từ database
            partnerId = fetchPartnerIdByAccountId(acc.getAccountId());
            if (partnerId != null) {
                req.getSession().setAttribute("partnerId", partnerId);
                System.out.println("Lấy partnerId từ database: " + partnerId);
            } else {
                System.err.println("Không tìm thấy partnerId cho account: " + acc.getAccountId());
            }
            return partnerId;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Integer fetchPartnerIdByAccountId(int accountId) throws Exception {
        String sql = "SELECT partner_id FROM Partners WHERE account_id = ?";
        try (Connection cn = DBConnection.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }
}