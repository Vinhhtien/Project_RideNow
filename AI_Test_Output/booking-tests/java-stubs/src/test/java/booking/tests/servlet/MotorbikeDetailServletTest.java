package booking.tests.servlet;

import booking.servlets.MotorbikeDetailServletUnderTest;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.IMotorbikeService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MotorbikeDetailServletTest {
    @Mock private IMotorbikeService service;
    @org.mockito.InjectMocks private MotorbikeDetailServletUnderTest servlet;

    @Test
    @DisplayName("TC-CTL-DETAIL-003: testDoGet_ExistingBike_ShouldSetAttributesAndForward")
    void testDoGet_ExistingBike_ShouldSetAttributesAndForward() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        RequestDispatcher rd = org.mockito.Mockito.mock(RequestDispatcher.class);
        when(req.getParameter("bike_id")).thenReturn("6");
        when(req.getRequestDispatcher("/motorbikes/detail.jsp")).thenReturn(rd);
        when(service.getDetail(6)).thenReturn(new MotorbikeListItem(6, "Name"));

        servlet.doGetPublic(req, resp);

        verify(req, times(1)).setAttribute(org.mockito.ArgumentMatchers.eq("bike"), org.mockito.ArgumentMatchers.any());
        verify(rd, times(1)).forward(req, resp);
    }

    @Test
    @DisplayName("TC-CTL-DETAIL-002: testDoGet_InvalidBike_ShouldForwardWithError")
    void testDoGet_InvalidBike_ShouldForwardWithError() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        RequestDispatcher rd = org.mockito.Mockito.mock(RequestDispatcher.class);
        when(req.getParameter("bike_id")).thenReturn("5");
        when(req.getRequestDispatcher("/motorbikes/detail.jsp")).thenReturn(rd);
        when(service.getDetail(5)).thenReturn(null);

        servlet.doGetPublic(req, resp);

        verify(req, times(1)).setAttribute(org.mockito.ArgumentMatchers.eq("error"), anyString());
        verify(rd, times(1)).forward(req, resp);
    }
}
