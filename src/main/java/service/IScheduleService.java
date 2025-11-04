package service;

import model.ScheduleItem;

import java.time.LocalDate;
import java.util.List;

public interface IScheduleService {
    List<ScheduleItem> getAdminSchedule(int adminId, LocalDate from, LocalDate to);
}
