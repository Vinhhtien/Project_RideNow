package service;

import model.Partner;
import java.util.List;

public interface IPartnerAdminService {
    boolean usernameExists(String username);
    void createPartner(String username, String companyName, String address, String phone, int adminId);
    List<Partner> getAllPartners();
    boolean deletePartner(int partnerId);
}