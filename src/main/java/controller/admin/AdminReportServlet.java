package controller.admin;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import model.report.*;
import service.AdminReportService;
import service.IAdminReportService;

@WebServlet(name = "AdminReportServlet", urlPatterns = {"/admin/reports"})
public class AdminReportServlet extends HttpServlet {
    private final IAdminReportService svc = new AdminReportService();
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String sFrom  = req.getParameter("from");
            String sTo    = req.getParameter("to");
            String view   = req.getParameter("view");
            String sPage  = req.getParameter("page");
            String sSize  = req.getParameter("size");
            String sLimit = req.getParameter("limit");
            String action = req.getParameter("action");
            String type   = req.getParameter("type");
            String id     = req.getParameter("id");
            String sPartnerId = req.getParameter("partnerId");

            Integer partnerId = null;
            try { if (sPartnerId != null && !sPartnerId.isBlank()) partnerId = Integer.valueOf(sPartnerId); } catch (Exception ignore) {}

            LocalDate today = LocalDate.now();
            LocalDate from  = (sFrom == null || sFrom.isBlank()) ? today.withDayOfMonth(1) : LocalDate.parse(sFrom, F);
            LocalDate to    = (sTo   == null || sTo.isBlank())   ? today                 : LocalDate.parse(sTo, F);

            int page  = (sPage  == null || sPage.isBlank())  ? 1  : Math.max(1, Integer.parseInt(sPage));
            int size  = (sSize  == null || sSize.isBlank())  ? 20 : Math.min(200, Math.max(5, Integer.parseInt(sSize)));
            int limit = (sLimit == null || sLimit.isBlank()) ? 10 : Math.min(100, Math.max(3, Integer.parseInt(sLimit)));

            if (view == null || view.isBlank()) view = "overview";

            if ("export".equals(action)) {
                exportCsv(resp, from, to, type, page, size, limit, partnerId);
                return;
            }

            req.setAttribute("from", from.format(F));
            req.setAttribute("to",   to.format(F));
            req.setAttribute("view", view);
            req.setAttribute("page", page);
            req.setAttribute("size", size);
            req.setAttribute("limit", limit);
            req.setAttribute("partnerId", partnerId);
            req.setAttribute("menu", "reports");
            req.setAttribute("pageTitle", "Reports");

