package dao;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
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
class WalletDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement psInsert;
    @Mock PreparedStatement psUpdate;
    @Mock PreparedStatement psSelect;
    @Mock PreparedStatement psInsWallet;
    @Mock ResultSet rs;
    @Mock ResultSet rs2;

    @Spy WalletDao dao;

    @Test
    void creditRefund_happyPath_commits() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            doReturn(99).when(dao).ensureWallet(7);

            when(con.prepareStatement(startsWith("INSERT INTO Wallet_Transactions"))).thenReturn(psInsert);
            when(con.prepareStatement(startsWith("UPDATE Wallets"))).thenReturn(psUpdate);

            dao.creditRefund(7, new BigDecimal("50.00"), 123);

            verify(con).setAutoCommit(false);
            verify(psInsert).setInt(1, 99);
            verify(psInsert).setBigDecimal(2, new BigDecimal("50.00"));
            verify(psInsert).setObject(3, 123, Types.INTEGER);
            verify(psInsert).executeUpdate();
            verify(psUpdate).executeUpdate();
            verify(con).commit();
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void creditRefund_error_rollback() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            doReturn(99).when(dao).ensureWallet(7);

            when(con.prepareStatement(startsWith("INSERT INTO Wallet_Transactions"))).thenReturn(psInsert);
            when(psInsert.executeUpdate()).thenThrow(new SQLException("boom"));

            try {
                dao.creditRefund(7, new BigDecimal("10"), null);
            } catch (Exception ignored) {}

            verify(con).rollback();
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void ensureWallet_createsNew_whenNotExists() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(psSelect, psInsWallet);
            when(psSelect.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);
            when(psInsWallet.executeQuery()).thenReturn(rs2);
            when(rs2.next()).thenReturn(true);
            when(rs2.getInt(1)).thenReturn(555);

            int wid = new WalletDao().ensureWallet(1);
            Assertions.assertThat(wid).isEqualTo(555);
        }
    }
}

