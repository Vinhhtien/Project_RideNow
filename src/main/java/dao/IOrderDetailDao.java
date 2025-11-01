
package dao;

import java.util.List;

import model.OrderDetail;

public interface IOrderDetailDao {
    List<OrderDetail> findAll() throws Exception;
}
