

package service;

import java.util.List;

import dao.IRentalOrderDao;
import dao.RentalOrderDao;
import model.RentalOrder;
import utils.DBConnection;

public class RentalOrderService implements IRentalOrderService {
    private final IRentalOrderDao rentalOrderDao;

    public RentalOrderService() {
        this.rentalOrderDao = new RentalOrderDao(DBConnection.getConnection());
    }

    public RentalOrderService(IRentalOrderDao dao) {
        this.rentalOrderDao = dao;
    }

    @Override
    public List<RentalOrder> getAll() throws Exception {
        return rentalOrderDao.findAll();
    }

    @Override
    public RentalOrder findById(int orderId) throws Exception {
        List<RentalOrder> list = rentalOrderDao.findById(orderId);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }
}
