package booking.tests.servlet;

import booking.servlets.BookingServletUnderTest;
import booking.stubs.model.Account;
import booking.stubs.model.Customer;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.ICustomerService;
import booking.stubs.service.IMotorbikeService;
import booking.stubs.service.IOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServletTest {

    @Mock private IMotorbikeService bikeService;
    @Mock private ICustomerService customerService;
    @Mock private IOrderService orderService;

    @InjectMocks private BookingServletUnderTest servlet;

    @Test
    @DisplayName("TC-CTL-BOOKING-004: testDoPost_LoggedInAndValidParams_ShouldRedirectSuccess")
    void testDoPost_LoggedInAndValidParams_ShouldRedirectSuccess() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(new Account(7));
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("bikeId")).thenReturn("2");
        when(req.getParameter("start")).thenReturn("2025-10-25");
        when(req.getParameter("end")).thenReturn("2025-10-26");

        when(bikeService.getDetail(2)).thenReturn(new MotorbikeListItem(2, "B2"));
        when(customerService.getProfile(7)).thenReturn(new Customer(70));
        when(orderService.bookOneBike(
                70, 2,
                Date.valueOf("2025-10-25"),
                Date.valueOf("2025-10-26")
        )).thenReturn(123);

        servlet.doPostPublic(req, resp);

        verify(orderService, times(1)).bookOneBike(
                70, 2,
                Date.valueOf("2025-10-25"),
                Date.valueOf("2025-10-26")
        );
        verify(resp, times(1)).sendRedirect("/ctx/customerorders?justCreated=123");
    }

    @Test
    @DisplayName("TC-CTL-BOOKING-005: testDoPost_MissingDates_ShouldRedirectWithError")
    void testDoPost_MissingDates_ShouldRedirectWithError() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(new Account(3));
        when(req.getContextPath()).thenReturn("/ctx");
        when(req.getParameter("bikeId")).thenReturn("99");
        when(req.getParameter("start")).thenReturn(null); // thiếu start -> nhánh lỗi
        // KHÔNG stub "end" để tránh UnnecessaryStubbingException

        servlet.doPostPublic(req, resp);

        verify(session, times(1)).setAttribute(
                org.mockito.ArgumentMatchers.eq("book_error"),
                org.mockito.ArgumentMatchers.nullable(String.class)
        );
        verify(resp, times(1)).sendRedirect(anyString());

        // Không gọi service trong nhánh lỗi
        verify(bikeService, never()).getDetail(anyInt());
        verify(customerService, never()).getProfile(anyInt());
        verify(orderService, never()).bookOneBike(anyInt(), anyInt(), any(), any());
    }

    @Test
    @DisplayName("TC-CTL-BOOKING-001: testDoPost_NotLoggedIn_ShouldRedirectToLogin")
    void testDoPost_NotLoggedIn_ShouldRedirectToLogin() throws Exception {
        HttpServletRequest req = org.mockito.Mockito.mock(HttpServletRequest.class);
        HttpServletResponse resp = org.mockito.Mockito.mock(HttpServletResponse.class);
        HttpSession session = org.mockito.Mockito.mock(HttpSession.class);

        when(req.getSession()).thenReturn(session);
        when(session.getAttribute("account")).thenReturn(null);
        when(req.getContextPath()).thenReturn("/ctx");

        servlet.doPostPublic(req, resp);

        verify(resp, times(1)).sendRedirect("/ctx/login");
    }
}
