package controller.admin;

import controller.testsupport.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminReportService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminReportServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock IAdminReportService svc;

    @Test
    @DisplayName("Export overview sets CSV headers")
    void export_overview_headers() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);

        when(req.getParameter("action")).thenReturn("export");
        when(req.getParameter("type")).thenReturn(null);

        when(svc.getSummary(any(), any())).thenReturn(new model.report.AdminReportSummary());

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp).setContentType(contains("text/csv"));
        verify(resp).setHeader(eq("Content-Disposition"), contains("attachment; filename="));
    }

    @Test
    @DisplayName("Parsing defaults: missing view -> overview, limits clamped")
    void parsing_defaults_and_limits() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(svc.getSummary(any(), any())).thenReturn(new model.report.AdminReportSummary());

        // No view param -> overview; extreme size/limit should be clamped internally
        when(req.getParameter("size")).thenReturn("1000");
        when(req.getParameter("limit")).thenReturn("1000");

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");
        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("view"), eq("overview"));
        verify(req).setAttribute(eq("size"), any());
        verify(req).setAttribute(eq("limit"), any());
    }

    @Test
    @DisplayName("Stores view totals calculation for partner vs store-owned")
    void stores_view_totals_calc() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);

        model.report.AdminRevenueItem storeOwned = new model.report.AdminRevenueItem();
        storeOwned.setPartnerId(null); // store-owned
        storeOwned.setNetRevenue(100); // full to admin
        storeOwned.setStoreName(""); // triggers fallback to RideNow

        model.report.AdminRevenueItem partnerItem = new model.report.AdminRevenueItem();
        partnerItem.setPartnerId(7);
        partnerItem.setNetRevenue(200); // 60/40 split => 120/80
        partnerItem.setPartnerName(""); // triggers fallback Partner #7

        when(req.getParameter("view")).thenReturn("stores");
        when(svc.getStoreRevenueSummary(any(), any(), any())).thenReturn(java.util.List.of(storeOwned, partnerItem));
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        org.mockito.ArgumentCaptor<Object> cap = org.mockito.ArgumentCaptor.forClass(Object.class);
        verify(req).setAttribute(eq("totalNetRevenue"), cap.capture());
        double totalNet = ((Number) cap.getValue()).doubleValue();
        org.assertj.core.api.Assertions.assertThat(totalNet).isEqualTo(300.0);

        verify(req).setAttribute(eq("totalAdminShare60"), cap.capture());
        double admin60 = ((Number) cap.getValue()).doubleValue();
        org.assertj.core.api.Assertions.assertThat(admin60).isEqualTo(220.0);

        verify(req).setAttribute(eq("totalPartnerShare40"), cap.capture());
        double partner40 = ((Number) cap.getValue()).doubleValue();
        org.assertj.core.api.Assertions.assertThat(partner40).isEqualTo(80.0);

        verify(req).setAttribute(eq("platformNetTotal"), cap.capture());
        double platformNet = ((Number) cap.getValue()).doubleValue();
        org.assertj.core.api.Assertions.assertThat(platformNet).isEqualTo(220.0);

        verify(rd).forward(req, resp);
    }

    @Test
    @DisplayName("Top customers view forwards with list")
    void top_customers_view() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("top-customers");
        when(svc.getTopCustomers(any(), any(), anyInt())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("topCustomers"), any());
    }

    @Test
    @DisplayName("Payment detail view forwards with payment")
    void payment_detail_view() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("payment-detail");
        when(req.getParameter("id")).thenReturn("5");
        when(svc.getPaymentById(anyInt())).thenReturn(new model.report.AdminRevenueItem());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("payment"), any());
    }

    @Test
    @DisplayName("Order detail view forwards with order")
    void order_detail_view() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("order-detail");
        when(req.getParameter("id")).thenReturn("6");
        when(svc.getOrderDetail(anyInt())).thenReturn(new model.report.AdminOrderDetail());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("order"), any());
    }

    @Test
    @DisplayName("Overview-net view computes totals and dailyNet")
    void overview_net_view() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("overview-net");

        model.report.AdminOrderDetail o1 = new model.report.AdminOrderDetail();
        o1.setTotalPrice(100); o1.setDepositAmount(20); o1.setRefundedAmount(10);
        model.report.AdminOrderDetail o2 = new model.report.AdminOrderDetail();
        o2.setTotalPrice(50); o2.setDepositAmount(0); o2.setRefundedAmount(5);
        when(svc.getRevenueOrdersNet(any(), any())).thenReturn(java.util.List.of(o1, o2));

        model.report.AdminRevenueItem d = new model.report.AdminRevenueItem();
        d.setPaymentDate(new java.util.Date()); d.setTotalPaid(10); d.setRefundedAmount(2); d.setNetRevenue(8);
        when(svc.getDailyRevenueNet(any(), any())).thenReturn(java.util.List.of(d));

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("ordersNet"), any());
        verify(req).setAttribute(eq("totalCollected"), eq(170.0));
        verify(req).setAttribute(eq("totalRefunded"),  eq(15.0));
        verify(req).setAttribute(eq("netRevenue"),     eq(155.0));
        verify(req).setAttribute(eq("dailyNet"), any());
    }

    @Test
    @DisplayName("CSV exports include headers and one data line for each type")
    void csv_exports_content() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);

        java.io.StringWriter sw = new java.io.StringWriter();
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));

        // payments
        when(req.getParameter("action")).thenReturn("export");
        when(req.getParameter("type")).thenReturn("payments");
        model.report.AdminRevenueItem p = new model.report.AdminRevenueItem();
        p.setPaymentId(1); p.setOrderId(2); p.setCustomerName("John, \"Doe\""); p.setAmount(12.5); p.setMethod("cash"); p.setStatus("paid"); p.setPaymentDate(new java.util.Date()); p.setVerifiedAt(new java.util.Date()); p.setReference("ref");
        when(svc.getPayments(any(), any(), anyInt(), anyInt())).thenReturn(java.util.List.of(p));
        servlet.doGet(req, resp);
        String out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("payment_id,order_id,customer,amount,method,status,payment_date,verified_at,reference");
        org.assertj.core.api.Assertions.assertThat(out.split("\r?\n").length).isGreaterThanOrEqualTo(2);

        // refunds
        sw.getBuffer().setLength(0);
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));
        when(req.getParameter("type")).thenReturn("refunds");
        model.report.AdminRefundItem r = new model.report.AdminRefundItem();
        r.setPaymentId(3); r.setOrderId(4); r.setCustomerName("Alice"); r.setAmount(7.5); r.setMethod("wallet"); r.setPaymentDate(new java.util.Date()); r.setVerifiedAt(new java.util.Date()); r.setReference("ref2");
        when(svc.getRefunds(any(), any(), anyInt(), anyInt())).thenReturn(java.util.List.of(r));
        servlet.doGet(req, resp);
        out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("refund_id,order_id,customer,refund_amount,refund_method,inspected_at,verified_at,reference");

        // stores
        sw.getBuffer().setLength(0);
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));
        when(req.getParameter("type")).thenReturn("stores");
        when(svc.getStoreRevenueSummary(any(), any(), any())).thenReturn(java.util.Collections.emptyList());
        servlet.doGet(req, resp);
        out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("partner_id,partner_name,store_id,store_name,order_count,total_paid,refunded_amount,net_revenue,partner_40,admin_60");

        // methods
        sw.getBuffer().setLength(0);
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));
        when(req.getParameter("type")).thenReturn("methods");
        model.report.AdminPaymentMethodStat ms = new model.report.AdminPaymentMethodStat(); ms.setMethod("cash"); ms.setPaidAmount(5); ms.setRefundedAmount(1);
        when(svc.getMethodStats(any(), any())).thenReturn(java.util.List.of(ms));
        servlet.doGet(req, resp);
        out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("method,paid,refunded,net");

        // daily
        sw.getBuffer().setLength(0);
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));
        when(req.getParameter("type")).thenReturn("daily");
        model.report.AdminDailyRevenuePoint dp = new model.report.AdminDailyRevenuePoint(); dp.setDay(new java.util.Date()); dp.setPaidAmount(9); dp.setRefundedAmount(3);
        when(svc.getDailyRevenue(any(), any())).thenReturn(java.util.List.of(dp));
        servlet.doGet(req, resp);
        out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("day,paid,refunded,net");

        // default (overview)
        sw.getBuffer().setLength(0);
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));
        when(req.getParameter("type")).thenReturn(null);
        model.report.AdminReportSummary sum = new model.report.AdminReportSummary();
        when(svc.getSummary(any(), any())).thenReturn(sum);
        servlet.doGet(req, resp);
        out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).contains("total_orders,total_paid,total_refunded,net");
    }

    @Test
    @DisplayName("Export all types headers ok")
    void export_all_types() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        String[] types = {"payments", "refunds", "stores", "methods", "daily"};
        for (String t : types) {
            reset(resp);
            when(resp.getWriter()).thenReturn(new PrintWriter(new StringWriter()));
            when(req.getParameter("action")).thenReturn("export");
            when(req.getParameter("type")).thenReturn(t);

            // stub minimal returns for service methods used
            when(svc.getPayments(any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
            when(svc.getRefunds(any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
            when(svc.getStoreRevenueSummary(any(), any(), any())).thenReturn(java.util.Collections.emptyList());
            when(svc.getMethodStats(any(), any())).thenReturn(java.util.Collections.emptyList());
            when(svc.getDailyRevenue(any(), any())).thenReturn(java.util.Collections.emptyList());

            servlet.doGet(req, resp);

            verify(resp).setContentType(contains("text/csv"));
        }
    }

    @Test
    @DisplayName("Overview view forwards to JSP")
    void overview_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(svc.getSummary(any(), any())).thenReturn(new model.report.AdminReportSummary());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("view"), eq("overview"));
    }

    @Test
    @DisplayName("Payments view forwards with paging attrs")
    void payments_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("payments");
        when(svc.getPaymentsCount(any(), any())).thenReturn(0);
        when(svc.getPayments(any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("payments"), any());
        verify(req).setAttribute(eq("totalPages"), anyInt());
    }

    @Test
    @DisplayName("Refunds view forwards with paging attrs")
    void refunds_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("refunds");
        when(svc.getRefundsCount(any(), any())).thenReturn(0);
        when(svc.getRefunds(any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("refunds"), any());
        verify(req).setAttribute(eq("totalPages"), anyInt());
    }

    @Test
    @DisplayName("Stores view forwards with partnerId attr and totals")
    void stores_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("stores");
        when(req.getParameter("partnerId")).thenReturn("5");
        when(svc.getStoreRevenueSummary(any(), any(), any())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("partnerId"), any());
        verify(req).setAttribute(eq("storeRevenue"), any());
        verify(req).setAttribute(eq("totalNetRevenue"), any());
        verify(req).setAttribute(eq("totalAdminShare60"), any());
        verify(req).setAttribute(eq("totalPartnerShare40"), any());
        verify(req).setAttribute(eq("platformNetTotal"), any());
    }

    @Test
    @DisplayName("Methods view forwards with stats")
    void methods_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("methods");
        when(svc.getMethodStats(any(), any())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("methodStats"), any());
    }

    @Test
    @DisplayName("Daily view forwards with points")
    void daily_view_forwards() throws Exception {
        AdminReportServlet servlet = new AdminReportServlet();
        TestUtils.forceSet(servlet, "svc", svc);
        when(req.getParameter("view")).thenReturn("daily");
        when(svc.getDailyRevenue(any(), any())).thenReturn(java.util.Collections.emptyList());
        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-report.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("daily"), any());
    }
}
