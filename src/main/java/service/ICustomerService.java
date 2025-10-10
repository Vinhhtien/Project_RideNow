// ICustomerService.java
package service;

import java.util.List;
import model.Customer;

public interface ICustomerService {
    Customer getProfile(int accountId) throws Exception;
    void saveProfile(Customer c) throws Exception;
    boolean changePassword(int accountId, String currentPw, String newPw) throws Exception;
    boolean cancelOrder(int customerId, int orderId) throws Exception; 
    
    List<model.Customer> getAll() throws Exception;
}