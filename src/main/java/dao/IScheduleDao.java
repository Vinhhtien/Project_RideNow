package dao;

import model.ScheduleItem;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleDao {
    /**
     * Lấy lịch thuê xe thuộc Admin (bao gồm xe của Store của admin và xe Partner do admin quản lý)
     * trong khoảng ngày [from, to].
     */
    List<ScheduleItem> findByAdminAndDateRange(int adminId, LocalDate from, LocalDate to);
}
