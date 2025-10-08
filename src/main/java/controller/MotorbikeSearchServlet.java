package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.MotorbikeListItem;
import service.IMotorbikeService;
import service.MotorbikeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

@WebServlet(name = "MotorbikeSearchServlet", urlPatterns = {"/motorbikesearch"})
public class MotorbikeSearchServlet extends HttpServlet {

    private final IMotorbikeService service = new MotorbikeService();
    private static final int DEFAULT_SIZE = 12;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String typeStr  = req.getParameter("type_id");
        String sdStr    = req.getParameter("start_date");
        String edStr    = req.getParameter("end_date");
        String maxStr   = req.getParameter("max_price");
        String keyword  = trimToNull(req.getParameter("keyword"));
        String pageStr  = req.getParameter("page");
        String sizeStr  = req.getParameter("size");
        String sort     = trimToNull(req.getParameter("sort")); // NEW

        Integer typeId     = parseInt(typeStr);
        Date startDate     = parseDate(sdStr);
        Date endDate       = parseDate(edStr);
        BigDecimal maxPrice= parseDecimal(maxStr);
        int page           = parseIntOrDefault(pageStr, 1);
        int size           = parseIntOrDefault(sizeStr, DEFAULT_SIZE);

        // Chỉ lọc theo lịch khi đủ cả 2 ngày
        if (startDate == null || endDate == null) {
            startDate = null;
            endDate = null;
        }

        try {
            int total = service.count(typeId, startDate, endDate, maxPrice, keyword);
            int totalPages = (int) Math.ceil(total / (double) size);
            if (totalPages == 0) totalPages = 1;

            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            List<MotorbikeListItem> items = (total == 0)
                    ? Collections.emptyList()
                    : service.search(typeId, startDate, endDate, maxPrice, keyword, sort, page, size); // pass sort

            // Giữ filter để hiển thị lại
            req.setAttribute("type_id", typeId);
            req.setAttribute("start_date", sdStr);
            req.setAttribute("end_date", edStr);
            req.setAttribute("max_price", maxPrice);
            req.setAttribute("keyword", keyword);
            req.setAttribute("sort", sort); // NEW

            // Data hiển thị
            req.setAttribute("items", items);
            req.setAttribute("total", total);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("totalPages", totalPages);

            req.getRequestDispatcher("/motorbikes/search.jsp").forward(req, resp);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
    private static Integer parseInt(String s) {
        try { return (s == null || s.isBlank()) ? null : Integer.valueOf(s); }
        catch (Exception e) { return null; }
    }
    private static int parseIntOrDefault(String s, int def) {
        try { return (s == null || s.isBlank()) ? def : Math.max(1, Integer.parseInt(s)); }
        catch (Exception e) { return def; }
    }
    private static Date parseDate(String s) {
        try { return (s == null || s.isBlank()) ? null : Date.valueOf(s); }
        catch (Exception e) { return null; }
    }
    private static BigDecimal parseDecimal(String s) {
        try { return (s == null || s.isBlank()) ? null : new BigDecimal(s); }
        catch (Exception e) { return null; }
    }
}
