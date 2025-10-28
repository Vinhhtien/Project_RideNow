package booking.servlets;

import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.IMotorbikeService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class MotorbikeDetailServletUnderTest extends HttpServlet {
    private final IMotorbikeService service;

    public MotorbikeDetailServletUnderTest(IMotorbikeService service) {
        this.service = service;
    }

    private Integer parseId(HttpServletRequest req) {
        String idStr = req.getParameter("bike_id");
        if (idStr == null || idStr.isBlank()) idStr = req.getParameter("id");
        if (idStr == null || idStr.isBlank()) return null;
        try { return Integer.valueOf(idStr.trim()); } catch (Exception e) { return null; }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer bikeId = parseId(req);
        RequestDispatcher rd = req.getRequestDispatcher("/motorbikes/detail.jsp");
        if (bikeId == null) {
            req.setAttribute("error", "Thiếu hoặc sai tham số mã xe.");
            rd.forward(req, resp);
            return;
        }

        try {
            MotorbikeListItem bike = service.getDetail(bikeId);
            if (bike == null) {
                req.setAttribute("error", "Không tìm thấy xe.");
                rd.forward(req, resp);
                return;
            }
            req.setAttribute("bike", bike);
            rd.forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("error", "Có lỗi khi tải chi tiết xe.");
            rd.forward(req, resp);
        }
    }

    // Bridge for tests
    public void doGetPublic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}

