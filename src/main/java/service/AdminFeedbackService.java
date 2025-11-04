package service;

import dao.AdminFeedbackDao;
import dao.IAdminFeedbackDao;
import model.adminfeedback.AdminFeedbackItem;
import model.adminfeedback.AdminFeedbackSummary;
import model.adminfeedback.FeedbackType;

import java.util.Date;
import java.util.List;

public class AdminFeedbackService implements IAdminFeedbackService {
    private final IAdminFeedbackDao dao = new AdminFeedbackDao();

    /** Giới hạn kích thước trang. Dùng chung cho export (50k). */
    public static final int MAX_PAGE_SIZE = 50_000;

    @Override
    public AdminFeedbackSummary getSummary(Date from, Date to) throws Exception {
        Date[] range = norm(from, to);
        return dao.getSummary(range[0], range[1]);
    }

    @Override
    public List<AdminFeedbackItem> findAll(
            Date from, Date to, FeedbackType type, Integer star, int page, int size) throws Exception {

        Date[] range = norm(from, to);
        int p = page < 1 ? 1 : page;
        int s = size < 1 ? 1 : Math.min(size, MAX_PAGE_SIZE);
        int offset = (p - 1) * s;

        Integer safeStar = clampStar(star); // phòng caller khác truyền 0/6/null

        return dao.findAll(range[0], range[1], type, safeStar, offset, s);
    }

    @Override
    public int countAll(Date from, Date to, FeedbackType type, Integer star) throws Exception {
        Date[] range = norm(from, to);
        Integer safeStar = clampStar(star);
        return dao.countAll(range[0], range[1], type, safeStar);
    }

    // Hoán đổi nếu from > to để tránh query trống
    private static Date[] norm(Date from, Date to) {
        if (from != null && to != null && from.after(to)) {
            Date tmp = from; from = to; to = tmp;
        }
        return new Date[]{from, to};
    }

    // Đảm bảo chỉ 1..5 hoặc null
    private static Integer clampStar(Integer star) {
        if (star == null) return null;
        if (star < 1) return 1;
        if (star > 5) return 5;
        return star;
    }
}