            switch (view) {
                case "payments": {
                    int total = svc.getPaymentsCount(from, to);
                    List<AdminRevenueItem> items = svc.getPayments(from, to, page, size);
                    int totalPages = (int) Math.ceil((double) total / size);
                    req.setAttribute("items", items);
                    req.setAttribute("payments", items); // alias thêm để JSP cũ/new đều ăn
                    req.setAttribute("total", total);
                    req.setAttribute("totalPages", totalPages);
                    break;
                }
                case "refunds": {
                    int total = svc.getRefundsCount(from, to);
                    List<AdminRefundItem> items = svc.getRefunds(from, to, page, size);
                    int totalPages = (int) Math.ceil((double) total / size);
                    req.setAttribute("refunds", items);
                    req.setAttribute("items", items); // alias thêm nếu JSP dùng ${items}
                    req.setAttribute("total", total);
                    req.setAttribute("totalPages", totalPages);
                    break;
                }
                case "stores": {
                    // Lấy ròng theo partner/store (anchor inspected_at)
                    List<AdminRevenueItem> storeRevenue = svc.getStoreRevenueSummary(from, to, partnerId);

                    double totalNet = 0d;
                    double totalPartnerShare40 = 0d;
                    double totalAdminShare60 = 0d;
                    double platformNetTotal = 0d; // 100% net của cửa hàng + 60% net của partner

                    for (AdminRevenueItem it : storeRevenue) {
                        double net = n(it.getNetRevenue());
                        totalNet += net;

                        boolean isStoreOwned = (it.getPartnerId() == null);

                        // Tên hiển thị (DAO có thể bơm sau; tạm fallback an toàn)
                        if (isBlank(it.getStoreName())) it.setStoreName("RideNow");
                        if (isBlank(it.getPartnerName()) && it.getPartnerId() != null)
                            it.setPartnerName("Partner #" + it.getPartnerId());

                        if (isStoreOwned) {
                            // Đơn thuộc cửa hàng: admin nhận 100%, partner 0%
                            it.setAdminShare60(net);
                            it.setPartnerShare40(0d);
                            totalAdminShare60 += net;
                            platformNetTotal  += net; // 100% net của store
                        } else {
                            double admin60   = round2(net * 0.60);
                            double partner40 = round2(net * 0.40);
                            it.setAdminShare60(admin60);
                            it.setPartnerShare40(partner40);
                            totalAdminShare60    += admin60;
                            totalPartnerShare40  += partner40;
                            platformNetTotal     += admin60; // chỉ cộng 60% partner
                        }
                    }

                    req.setAttribute("storeRevenue", storeRevenue);
                    req.setAttribute("totalNetRevenue", totalNet);
                    req.setAttribute("totalAdminShare60", totalAdminShare60);
                    req.setAttribute("totalPartnerShare40", totalPartnerShare40);
                    req.setAttribute("platformNetTotal", platformNetTotal);
                    break;
                }
                case "top-customers": {
                    List<AdminTopCustomerStat> top = svc.getTopCustomers(from, to, limit);
                    req.setAttribute("topCustomers", top);
                    break;
                }
                case "payment-detail": {
                    AdminRevenueItem p = svc.getPaymentById(parseIntSafe(id));
                    req.setAttribute("payment", p);
                    break;
                }
                case "order-detail": {
                    AdminOrderDetail od = svc.getOrderDetail(parseIntSafe(id));
                    req.setAttribute("order", od);
                    break;
                }

                // ===== Đồng bộ theo nghiệp vụ: tập đơn đã hoàn tiền (anchor inspected_at) =====
                case "overview-net": {
                    List<AdminOrderDetail> orders = svc.getRevenueOrdersNet(from, to);

                    double totalCollected = 0d, totalRefunded = 0d, netRevenue = 0d;
                    for (AdminOrderDetail o : orders) {
                        double collected = n(o.getTotalPrice()) + n(o.getDepositAmount());
                        double refunded  = n(o.getRefundedAmount());
                        totalCollected += collected;
                        totalRefunded  += refunded;
                        netRevenue     += (collected - refunded);
                    }
                    req.setAttribute("ordersNet", orders);
                    req.setAttribute("totalCollected", totalCollected);
                    req.setAttribute("totalRefunded",  totalRefunded);
                    req.setAttribute("netRevenue",     netRevenue);

                    // Theo ngày: dùng DAO đã anchor inspected_at
                    List<AdminRevenueItem> dailyItems = svc.getDailyRevenueNet(from, to);
                    List<Map<String,Object>> dailyNet = new ArrayList<>();
                    for (AdminRevenueItem it : dailyItems) {
                        Map<String,Object> row = new HashMap<>();
                        row.put("paymentDate", it.getPaymentDate());
                        row.put("totalPaid", it.getTotalPaid());
                        row.put("refundedAmount", it.getRefundedAmount());
                        row.put("netRevenue", it.getNetRevenue());
                        dailyNet.add(row);
                    }
                    req.setAttribute("dailyNet", dailyNet);
                    break;
                }

                case "overview":
                default: {
                    // Tập đơn đã hoàn tiền
                    List<AdminOrderDetail> ordersNet = svc.getRevenueOrdersNet(from, to);

                    double totalCollected = 0d, totalRefunded = 0d, netRevenue = 0d;
                    for (AdminOrderDetail o : ordersNet) {
                        double collected = n(o.getTotalPrice()) + n(o.getDepositAmount());
                        double refunded  = n(o.getRefundedAmount());
                        totalCollected += collected;
                        totalRefunded  += refunded;
                        netRevenue     += (collected - refunded);
                    }
                    req.setAttribute("ordersNet", ordersNet);
                    req.setAttribute("totalCollected", totalCollected);
                    req.setAttribute("totalRefunded",  totalRefunded);
                    req.setAttribute("netRevenue",     netRevenue);

                    // Theo phương thức: DAO đã phân bổ theo tỷ trọng (kể cả cọc)
                    List<AdminPaymentMethodStat> methodRows = svc.getMethodStats(from, to);
                    req.setAttribute("methodStats", methodRows);

                    // Theo tháng: dùng DAO anchor inspected_at
                    List<AdminDailyRevenuePoint> monthly = svc.getDailyRevenue(from, to);
                    req.setAttribute("daily", monthly);
                    break;
                }
            }

            req.getRequestDispatcher("/admin/admin-report.jsp").forward(req, resp);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    private int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private static double n(Number v) { return v == null ? 0d : v.doubleValue(); }
    private static boolean isBlank(String s){ return s == null || s.trim().isEmpty(); }
    private static double round2(double x){ return Math.round(x * 100.0) / 100.0; }

    private void exportCsv(HttpServletResponse resp, LocalDate from, LocalDate to,
                           String type, int page, int size, int limit, Integer partnerId) throws IOException, ServletException {
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("text/csv; charset=UTF-8");
        String fname = "report_" + (type == null ? "overview" : type) + "_" + from + "_" + to + ".csv";
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + fname + "\"");

