package service;

import model.Motorbike;
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
class MotorbikeAdminServiceTest {

    @Mock Connection con;
    @Mock PreparedStatement ps;
    @Mock ResultSet rs;

    @Test
    void addUpdateDelete_flow() throws Exception {
        MotorbikeAdminService svc = new MotorbikeAdminService();

        // add
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(startsWith("INSERT INTO Motorbikes"), anyInt())).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            ResultSet keys = mock(ResultSet.class);
            when(ps.getGeneratedKeys()).thenReturn(keys);
            when(keys.next()).thenReturn(true);
            when(keys.getInt(1)).thenReturn(99);

            Motorbike m = new Motorbike();
            m.setBikeName("X"); m.setLicensePlate("43E1-68932"); m.setPricePerDay(new BigDecimal("100000"));
            m.setStatus("available"); m.setTypeId(1);
            boolean ok = svc.addMotorbike(m);
            Assertions.assertThat(ok).isTrue();
            Assertions.assertThat(m.getBikeId()).isEqualTo(99);
        }

        // update
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(startsWith("UPDATE Motorbikes SET"))).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            Motorbike m = new Motorbike();
            m.setBikeId(10); m.setBikeName("Y"); m.setLicensePlate("12A3-12345"); m.setPricePerDay(new BigDecimal("1"));
            m.setStatus("available"); m.setDescription("d"); m.setTypeId(1);
            Assertions.assertThat(new MotorbikeAdminService().updateMotorbike(m)).isTrue();
        }

        // delete
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(startsWith("DELETE FROM Motorbikes"))).thenReturn(ps);
            when(ps.executeUpdate()).thenReturn(1);
            Assertions.assertThat(new MotorbikeAdminService().deleteMotorbike(10)).isTrue();
        }
    }
}
