package booking.stubs.service;

import booking.stubs.model.Customer;

public interface ICustomerService {
    Customer getProfile(int accountId) throws Exception;
    boolean cancelOrder(int customerId, int orderId) throws Exception;
}

