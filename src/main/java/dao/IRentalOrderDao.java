
package dao;

import java.util.List;
import model.RentalOrder;

public interface IRentalOrderDao {
    List<RentalOrder> findAll() throws Exception;
    List<RentalOrder> findById(int orderId) throws Exception;
}
