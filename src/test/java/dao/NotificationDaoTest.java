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

import java.sql.*;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationDaoTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;

    private static ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    @Test
    void test_insert_notify_success() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Notifications")))).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);

            new NotificationDao().createNotification(5, "Title", "Message");

            verify(ps).setInt(1, 5);
            verify(ps).setString(2, "Title");
            verify(ps).setString(3, "Message");
            verify(ps).executeUpdate();
            verify(ps).close();
            verify(con).close();
        }
    }

    @Test
    void test_insert_notify_sqlException() throws Exception {
        try (MockedStatic<DBConnection> db = mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(anyString())).thenThrow(new SQLException("down"));
            Assertions.assertThatThrownBy(() -> new NotificationDao().createNotification(1, "t", "m"))
                    .isInstanceOf(SQLException.class);
        }
    }
}

