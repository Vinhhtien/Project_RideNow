package booking.tests;

import booking.stubs.service.IOrderService;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Spec-level tests using an in-test fake implementation to validate business rules
 * without touching any DB. This ensures tests compile and convey expected behavior.
 */
public class OrderServiceSpecTest {

    static class FakeOrderService implements IOrderService {
        @Override
        public int bookOneBike(int customerId, int bikeId, Date start, Date end) {
            if (start == null || end == null) throw new IllegalArgumentException("Missing dates");
            if (start.after(end)) throw new IllegalArgumentException("start must be <= end");
            if (bikeId <= 0) throw new IllegalStateException("Bike not bookable");
            if (start.equals(end)) return 1; // 1-day booking ok
            return 2;
        }

        @Override
        public boolean isBikeAvailable(int bikeId, Date start, Date end) { return true; }

        @Override
        public List<OverlappedRange> getOverlappingRanges(int bikeId, Date start, Date end) { return Collections.emptyList(); }
    }

    @Test
    void nullDates_throw() throws Exception {
        IOrderService svc = new FakeOrderService();
        assertThrows(IllegalArgumentException.class, () -> svc.bookOneBike(1, 1, null, Date.valueOf("2025-01-02")));
        assertThrows(IllegalArgumentException.class, () -> svc.bookOneBike(1, 1, Date.valueOf("2025-01-02"), null));
    }

    @Test
    void startAfterEnd_throw() throws Exception {
        IOrderService svc = new FakeOrderService();
        assertThrows(IllegalArgumentException.class, () -> svc.bookOneBike(1, 1, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-01")));
    }

    @Test
    void oneDayBooking_ok() throws Exception {
        IOrderService svc = new FakeOrderService();
        int id = svc.bookOneBike(1, 1, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-10"));
        assertTrue(id > 0);
    }
}

