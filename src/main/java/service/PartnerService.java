package service;

import dao.IPartnerDao;
import dao.PartnerDao;
import model.Motorbike;
import model.OrderDetail;
import model.Partner;
import model.Review;

import java.util.List;

public class PartnerService implements IPartnerService {
    private final IPartnerDao dao = new PartnerDao();          // gọi xuống DAO
    private final IMotorbikeService motorbikeService = new MotorbikeService(); // dùng cho motorbike

    // 1. Login as the Partner
    @Override
    public Partner login(String username, String password) throws Exception {
        return dao.login(username, password);
    }

    // 2. Update Account Information
    @Override
    public boolean updateAccountInfo(Partner partner) throws Exception {
        return dao.updateAccountInfo(partner);
    }

    // 3. View Bike Details
    @Override
    public Motorbike getBikeDetails(int bikeId) throws Exception {
        return (Motorbike) motorbikeService.findByTypeId(bikeId);
    }

    // 4. View My Bikes (danh sách xe của partner)
    @Override
    public List<Motorbike> getMyBikes(int partnerId) throws Exception {
        return motorbikeService.getByPartnerId(partnerId);
    }

    // 5. View Rental History
    @Override
    public List<OrderDetail> getRentalHistory(int partnerId) throws Exception {
        // TODO: cần OrderDetailDao để implement
        throw new UnsupportedOperationException("Chưa implement getRentalHistory");
    }

    // 6. View Bike Reviews
    @Override
    public List<Review> getBikeReviews(int bikeId) throws Exception {
        // TODO: cần ReviewDao để implement
        throw new UnsupportedOperationException("Chưa implement getBikeReviews");
    }

    // 7. Get Partner by Account ID
    @Override
    public Partner getByAccountId(int accountId) throws Exception {
        return dao.getByAccountId(accountId);
    }
}
