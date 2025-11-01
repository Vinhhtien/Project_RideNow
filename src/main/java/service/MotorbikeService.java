package service;

import dao.IMotorbikeDao;
import dao.MotorbikeDao;
import model.Motorbike;
import model.MotorbikeListItem;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public class MotorbikeService implements IMotorbikeService {
    private final IMotorbikeDao dao = new MotorbikeDao();

    @Override
    public List<Motorbike> findAll() throws Exception {
        return dao.findAll();
    }

    @Override
    public List<Motorbike> findByTypeId(int typeId) throws Exception {
        return dao.findByTypeId(typeId);
    }

    @Override
    public List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate,
                                          BigDecimal maxPrice, String keyword,
                                          String sort, int page, int size) throws Exception {
        return dao.search(typeId, startDate, endDate, maxPrice, keyword, sort, page, size);
    }

    @Override
    public int count(Integer typeId, Date startDate, Date endDate,
                     BigDecimal maxPrice, String keyword) throws Exception {
        return dao.count(typeId, startDate, endDate, maxPrice, keyword);
    }

    @Override
    public List<Motorbike> findAllByOwnerAccount(int accountId, String role) throws Exception {
        return dao.findAllByOwnerAccount(accountId, role);
    }

    @Override
    public MotorbikeListItem getDetail(int bikeId) throws Exception {
        return dao.findDetailById(bikeId);
    }

    @Override
    public List<Motorbike> getByPartnerId(int partnerId) throws Exception {
        return dao.findByPartnerId(partnerId);
    }
}
