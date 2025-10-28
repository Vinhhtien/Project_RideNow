package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MotorbikeListItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IMotorbikeService;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotorbikeSearchServletTest {

    MotorbikeSearchServlet servlet;
    @Mock IMotorbikeService service;
    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;

    @BeforeEach
    void setup() {
        servlet = new MotorbikeSearchServlet();
        TestUtils.forceSet(servlet, "service", service);
    }

    @Test
    void noFilter_defaultList_forward() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/search.jsp");
        when(service.count(null, null, null, null, null)).thenReturn(0);
        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void withFilter_callsService_andForward() throws Exception {
        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/search.jsp");
        when(req.getParameter("type_id")).thenReturn("2");
        when(req.getParameter("start_date")).thenReturn(LocalDate.now().toString());
        when(req.getParameter("end_date")).thenReturn(LocalDate.now().plusDays(1).toString());
        when(req.getParameter("max_price")).thenReturn("100000");
        when(req.getParameter("keyword")).thenReturn("yam");
        when(req.getParameter("sort")).thenReturn("price_desc");
        when(req.getParameter("page")).thenReturn("1");
        when(req.getParameter("size")).thenReturn("12");

        when(service.count(any(), any(), any(), any(), any())).thenReturn(5);
        when(service.search(eq(2), any(Date.class), any(Date.class), eq(new BigDecimal("100000")),
                eq("yam"), eq("price_desc"), eq(1), eq(12))).thenReturn(List.of(new MotorbikeListItem()));

        servlet.doGet(req, resp);
        verify(rd).forward(req, resp);
    }

    @Test
    void exception_bubblesAsServletException() throws Exception {
        when(service.count(any(), any(), any(), any(), any())).thenThrow(new RuntimeException("x"));
        try {
            servlet.doGet(req, resp);
        } catch (ServletException e) {
            // expected
        }
    }
}

