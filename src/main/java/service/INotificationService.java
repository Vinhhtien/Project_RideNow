 package service;

import java.sql.Connection;
import model.Notification;
import java.util.List;

public interface INotificationService {

    /** Lấy tối đa `limit` thông báo chưa đọc để hiện toast nhanh. */
    List<Notification> getUnreadToasts(int accountId, int limit);

    /** Lấy danh sách thông báo của 1 account, sắp xếp mới → cũ. */
    List<Notification> findByAccount(int accountId, int page, int size, String q, Boolean onlyUnread);

//    /** Đếm tổng thông báo chưa đọc (hiển thị badge chuông). */
//    int countUnread(int accountId);

    /** NEW: Lấy 1 thông báo theo id, có kiểm tra thuộc về accountId (trả null nếu không thuộc). */
    Notification findByIdForAccount(int notificationId, int accountId);

//    /** Đánh dấu đã đọc 1 thông báo. */
//    void readOne(int notificationId, int accountId);
//
//    /** Đánh dấu đã đọc tất cả thông báo của account. */
//    void readAll(int accountId);

    /** Gửi 1 thông báo cho 1 account (admin/hệ thống dùng). */
    int sendToAccount(int accountId, String title, String message);

    /** Gửi broadcast tới tất cả partner đang active. */
    int broadcastToPartners(String title, String message);
    
    
    int sendToPartnersByOrder(int orderId, String title, String message);
   int sendToPartnersByOrder(Connection c, int orderId, String title, String message) throws Exception;
   boolean readOne(int notificationId, int accountId);
    int readAll(int accountId);
    int countUnread(int accountId);
    List<Notification> latestForAccount(int accountId, int limit, int offset);
    int deleteRead(int accountId);
}