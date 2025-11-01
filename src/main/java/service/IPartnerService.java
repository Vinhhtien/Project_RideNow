package service;

import java.util.List;

import model.Motorbike;
import model.MotorbikeListItem;
import model.OrderDetail;
import model.Partner;
import model.Review;

public interface IPartnerService {

    // 1. Login as the Partner
    Partner login(String username, String password) throws Exception;

    // 2. Update Account Information
    boolean updateAccountInfo(Partner partner) throws Exception;

    boolean updateAccountName(int accountId, String accountName);

    boolean updatePassword(int accountId, String newPassword);

    // 3. View Bike Details
    MotorbikeListItem getBikeDetails(int bikeId) throws Exception;

    // 4. View My Bikes (danh sách xe của partner)
    List<Motorbike> getMyBikes(int partnerId) throws Exception;

    // 5. View Rental History
    List<OrderDetail> getRentalHistory(int partnerId) throws Exception;

    // 6. View Bike Reviews
    List<Review> getBikeReviews(int bikeId) throws Exception;

    // 7. Receive Notification
    // List<Notification> getNotifications(int partnerId) throws Exception;

    // 8. Get Partner by Account ID
    Partner getByAccountId(int accountId) throws Exception;
}
