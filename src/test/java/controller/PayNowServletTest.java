package controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.RequestDispatcher;
import model.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import utils.DBConnection;
import utils.EmailUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PayNowServletTest {

    PayNowServlet servlet;

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock HttpSession session;
    @Mock RequestDispatcher requestDispatcher;

    @Mock Connection con;
    @Mock PreparedStatement psMain, psDeposit, psInsertPayment, psUpdateConfirm,
            psSafe1, psSafe2, psLockBikes, psCusInfo, psOrdersEmail;
    @Mock ResultSet rsMain, rsDeposit, rsCusInfo, rsOrdersEmail;

    Map<String, Object> bag;

    private static ArgumentMatcher<String> sqlContains(String token) {
        return s -> s != null && s.contains(token);
    }

    private static Account acc(int id) {
        Account a = new Account();
        a.setAccountId(id);
        a.setRole("customer");
        return a;
    }

    @BeforeEach
    void setup() {
        servlet = new PayNowServlet();
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        bag = new HashMap<>();
        when(session.getAttribute(anyString())).thenAnswer(i -> bag.get(i.getArgument(0)));
        doAnswer(i -> { bag.put(i.getArgument(0), i.getArgument(1)); return null; }).when(session).setAttribute(anyString(), any());
        doAnswer(i -> { bag.remove(i.getArgument(0)); return null; }).when(session).removeAttribute(anyString());
    }

    // ======================= doGet() Tests =======================

    @Test
    void get_notLoggedIn_redirectsLogin() throws Exception {
        servlet.doGet(req, resp);
        verify(resp).sendRedirect("/ctx/login.jsp");
    }

    @Test
    void get_noOrders_redirectsOrders() throws Exception {
        bag.put("account", acc(1));
        when(req.getParameter("orders")).thenReturn(null);
        servlet.doGet(req, resp);
        Assertions.assertThat(bag).containsKey("flash");
        verify(resp).sendRedirect("/ctx/customerorders");
    }

    @Test
    void get_emptyOrders_redirectsOrders() throws Exception {
        bag.put("account", acc(1));
        when(req.getParameter("orders")).thenReturn("");
        servlet.doGet(req, resp);
        Assertions.assertThat(bag).containsKey("flash");
        verify(resp).sendRedirect("/ctx/customerorders");
    }

    @Test
    void get_exception_catchPath_flashRedirect() throws Exception {
        bag.put("account", acc(2));
        when(req.getParameter("orders")).thenReturn("10");
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenThrow(new RuntimeException("boom"));
            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

    @Test
    void get_noValidOrders_foundNone() throws Exception {
        bag.put("account", acc(3));
        when(req.getParameter("orders")).thenReturn("100");
        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(false);
            servlet.doGet(req, resp);
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

    @Test
    void get_validOrders_returnsRowsAndAttributes() throws Exception {
        bag.put("account", acc(5));
        when(req.getParameter("orders")).thenReturn("200,201");
        when(req.getRequestDispatcher("/cart/paynow.jsp")).thenReturn(requestDispatcher);

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock main query
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, true, false);

            // Mock row data
            when(rsMain.getInt("order_id")).thenReturn(200, 201);
            when(rsMain.getString("bike_name")).thenReturn("Yamaha R15", "Honda Vision");
            when(rsMain.getDate("start_date")).thenReturn(
                    java.sql.Date.valueOf(LocalDate.now()),
                    java.sql.Date.valueOf(LocalDate.now().plusDays(1))
            );
            when(rsMain.getDate("end_date")).thenReturn(
                    java.sql.Date.valueOf(LocalDate.now().plusDays(3)),
                    java.sql.Date.valueOf(LocalDate.now().plusDays(5))
            );
            when(rsMain.getBigDecimal("total_price")).thenReturn(
                    new BigDecimal("1500000"),
                    new BigDecimal("2000000")
            );
            when(rsMain.getString("status")).thenReturn("pending", "pending");

            // Mock deposit calculation
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true, true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(
                    new BigDecimal("500000"),
                    new BigDecimal("1000000")
            );

            // Mock customer and wallet
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(123);

            // Mock wallet balance
            PreparedStatement psWallet = mock(PreparedStatement.class);
            ResultSet rsWallet = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("SELECT balance FROM Wallets")))).thenReturn(psWallet);
            when(psWallet.executeQuery()).thenReturn(rsWallet);
            when(rsWallet.next()).thenReturn(true);
            when(rsWallet.getBigDecimal("balance")).thenReturn(new BigDecimal("5000000"));

            servlet.doGet(req, resp);

            verify(req).setAttribute(eq("rows"), anyList());
            verify(req).setAttribute(eq("grandTotal"), any(BigDecimal.class));
            verify(req).setAttribute(eq("ordersCsv"), anyString());
            verify(req).setAttribute(eq("walletBalance"), any(BigDecimal.class));
            verify(req).setAttribute(eq("qrAccountNo"), anyString());
            verify(req).setAttribute(eq("qrAccountName"), anyString());
            verify(req).setAttribute(eq("qrAddInfo"), anyString());
            verify(requestDispatcher).forward(req, resp);
        }
    }

    @Test
    void get_customerNotFound_redirectsWithFlash() throws Exception {
        bag.put("account", acc(6));
        when(req.getParameter("orders")).thenReturn("300");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock main query returns data
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(300);
            when(rsMain.getString("bike_name")).thenReturn("Test Bike");
            when(rsMain.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));
            when(rsMain.getDate("end_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(2)));
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));
            when(rsMain.getString("status")).thenReturn("pending");

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock customer not found
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(false);

            servlet.doGet(req, resp);

            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

    // ======================= doPost() Tests =======================

    @Test
    void post_notLoggedIn_redirectLogin() throws Exception {
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/login.jsp");
    }

    @Test
    void post_noOrders_flashRedirect() throws Exception {
        bag.put("account", acc(1));
        when(req.getParameter("orders")).thenReturn("");
        servlet.doPost(req, resp);
        verify(resp).sendRedirect("/ctx/customerorders");
        Assertions.assertThat(bag).containsKey("flash");
    }

    @Test
    void post_noPayableOrders_flashRedirect() throws Exception {
        bag.put("account", acc(8));
        when(req.getParameter("orders")).thenReturn("400");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock payable orders returns empty
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(false);

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

    @Test
    void post_success_bankTransfer_minimal() throws Exception {
        bag.put("account", acc(10));
        when(req.getParameter("orders")).thenReturn("101");
        when(req.getParameter("paymentMethod")).thenReturn("transfer");
        when(req.getParameter("walletAmount")).thenReturn(null);

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(101);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock customer lookup for wallet
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(456);

            // Mock wallet check (no wallet exists)
            PreparedStatement psWallet = mock(PreparedStatement.class);
            ResultSet rsWallet = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("SELECT wallet_id")))).thenReturn(psWallet);
            when(psWallet.executeQuery()).thenReturn(rsWallet);
            when(rsWallet.next()).thenReturn(false); // No wallet exists

            // Mock insert payment
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments"))))
                    .thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});

            // Mock confirm + safe
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'"))))
                    .thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET pickup_status"))))
                    .thenReturn(psSafe1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET return_status"))))
                    .thenReturn(psSafe2);

            // Mock lock bikes
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            // Mock email query
            when(con.prepareStatement(eq("SELECT full_name, email FROM Customers WHERE account_id = ?")))
                    .thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getString("full_name")).thenReturn("Tester");
            when(rsCusInfo.getString("email")).thenReturn("t@example.com");
            when(con.prepareStatement(argThat(sqlContains("SELECT r.order_id"))))
                    .thenReturn(psOrdersEmail);
            when(psOrdersEmail.executeQuery()).thenReturn(rsOrdersEmail);
            when(rsOrdersEmail.next()).thenReturn(true, false);
            when(rsOrdersEmail.getInt("order_id")).thenReturn(101);
            when(rsOrdersEmail.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));
            when(rsOrdersEmail.getDate("start_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now()));
            when(rsOrdersEmail.getDate("end_date")).thenReturn(java.sql.Date.valueOf(LocalDate.now().plusDays(1)));
            when(rsOrdersEmail.getString("bike_name")).thenReturn("Yamaha");

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).commit();
            verify(resp).sendRedirect("/ctx/customerorders");
            mail.verify(() -> EmailUtil.sendMailHTML(eq("t@example.com"), anyString(), anyString()));
        }
    }

    @Test
    void post_walletPayment_success() throws Exception {
        bag.put("account", acc(15));
        when(req.getParameter("orders")).thenReturn("301");
        when(req.getParameter("paymentMethod")).thenReturn("wallet");
        when(req.getParameter("walletAmount")).thenReturn("1000000");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock customer lookup
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(456);

            // Mock wallet deduction - existing wallet with sufficient balance
            PreparedStatement psWallet = mock(PreparedStatement.class);
            ResultSet rsWallet = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("SELECT wallet_id, balance FROM Wallets")))).thenReturn(psWallet);
            when(psWallet.executeQuery()).thenReturn(rsWallet);
            when(rsWallet.next()).thenReturn(true);
            when(rsWallet.getInt("wallet_id")).thenReturn(1);
            when(rsWallet.getBigDecimal("balance")).thenReturn(new BigDecimal("2000000"));

            // Mock payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r")))).thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(301);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("3000000"));

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock wallet update
            PreparedStatement psUpdateWallet = mock(PreparedStatement.class);
            when(con.prepareStatement(argThat(sqlContains("UPDATE Wallets SET balance")))).thenReturn(psUpdateWallet);
            when(psUpdateWallet.executeUpdate()).thenReturn(1);

            // Mock wallet transaction
            PreparedStatement psWalletTx = mock(PreparedStatement.class);
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Wallet_Transactions")))).thenReturn(psWalletTx);
            when(psWalletTx.executeUpdate()).thenReturn(1);

            // Mock other database operations
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments")))).thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'")))).thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            // Mock email
            when(con.prepareStatement(eq("SELECT full_name, email FROM Customers WHERE account_id = ?"))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getString("full_name")).thenReturn("Wallet User");
            when(rsCusInfo.getString("email")).thenReturn("wallet@example.com");

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).commit();
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

