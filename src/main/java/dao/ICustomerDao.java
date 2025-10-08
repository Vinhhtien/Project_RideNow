package dao;

import model.Customer;

public interface ICustomerDao {
    Customer findByAccountId(int accountId) throws Exception;

    /** Tạo mới hoặc cập nhật hồ sơ theo account_id */
    void upsertByAccountId(Customer c) throws Exception;

    /** Đổi mật khẩu cho tài khoản.
     *  Trả về true nếu đổi thành công (mật khẩu hiện tại đúng), ngược lại false. */
    boolean updatePassword(int accountId, String currentPw, String newPw) throws Exception;
}
