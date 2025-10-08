// src/main/java/controller/CheckoutServlet.java
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.CartItem;
import model.Customer;
import service.CustomerService;
import service.ICustomerService;
import service.IOrderService;
import service.OrderService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet {

    private final ICustomerService customerService = new CustomerService();
    private final IOrderService     orderService   = new OrderService();

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session) {
        Object o = session.getAttribute("cart");
        return (o instanceof List) ? (List<CartItem>) o : new ArrayList<>();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // 1) Yêu cầu đăng nhập
        Account acc = (Account) req.getSession().getAttribute("account");
        if (acc == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 2) Yêu cầu có hồ sơ Customer
        try {
            Customer c = customerService.getProfile(acc.getAccountId());
            if (c == null) {
                // ép người dùng hoàn thiện hồ sơ trước khi đặt
                req.getSession().setAttribute("book_error", "Vui lòng hoàn thiện hồ sơ trước khi thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/customer/profile");
                return;
            }

            // 3) Lấy giỏ hàng
            HttpSession session = req.getSession();
            List<CartItem> cart = getCart(session);
            if (cart.isEmpty()) {
                session.setAttribute("book_error", "Giỏ hàng đang trống.");
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }

            // 4) Lần lượt tạo các order pending
            List<Integer> created = new ArrayList<>();
            for (CartItem it : cart) {
                try {
                    // Sử dụng hàm 4 tham số (Service tự kiểm tra giá & tình trạng xe)
                    int orderId = orderService.bookOneBike(
                            c.getCustomerId(),
                            it.getBikeId(),
                            it.getStartDate(),
                            it.getEndDate()
                    );
                    created.add(orderId);
                } catch (Exception ex) {
                    // Nếu 1 item lỗi → dừng lại, báo lỗi và quay lại giỏ
                    session.setAttribute("book_error",
                            "Không thể tạo đơn cho xe \"" + it.getBikeName() + "\": " + ex.getMessage());
                    resp.sendRedirect(req.getContextPath() + "/cart");
                    return;
                }
            }

            // 5) Thành công hết → xoá giỏ & chuyển sang Pay Now
            session.removeAttribute("cart");
            String idList = created.stream().map(String::valueOf).collect(Collectors.joining(","));
            resp.sendRedirect(req.getContextPath() + "/paynow?orders=" + idList);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    // Nếu muốn chặt chẽ hơn, bạn có thể bắt buộc dùng POST:
    // form ở cart.jsp submit method="post" vào /checkout và bạn override doPost gọi doGet.
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }
}
