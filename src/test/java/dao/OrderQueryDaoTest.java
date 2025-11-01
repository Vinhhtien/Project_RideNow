package dao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderQueryDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    private static ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    @Test
    void test_findOrdersByCustomerId_happyPath() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(argThat(sqlContains("SELECT r.order_id")))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true, false);
            when(rs.getInt(1)).thenReturn(101);
            when(rs.getString(2)).thenReturn("Yamaha, Honda");
            when(rs.getDate(3)).thenReturn(java.sql.Date.valueOf(LocalDate.now()));
            when(rs.getDate(4)).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            when(rs.getBigDecimal(5)).thenReturn(new BigDecimal("1500000"));
            when(rs.getString(6)).thenReturn("pending");

            List<Object[]> rows = new OrderQueryDao().findOrdersOfCustomer(9);
            Assertions.assertThat(rows).hasSize(1);
            Object[] r = rows.get(0);
            Assertions.assertThat(r[0]).isEqualTo(101);
            Assertions.assertThat(r[1]).isEqualTo("Yamaha, Honda");
            Assertions.assertThat(r[5]).isEqualTo("pending");

            verify(con).prepareStatement(argThat(sqlContains("SELECT r.order_id")));
            verify(ps).setInt(1, 9);
            verify(ps).executeQuery();
            verify(rs).close();
            verify(ps).close();
            verify(con).close();
        }
    }

    @Test
    void test_findOrdersByCustomerId_empty() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            List<Object[]> rows = new OrderQueryDao().findOrdersOfCustomer(1);
            Assertions.assertThat(rows).isEmpty();

            verify(ps).setInt(1, 1);
            verify(ps).executeQuery();
        }
    }

    @Test
    void test_findOrdersByCustomerId_sqlException() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenThrow(new SQLException("down"));
            Assertions.assertThatThrownBy(() -> new OrderQueryDao().findOrdersOfCustomer(2))
                    .isInstanceOf(Exception.class);
        }
    }
}

