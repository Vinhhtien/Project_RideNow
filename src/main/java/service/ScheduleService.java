package service;

import dao.IScheduleDao;
import dao.ScheduleDao;
import model.ScheduleItem;

import java.time.LocalDate;
import java.util.List;

public class ScheduleService implements IScheduleService {

    private final IScheduleDao scheduleDao = new ScheduleDao();

    @Override
    public List<ScheduleItem> getAdminSchedule(int adminId, LocalDate from, LocalDate to) {
        if (from == null || to == null || to.isBefore(from)) {
            // fallback: tuần hiện tại
            LocalDate today = LocalDate.now();
            from = today.with(java.time.DayOfWeek.MONDAY);
            to = from.plusDays(6);
        }
        return scheduleDao.findByAdminAndDateRange(adminId, from, to);
    }
}
