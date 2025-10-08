
package dao;

import java.util.List;

public interface IOrderQueryDao {
    /** Trả về: order_id, bikes (chuỗi), start_date, end_date, total_price, status */
    List<Object[]> findOrdersOfCustomer(int customerId) throws Exception;
    List<Object[]> findOrdersOfCustomerWithPaymentStatus(int customerId);
}
