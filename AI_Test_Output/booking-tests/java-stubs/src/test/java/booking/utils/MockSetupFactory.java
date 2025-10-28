package booking.utils;

import booking.stubs.model.Customer;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.IMotorbikeService;
import booking.stubs.service.IOrderService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

public final class MockSetupFactory {
    private MockSetupFactory() {}

    public static void stubSession(HttpServletRequest req, HttpSession session) {
        when(req.getSession()).thenReturn(session);
    }

    public static RequestDispatcher stubForward(HttpServletRequest req, String path) {
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(path)).thenReturn(rd);
        return rd;
    }

    public static void setPrivateField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Domain factories
    public static Customer mockCustomer(int id) {
        return new Customer(id);
    }

    public static MotorbikeListItem mockBike(int id, String name) {
        return new MotorbikeListItem(id, name);
    }

    // Service stubs
    public static void whenIsBikeAvailable(IOrderService orderService, int bikeId, Date start, Date end, boolean result) throws Exception {
        when(orderService.isBikeAvailable(bikeId, start, end)).thenReturn(result);
    }

    public static void whenGetDetail(IMotorbikeService motorbikeService, int bikeId, MotorbikeListItem item) throws Exception {
        when(motorbikeService.getDetail(bikeId)).thenReturn(item);
    }
}

