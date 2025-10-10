// an
package service;

import dao.NotificationDao;
import model.Notification;

import java.util.List;

public class NotificationService implements INotificationService {
    private final NotificationDao dao = new NotificationDao();

    /** Lấy tối đa `limit` thông báo chưa đọc để hiện toast nhanh. */
    @Override
    public List<Notification> getUnreadToasts(int accountId, int limit) {
        int lim = Math.max(1, Math.min(limit, 50)); // chặn limit quá lớn
        return dao.findUnreadTop(accountId, lim);
    }

    /** Lấy danh sách thông báo (mới → cũ), có phân trang + lọc. */
    @Override
    public List<Notification> findByAccount(int accountId, int page, int size, String q, Boolean onlyUnread) {
        int p = Math.max(1, page);
        int s = Math.max(1, Math.min(size, 200)); // chặn size quá lớn
        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        return dao.findByAccount(accountId, p, s, query, onlyUnread);
    }

    /** Đếm tổng số chưa đọc (hiển thị badge chuông). */
    @Override
    public int countUnread(int accountId) {
        return dao.countUnread(accountId);
    }

    /** NEW: lấy 1 thông báo theo id, kiểm tra thuộc về accountId (không phải thì trả null). */
    @Override
    public Notification findByIdForAccount(int notificationId, int accountId) {
        if (notificationId <= 0) return null;
        return dao.findOneForAccount(notificationId, accountId);
    }

    @Override
    public void readOne(int notificationId, int accountId) {
        dao.markAsRead(notificationId, accountId);
    }

    @Override
    public void readAll(int accountId) {
        dao.markAllAsRead(accountId);
    }

    @Override
    public int sendToAccount(int accountId, String title, String message) {
        return dao.create(accountId, title, message);
    }

    @Override
    public int broadcastToPartners(String title, String message) {
        return dao.broadcastToAllPartners(title, message);
    }
}
