package dao;

import model.Motorbike;
import model.MotorbikeListItem;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface IMotorbikeDao {
    List<Motorbike> findAll() throws Exception;
    List<Motorbike> findByTypeId(int typeId) throws Exception;

    List<MotorbikeListItem> search(Integer typeId, Date startDate, Date endDate,
                                   BigDecimal maxPrice, String keyword,
                                   String sort, int page, int size) throws Exception;

    int count(Integer typeId, Date startDate, Date endDate,
              BigDecimal maxPrice, String keyword) throws Exception;

    List<Motorbike> findAllByOwnerAccount(int accountId, String role) throws Exception;

    MotorbikeListItem findDetailById(int bikeId) throws Exception;
    
    // fix từ quý 
    List<Motorbike> findByPartnerId(int partnerId) throws Exception;
}
