// dao/WalletDao.java (tối giản cho refund)
package dao;

import utils.DBConnection;

import java.math.BigDecimal;
import java.sql.*;

public class WalletDao {
    public int ensureWallet(int customerId) throws Exception {
        String find = "SELECT wallet_id FROM Wallets WHERE customer_id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(find)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
            // create
            String ins = "INSERT INTO Wallets(customer_id,balance) OUTPUT INSERTED.wallet_id VALUES(?,0)";
            try (PreparedStatement p2 = con.prepareStatement(ins)) {
                p2.setInt(1, customerId);
                try (ResultSet rs2 = p2.executeQuery()) { rs2.next(); return rs2.getInt(1); }
            }
        }
    }

    public void creditRefund(int customerId, BigDecimal amount, Integer orderId) throws Exception {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                int walletId = ensureWallet(customerId);
                String insTx = "INSERT INTO Wallet_Transactions(wallet_id,amount,type,order_id,note) VALUES(?, ?, 'refund', ?, 'Refund deposit')";
                try (PreparedStatement ps = con.prepareStatement(insTx)) {
                    ps.setInt(1, walletId);
                    ps.setBigDecimal(2, amount);
                    ps.setObject(3, orderId, Types.INTEGER);
                    ps.executeUpdate();
                }
                String upd = "UPDATE Wallets SET balance = balance + ?, updated_at=GETDATE() WHERE wallet_id=?";
                try (PreparedStatement ps = con.prepareStatement(upd)) {
                    ps.setBigDecimal(1, amount);
                    ps.setInt(2, walletId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (Exception ex) {
                con.rollback(); throw ex;
            } finally { con.setAutoCommit(true); }
        }
    }
}
