package model;

public class Partner {
    private int partnerId;
    private int accountId;
    private String fullName; // khớp cột company_name
    private String address;
    private String phone;
    private Integer adminId;    // có thể null ở dữ liệu cũ

    public Partner() {}

    public Partner(int partnerId, int accountId, String companyName,
                   String address, String phone, Integer adminId) {
        this.partnerId = partnerId;
        this.accountId = accountId;
        this.fullName = fullName;
        this.address = address;
        this.phone = phone;
        this.adminId = adminId;
    }

    public int getPartnerId() { return partnerId; }
    public void setPartnerId(int partnerId) { this.partnerId = partnerId; }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }

    public String getFullname() { return fullName; }
    public void setFullname(String companyName) { this.fullName = fullName; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }
}
