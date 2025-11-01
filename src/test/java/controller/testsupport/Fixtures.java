package controller.testsupport;

import model.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

public class Fixtures {
    public static Account account(int id, String role) {
        Account a = new Account();
        a.setAccountId(id);
        a.setUsername("user" + id);
        a.setRole(role);
        a.setStatus(true);
        return a;
    }

    public static Customer customer(int accountId, int customerId) {
        Customer c = new Customer();
        c.setAccountId(accountId);
        c.setCustomerId(customerId);
        c.setFullName("Customer " + customerId);
        c.setEmail("c" + customerId + "@ex.com");
        return c;
    }

    public static MotorbikeListItem bikeItem(int id, String name, String type, BigDecimal price) {
        MotorbikeListItem i = new MotorbikeListItem();
        i.setBikeId(id);
        i.setBikeName(name);
        i.setTypeName(type);
        i.setPricePerDay(price);
        i.setStatus("available");
        return i;
    }

    public static Date today() {
        return Date.valueOf(LocalDate.now());
    }

    public static Date tomorrow() {
        return Date.valueOf(LocalDate.now().plusDays(1));
    }
}

