package model;

import java.sql.Timestamp;

public class Account {
    private int accountId;
    private String username;
    private String password;   // demo: plaintext
    private String role;       // customer | partner | admin
    private boolean status;
    private boolean emailVerified; // ✅ thêm cờ xác thực email
     private Timestamp lastLogin;
    public Account() {}

    public Account(int accountId, String username, String password, String role, boolean status) {
        this.accountId = accountId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.status = status;
        
    }

    // Getter & Setter
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public Timestamp getLastLogin() { return lastLogin; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
}
