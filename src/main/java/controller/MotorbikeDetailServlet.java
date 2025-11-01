package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.MotorbikeListItem;
import service.IMotorbikeService;
import service.MotorbikeService;

import java.io.IOException;

@WebServlet(name = "MotorbikeDetailServlet", urlPatterns = {"/motorbikedetail"})
public class MotorbikeDetailServlet extends HttpServlet {

    private final IMotorbikeService service = new MotorbikeService();

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
            MotorbikeListItem bike = service.getDetail(bikeId);
            if (bike == null) req.setAttribute("error", "Không tìm thấy xe.");
            req.setAttribute("bike", bike);
            req.getRequestDispatcher("/motorbikes/detail.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Có lỗi khi tải chi tiết xe.");
            req.getRequestDispatcher("/motorbikes/detail.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