//    @Test
//    void post_walletInsufficientBalance_fails() throws Exception {
//        bag.put("account", acc(20));
//        when(req.getParameter("orders")).thenReturn("401");
//        when(req.getParameter("paymentMethod")).thenReturn("wallet");
//        when(req.getParameter("walletAmount")).thenReturn("1000000");
//
//        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
//            db.when(DBConnection::getConnection).thenReturn(con);
//
//            // Mock customer lookup
//            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
//            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
//            when(rsCusInfo.next()).thenReturn(true);
//            when(rsCusInfo.getInt("customer_id")).thenReturn(789);
//
//            // Mock wallet with insufficient balance
//            PreparedStatement psWallet = mock(PreparedStatement.class);
//            ResultSet rsWallet = mock(ResultSet.class);
//            when(con.prepareStatement(argThat(sqlContains("SELECT wallet_id, balance FROM Wallets")))).thenReturn(psWallet);
//            when(psWallet.executeQuery()).thenReturn(rsWallet);
//            when(rsWallet.next()).thenReturn(true);
//            when(rsWallet.getInt("wallet_id")).thenReturn(2);
//            when(rsWallet.getBigDecimal("balance")).thenReturn(new BigDecimal("500000")); // Only 500k, need 1M
//
//            doNothing().when(con).setAutoCommit(false);
//            doNothing().when(con).rollback();
//            doNothing().when(con).setAutoCommit(true);
//
//            servlet.doPost(req, resp);
//
//            verify(con).rollback();
//            verify(resp).sendRedirect("/ctx/customerorders");
//            Assertions.assertThat(bag.get("flash")).asString().contains("Số dư ví không đủ");
//        }
//    }

    @Test
    void post_mixedPayment_success() throws Exception {
        bag.put("account", acc(25));
        when(req.getParameter("orders")).thenReturn("501");
        when(req.getParameter("paymentMethod")).thenReturn("wallet_transfer");
        when(req.getParameter("walletAmount")).thenReturn("500000");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock customer lookup
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(999);

            // Mock wallet operations
            PreparedStatement psWallet = mock(PreparedStatement.class);
            ResultSet rsWallet = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("SELECT wallet_id, balance FROM Wallets")))).thenReturn(psWallet);
            when(psWallet.executeQuery()).thenReturn(rsWallet);
            when(rsWallet.next()).thenReturn(true);
            when(rsWallet.getInt("wallet_id")).thenReturn(3);
            when(rsWallet.getBigDecimal("balance")).thenReturn(new BigDecimal("1000000"));

            // Mock payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r")))).thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(501);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("2000000"));

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock wallet update and transaction
            PreparedStatement psUpdateWallet = mock(PreparedStatement.class);
            when(con.prepareStatement(argThat(sqlContains("UPDATE Wallets SET balance")))).thenReturn(psUpdateWallet);
            when(psUpdateWallet.executeUpdate()).thenReturn(1);

            PreparedStatement psWalletTx = mock(PreparedStatement.class);
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Wallet_Transactions")))).thenReturn(psWalletTx);
            when(psWalletTx.executeUpdate()).thenReturn(1);

            // Mock other operations
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments")))).thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'")))).thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            // Mock email
            when(con.prepareStatement(eq("SELECT full_name, email FROM Customers WHERE account_id = ?"))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getString("full_name")).thenReturn("Mixed User");
            when(rsCusInfo.getString("email")).thenReturn("mixed@example.com");

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).commit();
            verify(resp).sendRedirect("/ctx/customerorders");
        }
    }

    @Test
    void post_exceptionDuringProcessing_rollback() throws Exception {
        bag.put("account", acc(30));
        when(req.getParameter("orders")).thenReturn("601");
        when(req.getParameter("paymentMethod")).thenReturn("transfer");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock payable orders throws exception
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenThrow(new SQLException("Database error"));

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).rollback();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            verify(con).rollback();
            verify(resp).sendRedirect("/ctx/customerorders");
            Assertions.assertThat(bag).containsKey("flash");
        }
    }

    @Test
    void post_walletCreateNew_success() throws Exception {
        bag.put("account", acc(35));
        when(req.getParameter("orders")).thenReturn("701");
        when(req.getParameter("paymentMethod")).thenReturn("wallet");
        when(req.getParameter("walletAmount")).thenReturn("300000");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock customer lookup
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(111);

            // Mock wallet - no existing wallet
            PreparedStatement psWallet = mock(PreparedStatement.class);
            ResultSet rsWallet = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("SELECT wallet_id, balance FROM Wallets")))).thenReturn(psWallet);
            when(psWallet.executeQuery()).thenReturn(rsWallet);
            when(rsWallet.next()).thenReturn(false); // No wallet exists

            // Mock create new wallet
            PreparedStatement psCreateWallet = mock(PreparedStatement.class);
            ResultSet rsGeneratedKeys = mock(ResultSet.class);
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Wallets")))).thenReturn(psCreateWallet);
            when(psCreateWallet.executeUpdate()).thenReturn(1);
            when(psCreateWallet.getGeneratedKeys()).thenReturn(rsGeneratedKeys);
            when(rsGeneratedKeys.next()).thenReturn(true);
            when(rsGeneratedKeys.getInt(1)).thenReturn(999);

            // Mock payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r")))).thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(701);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock wallet operations will fail due to insufficient balance in new wallet
            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).rollback();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            // Should rollback because new wallet has 0 balance but trying to deduct 300k
            verify(con).rollback();
            verify(resp).sendRedirect("/ctx/customerorders");
        }
    }

    @Test
    void post_emailException_stillSuccess() throws Exception {
        bag.put("account", acc(40));
        when(req.getParameter("orders")).thenReturn("801");
        when(req.getParameter("paymentMethod")).thenReturn("transfer");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class);
             MockedStatic<EmailUtil> mail = Mockito.mockStatic(EmailUtil.class)) {

            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock payable orders
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(true, false);
            when(rsMain.getInt("order_id")).thenReturn(801);
            when(rsMain.getBigDecimal("total_price")).thenReturn(new BigDecimal("1000000"));

            // Mock deposit
            when(con.prepareStatement(argThat(sqlContains("SELECT SUM(")))).thenReturn(psDeposit);
            when(psDeposit.executeQuery()).thenReturn(rsDeposit);
            when(rsDeposit.next()).thenReturn(true);
            when(rsDeposit.getBigDecimal("deposit")).thenReturn(new BigDecimal("500000"));

            // Mock customer lookup
            when(con.prepareStatement(argThat(sqlContains("SELECT customer_id FROM Customers")))).thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getInt("customer_id")).thenReturn(222);

            // Mock other database operations
            when(con.prepareStatement(argThat(sqlContains("INSERT INTO Payments")))).thenReturn(psInsertPayment);
            when(psInsertPayment.executeBatch()).thenReturn(new int[]{1});
            when(con.prepareStatement(argThat(sqlContains("UPDATE RentalOrders SET status='confirmed'")))).thenReturn(psUpdateConfirm);
            when(psUpdateConfirm.executeUpdate()).thenReturn(1);
            when(con.prepareStatement(argThat(sqlContains("UPDATE m")))).thenReturn(psLockBikes);
            when(psLockBikes.executeUpdate()).thenReturn(1);

            // Mock email to throw exception
            when(con.prepareStatement(eq("SELECT full_name, email FROM Customers WHERE account_id = ?")))
                    .thenReturn(psCusInfo);
            when(psCusInfo.executeQuery()).thenReturn(rsCusInfo);
            when(rsCusInfo.next()).thenReturn(true);
            when(rsCusInfo.getString("full_name")).thenReturn("Email Test");
            when(rsCusInfo.getString("email")).thenReturn("email@example.com");

            mail.when(() -> EmailUtil.sendMailHTML(anyString(), anyString(), anyString()))
                    .thenThrow(new RuntimeException("Email server down"));

            doNothing().when(con).setAutoCommit(false);
            doNothing().when(con).commit();
            doNothing().when(con).setAutoCommit(true);

            servlet.doPost(req, resp);

            // Should still commit even if email fails
            verify(con).commit();
            verify(resp).sendRedirect("/ctx/customerorders");
        }
    }

    // ======================= Helper Method Tests =======================

    // Note: parseIds method is private, so we cannot test it directly
    // Instead, we test it indirectly through the public methods that use it

    @Test
    void parseIds_indirectTestThroughDoGet() throws Exception {
        bag.put("account", acc(50));
        when(req.getParameter("orders")).thenReturn("1,2,3");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            // Mock empty result set - we just want to test parameter parsing
            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(false);

            servlet.doGet(req, resp);

            // Verify that the query was prepared with the correct number of parameters
            // This indirectly tests that parseIds correctly parsed "1,2,3" into 3 IDs
            verify(con).prepareStatement(argThat(sql -> {
                // Count the number of ? in the SQL - should be 3 for the order IDs plus 1 for account_id
                long count = sql.chars().filter(ch -> ch == '?').count();
                return count == 4; // 3 order IDs + 1 account_id
            }));
        }
    }

    @Test
    void parseIds_indirectTestInvalidInput() throws Exception {
        bag.put("account", acc(51));
        when(req.getParameter("orders")).thenReturn("1,abc,3");

        try (MockedStatic<DBConnection> db = Mockito.mockStatic(DBConnection.class)) {
            db.when(DBConnection::getConnection).thenReturn(con);

            when(con.prepareStatement(argThat(sqlContains("FROM RentalOrders r"))))
                    .thenReturn(psMain);
            when(psMain.executeQuery()).thenReturn(rsMain);
            when(rsMain.next()).thenReturn(false);

            servlet.doGet(req, resp);

            // Should only process valid IDs (1 and 3), so 2 parameters for order IDs
            verify(con).prepareStatement(argThat(sql -> {
                long count = sql.chars().filter(ch -> ch == '?').count();
                return count == 3; // 2 valid order IDs + 1 account_id
            }));
        }
    }
}