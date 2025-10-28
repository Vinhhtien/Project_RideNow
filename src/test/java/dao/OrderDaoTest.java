package dao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderDaoTest {

    @Mock Connection con1; // for target call
    @Mock Connection con2; // for nested overlap check
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    private static org.mockito.ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    @Test
    void getBikePriceIfBookable_returnsPrice() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con1);
            when(con1.prepareStatement(argThat(sqlContains("FROM Motorbikes")))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getString("status")).thenReturn("available");
            when(rs.getBigDecimal("price_per_day")).thenReturn(new BigDecimal("100000"));
            Assertions.assertThat(new OrderDao().getBikePriceIfBookable(1)).isNotNull();
        }
    }

    @Test
    void isOverlappingLocked_trueWhenCountGt0() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con1);
            when(con1.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(2);
            Assertions.assertThat(new OrderDao().isOverlappingLocked(1, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02"))).isTrue();
        }
    }

    @Test
    void createPendingOrder_overlap_rollbackAndThrow() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            // First call (outer tx)
            db.when(DBConnection::getConnection).thenReturn(con1, con2);
            when(con1.prepareStatement(argThat(sqlContains("SELECT type_id")))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(1);

            // Nested isOverlappingLocked
            PreparedStatement psOver = mock(PreparedStatement.class);
            ResultSet rsOver = mock(ResultSet.class);
            when(con2.prepareStatement(argThat(sqlContains("FROM RentalOrders r")))).thenReturn(psOver);
            when(psOver.executeQuery()).thenReturn(rsOver);
            when(rsOver.next()).thenReturn(true);
            when(rsOver.getInt(1)).thenReturn(1);

            doNothing().when(con1).setAutoCommit(false);
            doNothing().when(con1).rollback();
            doNothing().when(con1).setAutoCommit(true);

            Assertions.assertThatThrownBy(() ->
                new OrderDao().createPendingOrder(9, 5, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02"), new BigDecimal("1"))
            ).isInstanceOf(IllegalStateException.class);

            verify(con1).rollback();
        }
    }
}
