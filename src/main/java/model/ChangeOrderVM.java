package model;

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

    
     // Thêm các trường mới
    private int bikeId;
    private int originalRentalDays;
    private List<Date[]> conflictingDates; // Danh sách ngày bị trùng
    
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

    public boolean isWithin30Min() {
        return "confirmed".equalsIgnoreCase(status) && confirmedAt != null && remainingMinutes > 0;
    }
    
    
    // Tính số ngày thuê ban đầu
    public int getOriginalRentalDays() {
        if (start != null && end != null) {
            long diff = end.getTime() - start.getTime();
            return (int) (diff / (1000 * 60 * 60 * 24)) + 1; // +1 để tính cả ngày cuối
        }
        return 0;
    }
    
    // Tính ngày kết thúc dựa trên ngày bắt đầu mới
    public Date calculateNewEnd(Date newStart) {
        if (newStart == null) return null;
        Calendar cal = Calendar.getInstance();
        cal.setTime(newStart);
        cal.add(Calendar.DATE, getOriginalRentalDays() - 1); // Trừ 1 vì tính cả ngày đầu
        return new Date(cal.getTimeInMillis());
    }
    
}