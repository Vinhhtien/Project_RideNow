
package dao;


import model.Partner;

public interface IPartnerDao {

    /** 
     * 1) Đăng nhập partner: trả về Partner nếu đúng user/pass, null nếu sai.
     */
    Partner login(String username, String password) throws Exception;

    /**
     * 2) Cập nhật thông tin tài khoản/partner.
     * Trả về true nếu có bản ghi được cập nhật.
     */
    boolean updateAccountInfo(Partner partner) throws Exception;
    boolean updateAccountName(int accountId, String accountName);
    boolean updatePassword(int accountId, String newPassword);

    /**
     * 3) Lấy Partner theo account_id (phục vụ getByAccountId trong service).
     */
    Partner getByAccountId(int accountId) throws Exception;
}