        try (PrintWriter w = resp.getWriter()) {
            try {
                switch (type == null ? "" : type) {
                    case "payments": {
                        List<AdminRevenueItem> items = svc.getPayments(from, to, page, size);
                        w.println("payment_id,order_id,customer,amount,method,status,payment_date,verified_at,reference");
                        for (AdminRevenueItem it : items) {
                            w.println(csv(it.getPaymentId()) + "," +
                                      csv(it.getOrderId()) + "," +
                                      csv(it.getCustomerName()) + "," +
                                      csv(it.getAmount()) + "," +
                                      csv(it.getMethod()) + "," +
                                      csv(it.getStatus()) + "," +
                                      csvDt(it.getPaymentDate()) + "," +
                                      csvDt(it.getVerifiedAt()) + "," +
                                      csv(it.getReference()));
                        }
                        break;
                    }
                    case "refunds": {
                        List<AdminRefundItem> items = svc.getRefunds(from, to, page, size);
                        w.println("refund_id,order_id,customer,refund_amount,refund_method,inspected_at,verified_at,reference");
                        for (AdminRefundItem it : items) {
                            w.println(csv(it.getPaymentId()) + "," +
                                      csv(it.getOrderId()) + "," +
                                      csv(it.getCustomerName()) + "," +
                                      csv(it.getAmount()) + "," +
                                      csv(it.getMethod()) + "," +
                                      csvDt(it.getPaymentDate()) + "," +
                                      csvDt(it.getVerifiedAt()) + "," +
                                      csv(it.getReference()));
                        }
                        break;
                    }
                    case "stores": {
                        List<AdminRevenueItem> items = svc.getStoreRevenueSummary(from, to, partnerId);
                        // Xuất kèm chia 60/40 & tên hiển thị
                        w.println("partner_id,partner_name,store_id,store_name,order_count,total_paid,refunded_amount,net_revenue,partner_40,admin_60");
                        for (AdminRevenueItem it : items) {
                            boolean isStoreOwned = (it.getPartnerId() == null);
                            double net = n(it.getNetRevenue());
                            double admin60   = isStoreOwned ? net : round2(net * 0.60);
                            double partner40 = isStoreOwned ? 0d  : round2(net * 0.40);

                            String partnerName = isBlank(it.getPartnerName())
                                    ? (it.getPartnerId() == null ? "" : ("Partner #" + it.getPartnerId()))
                                    : it.getPartnerName();
                            String storeName = isBlank(it.getStoreName()) ? "RideNow" : it.getStoreName();

                            w.println(csv(it.getPartnerId()) + "," +
                                      csv(partnerName) + "," +
                                      csv(it.getStoreId()) + "," +
                                      csv(storeName) + "," +
                                      csv(it.getOrderCount()) + "," +
                                      csv(it.getTotalPaid()) + "," +
                                      csv(it.getRefundedAmount()) + "," +
                                      csv(it.getNetRevenue()) + "," +
                                      csv(partner40) + "," +
                                      csv(admin60));
                        }
                        break;
                    }
                    case "methods": {
                        List<AdminPaymentMethodStat> items = svc.getMethodStats(from, to);
                        w.println("method,paid,refunded,net");
                        for (AdminPaymentMethodStat it : items) {
                            w.println(csv(it.getMethod()) + "," +
                                      csv(it.getPaidAmount()) + "," +
                                      csv(it.getRefundedAmount()) + "," +
                                      csv(it.getNet()));
                        }
                        break;
                    }
                    case "daily": {
                        List<AdminDailyRevenuePoint> items = svc.getDailyRevenue(from, to);
                        w.println("day,paid,refunded,net");
                        for (AdminDailyRevenuePoint it : items) {
                            w.println(csvDate(it.getDay()) + "," +
                                      csv(it.getPaidAmount()) + "," +
                                      csv(it.getRefundedAmount()) + "," +
                                      csv(it.getNet()));
                        }
                        break;
                    }
                    default: {
                        AdminReportSummary s = svc.getSummary(from, to);
                        w.println("total_orders,total_paid,total_refunded,net");
                        w.println(csv(s.getTotalOrders()) + "," +
                                  csv(s.getTotalPaid()) + "," +
                                  csv(s.getTotalRefunded()) + "," +
                                  csv(s.getNet()));
                    }
                }
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }

    private String csv(Object o) {
        if (o == null) return "";
        String s = String.valueOf(o).replace("\"", "\"\"");
        if (s.contains(",") || s.contains("\"")) return "\"" + s + "\"";
        return s;
    }

    private String csvDt(java.util.Date d) {
        if (d == null) return "";
        return String.valueOf(new java.sql.Timestamp(d.getTime()));
    }

    private String csvDate(java.util.Date d) {
        if (d == null) return "";
        java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd");
        return f.format(d);
    }
}
