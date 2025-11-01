package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Account;
import model.Customer;
import model.MotorbikeListItem;
import service.*;

import java.io.IOException;
import java.sql.Date;

@WebServlet(name = "BookingServlet", urlPatterns = {"/customerbook"})
public class BookingServlet extends HttpServlet {

    private final IMotorbikeService bikeService = new MotorbikeService();
    private final ICustomerService customerService = new CustomerService();
    private final IOrderService orderService = new OrderService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1) Bắt buộc đăng nhập
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            // 2) Input
            int bikeId = Integer.parseInt(req.getParameter("bikeId"));
            Date start = Date.valueOf(req.getParameter("start"));
            Date end = Date.valueOf(req.getParameter("end"));

            // 3) Kiểm tra xe có tồn tại
            MotorbikeListItem item = bikeService.getDetail(bikeId);
            if (item == null) {
                resp.sendError(404, "Xe không tồn tại");
                return;
            }

            // 4) Map account -> customer (yêu cầu có hồ sơ)
            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                // điều hướng về trang profile để bổ sung thông tin
                resp.sendRedirect(req.getContextPath() + "/customer/profile?need=1");
                return;
            }

            // 5) Tạo đơn pending
            // TỪ BÂY GIỜ KHÔNG TRUYỀN pricePerDay NỮA — OrderService tự đọc giá + kiểm tra “bookable”
            int orderId = orderService.bookOneBike(
                    customer.getCustomerId(), bikeId, start, end
            );

            // 6) Chuyển sang danh sách đơn
            resp.sendRedirect(req.getContextPath() + "/customerorders?justCreated=" + orderId);

        } catch (Exception e) {
            // Gắn lỗi và quay lại trang chi tiết xe
            req.getSession().setAttribute("book_error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/motorbikedetail?id=" + req.getParameter("bikeId"));
        }
    }
}
