
package service;

import java.util.List;
import model.RentalOrder;

public interface IRentalOrderService {
    List<RentalOrder> getAll() throws Exception;
    RentalOrder findById(int orderId) throws Exception;
}
