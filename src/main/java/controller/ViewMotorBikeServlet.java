package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.RequestDispatcher;

import model.Account;
import model.Motorbike;
import model.Partner;
import service.IMotorbikeService;
import service.MotorbikeService;
import service.IPartnerService;
import service.PartnerService;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "ViewMotorBikeServlet", urlPatterns = {"/viewmotorbike"})
public class ViewMotorBikeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    // Dùng Service -> DAO (không gọi DAO trực tiếp)
    private final IMotorbikeService motorbikeService = new MotorbikeService();
    private final IPartnerService partnerService     = new PartnerService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Không tự tạo session mới
        HttpSession session = request.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        // Chưa đăng nhập -> /login (đi qua LoginServlet)
        if (acc == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Chỉ cho partner (nếu muốn cho admin xem luôn thì nới điều kiện)
        if (!"partner".equalsIgnoreCase(acc.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Bạn không có quyền truy cập danh sách xe của partner.");
            return;
        }

        try {
            // Lấy Partner theo account_id qua PartnerService
            Partner partner = partnerService.getByAccountId(acc.getAccountId());
            if (partner == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Không tìm thấy thông tin đối tác cho tài khoản hiện tại.");
                return;
            }

            // Lấy danh sách xe theo partnerId qua MotorbikeService
            List<Motorbike> motorbikes = motorbikeService.getByPartnerId(partner.getPartnerId());

            // Gắn data cho JSP
            request.setAttribute("partner", partner);
            request.setAttribute("motorbikes", motorbikes);

            // Forward tới trang hiển thị
            RequestDispatcher rd = request.getRequestDispatcher("/partners/bikelist.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã xảy ra lỗi khi lấy danh sách xe: " + e.getMessage()
            );
        }
    }
}
