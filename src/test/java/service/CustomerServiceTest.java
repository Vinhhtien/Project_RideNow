package service;

import dao.ICustomerDao;
import model.Customer;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerServiceTest {

    CustomerService service;
    @Mock ICustomerDao dao;

    @BeforeEach
    void setup() {
        service = new CustomerService();
        controller.testsupport.TestUtils.forceSet(service, "dao", dao);
    }

    @Test
    void getProfile_delegates() throws Exception {
        when(dao.findByAccountId(1)).thenReturn(new Customer());
        Assertions.assertThat(service.getProfile(1)).isNotNull();
    }

    @Test
    void saveProfile_delegates() throws Exception {
        Customer c = new Customer(); c.setAccountId(1); c.setFullName("n"); c.setEmail("e@e");
        service.saveProfile(c);
        verify(dao).upsertByAccountId(any(Customer.class));
    }

    @Test
    void changePassword_delegates() throws Exception {
        when(dao.updatePassword(1, "old", "new")).thenReturn(true);
        Assertions.assertThat(service.changePassword(1, "old", "new")).isTrue();
    }
}

