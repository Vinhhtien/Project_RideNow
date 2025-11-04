package dao;

import model.adminfeedback.AdminFeedbackItem;
import model.adminfeedback.AdminFeedbackSummary;
import model.adminfeedback.FeedbackType;

import java.util.Date;
import java.util.List;

public interface IAdminFeedbackDao {
    AdminFeedbackSummary getSummary(Date from, Date to) throws Exception;

    List<AdminFeedbackItem> findAll(Date from, Date to,
                                    FeedbackType type, Integer star,
                                    int offset, int limit) throws Exception;

    int countAll(Date from, Date to, FeedbackType type, Integer star) throws Exception;
}
