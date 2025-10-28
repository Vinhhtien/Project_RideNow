package dao;

import model.MotorbikeListItem;
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
class MotorbikeDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    private static org.mockito.ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    @Test
    void findDetail_found_and_notFound() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(argThat(sqlContains("WHERE b.bike_id")))).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt("bike_id")).thenReturn(1);
            when(rs.getString("bike_name")).thenReturn("Yamaha");
            when(rs.getString("license_plate")).thenReturn("43E1-68932");
            when(rs.getBigDecimal("price_per_day")).thenReturn(new BigDecimal("100000"));
            when(rs.getString("status")).thenReturn("available");
            when(rs.getString("type_name")).thenReturn("Xe ga");
            when(rs.getString("owner_type")).thenReturn("store");
            when(rs.getString("owner_name")).thenReturn("HN Store");
            when(rs.getString("description")).thenReturn("d");

            MotorbikeListItem item = new MotorbikeDao().findDetailById(1);
            Assertions.assertThat(item).isNotNull();

            when(rs.next()).thenReturn(false);
            Assertions.assertThat(new MotorbikeDao().findDetailById(2)).isNull();
        }
    }

    @Test
    void count_withFilters_executes() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(3);

            int c = new MotorbikeDao().count(1, Date.valueOf("2025-01-01"), Date.valueOf("2025-01-02"), new BigDecimal("100000"), "kw");
            Assertions.assertThat(c).isEqualTo(3);
        }
    }
}
