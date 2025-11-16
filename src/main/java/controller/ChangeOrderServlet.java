package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import dao.IOrderChangeDao;
import dao.OrderChangeDao;
import model.Account;
import model.ChangeOrderVM;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

@WebServlet(name="ChangeOrderServlet", urlPatterns={"/change-order"})
public class ChangeOrderServlet extends HttpServlet {

    private final IOrderChangeDao changeDao = new OrderChangeDao();

    // ====================== GET: LOAD MÀN HÌNH ĐỔI/HỦY ======================
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("=== CHANGE ORDER SERVLET - START ===");
        System.out.println("[ChangeOrder][GET] HIT uri=" + req.getRequestURI()
                + " q=" + req.getQueryString());

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        System.out.println("[ChangeOrder] Session account: " + (acc != null ? acc.getAccountId() : "NULL"));

        if (acc == null) {
            System.out.println("[ChangeOrder] no session account → redirect login");
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String sid = req.getParameter("orderId");
        System.out.println("[ChangeOrder] orderId param = " + sid);
        if (sid == null) {
            System.out.println("[ChangeOrder] No orderId parameter → redirect");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(sid);
            System.out.println("[ChangeOrder] Parsed orderId: " + orderId);

            System.out.println("[ChangeOrder] Calling changeDao.loadChangeOrderVM...");
            ChangeOrderVM vm = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());

            System.out.println("[ChangeOrder] VM result: " + (vm != null ? "FOUND" : "NULL"));

            if (vm == null) {
                System.out.println("[ChangeOrder] VM is null - order not found or no permission");
                req.getSession().setAttribute("flash",
                        "Không tìm thấy đơn hoặc bạn không có quyền truy cập.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            System.out.println("[ChangeOrder] VM details - Status: " + vm.getStatus() +
                    ", BikeId: " + vm.getBikeId() +
                    ", RentalDays: " + vm.getOriginalRentalDays() +
                    ", RemainingMinutes: " + vm.getRemainingMinutes() +
                    ", ConfirmedAt: " + vm.getConfirmedAt() +
                    ", ChangeCount: " + vm.getChangeCount());

            // Lấy số lần HỦY (trong 30') theo account để show ở JSP
            int cancelCount = changeDao.getCancelCountByAccount(acc.getAccountId());
            System.out.println("[ChangeOrder] cancel_in30_count (by account) = " + cancelCount);
            req.setAttribute("cancelCount", cancelCount);

            if (!vm.isWithin30Min()) {
                System.out.println("[ChangeOrder] Outside 30min window - remaining: " + vm.getRemainingMinutes());
                req.getSession().setAttribute("flash",
                        "Đã quá 30 phút sau khi xác nhận, không thể đổi/hủy đơn.");
                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            req.setAttribute("vm", vm);
            System.out.println("[ChangeOrder] Forwarding to /customer/change-order.jsp");
            req.getRequestDispatcher("/customer/change-order.jsp").forward(req, resp);

        } catch (NumberFormatException e) {
            System.out.println("[ChangeOrder] Invalid orderId format: " + sid);
            req.getSession().setAttribute("flash", "Mã đơn hàng không hợp lệ.");
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        } catch (Exception e) {
            System.out.println("[ChangeOrder] Exception: " + e.getMessage());
            e.printStackTrace();
            req.getSession().setAttribute("flash","Lỗi khi tải trang đổi đơn: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }

        System.out.println("=== CHANGE ORDER SERVLET - END ===");
    }

    // ====================== POST: ĐỔI NGÀY / HỦY ĐƠN ======================
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login.jsp");
            return;
        }

        String action = req.getParameter("action");
        String sid = req.getParameter("orderId");
        if (sid == null) {
            resp.sendRedirect(req.getContextPath() + "/customerorders");
            return;
        }

        try {
            int orderId = Integer.parseInt(sid);

            // ==================== 1) HỦY ĐƠN TRONG 30 PHÚT ====================
            if ("cancel".equals(action)) {

                // Load VM để tính refund trước khi hủy
                ChangeOrderVM vmBeforeCancel = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());
                if (vmBeforeCancel == null) {
                    req.getSession().setAttribute("flash",
                            "Không tìm thấy đơn hàng hoặc không có quyền truy cập.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                BigDecimal expectedRefund = vmBeforeCancel.getRefundAmount();

                System.out.println("[ChangeOrderServlet] Cancelling order #" + orderId +
                        ", expected refund: " + expectedRefund);

                // DAO: 0 = fail, 1 = OK, 2 = OK + vừa bị ban
                int result = changeDao.cancelConfirmedOrderWithin30Min(orderId, acc.getAccountId());

                if (result == 0) {
                    req.getSession().setAttribute("flash",
                            "Hủy thất bại (có thể đã quá hạn 30 phút hoặc không đúng quyền).");
                    System.out.println("[ChangeOrderServlet] Failed to cancel order #" + orderId);
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                if (result == 2) {
                    // Hủy thành công + account đã bị ban trong DAO (status=0, cancel_in30_count=0)
                    System.out.println("[ChangeOrderServlet] Cancel OK and account "
                            + acc.getAccountId() + " has been BANNED. Invalidate session & go login.");

                    if (session != null) {
                        session.invalidate();
                    }

                    req.setAttribute("error",
                            "Đơn #" + orderId + " đã được hủy. Số tiền "
                            + String.format("%,d", expectedRefund.intValue())
                            + " đ (cọc + 30%) đã được hoàn vào ví.\n\n"
                            + "Tuy nhiên, bạn đã hủy đơn 3 lần trong thời gian ngắn, "
                            + "tài khoản của bạn đã bị khóa theo chính sách RideNow. "
                            + "Vui lòng liên hệ quản trị viên để được hỗ trợ.");

                    req.getRequestDispatcher("/login.jsp").forward(req, resp);
                    return;
                }

                // result == 1 → hủy OK, chưa bị ban
                req.getSession().setAttribute("flash",
                        "Đã hủy đơn #" + orderId + " thành công. Số tiền " +
                                String.format("%,d", expectedRefund.intValue()) +
                                " đ (cọc + 30%) đã được hoàn vào ví.");
                System.out.println("[ChangeOrderServlet] Successfully cancelled order #" + orderId +
                        ", refund amount: " + expectedRefund);

                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            // ==================== 2) ĐỔI NGÀY ĐẶT ====================
            if ("update_dates".equals(action)) {

                // Lấy ngày mới từ form (yyyy-MM-dd)
                Date newStart = Date.valueOf(req.getParameter("start"));
                Date newEnd   = Date.valueOf(req.getParameter("end"));

                // Load VM để kiểm tra 30' + số ngày + changeCount hiện tại
                ChangeOrderVM vm = changeDao.loadChangeOrderVM(orderId, acc.getAccountId());
                if (vm == null || !vm.isWithin30Min()) {
                    req.getSession().setAttribute("flash",
                            "Không thể đổi đơn: đơn không tồn tại, không thuộc về bạn hoặc đã quá hạn 30 phút.");
                    resp.sendRedirect(req.getContextPath() + "/customerorders");
                    return;
                }

                int originalDays   = vm.getOriginalRentalDays();
                int currentChanges = vm.getChangeCount();

                System.out.println("[ChangeOrderServlet] update_dates - orderId=" + orderId +
                        ", originalDays=" + originalDays +
                        ", currentChangeCount=" + currentChanges);

                // Kiểm tra số ngày thuê giữ nguyên
                int newRentalDays = (int) ((newEnd.getTime() - newStart.getTime()) / (1000L * 60 * 60 * 24)) + 1;
                if (newRentalDays != originalDays) {
                    req.getSession().setAttribute("flash",
                            "Số ngày thuê phải giữ nguyên (" + originalDays + " ngày). " +
                                    "Bạn đã chọn " + newRentalDays + " ngày.");
                    resp.sendRedirect(req.getContextPath() + "/change-order?orderId=" + orderId);
                    return;
                }

                // Ngày start <= end (safety)
                if (newStart.after(newEnd)) {
                    req.getSession().setAttribute("flash",
                            "Ngày bắt đầu không thể sau ngày kết thúc.");
                    resp.sendRedirect(req.getContextPath() + "/change-order?orderId=" + orderId);
                    return;
                }

                // Không cho chọn ngày quá khứ
                Date today = new Date(System.currentTimeMillis());
                if (newStart.before(today)) {
                    req.getSession().setAttribute("flash",
                            "Không thể chọn ngày trong quá khứ.");
                    resp.sendRedirect(req.getContextPath() + "/change-order?orderId=" + orderId);
                    return;
                }

                // Gọi DAO để update (check 30', trùng lịch, limit 3 lần,… ở trong DAO)
                IOrderChangeDao.ChangeResult result = changeDao.updateOrderDatesWithin30Min(
                        orderId, acc.getAccountId(), newStart, newEnd);

                switch (result) {
                    case OK -> {
                        int newChangeCount = currentChanges + 1;

                        if (newChangeCount == 3) {
                            req.getSession().setAttribute("flash",
                                    "Đã đổi thời gian cho đơn #" + orderId +
                                            " thành công (" + originalDays + " ngày). " +
                                            "Lưu ý: Bạn đã đổi lần thứ 3, hệ thống đã trừ 10% tiền cọc theo chính sách đổi đơn.");
                        } else {
                            req.getSession().setAttribute("flash",
                                    "Đã đổi thời gian cho đơn #" + orderId +
                                            " thành công (" + originalDays + " ngày). " +
                                            "Số lần đổi hiện tại: " + newChangeCount + "/3.");
                        }
                    }
                    case EXPIRED -> req.getSession().setAttribute("flash",
                            "Đã quá hạn 30 phút để đổi đơn. Vui lòng liên hệ hỗ trợ nếu cần.");
                    case CONFLICT -> req.getSession().setAttribute("flash",
                            "Khoảng thời gian này đã có người đặt xe này. Vui lòng chọn khoảng ngày khác.");
                    case LIMIT_REACHED -> req.getSession().setAttribute("flash",
                            "Đơn hàng này đã vượt quá số lần đổi cho phép (3 lần). Bạn không thể đổi thêm.");
                    case FAIL -> req.getSession().setAttribute("flash",
                            "Cập nhật thất bại. Vui lòng kiểm tra lại thông tin hoặc thử lại sau.");
                    default -> req.getSession().setAttribute("flash",
                            "Cập nhật thất bại. Vui lòng thử lại.");
                }

                resp.sendRedirect(req.getContextPath() + "/customerorders");
                return;
            }

            // Nếu action không khớp → quay lại danh sách đơn
            resp.sendRedirect(req.getContextPath() + "/customerorders");

        } catch (IllegalArgumentException e) {
            // Sai format ngày (Date.valueOf)
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Định dạng ngày không hợp lệ. Vui lòng chọn lại.");
            resp.sendRedirect(req.getContextPath() + "/change-order?orderId=" + sid);
        } catch (Exception e) {
            e.printStackTrace();
            req.getSession().setAttribute("flash",
                    "Lỗi đổi/hủy đơn: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/customerorders");
        }
    }
}
