
package service;

import java.util.List;

import model.OrderDetail;

public interface IOrderDetailService {
    List<OrderDetail> getAll() throws Exception;
}
