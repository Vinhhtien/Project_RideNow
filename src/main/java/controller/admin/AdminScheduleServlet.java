package controller.admin;

import dao.AdminSchedualDao;
import dao.IAdminSchedualDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.ScheduleItem;
import service.IScheduleService;
import service.ScheduleService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet(name = "AdminScheduleServlet", urlPatterns = {"/admin/schedule"})
public class AdminScheduleServlet extends HttpServlet {

    private final IScheduleService scheduleService = new ScheduleService();
    private final IAdminSchedualDao adminSchedualDao = new AdminSchedualDao();
    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Account acc = (Account) session.getAttribute("account");
        if (acc == null || !"admin".equalsIgnoreCase(acc.getRole())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Lấy adminId: ưu tiên lấy từ session nếu đã có (cache),
        // nếu chưa thì map từ account_id qua DAO (KHÔNG sửa LoginServlet).
        Integer adminId = (Integer) session.getAttribute("adminId");
        if (adminId == null) {
            adminId = adminSchedualDao.findAdminIdByAccountId(acc.getAccountId());
            if (adminId == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Không tìm thấy Admin tương ứng với tài khoản này.");
                return;
            }
            session.setAttribute("adminId", adminId); // cache cho các request sau
        }

        // Đọc filter view/from/to
        String view = req.getParameter("view"); // week|month
        String fromStr = req.getParameter("from");
        String toStr   = req.getParameter("to");

        LocalDate from, to;
        if (fromStr != null && toStr != null && !fromStr.isBlank() && !toStr.isBlank()) {
            from = LocalDate.parse(fromStr, DF);
            to   = LocalDate.parse(toStr, DF);
            if (to.isBefore(from)) {
                // tự chỉnh về tuần hiện tại khi nhập sai
                LocalDate mon = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
                from = mon; to = mon.plusDays(6); view = "week";
            }
        } else if ("month".equalsIgnoreCase(view)) {
            LocalDate now = LocalDate.now();
            from = now.withDayOfMonth(1);
            to   = from.plusMonths(1).minusDays(1);
        } else {
            LocalDate mon = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
            from = mon; to = mon.plusDays(6); view = "week";
        }

        List<ScheduleItem> items = scheduleService.getAdminSchedule(adminId, from, to);

        req.setAttribute("items", items);
        req.setAttribute("from", from);
        req.setAttribute("to", to);
        req.setAttribute("view", view);

        req.getRequestDispatcher("/admin/admin-schedule.jsp").forward(req, resp);
    }
}
