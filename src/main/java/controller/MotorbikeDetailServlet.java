package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.MotorbikeListItem;
import service.IMotorbikeService;
import service.MotorbikeService;
import service.BikeReviewDisplayService; // SERVICE MỚI

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "MotorbikeDetailServlet", urlPatterns = {"/motorbikedetail"})
public class MotorbikeDetailServlet extends HttpServlet {

    private final IMotorbikeService motorbikeService = new MotorbikeService();
    private final BikeReviewDisplayService reviewDisplayService = new BikeReviewDisplayService(); // SERVICE MỚI

    private Integer parseId(HttpServletRequest req) {
        String idStr = req.getParameter("bike_id");
        if (idStr == null || idStr.isBlank()) idStr = req.getParameter("id");
        if (idStr == null || idStr.isBlank()) return null;
        try {
            return Integer.valueOf(idStr.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Integer bikeId = parseId(req);
        if (bikeId == null) {
            req.setAttribute("error", "Thiếu hoặc sai tham số mã xe.");
            req.getRequestDispatcher("/motorbikes/detail.jsp").forward(req, resp);
            return;
        }

        try {
            MotorbikeListItem bike = motorbikeService.getDetail(bikeId);
            if (bike == null) {
                req.setAttribute("error", "Không tìm thấy xe.");
            } else {
                // SỬ DỤNG SERVICE MỚI - hoàn toàn độc lập
                loadReviewDisplayData(req, bikeId);
            }
            
            req.setAttribute("bike", bike);
            req.getRequestDispatcher("/motorbikes/detail.jsp").forward(req, resp);
            
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Có lỗi khi tải chi tiết xe: " + e.getMessage());
            req.getRequestDispatcher("/motorbikes/detail.jsp").forward(req, resp);
        }
    }

    /**
     * PHƯƠNG THỨC MỚI - Độc lập với reviewlist cũ
     * Sử dụng service mới để lấy dữ liệu hiển thị reviews
     */
    private void loadReviewDisplayData(HttpServletRequest req, int bikeId) {
        try {
            System.out.println("Loading review display data for bikeId: " + bikeId);
            
            // Lấy danh sách reviews public từ service mới
            List<Map<String, Object>> publicReviews = reviewDisplayService.getPublicReviewsForBike(bikeId);
            System.out.println("Found " + publicReviews.size() + " public reviews");
            
            // Lấy thống kê reviews từ service mới
            Map<String, Object> reviewStats = reviewDisplayService.getReviewStatsForBike(bikeId);
            System.out.println("Review stats - Total: " + reviewStats.get("totalReviews") + ", Avg: " + reviewStats.get("averageRating"));
            
            // Đặt dữ liệu vào request với tên attribute mới để tránh xung đột
            req.setAttribute("publicReviews", publicReviews);
            req.setAttribute("reviewDisplayStats", reviewStats);
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tải dữ liệu hiển thị reviews: " + e.getMessage());
            e.printStackTrace();
            // Không ảnh hưởng đến phần còn lại của trang
            // Đặt các attribute rỗng để tránh lỗi trong JSP
            req.setAttribute("publicReviews", null);
            req.setAttribute("reviewDisplayStats", null);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}