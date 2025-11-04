package controller.admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.adminfeedback.AdminFeedbackItem;
import model.adminfeedback.AdminFeedbackSummary;
import model.adminfeedback.FeedbackType;
import service.AdminFeedbackService;
import service.IAdminFeedbackService;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "AdminFeedbackServlet", urlPatterns = {"/admin/feedback"})
public class AdminFeedbackServlet extends HttpServlet {

    private static final String AFS_VER = "AFS-DEBUG-2025-11-04";
    static {
        System.out.println("=== ADMIN FEEDBACK SERVLET LOADED === " + AFS_VER);
    }

    private final IAdminFeedbackService service = new AdminFeedbackService();
    private static final String[] DATE_PATTERNS = {"dd/MM/yyyy", "yyyy-MM-dd"};

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");
        resp.setHeader("X-AFS", AFS_VER);

        // ====== START LOG ======
        String path = req.getServletPath();
        String qs = req.getQueryString();
        System.out.println("=== ADMIN FEEDBACK SERVLET START ===");
        System.out.println("Path: " + path);
        System.out.println("Query: " + (qs == null ? "" : qs));

        // Raw params
        String pView   = req.getParameter("view");
        String pFrom   = req.getParameter("from");
        String pTo     = req.getParameter("to");
        String pType   = req.getParameter("type");
        String pStar   = req.getParameter("star");
        String pPage   = req.getParameter("page");
        String pSize   = req.getParameter("size");
        String pAction = req.getParameter("action");

        System.out.println("[RAW] view=" + pView + ", action=" + pAction
                + ", from=" + pFrom + ", to=" + pTo
                + ", type=" + pType + ", star=" + pStar
                + ", page=" + pPage + ", size=" + pSize);

        // ===== view: overview | details =====
        String view = opt(pView, "overview");

        // ===== Filters =====
        Date from = parseDate(pFrom);
        Date to   = parseDate(pTo);

        // tránh đụng độ "type" với view
        String typeRaw = pType;
        if ("overview".equalsIgnoreCase(typeRaw) || "details".equalsIgnoreCase(typeRaw)) {
            typeRaw = null;
        }
        FeedbackType type = FeedbackType.from(typeRaw); // STORE/BIKE/null

        Integer star = clampStar(tryInt(pStar));
        int page = Math.max(1, tryInt(pPage, 1));
        int size = Math.max(1, tryInt(pSize, 20));

        System.out.println("[PARSE] view=" + view
                + ", from=" + d(from) + ", to=" + d(to)
                + ", ftype=" + (type == null ? "ALL" : type.name())
                + ", star=" + star + ", page=" + page + ", size=" + size);

        // ===== Export (CSV) =====
        if ("export".equalsIgnoreCase(pAction) && "details".equalsIgnoreCase(view)) {
            System.out.println("📤 EXPORT DETAILS CSV...");
            try {
                doExportDetails(resp, from, to, type, star);
                System.out.println("✅ EXPORT DONE");
            } catch (Exception ex) {
                System.err.println("❌ EXPORT ERROR: " + ex.getMessage());
                throw ex;
            }
            return;
        }

        // ===== Data =====
        AdminFeedbackSummary summary;
        List<AdminFeedbackItem> items = Collections.emptyList();
        int total = 0;
        int totalPages = 1;

        try {
            summary = service.getSummary(from, to);
            System.out.println("✅ SUMMARY_OK: " + (summary != null ? "HAS" : "NULL"));
        } catch (Exception e) {
            System.err.println("❌ SUMMARY_ERR: " + e.getMessage());
            summary = new AdminFeedbackSummary();
            summary.setFrom(from); summary.setTo(to);
        }

        if ("details".equalsIgnoreCase(view)) {
            try {
                total = service.countAll(from, to, type, star);
                System.out.println("✅ COUNT_OK: total=" + total);
            } catch (Exception e) {
                System.err.println("❌ COUNT_ERR: " + e.getMessage());
                total = 0;
            }

            totalPages = Math.max(1, (total + size - 1) / size);
            int pageIn = page;
            if (page > totalPages) page = totalPages;
            System.out.println("[PAGING] page_in=" + pageIn + ", page_use=" + page + ", totalPages=" + totalPages);

            try {
                items = (total == 0) ? Collections.emptyList()
                        : service.findAll(from, to, type, star, page, size);

                String first = items.isEmpty() ? "none"
                        : (items.get(0).getType() + "#" + items.get(0).getTargetId()
                           + " ⭐" + items.get(0).getRating());
                System.out.println("✅ LIST_OK: size=" + items.size() + ", first=" + first);
            } catch (Exception e) {
                System.err.println("❌ LIST_ERR: " + e.getMessage());
                items = Collections.emptyList();
            }
        } else {
            System.out.println("ℹ️ VIEW=overview, skip list.");
        }

