//package controller;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import model.MotorbikeListItem;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IMotorbikeService;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class MotorbikeDetailServletTest {
//
//    MotorbikeDetailServlet servlet;
//    @Mock IMotorbikeService service;
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    @BeforeEach
//    void setup() {
//        servlet = new MotorbikeDetailServlet();
//        TestUtils.forceSet(servlet, "service", service);
//    }
//
//    @Test
//    void idMissing_forwardWithError() throws Exception {
//        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/detail.jsp");
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void notFound_setsError_andForward() throws Exception {
//        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/detail.jsp");
//        when(req.getParameter("id")).thenReturn("5");
//        when(service.getDetail(5)).thenReturn(null);
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("error"), any());
//    }
//
//    @Test
//    void found_setsBike_andForward() throws Exception {
//        RequestDispatcher rd = TestUtils.stubForward(req, "/motorbikes/detail.jsp");
//        when(req.getParameter("id")).thenReturn("7");
//        when(service.getDetail(7)).thenReturn(new MotorbikeListItem());
//        servlet.doGet(req, resp);
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("bike"), any());
//    }
//}
//
