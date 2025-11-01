package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

import service.IAdminService;
import service.AdminService;

@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private final IAdminService adminService = new AdminService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("kpi", adminService.getKpiCards());
        req.setAttribute("latestOrders", adminService.getLatestOrders(8));
        req.setAttribute("maintenanceBikes", adminService.getMaintenanceBikes(5));

        req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
    }
}
