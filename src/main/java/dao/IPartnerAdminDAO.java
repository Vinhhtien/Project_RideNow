package dao;

import model.Partner;
import java.sql.Connection;
import java.util.List;

public interface IPartnerAdminDAO {
    boolean existsUsername(Connection c, String username);
    int insertAccountPartner(Connection c, String username, String rawPassword);
    int insertPartner(Connection c, int accountId, String companyName, String address, String phone, int adminId);
    int getAdminIdByAccountId(Connection c, int accountId);
    
    // Các method mới
    List<Partner> getAllPartners(Connection c);
    int getAccountIdByPartnerId(Connection c, int partnerId);
    boolean deletePartner(Connection c, int partnerId);
    boolean deleteAccount(Connection c, int accountId);
}