package dao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PaymentDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    void createPendingBankTransfer_happyPath() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(999);

            int id = new PaymentDao().createPendingBankTransfer(10, new BigDecimal("123.45"), "REF");
            Assertions.assertThat(id).isEqualTo(999);
            verify(ps).setInt(1, 10);
            verify(ps).setBigDecimal(2, new BigDecimal("123.45"));
            verify(ps).setString(3, "REF");
            verify(ps).executeQuery();
        }
    }

    @Test
    void createPendingBankTransfer_noRow_throwsSQLException() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            Assertions.assertThatThrownBy(() ->
                    new PaymentDao().createPendingBankTransfer(1, new BigDecimal("1"), "R"))
                .isInstanceOf(SQLException.class);
        }
    }

    @Test
    void insertPending_happyPath() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getInt(1)).thenReturn(11);
            int id = new PaymentDao().insertPending(5, new BigDecimal("200"), "x");
            Assertions.assertThat(id).isEqualTo(11);
            verify(ps).setInt(1, 5);
            verify(ps).setBigDecimal(2, new BigDecimal("200"));
        }
    }

    @Test
    void insertPending_empty_returnsZero() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);
            int id = new PaymentDao().insertPending(7, new BigDecimal("1"), "x");
            Assertions.assertThat(id).isZero();
        }
    }

    @Test
    void markPaid_updatesRow() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            boolean ok = new PaymentDao().markPaid(123);
            Assertions.assertThat(ok).isTrue();
            verify(ps).setInt(1, 123);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void getPendingPayments_mapsRows() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true, false);
            when(rs.getInt("payment_id")).thenReturn(1);
            when(rs.getInt("order_id")).thenReturn(2);
            when(rs.getString("full_name")).thenReturn("Name");
            when(rs.getString("phone")).thenReturn("090");
            when(rs.getBigDecimal("amount")).thenReturn(new BigDecimal("500"));
            when(rs.getString("method")).thenReturn("bank_transfer");
            when(rs.getTimestamp("payment_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
            when(rs.getString("status")).thenReturn("pending");
            when(rs.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000"));

            List<Object[]> rows = new PaymentDao().getPendingPayments();
            Assertions.assertThat(rows).hasSize(1);
            Assertions.assertThat(rows.get(0)[0]).isEqualTo(1);
            Assertions.assertThat(rows.get(0)[4]).isEqualTo(new BigDecimal("500"));
        }
    }

    @Test
    void updatePaymentStatus_ok() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            boolean ok = new PaymentDao().updatePaymentStatus(9, "paid", 100);
            Assertions.assertThat(ok).isTrue();
            verify(ps).setString(1, "paid");
            verify(ps).setObject(2, 100);
            verify(ps).setInt(3, 9);
            verify(ps).executeUpdate();
        }
    }
}
