package booking.tests.service;

import booking.stubs.service.IOrderService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    interface OrderRepo {
        BigDecimal getBikePriceIfBookable(int bikeId) throws Exception;
        boolean isOverlappingLocked(int bikeId, Date start, Date end) throws Exception;
        int createPendingOrder(int customerId, int bikeId, Date start, Date end, BigDecimal pricePerDay) throws Exception;
        int countOverlaps(int bikeId, Date start, Date end) throws SQLException;
        List<IOrderService.OverlappedRange> fetchOverlaps(int bikeId, Date start, Date end) throws SQLException;
    }

    static class OrderServiceImplForTest implements IOrderService {
        private final OrderRepo repo;
        OrderServiceImplForTest(OrderRepo repo) { this.repo = repo; }

        @Override
        public int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception {
            if (start == null || end == null) throw new IllegalArgumentException("Missing dates");
            if (start.after(end)) throw new IllegalArgumentException("start must be <= end");
            BigDecimal price = repo.getBikePriceIfBookable(bikeId);
            if (price == null) throw new IllegalStateException("Bike not bookable");
            if (repo.isOverlappingLocked(bikeId, start, end)) throw new IllegalStateException("Overlap");
            return repo.createPendingOrder(customerId, bikeId, start, end, price);
        }

        @Override
        public boolean isBikeAvailable(int bikeId, Date start, Date end) throws SQLException {
            return repo.countOverlaps(bikeId, start, end) == 0;
        }

        @Override
        public List<OverlappedRange> getOverlappingRanges(int bikeId, Date start, Date end) throws SQLException {
            return repo.fetchOverlaps(bikeId, start, end);
        }
    }

    @Mock private OrderRepo repo;

    @Test
    @DisplayName("TC-SVC-BOOK-001: testBookOneBike_ValidInput_ShouldReturnOrderId")
    void testBookOneBike_ValidInput_ShouldReturnOrderId() throws Exception {
        IOrderService svc = new OrderServiceImplForTest(repo);
        Date s = Date.valueOf("2025-10-25");
        Date e = Date.valueOf("2025-10-26");

        when(repo.getBikePriceIfBookable(10)).thenReturn(new BigDecimal("150000.00"));
        when(repo.isOverlappingLocked(10, s, e)).thenReturn(false);
        when(repo.createPendingOrder(1, 10, s, e, new BigDecimal("150000.00"))).thenReturn(123);

        int id = svc.bookOneBike(1, 10, s, e);

        Assertions.assertThat(id).isEqualTo(123);
        verify(repo, times(1)).getBikePriceIfBookable(10);
        verify(repo, times(1)).isOverlappingLocked(10, s, e);
        verify(repo, times(1)).createPendingOrder(1, 10, s, e, new BigDecimal("150000.00"));
    }

    @Test
    @DisplayName("TC-SVC-BOOK-004: testBookOneBike_BikeUnavailable_ShouldThrow")
    void testBookOneBike_BikeUnavailable_ShouldThrow() throws Exception {
        IOrderService svc = new OrderServiceImplForTest(repo);
        Date s = Date.valueOf("2025-10-25");
        Date e = Date.valueOf("2025-10-26");
        when(repo.getBikePriceIfBookable(10)).thenReturn(null);

        org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                () -> svc.bookOneBike(1, 10, s, e));
        verify(repo, times(1)).getBikePriceIfBookable(10);
    }

    @Test
    @DisplayName("TC-SVC-BOOK-003: testBookOneBike_InvalidDates_ShouldThrow")
    void testBookOneBike_InvalidDates_ShouldThrow() throws Exception {
        IOrderService svc = new OrderServiceImplForTest(repo);
        Date s = Date.valueOf("2025-10-26");
        Date e = Date.valueOf("2025-10-25");
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> svc.bookOneBike(1, 10, s, e));
    }

    @Test
    @DisplayName("TC-SVC-AVAIL-001/002/003: testIsBikeAvailable_ShouldReturnTrueFalse_AndHandleSQLException")
    void testIsBikeAvailable_ShouldReturnTrueFalse_AndHandleSQLException() throws Exception {
        IOrderService svc = new OrderServiceImplForTest(repo);
        Date s = Date.valueOf("2025-10-25");
        Date e = Date.valueOf("2025-10-26");

        when(repo.countOverlaps(10, s, e)).thenReturn(0);
        org.junit.jupiter.api.Assertions.assertTrue(svc.isBikeAvailable(10, s, e));
        verify(repo, times(1)).countOverlaps(10, s, e);

        when(repo.countOverlaps(10, s, e)).thenReturn(2);
        org.junit.jupiter.api.Assertions.assertFalse(svc.isBikeAvailable(10, s, e));

        when(repo.countOverlaps(10, s, e)).thenThrow(new SQLException("boom"));
        org.junit.jupiter.api.Assertions.assertThrows(SQLException.class,
                () -> svc.isBikeAvailable(10, s, e));
    }

    @Test
    @DisplayName("TC-SVC-OVERLAP-001/002: testGetOverlappingRanges_EmptyAndNonEmpty_ShouldMapCorrectly")
    void testGetOverlappingRanges_EmptyAndNonEmpty_ShouldMapCorrectly() throws Exception {
        IOrderService svc = new OrderServiceImplForTest(repo);
        Date s = Date.valueOf("2025-10-25");
        Date e = Date.valueOf("2025-10-30");

        when(repo.fetchOverlaps(10, s, e)).thenReturn(Arrays.asList(
                new IOrderService.OverlappedRange(1, Date.valueOf("2025-10-26"), Date.valueOf("2025-10-27")),
                new IOrderService.OverlappedRange(2, Date.valueOf("2025-10-28"), Date.valueOf("2025-10-29"))
        ));
        List<IOrderService.OverlappedRange> list = svc.getOverlappingRanges(10, s, e);
        Assertions.assertThat(list).hasSize(2);
        Assertions.assertThat(list.get(0).orderId).isEqualTo(1);

        when(repo.fetchOverlaps(10, s, e)).thenReturn(java.util.Collections.emptyList());
        Assertions.assertThat(svc.getOverlappingRanges(10, s, e)).isEmpty();
        verify(repo, times(2)).fetchOverlaps(10, s, e);
    }
}

