package controller.admin;

import controller.testsupport.Fixtures;
import controller.testsupport.TestUtils;
import dao.IAdminOrderDAO;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.Account;
import model.OrderDetailItem;
import model.OrderSummary;
import model.PaymentInfo;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminOrderDetailServletTest {

    @BeforeAll
    static void setUpEnv() {
        Locale.setDefault(Locale.US);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    void get_not_logged_in_redirects_login() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        new AdminOrderDetailServlet().doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    void get_missing_id_forwards_not_found() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        Account admin = Fixtures.account(1, "admin");
        sessionMap.put("account", admin);
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");

        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        new AdminOrderDetailServlet().doGet(req, resp);

        verify(req).setAttribute("notFound", true);
        verify(rd).forward(req, resp);
    }

    @Test
    void get_invalid_id_forwards_not_found() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(2, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("id")).thenReturn("abc");
        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        new AdminOrderDetailServlet().doGet(req, resp);

        verify(req).setAttribute("notFound", true);
        verify(rd).forward(req, resp);
    }

    @Test
    void get_header_not_found_forwards_not_found() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(3, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("id")).thenReturn("42");
        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderDAO dao = mock(IAdminOrderDAO.class);
        when(dao.findOrderHeader(42)).thenReturn(Optional.empty());

        AdminOrderDetailServlet servlet = new AdminOrderDetailServlet();
        TestUtils.forceSet(servlet, "orderDAO", dao);

        servlet.doGet(req, resp);

        verify(req).setAttribute("notFound", true);
        verify(rd).forward(req, resp);
    }

    @Test
    void get_happy_sets_attributes_and_refund_present() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(4, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("id")).thenReturn("77");

        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderDAO dao = mock(IAdminOrderDAO.class);
        OrderSummary header = new OrderSummary(); header.setOrderId(77);
        when(dao.findOrderHeader(77)).thenReturn(Optional.of(header));
        List<OrderDetailItem> items = List.of(new OrderDetailItem());
        when(dao.findOrderItems(77)).thenReturn(items);
        List<PaymentInfo> payments = List.of(new PaymentInfo());
        when(dao.findPayments(77)).thenReturn(payments);

        AdminOrderDetailServlet servlet = new AdminOrderDetailServlet();
        TestUtils.forceSet(servlet, "orderDAO", dao);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getBigDecimal("refund_amount")).thenReturn(new BigDecimal("10.00"));
            when(rs.getString("refund_method")).thenReturn("cash");
            when(rs.getString("refund_status")).thenReturn("completed");
            when(rs.getString("bike_condition")).thenReturn("excellent");
            when(rs.getBigDecimal("damage_fee")).thenReturn(new BigDecimal("0.00"));
            when(rs.getString("damage_notes")).thenReturn("none");
            when(rs.getString("admin_notes")).thenReturn("ok");
            Timestamp ts = new Timestamp(0L);
            when(rs.getTimestamp("inspected_at")).thenReturn(ts);

            new AdminOrderDetailServlet().getClass(); // touch class

            servlet.doGet(req, resp);

            verify(ps).setInt(1, 77);
            verify(req).setAttribute("order", header);
            verify(req).setAttribute("items", items);
            verify(req).setAttribute("payments", payments);
            verify(req).setAttribute(eq("refund"), any());
            verify(rd).forward(req, resp);
        }
    }

    @Test
    void get_refund_absent_attribute_not_set() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(5, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("id")).thenReturn("78");
        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderDAO dao = mock(IAdminOrderDAO.class);
        OrderSummary header = new OrderSummary(); header.setOrderId(78);
        when(dao.findOrderHeader(78)).thenReturn(Optional.of(header));
        when(dao.findOrderItems(78)).thenReturn(Collections.emptyList());
        when(dao.findPayments(78)).thenReturn(Collections.emptyList());

        AdminOrderDetailServlet servlet = new AdminOrderDetailServlet();
        TestUtils.forceSet(servlet, "orderDAO", dao);

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false); // no refund rows

            servlet.doGet(req, resp);

            verify(ps).setInt(1, 78);
            verify(req, never()).setAttribute(eq("refund"), any());
            verify(rd).forward(req, resp);
        }
    }

    @Test
    void get_dao_throws_exception_forward_not_found() throws Exception {
        Map<String, Object> sessionMap = TestUtils.createSessionMap();
        sessionMap.put("account", Fixtures.account(6, "admin"));
        HttpSession session = TestUtils.mockSession(sessionMap);
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("id")).thenReturn("99");

        RequestDispatcher rd = TestUtils.mockDispatcher(req);

        IAdminOrderDAO dao = mock(IAdminOrderDAO.class);
        when(dao.findOrderHeader(99)).thenThrow(new RuntimeException("fail"));

        AdminOrderDetailServlet servlet = new AdminOrderDetailServlet();
        TestUtils.forceSet(servlet, "orderDAO", dao);

        servlet.doGet(req, resp);

        verify(req).setAttribute("notFound", true);
        verify(rd).forward(req, resp);
    }
}

