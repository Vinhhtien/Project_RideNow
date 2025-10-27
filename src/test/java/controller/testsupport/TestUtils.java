package controller.testsupport;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import model.Account;
import model.CartItem;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

/**
 * Tiện ích hỗ trợ unit test cho servlet.
 */
public class TestUtils {

    /** Dùng reflection để gán field private/final cho test */
    public static void forceSet(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Mock RequestDispatcher để verify forward */
    public static RequestDispatcher stubForward(HttpServletRequest req, String path) {
        RequestDispatcher rd = mock(RequestDispatcher.class);
        when(req.getRequestDispatcher(path)).thenReturn(rd);
        return rd;
    }

    /** Tạo nhanh đối tượng Account */
    public static Account acc(int id) {
        Account a = new Account();
        a.setAccountId(id);
        a.setUsername("user" + id);
        a.setRole("customer");
        return a;
    }

    /** Tạo nhanh CartItem để test */
    public static CartItem item(int id, String name, BigDecimal price, Date start, Date end) {
        return new CartItem(id, name, price, "Xe test", start, end);
    }

    /** Tạo nhanh CartItem theo ngày hôm nay */
    public static CartItem itemToday(int id, String name, BigDecimal price) {
        LocalDate s = LocalDate.now();
        return new CartItem(id, name, price, "Xe test",
                Date.valueOf(s), Date.valueOf(s.plusDays(1)));
    }
}
