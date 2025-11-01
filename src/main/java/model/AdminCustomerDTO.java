package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AdminCustomerDTO {
    private int id;
    private String fullName;
    private String email;
    private String phone;
    private boolean emailVerified;
    private boolean banned;
    private String address;
    private java.sql.Date dob;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    private BigDecimal wallet;
    private int orders;
    private BigDecimal totalSpent;
    private Timestamp lastOrderAt;
    private List<OrderMini> recentOrders = new ArrayList<>();

    public static class OrderMini {
        private int orderId;
        private String bikeName;
        private BigDecimal total;
        private String status;
        private Timestamp createdAt;

        public OrderMini() {
        }

        public OrderMini(int orderId, String bikeName, BigDecimal total, String status, Timestamp createdAt) {
            this.orderId = orderId;
            this.bikeName = bikeName;
            this.total = total;
            this.status = status;
            this.createdAt = createdAt;
        }

        public int getOrderId() {
            return orderId;
        }

        public void setOrderId(int orderId) {
            this.orderId = orderId;
        }

        public String getBikeName() {
            return bikeName;
        }

        public void setBikeName(String bikeName) {
            this.bikeName = bikeName;
        }

        public BigDecimal getTotal() {
            return total;
        }

        public void setTotal(BigDecimal total) {
            this.total = total;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Timestamp getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Timestamp createdAt) {
            this.createdAt = createdAt;
        }
    }

    public AdminCustomerDTO() {
    }

    public AdminCustomerDTO(int id, String fullName, String email, String phone,
                            boolean emailVerified, boolean banned, String address,
                            java.sql.Date dob, Timestamp createdAt, Timestamp lastLogin,
                            BigDecimal wallet, int orders, BigDecimal totalSpent,
                            Timestamp lastOrderAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.emailVerified = emailVerified;
        this.banned = banned;
        this.address = address;
        this.dob = dob;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
        this.wallet = wallet;
        this.orders = orders;
        this.totalSpent = totalSpent;
        this.lastOrderAt = lastOrderAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isBanned() {
        return banned;
    }

    public void setBanned(boolean banned) {
        this.banned = banned;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public java.sql.Date getDob() {
        return dob;
    }

    public void setDob(java.sql.Date dob) {
        this.dob = dob;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    public BigDecimal getWallet() {
        return wallet;
    }

    public void setWallet(BigDecimal wallet) {
        this.wallet = wallet;
    }

    public int getOrders() {
        return orders;
    }

    public void setOrders(int orders) {
        this.orders = orders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(BigDecimal totalSpent) {
        this.totalSpent = totalSpent;
    }

    public Timestamp getLastOrderAt() {
        return lastOrderAt;
    }

    public void setLastOrderAt(Timestamp lastOrderAt) {
        this.lastOrderAt = lastOrderAt;
    }

    public List<OrderMini> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<OrderMini> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public void addRecentOrder(OrderMini order) {
        this.recentOrders.add(order);
    }


}