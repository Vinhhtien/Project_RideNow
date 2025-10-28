package booking.stubs.dao;

import java.util.List;

public interface IOrderQueryDao {
    List<Object[]> findOrdersOfCustomerWithPaymentStatus(int customerId) throws Exception;
}

