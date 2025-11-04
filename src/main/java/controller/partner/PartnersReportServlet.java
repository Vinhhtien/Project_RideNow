package controller.partner;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.Partner;
import model.report.PartnerReportSummary;
import model.report.PartnerBikeRevenueItem;
import model.report.PartnerStoreRevenueItem;
import service.IPartnerReportService;
import service.PartnerReportService;
import service.IPartnerService;
import service.PartnerService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@WebServlet(name = "PartnersReportServlet", urlPatterns = {"/partners/report"})
public class PartnersReportServlet extends HttpServlet {

    private final IPartnerReportService reportService = new PartnerReportService();
    private final IPartnerService partnerService = new PartnerService();

    private static final String VIEW = "/partners/report.jsp";
    private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Integer partnerId = resolvePartnerId(req, resp);
            if (partnerId == null) return;

            Date from = parse(req.getParameter("from"));
            Date to   = parse(req.getParameter("to"));

            PartnerReportSummary summary = reportService.getSummary(partnerId, from, to);
            List<PartnerBikeRevenueItem> bikes = reportService.getBikeRevenue(partnerId, from, to);
            List<PartnerStoreRevenueItem> stores = reportService.getStoreRevenue(partnerId, from, to);

            req.setAttribute("summary", summary);
            req.setAttribute("bikes", bikes);
            req.setAttribute("stores", stores);
            req.setAttribute("partnerId", partnerId);

            req.getRequestDispatcher(VIEW).forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private Date parse(String s) {
        if (s == null || s.isBlank()) return null;
        try { return DF.parse(s); } catch (ParseException ignored) { return null; }
    }

    // Enforce quyền: nếu có session => partnerId phải khớp
    private Integer resolvePartnerId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Account account = (session == null) ? null : (Account) session.getAttribute("account");
        String pidParam = req.getParameter("partnerId");
        Integer pidFromSession = null;

        try {
            if (account != null) {
                Partner p = partnerService.getByAccountId(account.getAccountId());
                if (p != null) {
                    pidFromSession = p.getPartnerId();
                    req.getSession().setAttribute("partnerId", pidFromSession);
                }
            }
        } catch (Exception ex) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot resolve partner from session");
            return null;
        }

        if (pidParam != null && !pidParam.isBlank()) {
            try {
                int pid = Integer.parseInt(pidParam);
                if (pidFromSession != null && !pidFromSession.equals(pid)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid partnerId");
                    return null;
                }
                return pid;
            } catch (NumberFormatException nfe) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid partnerId");
                return null;
            }
        }
        if (pidFromSession != null) return pidFromSession;

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Thiếu partnerId");
        return null;
    }
}
