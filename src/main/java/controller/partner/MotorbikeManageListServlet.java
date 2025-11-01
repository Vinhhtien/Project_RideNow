package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.Motorbike;
import service.IMotorbikeService;
import service.MotorbikeService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MotorbikeManageListServlet", urlPatterns = {"/motorbikemanagelist"})
public class MotorbikeManageListServlet extends HttpServlet {

    private final IMotorbikeService service = new MotorbikeService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Account acc = (session == null) ? null : (Account) session.getAttribute("account");
        if (acc == null || !("admin".equalsIgnoreCase(acc.getRole()) || "partner".equalsIgnoreCase(acc.getRole()))) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String typeStr = req.getParameter("type_id");
        Integer typeId = null;
        try {
            if (typeStr != null && !typeStr.isBlank()) typeId = Integer.valueOf(typeStr);
        } catch (Exception ignored) {
        }

        try {
            List<Motorbike> list = service.findAllByOwnerAccount(acc.getAccountId(), acc.getRole());
            if (typeId != null) {
                List<Motorbike> filtered = new ArrayList<>();
                for (Motorbike m : list) if (m.getTypeId() == typeId) filtered.add(m);
                list = filtered;
            }
            req.setAttribute("items", list);
            req.getRequestDispatcher("/motorbikes/manage/handleBike.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
