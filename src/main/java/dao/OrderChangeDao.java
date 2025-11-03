package dao;

import utils.DBConnection;
import model.ChangeOrderVM;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import static utils.DBConnection.getConnection;
public class OrderChangeDao implements IOrderChangeDao {

    @Override
    public ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception {
        System.out.println("[OrderChangeDao] Loading change order VM for orderId: " + orderId + ", accountId: " + accountId);
        
        // SỬA: Cập nhật theo đúng tên cột trong database
        final String sql = 
            "SELECT r.order_id, r.status, r.start_date, r.end_date, r.confirmed_at, " +
            "  r.total_price, r.deposit_amount, r.payment_submitted, " + // SỬA tên cột
            "  CASE WHEN r.status='confirmed' AND r.confirmed_at IS NOT NULL " +
            "       THEN CASE WHEN (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) < 0 THEN 0 " +
            "                 ELSE (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) END " +
            "       ELSE 0 END AS remaining_min, " +
            "  DATEDIFF(day, r.start_date, r.end_date) + 1 as rental_days, " +
            "  (SELECT TOP 1 bike_id FROM OrderDetails WHERE order_id = r.order_id) as bike_id " +
            "FROM RentalOrders r " +
            "JOIN Customers c ON c.customer_id = r.customer_id " +
            "JOIN Accounts  a ON a.account_id  = c.account_id " +
            "WHERE r.order_id = ? AND a.account_id = ?";

        System.out.println("[OrderChangeDao] SQL: " + sql);
        
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, orderId);
            ps.setInt(2, accountId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("[OrderChangeDao] No result found in ResultSet");
                    return null;
                }
                
                ChangeOrderVM vm = new ChangeOrderVM();
                vm.setOrderId(rs.getInt("order_id"));
                vm.setStatus(rs.getString("status"));
                vm.setStart(rs.getDate("start_date"));
                vm.setEnd(rs.getDate("end_date"));
                vm.setConfirmedAt(rs.getTimestamp("confirmed_at"));
                vm.setRemainingMinutes(rs.getInt("remaining_min"));
                vm.setBikeId(rs.getInt("bike_id"));
                vm.setOriginalRentalDays(rs.getInt("rental_days"));
                
                // SỬA: Dùng đúng tên cột database
                vm.setTotalAmount(rs.getBigDecimal("total_price"));
                vm.setDepositAmount(rs.getBigDecimal("deposit_amount"));
                
                System.out.println("[OrderChangeDao] Loaded VM: " +
                    "orderId=" + vm.getOrderId() + 
                    ", status=" + vm.getStatus() + 
                    ", totalPrice=" + vm.getTotalAmount() +
                    ", depositAmount=" + vm.getDepositAmount() +
                    ", paymentSubmitted=" + rs.getBigDecimal("payment_submitted") +
                    ", refundAmount=" + vm.getRefundAmount());
                
