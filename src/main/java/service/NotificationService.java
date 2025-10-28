// an
package service;

import dao.INotificationDao;
import dao.NotificationDao;
import java.sql.Connection;
import java.util.ArrayList;
import model.Notification;

import java.util.List;
import utils.DBConnection;

public class NotificationService implements INotificationService {
    private final INotificationDao notificationDao = new NotificationDao();

    /** Lấy tối đa `limit` thông báo chưa đọc để hiện toast nhanh. */
    @Override
    public List<Notification> getUnreadToasts(int accountId, int limit) {
        int lim = Math.max(1, Math.min(limit, 50)); // chặn limit quá lớn
        return notificationDao.findUnreadTop(accountId, lim);
    }

    /** Lấy danh sách thông báo (mới → cũ), có phân trang + lọc. */
    @Override
    public List<Notification> findByAccount(int accountId, int page, int size, String q, Boolean onlyUnread) {
        int p = Math.max(1, page);
        int s = Math.max(1, Math.min(size, 200)); // chặn size quá lớn
        String query = (q != null && !q.trim().isEmpty()) ? q.trim() : null;
        return notificationDao.findByAccount(accountId, p, s, query, onlyUnread);
    }

    /** Đếm tổng số chưa đọc (hiển thị badge chuông). */
//    @Override
//    public int countUnread(int accountId) {
//        return notificationDao.countUnread(accountId);
//    }
    
    @Override
    public int countUnread(int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.countUnread(c, accountId);
        } catch (Exception e) {
            return 0;
        }
    }
    /** NEW: lấy 1 thông báo theo id, kiểm tra thuộc về accountId (không phải thì trả null). */
    @Override
    public Notification findByIdForAccount(int notificationId, int accountId) {
        if (notificationId <= 0) return null;
        return notificationDao.findOneForAccount(notificationId, accountId);
    }

    @Override
    public boolean readOne(int notificationId, int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.markReadOne(c, notificationId, accountId) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public int readAll(int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.markReadAll(c, accountId);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int sendToAccount(int accountId, String title, String message) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.createNotification(c, accountId, title, message);
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int broadcastToPartners(String title, String message) {
        return notificationDao.broadcastToAllPartners(title, message);
    }

    @Override
    public int sendToPartnersByOrder(int orderId, String title, String message) {
        try (Connection c = DBConnection.getConnection()) {
            List<Integer> ids = notificationDao.findPartnerAccountIdsByOrderId(c, orderId);
            if (ids == null || ids.isEmpty()) return 0;
            int[] res = notificationDao.createNotificationsBatch(c, ids, title, message);
            int sum = 0; for (int r : res) sum += Math.max(0, r);
            return sum;
        } catch (Exception e) {
            return 0;
        }
    }

    @Override
    public int sendToPartnersByOrder(Connection c, int orderId, String title, String message) throws Exception {
        List<Integer> ids = notificationDao.findPartnerAccountIdsByOrderId(c, orderId);
        if (ids == null || ids.isEmpty()) return 0;
        int[] res = notificationDao.createNotificationsBatch(c, ids, title, message);
        int sum = 0; for (int r : res) sum += Math.max(0, r);
        return sum;
    }

    @Override
    public List<Notification> latestForAccount(int accountId, int limit, int offset) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.findLatestForAccount(c, accountId, limit, offset);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public int deleteRead(int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.deleteReadByAccountId(c, accountId);
        } catch (Exception e) {
            return 0;
        }
    }


}