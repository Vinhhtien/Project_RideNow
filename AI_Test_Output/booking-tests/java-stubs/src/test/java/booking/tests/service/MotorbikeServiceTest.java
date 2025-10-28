package booking.tests.service;

import booking.stubs.model.Motorbike;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.IMotorbikeService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MotorbikeServiceTest {

    interface MotorbikeRepo {
        List<Motorbike> findAll() throws Exception;
        List<Motorbike> findByTypeId(int typeId) throws Exception;
        List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword, String sort, int page, int size) throws Exception;
        int count(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword) throws Exception;
        List<Motorbike> findAllByOwnerAccount(int accountId, String role) throws Exception;
        MotorbikeListItem findDetailById(int bikeId) throws Exception;
        List<Motorbike> findByPartnerId(int partnerId) throws Exception;
    }

    static class MotorbikeServiceImplForTest implements IMotorbikeService {
        private final MotorbikeRepo repo;
        MotorbikeServiceImplForTest(MotorbikeRepo repo) { this.repo = repo; }

        @Override public List<Motorbike> findAll() throws Exception { return repo.findAll(); }
        @Override public List<Motorbike> findByTypeId(int typeId) throws Exception { return typeId < 0 ? Collections.emptyList() : repo.findByTypeId(typeId); }
        @Override public List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword, String sort, int page, int size) throws Exception {
            if (startDate != null && endDate != null && startDate.after(endDate)) return Collections.emptyList();
            return repo.search(typeId, startDate, endDate, maxPrice, keyword, sort, page, size);
        }
        @Override public int count(Integer typeId, Date startDate, Date endDate, BigDecimal maxPrice, String keyword) throws Exception {
            if (maxPrice != null && maxPrice.signum() < 0) throw new IllegalArgumentException("maxPrice");
            return repo.count(typeId, startDate, endDate, maxPrice, keyword);
        }
        @Override public List<Motorbike> findAllByOwnerAccount(int accountId, String role) throws Exception {
            if (role == null || role.isBlank()) return Collections.emptyList();
            return repo.findAllByOwnerAccount(accountId, role);
        }
        @Override public MotorbikeListItem getDetail(int bikeId) throws Exception { return repo.findDetailById(bikeId); }
        @Override public List<Motorbike> getByPartnerId(int partnerId) throws Exception { return repo.findByPartnerId(partnerId); }
    }

    @Mock private MotorbikeRepo repo;

    @Test
    @DisplayName("TC-SVC-FINDALL-001: testFindAll_ShouldReturnList")
    void testFindAll_ShouldReturnList() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.findAll()).thenReturn(Arrays.asList(new Motorbike(1), new Motorbike(2)));
        List<Motorbike> rs = svc.findAll();
        Assertions.assertThat(rs).hasSize(2);
        verify(repo, times(1)).findAll();
    }

    @Test
    @DisplayName("TC-SVC-FINDTYPE-003: testFindByTypeId_InvalidType_ShouldReturnEmpty")
    void testFindByTypeId_InvalidType_ShouldReturnEmpty() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        List<Motorbike> rs = svc.findByTypeId(-1);
        Assertions.assertThat(rs).isEmpty();
        verify(repo, times(0)).findByTypeId(anyInt());
    }

    @Test
    @DisplayName("TC-SVC-SRCH-001: testSearch_WithFiltersAndPaging_ShouldReturnExpectedSlice")
    void testSearch_WithFiltersAndPaging_ShouldReturnExpectedSlice() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.search(null, null, null, new BigDecimal("150000.00"), "k", "price", 0, 1))
                .thenReturn(Collections.singletonList(new MotorbikeListItem(1, "A")));
        List<MotorbikeListItem> rs = svc.search(null, null, null, new BigDecimal("150000.00"), "k", "price", 0, 1);
        Assertions.assertThat(rs).hasSize(1);
        verify(repo, times(1)).search(null, null, null, new BigDecimal("150000.00"), "k", "price", 0, 1);
    }

    @Test
    @DisplayName("TC-SVC-COUNT-001: testCount_WithFilters_ShouldReturnCorrectTotal")
    void testCount_WithFilters_ShouldReturnCorrectTotal() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.count(null, null, null, new BigDecimal("150000.00"), "k")).thenReturn(42);
        int total = svc.count(null, null, null, new BigDecimal("150000.00"), "k");
        Assertions.assertThat(total).isEqualTo(42);
        verify(repo, times(1)).count(null, null, null, new BigDecimal("150000.00"), "k");
    }

    @Test
    @DisplayName("TC-SVC-DETAIL-001: testGetDetail_Found_ShouldReturnItem")
    void testGetDetail_Found_ShouldReturnItem() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.findDetailById(10)).thenReturn(new MotorbikeListItem(10, "Name"));
        MotorbikeListItem item = svc.getDetail(10);
        Assertions.assertThat(item).isNotNull();
        verify(repo, times(1)).findDetailById(10);
    }

    @Test
    @DisplayName("TC-SVC-DETAIL-002: testGetDetail_NotFound_ShouldHandleGracefully")
    void testGetDetail_NotFound_ShouldHandleGracefully() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.findDetailById(99)).thenReturn(null);
        MotorbikeListItem item = svc.getDetail(99);
        Assertions.assertThat(item).isNull();
        verify(repo, times(1)).findDetailById(99);
    }

    @Test
    @DisplayName("TC-SVC-OWNER-002: testFindAllByOwnerAccount_RoleEdgeCases")
    void testFindAllByOwnerAccount_RoleEdgeCases() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        List<Motorbike> rs = svc.findAllByOwnerAccount(5, null);
        Assertions.assertThat(rs).isEmpty();
        verify(repo, times(0)).findAllByOwnerAccount(5, null);
    }

    @Test
    @DisplayName("TC-SVC-PARTNER-001: testGetByPartnerId_ShouldReturnPartnerBikes")
    void testGetByPartnerId_ShouldReturnPartnerBikes() throws Exception {
        IMotorbikeService svc = new MotorbikeServiceImplForTest(repo);
        when(repo.findByPartnerId(3)).thenReturn(Arrays.asList(new Motorbike(1)));
        List<Motorbike> rs = svc.getByPartnerId(3);
        Assertions.assertThat(rs).hasSize(1);
        verify(repo, times(1)).findByPartnerId(3);
    }
}

