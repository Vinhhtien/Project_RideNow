//an
package service;

import java.sql.Connection;
import java.util.List;
import model.Notification;

public interface INotificationService {
    // Send
    int sendToAccount(int accountId, String title, String message);
    int sendToPartnersByOrder(int orderId, String title, String message);
   int sendToPartnersByOrder(Connection c, int orderId, String title, String message) throws Exception;


    // Read/list
    Notification findByIdForAccount(int notificationId, int accountId);
    boolean readOne(int notificationId, int accountId);
    int readAll(int accountId);
    int countUnread(int accountId);
    List<Notification> latestForAccount(int accountId, int limit, int offset);
    int deleteRead(int accountId);
}
