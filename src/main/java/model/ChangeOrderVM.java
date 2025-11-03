package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;

public class ChangeOrderVM {
    private int orderId;
    private String status;
    private Date start;
    private Date end;
    private Timestamp confirmedAt;
    private int remainingMinutes;
    private int bikeId;
    private int originalRentalDays; // SỬA: dùng int thay vì Integer
    private List<Date[]> conflictingDates;

     private BigDecimal depositAmount;
    private BigDecimal totalAmount;
    private BigDecimal refundAmount;
    
    // Getter và Setter
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getStart() { return start; }
    public void setStart(Date start) { this.start = start; }

    public Date getEnd() { return end; }
    public void setEnd(Date end) { this.end = end; }

    public Timestamp getConfirmedAt() { return confirmedAt; }
    public void setConfirmedAt(Timestamp confirmedAt) { this.confirmedAt = confirmedAt; }

    public int getRemainingMinutes() { return remainingMinutes; }
    public void setRemainingMinutes(int remainingMinutes) { this.remainingMinutes = remainingMinutes; }

    public int getBikeId() { return bikeId; }
    public void setBikeId(int bikeId) { this.bikeId = bikeId; }

    public int getOriginalRentalDays() { 
        // Ưu tiên giá trị đã được set từ database
        if (originalRentalDays > 0) {
            return originalRentalDays;
        }
        // Nếu chưa set, tính từ start và end
        if (start != null && end != null) {
            long diff = end.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1;
        }
        return 0;
    }
    
    public void setOriginalRentalDays(int originalRentalDays) { 
        this.originalRentalDays = originalRentalDays; 
    }

    public List<Date[]> getConflictingDates() { return conflictingDates; }
    public void setConflictingDates(List<Date[]> conflictingDates) { this.conflictingDates = conflictingDates; }
    
    public boolean isWithin30Min() {
        return "confirmed".equalsIgnoreCase(status) && confirmedAt != null && remainingMinutes > 0;
    }
    
    // Tính ngày kết thúc dựa trên ngày bắt đầu mới
    public Date calculateNewEnd(Date newStart) {
        if (newStart == null) return null;
        
        int rentalDays = getOriginalRentalDays();
        if (rentalDays <= 0) return null;
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(newStart);
        cal.add(Calendar.DATE, rentalDays - 1); // Trừ 1 vì tính cả ngày đầu
        return new Date(cal.getTimeInMillis());
    }
    
     public BigDecimal getDepositAmount() { return depositAmount; }
    public void setDepositAmount(BigDecimal depositAmount) { this.depositAmount = depositAmount; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public BigDecimal getRefundAmount() { 
        if (depositAmount != null && totalAmount != null) {
            return depositAmount.add(totalAmount.multiply(new BigDecimal("0.3")));
        }
        return BigDecimal.ZERO;
    }
    
}