        // ===== Attrs =====
        req.setAttribute("view", view);
        req.setAttribute("summary", summary);
        req.setAttribute("items", items);
        req.setAttribute("total", total);
        req.setAttribute("page", page);
        req.setAttribute("size", size);
        req.setAttribute("totalPages", totalPages);

        Map<String, Object> filters = new HashMap<>();
        filters.put("from", from);
        filters.put("to", to);
        filters.put("type", type);
        filters.put("star", star);
        req.setAttribute("filters", filters);

        // ===== Forward =====
        String jsp = resolveJsp();
        System.out.println("➡️ Forwarding to: " + jsp + " | total=" + total + " | items=" + (items == null ? 0 : items.size()));
        try {
            req.getRequestDispatcher(jsp).forward(req, resp);
        } catch (Throwable t) {
            System.err.println("❌ FORWARD_ERR: " + t.getMessage());
            throw t;
        }
        System.out.println("=== ADMIN FEEDBACK SERVLET DONE ===");
    }

    private void doExportDetails(HttpServletResponse resp,
                                 Date from, Date to, FeedbackType type, Integer star) throws IOException {
        try {
            List<AdminFeedbackItem> items = service.findAll(from, to, type, star, 1, 50_000);

            String filename = "feedback_details_" +
                    new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
            String encoded = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/csv; charset=UTF-8");
            resp.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encoded);
            resp.setHeader("X-AFS", AFS_VER);

            PrintWriter out = resp.getWriter();
            out.write('\uFEFF'); // BOM UTF-8

            out.println("type,targetId,targetCode,targetName,orderId,customerId,customerName,rating,title,content,createdAt");

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (AdminFeedbackItem it : items) {
                String created = (it.getCreatedAt() == null) ? "" : fmt.format(it.getCreatedAt());
                out.println(csv(it.getType()==null?null:it.getType().name()) + "," +
                        csv(it.getTargetId()) + "," +
                        csv(it.getTargetCode()) + "," +
                        csv(it.getTargetName()) + "," +
                        csv(it.getOrderId()) + "," +
                        csv(it.getCustomerId()) + "," +
                        csv(it.getCustomerName()) + "," +
                        csv(it.getRating()) + "," +
                        csv(it.getTitle()) + "," +
                        csv(it.getContent()) + "," +
                        csv(created));
            }
            out.flush();
        } catch (Exception ex) {
            System.err.println("❌ EXPORT_ERR: " + ex.getMessage());
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println("Export failed.");
        }
    }

    // ===== Helpers =====
    private static String csv(Object v) {
        if (v == null) return "\"\"";
        String s = String.valueOf(v).replace("\"", "\"\"");
        return "\"" + s + "\"";
    }

    private String resolveJsp() {
        try {
            if (getServletContext().getResource("/admin/admin-feedback.jsp") != null) {
                System.out.println("[JSP] resolved: /admin/admin-feedback.jsp");
                return "/admin/admin-feedback.jsp";
            }
        } catch (Exception ignored) {}
        System.out.println("[JSP] resolved: /admin/feedback.jsp");
        return "/admin/feedback.jsp";
    }

    private static String opt(String s, String def) { return (s == null || s.trim().isEmpty()) ? def : s.trim(); }
    private static Integer tryInt(String s) { return tryInt(s, null); }
    private static Integer tryInt(String s, Integer def) {
        if (s == null) return def;
        s = s.trim();
        if (s.matches("-?\\d+")) return Integer.valueOf(s);
        return def;
    }
    private static Integer clampStar(Integer star) {
        if (star == null) return null;
        return Math.max(1, Math.min(5, star));
    }
    private static Date parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        String v = s.trim();
        for (String p : DATE_PATTERNS) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(p);
                f.setLenient(false);
                return f.parse(v);
            } catch (ParseException ignored) {}
        }
        return null;
    }

    private static String d(Date dt) {
        if (dt == null) return "null";
        return new SimpleDateFormat("yyyy-MM-dd").format(dt);
    }
}
