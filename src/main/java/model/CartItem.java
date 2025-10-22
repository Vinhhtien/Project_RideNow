package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.temporal.ChronoUnit;

public class CartItem {
    private int bikeId;
    private String bikeName;
    private BigDecimal pricePerDay;
    private String typeName;
    private Date startDate;
    private Date endDate;
    private int days;
    private BigDecimal subtotal;
    private BigDecimal deposit;

    public CartItem(int bikeId, String bikeName, BigDecimal pricePerDay,
                    String typeName, Date startDate, Date endDate) {
        this.bikeId = bikeId;
        this.bikeName = bikeName;
        this.pricePerDay = pricePerDay;
        this.typeName = typeName;
        this.startDate = startDate;
        this.endDate = endDate;
        calculateValues();
    }

    private void calculateValues() {
        // Tính số ngày
        if (startDate != null && endDate != null) {
            long diff = ChronoUnit.DAYS.between(startDate.toLocalDate(), endDate.toLocalDate()) + 1;
            this.days = (int) Math.max(diff, 1);
        } else {
            this.days = 0;
        }
        
        // Tính subtotal
        this.subtotal = pricePerDay.multiply(BigDecimal.valueOf(this.days));
        
        // Tính tiền cọc
        if (typeName != null) {
            String t = typeName.toLowerCase();
            if (t.contains("phân khối") || t.contains("pkl")) {
                this.deposit = BigDecimal.valueOf(1_000_000);
            } else {
                this.deposit = BigDecimal.valueOf(500_000);
            }
        } else {
            this.deposit = BigDecimal.valueOf(500_000);
        }
    }

    // === getters ===
    public int getBikeId() { return bikeId; }
    public String getBikeName() { return bikeName; }
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public String getTypeName() { return typeName; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public int getDays() { return days; }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDeposit() { return deposit; }

    // === setters ===
    public void setStartDate(Date startDate) { 
        this.startDate = startDate; 
        calculateValues();
    }
    
    public void setEndDate(Date endDate) { 
        this.endDate = endDate; 
        calculateValues();
    }
    
    public void setDays(int days) { 
        this.days = days; 
    }
    
    public void setSubtotal(BigDecimal subtotal) { 
        this.subtotal = subtotal; 
    }
    
    public void setDeposit(BigDecimal deposit) { 
        this.deposit = deposit; 
    }
    
    // Phương thức cập nhật toàn bộ ngày
    public void updateDates(Date newStart, Date newEnd) {
        this.startDate = newStart;
        this.endDate = newEnd;
        calculateValues();
    }
}