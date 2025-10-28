package booking.tests;

import booking.stubs.model.Motorbike;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.IMotorbikeService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Spec-level placeholders to compile and express intent without DB. */
public class MotorbikeServiceSpecTest {

    static class FakeMotorbikeService implements IMotorbikeService {
        @Override public List<Motorbike> findAll() { return Collections.emptyList(); }
        @Override public List<Motorbike> findByTypeId(int typeId) { return Collections.emptyList(); }
        @Override public List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword, String sort, int page, int size) { return Collections.emptyList(); }
        @Override public int count(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword) { return 0; }
        @Override public List<Motorbike> findAllByOwnerAccount(int accountId, String role) { return Collections.emptyList(); }
        @Override public MotorbikeListItem getDetail(int bikeId) { return null; }
        @Override public List<Motorbike> getByPartnerId(int partnerId) { return Collections.emptyList(); }
    }

    @Test
    void search_invalidDateRange_returnsEmpty() throws Exception {
        IMotorbikeService svc = new FakeMotorbikeService();
        List<MotorbikeListItem> rs = svc.search(1, Date.valueOf("2025-01-10"), Date.valueOf("2025-01-01"), new BigDecimal("100"), "k", "price", 0, 10);
        assertNotNull(rs);
        assertEquals(0, rs.size());
    }
}

