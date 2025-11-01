
package service;

import java.util.List;

import dao.IOrderDetailDao;
import dao.OrderDetailDao;
import model.OrderDetail;
import utils.DBConnection;

public class OrderDetailService implements IOrderDetailService {
    private final IOrderDetailDao orderDetailDao;

    public OrderDetailService() {
        this.orderDetailDao = new OrderDetailDao(DBConnection.getConnection());
    }

    public OrderDetailService(IOrderDetailDao dao) {
        this.orderDetailDao = dao;
    }

    @Override
    public List<OrderDetail> getAll() throws Exception {
        return orderDetailDao.findAll();
    }
}
