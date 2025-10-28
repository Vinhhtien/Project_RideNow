package service;

import dao.IMotorbikeDao;
import model.Motorbike;
import model.MotorbikeListItem;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MotorbikeServiceTest {

    MotorbikeService service;
    @Mock IMotorbikeDao dao;

    @BeforeEach
    void setup() {
        service = new MotorbikeService();
        controller.testsupport.TestUtils.forceSet(service, "dao", dao);
    }

    @Test
    void delegates_search_and_count() throws Exception {
        when(dao.count(null, null, null, null, null)).thenReturn(5);
        int c = service.count(null, null, null, null, null);
        Assertions.assertThat(c).isEqualTo(5);

        when(dao.search(any(), any(), any(), any(), any(), anyString(), anyInt(), anyInt()))
                .thenReturn(List.of(new MotorbikeListItem()));
        Assertions.assertThat(
                service.search(null, null, null, new BigDecimal("1"), "k", "name_asc", 1, 10)
        ).hasSize(1);
    }

    @Test
    void delegates_detail_and_owner() throws Exception {
        when(dao.findDetailById(1)).thenReturn(new MotorbikeListItem());
        Assertions.assertThat(service.getDetail(1)).isNotNull();

        when(dao.findAllByOwnerAccount(2, "admin")).thenReturn(List.of(new Motorbike()));
        Assertions.assertThat(service.findAllByOwnerAccount(2, "admin")).hasSize(1);
    }
}

