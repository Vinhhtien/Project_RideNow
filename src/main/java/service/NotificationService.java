package service;

import dao.INotificationDao;
import dao.NotificationDao;
import model.Notification;
import utils.DBConnection;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class NotificationService implements INotificationService {
    private final INotificationDao notificationDao = new NotificationDao();

    @Override
    public int sendToAccount(int accountId, String title, String message) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.createNotification(c, accountId, title, message);
        } catch (Exception e) {
            return 0;
        }
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
    public Notification findByIdForAccount(int notificationId, int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.findByIdForAccount(c, notificationId, accountId);
        } catch (Exception e) {
            return null;
        }
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
    public int countUnread(int accountId) {
        try (Connection c = DBConnection.getConnection()) {
            return notificationDao.countUnread(c, accountId);
        } catch (Exception e) {
            return 0;
        }
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
