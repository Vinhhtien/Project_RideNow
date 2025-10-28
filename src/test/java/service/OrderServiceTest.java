package service;

import dao.IOrderDao;
import dao.NotificationDao;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.sql.Date;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    OrderService service;
    @Mock IOrderDao orderDao;
    @Mock NotificationDao notificationDao;

    @BeforeEach
    void setup() {
        service = new OrderService();
        controller.testsupport.TestUtils.forceSet(service, "orderDao", orderDao);
        controller.testsupport.TestUtils.forceSet(service, "notificationDAO", notificationDao);
    }

    @Test
    void bookOneBike_invalidDates_throw() {
        Assertions.assertThatThrownBy(() -> service.bookOneBike(1, 1, null, Date.valueOf("2025-01-01")))
                .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> service.bookOneBike(1, 1, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-01")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void bookOneBike_notBookable_throw() throws Exception {
        when(orderDao.getBikePriceIfBookable(5)).thenReturn(null);
        Assertions.assertThatThrownBy(() -> service.bookOneBike(1, 5, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bookOneBike_overlap_throw() throws Exception {
        when(orderDao.getBikePriceIfBookable(5)).thenReturn(new BigDecimal("100000"));
        when(orderDao.isOverlappingLocked(eq(5), any(Date.class), any(Date.class))).thenReturn(true);
        Assertions.assertThatThrownBy(() -> service.bookOneBike(1, 5, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bookOneBike_success_returnsOrderId() throws Exception {
        when(orderDao.getBikePriceIfBookable(5)).thenReturn(new BigDecimal("100000"));
        when(orderDao.isOverlappingLocked(eq(5), any(Date.class), any(Date.class))).thenReturn(false);
        when(orderDao.createPendingOrder(eq(9), eq(5), any(Date.class), any(Date.class), eq(new BigDecimal("100000"))))
                .thenReturn(123);
        int id = service.bookOneBike(9, 5, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02"));
        Assertions.assertThat(id).isEqualTo(123);
    }
}

