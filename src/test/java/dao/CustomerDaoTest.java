package dao;

import model.Customer;
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
import utils.PasswordUtil;

import java.sql.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    void findByAccountId_found_and_notFound() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);

            when(rs.next()).thenReturn(true);
            when(rs.getInt("customer_id")).thenReturn(1);
            when(rs.getInt("account_id")).thenReturn(9);
            when(rs.getString("full_name")).thenReturn("A");
            when(rs.getString("email")).thenReturn("a@a");
            when(rs.getString("phone")).thenReturn("1");
            when(rs.getString("address")).thenReturn("addr");
            Customer c = new CustomerDao().findByAccountId(9);
            Assertions.assertThat(c).isNotNull();

            when(rs.next()).thenReturn(false);
            Assertions.assertThat(new CustomerDao().findByAccountId(10)).isNull();
        }
    }

    @Test
    void updatePassword_matchesAndUpdates() throws Exception {
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<PasswordUtil> pwd = Mockito.mockStatic(PasswordUtil.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            PreparedStatement psGet = mock(PreparedStatement.class);
            ResultSet rsGet = mock(ResultSet.class);
            when(con.prepareStatement(startsWith("SELECT password"))).thenReturn(psGet);
            when(psGet.executeQuery()).thenReturn(rsGet);
            when(rsGet.next()).thenReturn(true);
            when(rsGet.getString("password")).thenReturn("stored");

            pwd.when(() -> PasswordUtil.matches("old", "stored")).thenReturn(true);
            pwd.when(() -> PasswordUtil.hashPassword("new")).thenReturn("hash");

            PreparedStatement psUpd = mock(PreparedStatement.class);
            when(con.prepareStatement(startsWith("UPDATE Accounts SET password"))).thenReturn(psUpd);
            when(psUpd.executeUpdate()).thenReturn(1);

            Assertions.assertThat(new CustomerDao().updatePassword(1, "old", "new")).isTrue();
        }
    }
}
