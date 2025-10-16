package service;

import dao.IPartnerAdminDAO;
import dao.PartnerAdminDAO;
import model.Partner;
import utils.DBConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PartnerAdminService implements IPartnerAdminService {
    private IPartnerAdminDAO partnerAdminDAO;

    public PartnerAdminService() {
        this.partnerAdminDAO = new PartnerAdminDAO();
    }

    @Override
    public boolean usernameExists(String username) {
        try (Connection c = DBConnection.getConnection()) {
            return partnerAdminDAO.existsUsername(c, username);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối database", e);
        }
    }

    @Override
    public void createPartner(String username, String companyName, String address, String phone, int adminAccountId) {
        Connection c = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);

            if (partnerAdminDAO.existsUsername(c, username)) {
                throw new RuntimeException("Tên đăng nhập đã tồn tại!");
            }

            if (companyName == null || companyName.trim().isEmpty()) {
                throw new RuntimeException("Tên công ty không được để trống!");
            }

            // Lấy admin_id từ bảng Admins dựa trên account_id
            int adminId = partnerAdminDAO.getAdminIdByAccountId(c, adminAccountId);
            
            System.out.println("=== DEBUG: Using admin_id: " + adminId + " for account_id: " + adminAccountId);

            int accId = partnerAdminDAO.insertAccountPartner(c, username, "1");
            partnerAdminDAO.insertPartner(c, accId, companyName.trim(), address, phone, adminId);

            c.commit();
        } catch (SQLException e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Lỗi hệ thống khi tạo partner: " + e.getMessage());
        } catch (RuntimeException e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) {}
            }
            throw e;
        } finally {
            if (c != null) {
                try { c.close(); } catch (SQLException e) {}
            }
        }
    }

    @Override
    public List<Partner> getAllPartners() {
        try (Connection c = DBConnection.getConnection()) {
            return partnerAdminDAO.getAllPartners(c);
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi lấy danh sách partners: " + e.getMessage());
        }
    }

    @Override
    public boolean deletePartner(int partnerId) {
        Connection c = null;
        try {
            c = DBConnection.getConnection();
            c.setAutoCommit(false);
            
            // Lấy account_id từ partner_id
            int accountId = partnerAdminDAO.getAccountIdByPartnerId(c, partnerId);
            
            // Xóa partner trước (vì có foreign key constraint)
            boolean partnerDeleted = partnerAdminDAO.deletePartner(c, partnerId);
            if (!partnerDeleted) {
                c.rollback();
                return false;
            }
            
            // Sau đó xóa account
            boolean accountDeleted = partnerAdminDAO.deleteAccount(c, accountId);
            if (!accountDeleted) {
                c.rollback();
                return false;
            }
            
            c.commit();
            return true;
        } catch (SQLException e) {
            if (c != null) {
                try { c.rollback(); } catch (SQLException ex) {}
            }
            throw new RuntimeException("Lỗi xóa partner: " + e.getMessage());
        } finally {
            if (c != null) {
                try { c.close(); } catch (SQLException e) {}
            }
        }
    }
}