                return vm;
            }
        } catch (Exception e) {
            System.out.println("[OrderChangeDao] Exception: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

//    @Override
//    public int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception {
//        final String sql =
//            "UPDATE r SET r.status='cancelled' " +
//            "FROM RentalOrders r " +
//            "JOIN Customers c ON c.customer_id = r.customer_id " +
//            "JOIN Accounts  a ON a.account_id  = c.account_id " +
//            "WHERE r.order_id = ? AND a.account_id = ? " +
//            "  AND r.status = 'confirmed' " +
//            "  AND r.confirmed_at IS NOT NULL " +
//            "  AND DATEDIFF(MINUTE, r.confirmed_at, GETDATE()) <= 30";
//
//        try (Connection con = DBConnection.getConnection();
//             PreparedStatement ps = con.prepareStatement(sql)) {
//
//            ps.setInt(1, orderId);
//            ps.setInt(2, accountId);
//            return ps.executeUpdate();
//        }
//    }

    @Override
    public ChangeResult updateOrderDatesWithin30Min(int orderId, int accountId, Date newStart, Date newEnd) throws Exception {
        if (newStart.after(newEnd)) return ChangeResult.FAIL;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 1) Kiểm tra còn trong 30' và lấy thông tin đơn hàng
                final String checkSql =
                    "SELECT r.confirmed_at, r.status, " +
                    "       DATEDIFF(day, r.start_date, r.end_date) + 1 as rental_days, " +
                    "       (SELECT TOP 1 bike_id FROM OrderDetails WHERE order_id = r.order_id) as bike_id " + // LẤY bike_id
                    "FROM RentalOrders r " +
                    "JOIN Customers c ON c.customer_id = r.customer_id " +
                    "JOIN Accounts  a ON a.account_id  = c.account_id " +
                    "WHERE r.order_id=? AND a.account_id=?";
                    
                Timestamp confirmedAt = null;
                String status = null;
                int bikeId = 0;
                int rentalDays = 0;
                
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, accountId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { 
                            con.rollback(); 
                            return ChangeResult.FAIL; 
                        }
                        confirmedAt = rs.getTimestamp("confirmed_at");
                        status = rs.getString("status");
                        bikeId = rs.getInt("bike_id");
                        rentalDays = rs.getInt("rental_days");
                    }
                }

                // Kiểm tra điều kiện
                if (!"confirmed".equalsIgnoreCase(status) || confirmedAt == null) {
                    con.rollback(); 
                    return ChangeResult.EXPIRED;
                }

                // Kiểm tra thời gian 30 phút
                final String timeCheckSql = 
                    "SELECT CASE WHEN DATEDIFF(MINUTE, ?, GETDATE()) <= 30 THEN 1 ELSE 0 END";
                try (PreparedStatement ps = con.prepareStatement(timeCheckSql)) {
                    ps.setTimestamp(1, confirmedAt);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next() && rs.getInt(1) != 1) { 
                            con.rollback(); 
                            return ChangeResult.EXPIRED; 
                        }
                    }
                }

                // 2) Kiểm tra số ngày thuê có giống ban đầu không
                long diffMillis = newEnd.getTime() - newStart.getTime();
                int newRentalDays = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
                
                if (newRentalDays != rentalDays) {
                    con.rollback();
                    return ChangeResult.FAIL; // Số ngày thuê thay đổi
                }

                // 3) Kiểm tra trùng lịch với đơn khác
                final String conflictSql =
                    "SELECT TOP 1 1 " +
                    "FROM RentalOrders r2 " +
                    "WHERE r2.order_id IN (SELECT order_id FROM OrderDetails WHERE bike_id = ?) " + // SỬA: Tìm orders qua bike_id
                    "  AND r2.order_id <> ? " +
                    "  AND r2.status IN ('pending', 'confirmed', 'completed') " +
                    "  AND NOT (r2.end_date < ? OR r2.start_date > ?)";
                    
                try (PreparedStatement ps = con.prepareStatement(conflictSql)) {
                    ps.setInt(1, bikeId);
                    ps.setInt(2, orderId);
                    ps.setDate(3, newStart);
                    ps.setDate(4, newEnd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { 
                            con.rollback(); 
                            return ChangeResult.CONFLICT; 
                        }
                    }
                }

                // 4) Update ngày
                final String updateSql =
                    "UPDATE RentalOrders SET start_date=?, end_date=? WHERE order_id=?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setDate(1, newStart);
                    ps.setDate(2, newEnd);
                    ps.setInt(3, orderId);
                    int rows = ps.executeUpdate();
                    if (rows == 0) { 
                        con.rollback(); 
                        return ChangeResult.FAIL; 
                    }
                }

                con.commit();
                System.out.println("[OrderChangeDao] Successfully updated order #" + orderId + 
                    " to dates: " + newStart + " - " + newEnd + " (" + rentalDays + " days)");
                return ChangeResult.OK;
                
            } catch (Exception ex) {
                con.rollback();
                System.out.println("[OrderChangeDao] Error updating order: " + ex.getMessage());
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    @Override
    public boolean checkDateConflict(int excludeOrderId, int bikeId, Date newStart, Date newEnd) {
        // SỬA: Sử dụng đúng tên bảng RentalOrders
        String sql = "SELECT COUNT(*) FROM RentalOrders " +
                    "WHERE order_id IN (SELECT order_id FROM OrderDetails WHERE bike_id = ?) " + // Tìm orders qua bike_id
                    "  AND order_id != ? " +
                    "  AND status IN ('pending', 'confirmed', 'completed') " +
                    "  AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                    "  OR (? BETWEEN start_date AND end_date) OR (? BETWEEN start_date AND end_date))";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, bikeId);
            ps.setInt(2, excludeOrderId);
            ps.setDate(3, newStart);
            ps.setDate(4, newEnd);
            ps.setDate(5, newStart);
            ps.setDate(6, newEnd);
            ps.setDate(7, newStart);
            ps.setDate(8, newEnd);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean hasConflict = rs.getInt(1) > 0;
                System.out.println("[OrderChangeDao] Date conflict check - bikeId: " + bikeId + 
                    ", dates: " + newStart + " to " + newEnd + ", conflict: " + hasConflict);
                return hasConflict;
            }
        } catch (SQLException e) {
            System.out.println("[OrderChangeDao] Error checking date conflict: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    
    private boolean refundOrderAmount(int orderId, int accountId, Connection con) throws SQLException {
        try {
            // 1. Lấy thông tin số tiền đã thanh toán của đơn hàng
            String getPaymentSql = "SELECT total_amount, deposit_amount, paid_amount FROM RentalOrders WHERE order_id = ?";
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal depositAmount = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            
            try (PreparedStatement ps = con.prepareStatement(getPaymentSql)) {
                ps.setInt(1, orderId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    totalAmount = rs.getBigDecimal("total_amount") != null ? rs.getBigDecimal("total_amount") : BigDecimal.ZERO;
                    depositAmount = rs.getBigDecimal("deposit_amount") != null ? rs.getBigDecimal("deposit_amount") : BigDecimal.ZERO;
                    paidAmount = rs.getBigDecimal("paid_amount") != null ? rs.getBigDecimal("paid_amount") : BigDecimal.ZERO;
                }
            }
            
            System.out.println("[OrderChangeDao] Order #" + orderId + " - Total: " + totalAmount + 
                ", Deposit: " + depositAmount + ", Paid: " + paidAmount);
            
            // 2. Tính số tiền cần hoàn (cọc + 30%)
            BigDecimal refundAmount = calculateRefundAmount(totalAmount, depositAmount, paidAmount);
            
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("[OrderChangeDao] No refund amount for order #" + orderId);
                return true; // Không có tiền để hoàn
            }
            
            System.out.println("[OrderChangeDao] Refund amount for order #" + orderId + ": " + refundAmount);
            
            // 3. Lấy wallet_id của customer
            String getWalletSql = "SELECT w.wallet_id FROM Wallets w " +
                                 "JOIN Customers c ON w.customer_id = c.customer_id " +
                                 "JOIN Accounts a ON c.account_id = a.account_id " +
                                 "WHERE a.account_id = ?";
            Integer walletId = null;
            
            try (PreparedStatement ps = con.prepareStatement(getWalletSql)) {
                ps.setInt(1, accountId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    walletId = rs.getInt("wallet_id");
                }
            }
            
            if (walletId == null) {
                // Tạo wallet mới nếu chưa có
                walletId = createWalletForCustomer(accountId, con);
                if (walletId == null) {
                    System.out.println("[OrderChangeDao] Failed to create wallet for account #" + accountId);
                    return false;
                }
            }
            
            // 4. Cập nhật số dư ví
            String updateWalletSql = "UPDATE Wallets SET balance = balance + ? WHERE wallet_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateWalletSql)) {
                ps.setBigDecimal(1, refundAmount);
                ps.setInt(2, walletId);
                int updated = ps.executeUpdate();
                System.out.println("[OrderChangeDao] Updated wallet balance, rows affected: " + updated);
            }
            
            // 5. Ghi log transaction
            String insertTxSql = "INSERT INTO Wallet_Transactions (wallet_id, amount, type, description, order_id, created_at) " +
                               "VALUES (?, ?, 'refund', ?, ?, GETDATE())";
            try (PreparedStatement ps = con.prepareStatement(insertTxSql)) {
                ps.setInt(1, walletId);
                ps.setBigDecimal(2, refundAmount);
                ps.setString(3, "Hoàn tiền hủy đơn #" + orderId + " (cọc + 30%)");
                ps.setInt(4, orderId);
                int inserted = ps.executeUpdate();
                System.out.println("[OrderChangeDao] Created transaction record, rows affected: " + inserted);
            }
            
            return true;
            
        } catch (Exception e) {
            System.out.println("[OrderChangeDao] Error in refundOrderAmount: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private BigDecimal calculateRefundAmount(BigDecimal totalAmount, BigDecimal depositAmount, BigDecimal paidAmount) {
    if (totalAmount == null) totalAmount = BigDecimal.ZERO;
    if (depositAmount == null) depositAmount = BigDecimal.ZERO;
    
    // Logic tính toán số tiền hoàn lại: cọc + 30% tổng tiền
    BigDecimal thirtyPercent = totalAmount.multiply(new BigDecimal("0.3"));
    BigDecimal refund = depositAmount.add(thirtyPercent);
    
    System.out.println("[OrderChangeDao] Refund calculation - " +
        "Total: " + totalAmount + 
        ", Deposit: " + depositAmount + 
        ", 30%: " + thirtyPercent + 
        ", Total refund: " + refund);
    
    // 🟢 QUAN TRỌNG: LUÔN hoàn đúng theo chính sách (cọc + 30%)
    // Bỏ qua kiểm tra payment_submitted vì đây là chính sách hủy trong 30 phút
    System.out.println("[OrderChangeDao] Applying cancellation policy: full deposit + 30% regardless of payment status");
    
    return refund;
}
    
    private Integer createWalletForCustomer(int accountId, Connection con) throws SQLException {
        String getCustomerSql = "SELECT customer_id FROM Customers WHERE account_id = ?";
        Integer customerId = null;
        
        try (PreparedStatement ps = con.prepareStatement(getCustomerSql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
                System.out.println("[OrderChangeDao] Found customer ID: " + customerId + " for account: " + accountId);
            }
        }
        
        if (customerId == null) {
            System.out.println("[OrderChangeDao] No customer found for account #" + accountId);
            return null;
        }
        
        String insertWalletSql = "INSERT INTO Wallets (customer_id, balance, created_at) VALUES (?, 0, GETDATE())";
        try (PreparedStatement ps = con.prepareStatement(insertWalletSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newWalletId = rs.getInt(1);
                System.out.println("[OrderChangeDao] Created new wallet #" + newWalletId + " for customer #" + customerId);
                return newWalletId;
            }
        }
        System.out.println("[OrderChangeDao] Failed to create wallet for customer #" + customerId);
        return null;
    }
    
    @Override
public int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception {
    try (Connection con = DBConnection.getConnection()) {
        con.setAutoCommit(false);
        try {
            System.out.println("[OrderChangeDao] Starting cancellation process for order #" + orderId);
            
            // 1. Kiểm tra điều kiện hủy và lấy thông tin đơn
            final String checkSql =
                "SELECT r.confirmed_at, r.status, r.total_price, r.deposit_amount, r.payment_submitted " +
                "FROM RentalOrders r " +
                "JOIN Customers c ON c.customer_id = r.customer_id " +
                "JOIN Accounts  a ON a.account_id  = c.account_id " +
                "WHERE r.order_id = ? AND a.account_id = ? " +
                "  AND r.status = 'confirmed' " +
                "  AND r.confirmed_at IS NOT NULL " +
                "  AND DATEDIFF(MINUTE, r.confirmed_at, GETDATE()) <= 30";
            
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal depositAmount = BigDecimal.ZERO;
            BigDecimal paidAmount = BigDecimal.ZERO;
            
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setInt(1, orderId);
                ps.setInt(2, accountId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("[OrderChangeDao] Order doesn't meet cancellation conditions");
                        con.rollback();
                        return 0;
                    }
                    totalAmount = rs.getBigDecimal("total_price");
                    depositAmount = rs.getBigDecimal("deposit_amount");
                    paidAmount = rs.getBigDecimal("payment_submitted");
                    
                    System.out.println("[OrderChangeDao] RAW DATA FROM DB - " +
                        "total_price: " + totalAmount + 
                        ", deposit_amount: " + depositAmount + 
                        ", payment_submitted: " + paidAmount);
                }
            }

            // 2. Tính toán số tiền hoàn - LUÔN theo chính sách (cọc + 30%)
            BigDecimal refundAmount = calculateRefundAmount(totalAmount, depositAmount, paidAmount);
            System.out.println("[OrderChangeDao] Final refund amount (policy-based): " + refundAmount);

            // 3. Hoàn tiền vào ví - LUÔN thực hiện dù payment_submitted là bao nhiêu
            boolean refundSuccess = refundOrderAmount(orderId, accountId, refundAmount, con);
            if (!refundSuccess) {
                System.out.println("[OrderChangeDao] Refund failed, rolling back");
                con.rollback();
                return 0;
            }

            // 4. Cập nhật trạng thái đơn hàng
            final String updateSql =
                "UPDATE RentalOrders SET status = 'cancelled' " +
                "WHERE order_id = ?";
                
            int rowsUpdated;
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setInt(1, orderId);
                rowsUpdated = ps.executeUpdate();
                System.out.println("[OrderChangeDao] Order status updated to 'cancelled', rows affected: " + rowsUpdated);
            }

            if (rowsUpdated == 0) {
                System.out.println("[OrderChangeDao] No rows updated, rolling back");
                con.rollback();
                return 0;
            }

            con.commit();
            System.out.println("[OrderChangeDao] ✅ TRANSACTION COMMITTED: Order #" + orderId + 
                " cancelled with FULL refund: " + refundAmount);
            
            return rowsUpdated;
            
        } catch (Exception ex) {
            System.out.println("[OrderChangeDao] ❌ Exception during cancellation: " + ex.getMessage());
            ex.printStackTrace();
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(true);
        }
    }
}
    // SỬA: Thêm tham số refundAmount
    private boolean refundOrderAmount(int orderId, int accountId, BigDecimal refundAmount, Connection con) throws SQLException {
        try {
            System.out.println("[OrderChangeDao] Processing refund: " + refundAmount + " for account: " + accountId);
            
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("[OrderChangeDao] No refund amount, skipping wallet update");
                return true;
            }
            
            // 1. Lấy wallet_id của customer
            String getWalletSql = "SELECT w.wallet_id FROM Wallets w " +
                                 "JOIN Customers c ON w.customer_id = c.customer_id " +
                                 "JOIN Accounts a ON c.account_id = a.account_id " +
                                 "WHERE a.account_id = ?";
            Integer walletId = null;
            
            try (PreparedStatement ps = con.prepareStatement(getWalletSql)) {
                ps.setInt(1, accountId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    walletId = rs.getInt("wallet_id");
                    System.out.println("[OrderChangeDao] Found wallet ID: " + walletId + " for account: " + accountId);
                }
            }
            
            if (walletId == null) {
                // Tạo wallet mới nếu chưa có
                walletId = createWalletForCustomer(accountId, con);
                if (walletId == null) {
                    System.out.println("[OrderChangeDao] Failed to create wallet for account #" + accountId);
                    return false;
                }
            }
            
            // 2. Cập nhật số dư ví
            String updateWalletSql = "UPDATE Wallets SET balance = balance + ? WHERE wallet_id = ?";
            int walletUpdated;
            try (PreparedStatement ps = con.prepareStatement(updateWalletSql)) {
                ps.setBigDecimal(1, refundAmount);
                ps.setInt(2, walletId);
                walletUpdated = ps.executeUpdate();
                System.out.println("[OrderChangeDao] Updated wallet balance, rows affected: " + walletUpdated);
                
                if (walletUpdated == 0) {
                    return false;
                }
            }
            
            // 3. Ghi log transaction
            String insertTxSql = "INSERT INTO Wallet_Transactions (wallet_id, amount, type, description, order_id, created_at) " +
                               "VALUES (?, ?, 'refund', ?, ?, GETDATE())";
            int transactionInserted;
            try (PreparedStatement ps = con.prepareStatement(insertTxSql)) {
                ps.setInt(1, walletId);
                ps.setBigDecimal(2, refundAmount);
                ps.setString(3, "Hoàn tiền hủy đơn #" + orderId + " (cọc + 30%)");
                ps.setInt(4, orderId);
                transactionInserted = ps.executeUpdate();
                System.out.println("[OrderChangeDao] Created transaction record, rows affected: " + transactionInserted);
                
                return transactionInserted > 0;
            }
            
        } catch (Exception e) {
            System.out.println("[OrderChangeDao] Error in refundOrderAmount: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    // Thêm vào OrderChangeDao
    @Override
    public BigDecimal getDepositAmount(int orderId) throws SQLException {
        String sql = "SELECT deposit_amount FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("deposit_amount") != null ? rs.getBigDecimal("deposit_amount") : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }


     @Override
    public BigDecimal getTotalAmount(int orderId) throws SQLException {
        // SỬA: dùng total_price thay vì total_amount
        String sql = "SELECT total_price FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total_price") != null ? rs.getBigDecimal("total_price") : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    
}