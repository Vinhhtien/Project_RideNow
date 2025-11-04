package dao;

public interface IAdminSchedualDao {
    /**
     * Trả về admin_id tương ứng account_id; null nếu không tồn tại.
     */
    Integer findAdminIdByAccountId(int accountId);
}
