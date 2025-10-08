package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

import model.Account;
import model.Partner;
import model.Motorbike;
import service.PartnerService;
import service.IPartnerService;
import service.IMotorbikeService;
import service.MotorbikeService;

@WebServlet(name = "PartnerServlet", urlPatterns = {"/partner"})
public class PartnerServlet extends HttpServlet {

    private final IPartnerService partnerService = new PartnerService();
    private final IMotorbikeService motorbikeService = new MotorbikeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if (action == null) action = "profile";

        try {
            switch (action) {
                case "bikes": {
                    List<Motorbike> bikes = partnerService.getMyBikes(acc.getAccountId());
                    req.setAttribute("bikes", bikes);
                    req.getRequestDispatcher("views/partner_bikes.jsp").forward(req, resp);
                    break;
                }
                case "bikeDetail": {
                    int bikeId = Integer.parseInt(req.getParameter("id"));
                    Motorbike bike = partnerService.getBikeDetails(bikeId);
                    req.setAttribute("bike", bike);
                    req.getRequestDispatcher("views/partner_bike_detail.jsp").forward(req, resp);
                    break;
                }
                case "profile":
                default: {
                    Partner partner = partnerService.getByAccountId(acc.getAccountId());
                    req.setAttribute("partner", partner);
                    req.getRequestDispatcher("views/partner_profile.jsp").forward(req, resp);
                }
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String action = req.getParameter("action");
        if ("updateProfile".equals(action)) {
            try {
                Partner p = new Partner();
                p.setPartnerId(Integer.parseInt(req.getParameter("partnerId")));
                p.setFullname(req.getParameter("fullName"));
                //p.setEmail(req.getParameter("email"));
                p.setPhone(req.getParameter("phone"));
                p.setAddress(req.getParameter("address"));
                p.setAccountId(acc.getAccountId()); // đảm bảo liên kết account

                boolean ok = partnerService.updateAccountInfo(p);
                if (ok) {
                    req.setAttribute("msg", "Cập nhật thông tin thành công");
                } else {
                    req.setAttribute("error", "Cập nhật thất bại");
                }

                // Lấy lại partner mới nhất từ DB
                Partner updatedPartner = partnerService.getByAccountId(acc.getAccountId());
                req.setAttribute("partner", updatedPartner);

                req.getRequestDispatcher("views/partner_profile.jsp").forward(req, resp);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } else {
            doGet(req, resp);
        }
    }
}
