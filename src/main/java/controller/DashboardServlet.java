package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import model.Account;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) { resp.sendRedirect(req.getContextPath() + "/login"); return; }

        String role = acc.getRole();
        if ("partner".equalsIgnoreCase(role)) {
            req.setAttribute("role", role);
            req.getRequestDispatcher("partners/dashboard.jsp").forward(req, resp);
        } else if("admin".equalsIgnoreCase(role)) {
            req.setAttribute("role", role);
            req.getRequestDispatcher("admin/dashboard.jsp").forward(req, resp);
        } else{
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
        }
            
    }
}
