
package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import model.Account;
import model.Motorbike;
import model.Partner;
import model.MotorbikeListItem;
import model.Review;

import service.IMotorbikeService;
import service.MotorbikeService;
import service.IPartnerService;
import service.PartnerService;
import service.IReviewService;
import service.ReviewService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ViewMotorBikeServlet", urlPatterns = {"/viewmotorbike"})
public class ViewMotorBikeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final IMotorbikeService motorbikeService = new MotorbikeService();
    private final IPartnerService partnerService = new PartnerService();
    private final IReviewService reviewService = new ReviewService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        if (acc == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!"partner".equalsIgnoreCase(acc.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập danh sách xe của partner.");
            return;
        }

        try {
            Partner partner = partnerService.getByAccountId(acc.getAccountId());
            if (partner == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy thông tin đối tác cho tài khoản hiện tại.");
                return;
            }

            List<Motorbike> motorbikes = motorbikeService.getByPartnerId(partner.getPartnerId());
            request.setAttribute("partner", partner);
            request.setAttribute("motorbikes", motorbikes);

            String bikeIdParam = request.getParameter("id");
            RequestDispatcher rd;

            if (bikeIdParam != null && !bikeIdParam.isBlank()) {
                try {
                    int bikeId = Integer.parseInt(bikeIdParam);
                    MotorbikeListItem bikeDetail = motorbikeService.getDetail(bikeId);

                    // ✅ Kiểm tra xe có thuộc partner hiện tại không
                    boolean owned = false;
                    if (bikeDetail != null) {
                        List<Motorbike> myBikes = motorbikeService.getByPartnerId(partner.getPartnerId());
                        for (Motorbike b : myBikes) {
                            if (b.getBikeId() == bikeId) {
                                owned = true;
                                break;
                            }
                        }
                    }

                    if (bikeDetail != null && owned) {
                        request.setAttribute("bikeDetail", bikeDetail);

                        // giữ nguyên logic review
                        List<Review> reviews = reviewService.getReviewByBikeId(bikeId);
                        request.setAttribute("reviews", reviews);

                        rd = request.getRequestDispatcher("/partners/viewbikedetail.jsp");
                    } else {
                        request.setAttribute("errorMessage",
                                "Không tìm thấy thông tin xe hoặc xe không thuộc quyền sở hữu.");
                        rd = request.getRequestDispatcher("/partners/bikelist.jsp");
                    }
                } catch (NumberFormatException e) {
                    request.setAttribute("errorMessage", "ID xe không hợp lệ.");
                    rd = request.getRequestDispatcher("/partners/bikelist.jsp");
                }
            } else {
                rd = request.getRequestDispatcher("/partners/bikelist.jsp");
            }

            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage()
            );
        }
    }
}