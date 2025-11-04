// an
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import model.Account;
import model.Notification;
import service.INotificationService;
import service.NotificationService;
// an: dùng tên đầy đủ ở dưới nên không cần import thêm PartnerService/Partner

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {
    private final INotificationService notificationService = new NotificationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/html; charset=UTF-8");

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        String role = acc.getRole();
        if ("partner".equalsIgnoreCase(role)) {
            final int accountId = acc.getAccountId();

            // ===== Notifications =====
            List<Notification> all = notificationService.latestForAccount(accountId, 100, 0);
            req.setAttribute("allNotifications", all);
            List<Notification> toasts = all.stream().filter(n -> n != null && !n.isRead()).limit(5).collect(Collectors.toList());
            req.setAttribute("toastNotifications", toasts);
            req.setAttribute("unreadCount", notificationService.countUnread(accountId));

            // ===== KPIs Partner =====
            Integer partnerId = tryGetPartnerId(session, acc);

            Object summary = null;
            BigDecimal net = BigDecimal.ZERO;
            BigDecimal gross = BigDecimal.ZERO;
            BigDecimal refunded = BigDecimal.ZERO;
            Integer totalOrders = null;

            try {
                if (partnerId != null) {
                    summary = invokePartnerSummary(partnerId); // gọi service trực tiếp
                    Kpis k = extractKpis(summary);
                    net = k.net != null ? k.net : BigDecimal.ZERO;
                    gross = k.gross != null ? k.gross : BigDecimal.ZERO;
                    refunded = k.refunded != null ? k.refunded : BigDecimal.ZERO;
                    totalOrders = k.totalOrders;
                    if (net.signum() < 0) net = BigDecimal.ZERO;
                }
            } catch (Exception ex) {
                getServletContext().log("Partner report summary error", ex);
            }

            BigDecimal share40 = net.multiply(new BigDecimal("0.40"));

            // Aliases cho JSP
            req.setAttribute("partnerSummary", summary);
            req.setAttribute("summary", summary);

            req.setAttribute("netRevenue", net);
            req.setAttribute("partnerShare40", share40);
            req.setAttribute("totalRevenue", gross);
            req.setAttribute("totalRefund", refunded);
            if (totalOrders != null) req.setAttribute("totalOrders", totalOrders);

            req.setAttribute("timeRangeLabel", "Toàn bộ");
            Map<String, Object> kpis = new HashMap<>();
            kpis.put("netRevenue", net);
            kpis.put("partnerShare40", share40);
            kpis.put("totalRevenue", gross);
            kpis.put("totalRefund", refunded);
            kpis.put("totalOrders", totalOrders);
            kpis.put("rangeLabel", "Toàn bộ");
            req.setAttribute("kpis", kpis);

            req.setAttribute("role", role);
            req.getRequestDispatcher("/partners/dashboard.jsp").forward(req, resp);

        } else if ("admin".equalsIgnoreCase(role)) {
            req.setAttribute("role", role);
            req.getRequestDispatcher("/admin/dashboard.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(req.getContextPath() + "/home.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;
        if (acc == null || !"partner".equalsIgnoreCase(acc.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = req.getParameter("action");
        if ("read".equals(action)) {
            try {
                int id = Integer.parseInt(req.getParameter("id"));
                notificationService.readOne(id, acc.getAccountId());
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204
            } catch (Exception e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
            return;
        }

        if ("readAll".equals(action)) {
            notificationService.readAll(acc.getAccountId());
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }

    // ===== Helpers =====

    /** Lấy partnerId: 1) session.partnerId; 2) Account.getPartnerId()/getPartner().getPartnerId(); 3) PartnerService.getByAccountId(...) */
    private static Integer tryGetPartnerId(HttpSession session, Account acc) {
        // 1) session
        Object pidAttr = (session != null) ? session.getAttribute("partnerId") : null;
        if (pidAttr instanceof Number) return ((Number) pidAttr).intValue();
        if (pidAttr instanceof String) {
            String s = ((String) pidAttr).trim();
            if (s.matches("\\d+")) return Integer.valueOf(s);
        }

        // 2) reflection trên Account (nếu có)
        try {
            Method m = acc.getClass().getMethod("getPartnerId");
            Object v = m.invoke(acc);
            if (v instanceof Number) return ((Number) v).intValue();
            if (v instanceof String && ((String) v).trim().matches("\\d+")) return Integer.valueOf(((String) v).trim());
        } catch (Exception ignored) {}

        try {
            Method m = acc.getClass().getMethod("getPartner");
            Object partner = m.invoke(acc);
            if (partner != null) {
                Method mid = partner.getClass().getMethod("getPartnerId");
                Object v = mid.invoke(partner);
                if (v instanceof Number) return ((Number) v).intValue();
                if (v instanceof String && ((String) v).trim().matches("\\d+")) return Integer.valueOf(((String) v).trim());
            }
        } catch (Exception ignored) {}

        // 3) Fallback chuẩn: PartnerService.getByAccountId(accountId)
        try {
            service.IPartnerService psvc = new service.PartnerService();
            Object p = psvc.getByAccountId(acc.getAccountId());
            if (p != null) {
                try {
                    Method mid = p.getClass().getMethod("getPartnerId");
                    Object v = mid.invoke(p);
                    if (v instanceof Number) return ((Number) v).intValue();
                    if (v instanceof String && ((String) v).trim().matches("\\d+")) return Integer.valueOf(((String) v).trim());
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        return null;
    }

    /** Gọi trực tiếp PartnerReportService.getSummary(partnerId, null, null) */
    private static Object invokePartnerSummary(Integer partnerId) throws Exception {
        service.PartnerReportService svc = new service.PartnerReportService();
        return svc.getSummary(partnerId, null, null);
    }

    /** Rút KPIs từ summary bằng cách dò getter + field. */
    private static Kpis extractKpis(Object summary) {
        Kpis k = new Kpis();
        if (summary == null) return k;

        Map<String, Object> values = new HashMap<>();
        for (Method m : summary.getClass().getMethods()) {
            if (m.getParameterCount() == 0 && m.getName().startsWith("get")) {
                try { values.put(m.getName(), m.invoke(summary)); } catch (Exception ignored) {}
            }
        }
        for (Field f : summary.getClass().getDeclaredFields()) {
            try { f.setAccessible(true); values.putIfAbsent(f.getName(), f.get(summary)); } catch (Exception ignored) {}
        }

        String[] netKeys = {"getNetRevenue","getNet","getNetAmount","getNetTotal","getNetIncome","net","netRevenue","netAmount"};
        for (String key : netKeys) if (values.containsKey(key)) { k.net = toBD(values.get(key)); break; }

        String[] grossKeys = {"getGross","getTotalCollected","getTotalRevenue","getRevenue","getTotal","gross","totalCollected","totalRevenue"};
        for (String key : grossKeys) if (values.containsKey(key)) { k.gross = toBD(values.get(key)); break; }

        String[] refundKeys = {"getRefunded","getRefundedAmount","getRefundTotal","getTotalRefund","getRefunds","refunded","refundTotal","totalRefund"};
        for (String key : refundKeys) if (values.containsKey(key)) { k.refunded = toBD(values.get(key)); break; }

        String[] orderKeys = {"getTotalOrders","getOrders","getCountOrders","totalOrders","orders"};
        for (String key : orderKeys) if (values.containsKey(key)) {
            Object v = values.get(key);
            if (v instanceof Number) k.totalOrders = ((Number) v).intValue();
            else {
                String s = String.valueOf(v).trim();
                if (s.matches("\\d+")) k.totalOrders = Integer.parseInt(s);
            }
            break;
        }

        if (k.net == null && k.gross != null && k.refunded != null) k.net = k.gross.subtract(k.refunded);
        if (k.net == null) k.net = BigDecimal.ZERO;

        return k;
    }

    /** Convert về BigDecimal, hỗ trợ chuỗi tiền tệ có dấu . , space, ₫ */
    private static BigDecimal toBD(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal) return (BigDecimal) v;
        if (v instanceof Number) return new BigDecimal(v.toString());
        String s = v.toString();
        if (s == null) return BigDecimal.ZERO;
        s = s.trim().replaceAll("[^0-9\\-]", "");
        if (s.isEmpty() || "-".equals(s)) return BigDecimal.ZERO;
        try { return new BigDecimal(s); }
        catch (NumberFormatException e) {
            s = s.replace("-", "");
            return s.isEmpty() ? BigDecimal.ZERO : new BigDecimal(s);
        }
    }

    private static final class Kpis {
        BigDecimal gross;
        BigDecimal refunded;
        BigDecimal net;
        Integer totalOrders;
    }
}
