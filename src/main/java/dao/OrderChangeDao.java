package dao;

import model.ChangeOrderVM;
import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class OrderChangeDao implements IOrderChangeDao {

    // ========================================================================
    // 1) LOAD VIEWMODEL CHO CHANGE-ORDER
    // ========================================================================
    @Override
    public ChangeOrderVM loadChangeOrderVM(int orderId, int accountId) throws Exception {
        System.out.println("[OrderChangeDao] Loading change order VM for orderId: "
                + orderId + ", accountId: " + accountId);

        final String sql =
                "SELECT r.order_id, r.status, r.start_date, r.end_date, r.confirmed_at, " +
                "       r.total_price, r.deposit_amount, r.payment_submitted, " +
                "       CASE WHEN r.status = 'confirmed' AND r.confirmed_at IS NOT NULL " +
                "            THEN CASE WHEN (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) < 0 " +
                "                      THEN 0 " +
                "                      ELSE (30 - DATEDIFF(MINUTE, r.confirmed_at, GETDATE())) " +
                "                 END " +
                "            ELSE 0 END AS remaining_min, " +
                "       DATEDIFF(day, r.start_date, r.end_date) + 1 AS rental_days, " +
                "       (SELECT TOP 1 bike_id FROM OrderDetails WHERE order_id = r.order_id) AS bike_id, " +
                "       ISNULL(r.change_count, 0) AS change_count " +
                "FROM   RentalOrders r " +
                "JOIN   Customers c ON c.customer_id = r.customer_id " +
                "JOIN   Accounts  a ON a.account_id  = c.account_id " +
                "WHERE  r.order_id = ? AND a.account_id = ?";

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
                vm.setChangeCount(rs.getInt("change_count"));

                vm.setTotalAmount(rs.getBigDecimal("total_price"));
                vm.setDepositAmount(rs.getBigDecimal("deposit_amount"));

                System.out.println("[OrderChangeDao] Loaded VM: " +
                        "orderId=" + vm.getOrderId() +
                        ", status=" + vm.getStatus() +
                        ", totalPrice=" + vm.getTotalAmount() +
                        ", depositAmount=" + vm.getDepositAmount() +
                        ", changeCount=" + vm.getChangeCount() +
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

    // ========================================================================
    // 2) ĐỔI NGÀY THUÊ TRONG 30' + GIỚI HẠN 3 LẦN + PHẠT 10% CỌC Ở LẦN THỨ 3
    // ========================================================================
    @Override
    public ChangeResult updateOrderDatesWithin30Min(int orderId,
                                                    int accountId,
                                                    Date newStart,
                                                    Date newEnd) throws Exception {
        if (newStart.after(newEnd)) return ChangeResult.FAIL;

        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // 2.1) Lấy thông tin đơn + change_count
                final String checkSql =
                        "SELECT r.confirmed_at, r.status, " +
                        "       DATEDIFF(day, r.start_date, r.end_date) + 1 AS rental_days, " +
                        "       (SELECT TOP 1 bike_id FROM OrderDetails WHERE order_id = r.order_id) AS bike_id, " +
                        "       ISNULL(r.change_count, 0) AS change_count, " +
                        "       r.deposit_amount " +
                        "FROM   RentalOrders r " +
                        "JOIN   Customers c ON c.customer_id = r.customer_id " +
                        "JOIN   Accounts  a ON a.account_id  = c.account_id " +
                        "WHERE  r.order_id = ? AND a.account_id = ?";

                Timestamp confirmedAt = null;
                String status = null;
                int bikeId = 0;
                int rentalDays = 0;
                int changeCount = 0;
                BigDecimal depositAmount = BigDecimal.ZERO;

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
                        changeCount = rs.getInt("change_count");
                        depositAmount = rs.getBigDecimal("deposit_amount");
                        if (depositAmount == null) depositAmount = BigDecimal.ZERO;
                    }
                }

                System.out.println("[OrderChangeDao] updateOrderDates - orderId=" + orderId +
                        ", status=" + status +
                        ", changeCount=" + changeCount +
                        ", deposit=" + depositAmount);

                // 2.1.a) Nếu đã đổi >= 3 lần thì khóa
                if (changeCount >= 3) {
                    System.out.println("[OrderChangeDao] Change limit reached for order #" + orderId);
                    con.rollback();
                    return ChangeResult.LIMIT_REACHED;
                }

                // 2.2) Check confirmed + còn trong 30'
                if (!"confirmed".equalsIgnoreCase(status) || confirmedAt == null) {
                    con.rollback();
                    return ChangeResult.EXPIRED;
                }

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

                // 2.3) Check số ngày thuê giữ nguyên
                long diffMillis = newEnd.getTime() - newStart.getTime();
                int newRentalDays = (int) (diffMillis / (1000 * 60 * 60 * 24)) + 1;
                if (newRentalDays != rentalDays) {
                    System.out.println("[OrderChangeDao] Rental days changed: " + newRentalDays
                            + " != original " + rentalDays);
                    con.rollback();
                    return ChangeResult.FAIL;
                }

                // 2.4) Check trùng lịch
                final String conflictSql =
                        "SELECT TOP 1 1 " +
                        "FROM   RentalOrders r2 " +
                        "WHERE  r2.order_id IN (SELECT order_id FROM OrderDetails WHERE bike_id = ?) " +
                        "  AND  r2.order_id <> ? " +
                        "  AND  r2.status IN ('pending', 'confirmed', 'completed') " +
                        "  AND  NOT (r2.end_date < ? OR r2.start_date > ?)";

                try (PreparedStatement ps = con.prepareStatement(conflictSql)) {
                    ps.setInt(1, bikeId);
                    ps.setInt(2, orderId);
                    ps.setDate(3, newStart);
                    ps.setDate(4, newEnd);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("[OrderChangeDao] Date conflict detected for bikeId=" + bikeId);
                            con.rollback();
                            return ChangeResult.CONFLICT;
                        }
                    }
                }

                // 2.5) Update ngày + tăng change_count
                //     Nếu đây là lần đổi thứ 3 => trừ 10% cọc (còn 90%)
                final String updateSql =
                        "UPDATE RentalOrders " +
                        "SET start_date = ?, " +
                        "    end_date   = ?, " +
                        "    change_count = change_count + 1, " +
                        "    deposit_amount = CASE " +
                        "        WHEN change_count + 1 = 3 THEN deposit_amount * 0.9 " +
                        "        ELSE deposit_amount " +
                        "    END " +
                        "WHERE order_id = ?";

                int rowsUpdated;
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setDate(1, newStart);
                    ps.setDate(2, newEnd);
                    ps.setInt(3, orderId);
                    rowsUpdated = ps.executeUpdate();
                }

                if (rowsUpdated == 0) {
                    System.out.println("[OrderChangeDao] No rows updated (order not found?)");
                    con.rollback();
                    return ChangeResult.FAIL;
                }

                int newChangeCount = changeCount + 1;
                System.out.println("[OrderChangeDao] Updated order #" + orderId +
                        " new dates: " + newStart + " - " + newEnd +
                        ", newChangeCount=" + newChangeCount +
                        (newChangeCount == 3 ? " (10% deposit penalty applied)" : ""));

                con.commit();
                return ChangeResult.OK;

            } catch (Exception ex) {
                con.rollback();
                System.out.println("[OrderChangeDao] Error updating order: " + ex.getMessage());
                ex.printStackTrace();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ========================================================================
    // 3) CHECK TRÙNG LỊCH (support function)
    // ========================================================================
    @Override
    public boolean checkDateConflict(int excludeOrderId,
                                     int bikeId,
                                     Date newStart,
                                     Date newEnd) {
        String sql =
                "SELECT COUNT(*) FROM RentalOrders " +
                "WHERE order_id IN (SELECT order_id FROM OrderDetails WHERE bike_id = ?) " +
                "  AND order_id != ? " +
                "  AND status IN ('pending', 'confirmed', 'completed') " +
                "  AND ((start_date BETWEEN ? AND ?) OR (end_date BETWEEN ? AND ?) " +
                "       OR (? BETWEEN start_date AND end_date) " +
                "       OR (? BETWEEN start_date AND end_date))";

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
                        ", dates: " + newStart + " to " + newEnd +
                        ", conflict: " + hasConflict);
                return hasConflict;
            }
        } catch (SQLException e) {
            System.out.println("[OrderChangeDao] Error checking date conflict: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    // ========================================================================
    // 4) HỦY ĐƠN TRONG 30' + HOÀN TIỀN (cọc + 30%) + BAN ACC SAU 3 LẦN
    // ========================================================================
    @Override
    public int cancelConfirmedOrderWithin30Min(int orderId, int accountId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                System.out.println("[OrderChangeDao] Starting cancellation process for order #" + orderId);

                // 1. Kiểm tra điều kiện hủy & lấy thông tin tiền
                final String checkSql =
                        "SELECT r.confirmed_at, r.status, r.total_price, r.deposit_amount, r.payment_submitted " +
                        "FROM RentalOrders r " +
                        "JOIN Customers c ON c.customer_id = r.customer_id " +
                        "JOIN Accounts  a ON a.account_id  = c.account_id " +
                        "WHERE r.order_id = ? AND a.account_id = ? " +
                        "  AND r.status = 'confirmed' " +
                        "  AND r.confirmed_at IS NOT NULL " +
                        "  AND DATEDIFF(MINUTE, r.confirmed_at, GETDATE()) <= 30";

                BigDecimal totalAmount   = BigDecimal.ZERO;
                BigDecimal depositAmount = BigDecimal.ZERO;
                BigDecimal paidAmount    = BigDecimal.ZERO;

                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, accountId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("[OrderChangeDao] Order doesn't meet cancellation conditions");
                            con.rollback();
                            return 0; // không đủ điều kiện hủy
                        }
                        totalAmount   = rs.getBigDecimal("total_price");
                        depositAmount = rs.getBigDecimal("deposit_amount");
                        paidAmount    = rs.getBigDecimal("payment_submitted");

                        System.out.println("[OrderChangeDao] RAW DATA FROM DB - " +
                                "total_price: " + totalAmount +
                                ", deposit_amount: " + depositAmount +
                                ", payment_submitted: " + paidAmount);
                    }
                }

                // 2. Tính số tiền hoàn (cọc + 30%)
                BigDecimal refundAmount = calculateRefundAmount(totalAmount, depositAmount, paidAmount);
                System.out.println("[OrderChangeDao] Final refund amount (policy-based): " + refundAmount);

                // 3. Hoàn tiền vào ví
                boolean refundSuccess = refundOrderAmount(orderId, accountId, refundAmount, con);
                if (!refundSuccess) {
                    System.out.println("[OrderChangeDao] Refund failed, rolling back");
                    con.rollback();
                    return 0;
                }

                // 4. Cập nhật trạng thái đơn hàng -> cancelled
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

                // ======================= 5. TĂNG SỐ LẦN HỦY & BAN NẾU ĐỦ 3 =======================
                int resultFlag = 1; // 1 = hủy thành công, chưa ban

                // 5.1) Tăng cả hai: cancel_in30_count (dùng để ban) & cancel_count (lịch sử tổng)
                String incSql =
                        "UPDATE Accounts " +
                        "SET cancel_in30_count = cancel_in30_count + 1, " +
                        "    cancel_count      = cancel_count + 1 " +
                        "WHERE account_id = ?";
                try (PreparedStatement ps = con.prepareStatement(incSql)) {
                    ps.setInt(1, accountId);
                    ps.executeUpdate();
                }

                // 5.2) Lấy lại giá trị cancel_in30_count mới
                int cancelCount = 0;
                String getSql = "SELECT cancel_in30_count FROM Accounts WHERE account_id = ?";
                try (PreparedStatement ps = con.prepareStatement(getSql)) {
                    ps.setInt(1, accountId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            cancelCount = rs.getInt(1);
                        }
                    }
                }

                System.out.println("[OrderChangeDao] accountId=" + accountId +
                        ", cancel_in30_count after increment = " + cancelCount);

                // 5.3) Nếu >= 3 lần -> KHÓA TÀI KHOẢN + RESET cancel_in30_count VỀ 0
                if (cancelCount >= 3) {
                    String banSql =
                            "UPDATE Accounts " +
                            "SET status = 0, cancel_in30_count = 0 " + // reset luôn counter
                            "WHERE account_id = ?";
                    try (PreparedStatement ps = con.prepareStatement(banSql)) {
                        ps.setInt(1, accountId);
                        ps.executeUpdate();
                    }

                    System.out.println("[OrderChangeDao] accountId=" + accountId +
                            " reached 3 cancels -> BANNED & reset cancel_in30_count to 0");

                    resultFlag = 2; // 2 = hủy OK + VỪA BỊ BAN
                }

                // 6. Commit toàn bộ transaction
                con.commit();
                System.out.println("[OrderChangeDao] ✅ TRANSACTION COMMITTED for cancel order #" + orderId);
                return resultFlag;

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

    private BigDecimal calculateRefundAmount(BigDecimal totalAmount,
                                             BigDecimal depositAmount,
                                             BigDecimal paidAmount) {
        if (totalAmount == null) totalAmount = BigDecimal.ZERO;
        if (depositAmount == null) depositAmount = BigDecimal.ZERO;

        BigDecimal thirtyPercent = totalAmount.multiply(new BigDecimal("0.3"));
        BigDecimal refund = depositAmount.add(thirtyPercent);

        System.out.println("[OrderChangeDao] Refund calculation - " +
                "Total: " + totalAmount +
                ", Deposit: " + depositAmount +
                ", 30%: " + thirtyPercent +
                ", Total refund: " + refund);

        System.out.println("[OrderChangeDao] Applying cancellation policy: full deposit + 30% regardless of payment status");
        return refund;
    }

    private boolean refundOrderAmount(int orderId,
                                      int accountId,
                                      BigDecimal refundAmount,
                                      Connection con) throws SQLException {
        try {
            System.out.println("[OrderChangeDao] Processing refund: " + refundAmount +
                    " for account: " + accountId);

            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.out.println("[OrderChangeDao] No refund amount, skipping wallet update");
                return true;
            }

            String getWalletSql =
                    "SELECT w.wallet_id FROM Wallets w " +
                    "JOIN Customers c ON w.customer_id = c.customer_id " +
                    "JOIN Accounts  a ON c.account_id = a.account_id " +
                    "WHERE a.account_id = ?";

            Integer walletId = null;
            try (PreparedStatement ps = con.prepareStatement(getWalletSql)) {
                ps.setInt(1, accountId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    walletId = rs.getInt("wallet_id");
                    System.out.println("[OrderChangeDao] Found wallet ID: " + walletId +
                            " for account: " + accountId);
                }
            }

            if (walletId == null) {
                walletId = createWalletForCustomer(accountId, con);
                if (walletId == null) {
                    System.out.println("[OrderChangeDao] Failed to create wallet for account #" + accountId);
                    return false;
                }
            }

            String updateWalletSql =
                    "UPDATE Wallets SET balance = balance + ? WHERE wallet_id = ?";
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

            String insertTxSql =
                    "INSERT INTO Wallet_Transactions (wallet_id, amount, type, description, order_id, created_at) " +
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

    private Integer createWalletForCustomer(int accountId, Connection con) throws SQLException {
        String getCustomerSql = "SELECT customer_id FROM Customers WHERE account_id = ?";
        Integer customerId = null;

        try (PreparedStatement ps = con.prepareStatement(getCustomerSql)) {
            ps.setInt(1, accountId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                customerId = rs.getInt("customer_id");
                System.out.println("[OrderChangeDao] Found customer ID: " + customerId +
                        " for account: " + accountId);
            }
        }

        if (customerId == null) {
            System.out.println("[OrderChangeDao] No customer found for account #" + accountId);
            return null;
        }

        String insertWalletSql =
                "INSERT INTO Wallets (customer_id, balance, created_at) VALUES (?, 0, GETDATE())";
        try (PreparedStatement ps = con.prepareStatement(insertWalletSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int newWalletId = rs.getInt(1);
                System.out.println("[OrderChangeDao] Created new wallet #" + newWalletId +
                        " for customer #" + customerId);
                return newWalletId;
            }
        }

        System.out.println("[OrderChangeDao] Failed to create wallet for customer #" + customerId);
        return null;
    }

    // ========================================================================
    // 5) HÀM HỖ TRỢ LẤY SỐ TIỀN + SỐ LẦN HỦY
    // ========================================================================
    @Override
    public BigDecimal getDepositAmount(int orderId) throws SQLException {
        String sql = "SELECT deposit_amount FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal val = rs.getBigDecimal("deposit_amount");
                return val != null ? val : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getTotalAmount(int orderId) throws SQLException {
        String sql = "SELECT total_price FROM RentalOrders WHERE order_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                BigDecimal val = rs.getBigDecimal("total_price");
                return val != null ? val : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }

    // Dùng cancel_in30_count để hiển thị lịch sử hủy trong 30'
    @Override
    public int getCancelCountByAccount(int accountId) throws Exception {
        final String sql = "SELECT cancel_in30_count FROM Accounts WHERE account_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

}
