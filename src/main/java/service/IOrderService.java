package service;

import java.sql.Date;

public interface IOrderService {
    int bookOneBike(int customerId, int bikeId, Date start, Date end) throws Exception;
}
