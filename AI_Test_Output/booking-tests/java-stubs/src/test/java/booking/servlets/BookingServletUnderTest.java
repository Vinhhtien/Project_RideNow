package booking.servlets;

import booking.stubs.model.Account;
import booking.stubs.model.Customer;
import booking.stubs.model.MotorbikeListItem;
import booking.stubs.service.ICustomerService;
import booking.stubs.service.IMotorbikeService;
import booking.stubs.service.IOrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;

public class BookingServletUnderTest extends HttpServlet {

    private final IMotorbikeService bikeService;
    private final ICustomerService customerService;
    private final IOrderService orderService;

    public BookingServletUnderTest(IMotorbikeService bikeService,
                                   ICustomerService customerService,
                                   IOrderService orderService) {
        this.bikeService = bikeService;
        this.customerService = customerService;
        this.orderService = orderService;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        Account acc = (Account) session.getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        try {
            int bikeId = Integer.parseInt(req.getParameter("bikeId"));
            Date start = Date.valueOf(req.getParameter("start"));
            Date end = Date.valueOf(req.getParameter("end"));

            MotorbikeListItem item = bikeService.getDetail(bikeId);
            if (item == null) {
                resp.sendError(404, "Xe không tồn tại");
                return;
            }

            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                resp.sendRedirect(req.getContextPath() + "/customer/profile?need=1");
                return;
            }

            int orderId = orderService.bookOneBike(customer.getCustomerId(), bikeId, start, end);
            resp.sendRedirect(req.getContextPath() + "/customerorders?justCreated=" + orderId);
        } catch (Exception e) {
            req.getSession().setAttribute("book_error", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/motorbikedetail?id=" + req.getParameter("bikeId"));
        }
    }

    // Bridge for tests
    public void doPostPublic(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }
}
