package controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminAvailabilityServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    void missingParams_returnsErrorJson() throws Exception {
        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        new AdminAvailabilityServlet().doGet(req, resp);
        Assertions.assertThat(sw.toString()).contains("\"ok\":false");
    }

    @Test
    void overlapFound_returnsUnavailable() throws Exception {
        when(req.getParameter("bikeId")).thenReturn("5");
        when(req.getParameter("start")).thenReturn("2099-01-01");
        when(req.getParameter("end")).thenReturn("2099-01-02");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);

            new AdminAvailabilityServlet().doGet(req, resp);
            Assertions.assertThat(sw.toString()).contains("\"available\":false");
        }
    }

    @Test
    void noOverlap_returnsAvailable() throws Exception {
        when(req.getParameter("bikeId")).thenReturn("5");
        when(req.getParameter("start")).thenReturn("2099-01-01");
        when(req.getParameter("end")).thenReturn("2099-01-02");

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            new AdminAvailabilityServlet().doGet(req, resp);
            Assertions.assertThat(sw.toString()).contains("\"available\":true");
        }
    }
}

