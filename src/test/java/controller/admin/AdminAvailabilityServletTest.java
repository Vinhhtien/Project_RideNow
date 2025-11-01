//package controller.admin;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.MockedStatic;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import utils.DBConnection;
//
//import java.io.PrintWriter;
//import java.io.StringWriter;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.util.Locale;
//import java.util.TimeZone;
//
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminAvailabilityServletTest {
//
//    @BeforeAll
//    static void initEnv() {
//        Locale.setDefault(Locale.US);
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    @Mock Connection con;
//    @Mock PreparedStatement ps;
//    @Mock ResultSet rs;
//
//    @Test
//    void ADMIN-AVAILABILITY-001_missing_params_error_json() throws Exception {
//        StringWriter sw = new StringWriter();
//        when(resp.getWriter()).thenReturn(new PrintWriter(sw));
//        new controller.AdminAvailabilityServlet().doGet(req, resp);
//        org.assertj.core.api.Assertions.assertThat(sw.toString()).contains("\"ok\":false");
//    }
//
//    @Test
//    void ADMIN-AVAILABILITY-002_overlap_found_returns_unavailable() throws Exception {
//        when(req.getParameter("bikeId")).thenReturn("5");
//        when(req.getParameter("start")).thenReturn("2099-01-01");
//        when(req.getParameter("end")).thenReturn("2099-01-02");
//        StringWriter sw = new StringWriter();
//        when(resp.getWriter()).thenReturn(new PrintWriter(sw));
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//
//            new controller.AdminAvailabilityServlet().doGet(req, resp);
//            org.assertj.core.api.Assertions.assertThat(sw.toString()).contains("\"available\":false");
//        }
//    }
//
//    @Test
//    void ADMIN-AVAILABILITY-003_no_overlap_returns_available() throws Exception {
//        when(req.getParameter("bikeId")).thenReturn("5");
//        when(req.getParameter("start")).thenReturn("2099-01-01");
//        when(req.getParameter("end")).thenReturn("2099-01-02");
//        StringWriter sw = new StringWriter();
//        when(resp.getWriter()).thenReturn(new PrintWriter(sw));
//
//        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(false);
//
//            new controller.AdminAvailabilityServlet().doGet(req, resp);
//            org.assertj.core.api.Assertions.assertThat(sw.toString()).contains("\"available\":true");
//        }
//    }
//}
//
