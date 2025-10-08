package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import model.Account;
import model.CartItem;
import model.Customer;
import model.MotorbikeListItem;
import service.CustomerService;
import service.ICustomerService;
import service.IOrderService;
import service.OrderService;
import service.IMotorbikeService;
import service.MotorbikeService;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "CartServlet", urlPatterns = {"/cart"})
public class CartServlet extends HttpServlet {

    private final IMotorbikeService motorbikeService = new MotorbikeService();
    private final IOrderService orderService = new OrderService();
    private final ICustomerService customerService = new CustomerService();

    @SuppressWarnings("unchecked")
    private List<CartItem> getCart(HttpSession session){
        Object o = session.getAttribute("cart");
        if (o == null) {
            List<CartItem> list = new ArrayList<>();
            session.setAttribute("cart", list);
            return list;
        }
        return (List<CartItem>) o;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<CartItem> cart = getCart(request.getSession());

        BigDecimal total = BigDecimal.ZERO;         // tổng tiền xe (không gồm cọc)
        BigDecimal depositTotal = BigDecimal.ZERO;  // tổng tiền cọc
        for (CartItem it : cart) {
            total = total.add(it.getSubtotal());
            depositTotal = depositTotal.add(it.getDeposit());
        }
        BigDecimal upfront30 = total.multiply(BigDecimal.valueOf(0.3)); // 30%
        BigDecimal toPayNow  = upfront30.add(depositTotal);             // trả ngay

        request.setAttribute("cartItems", cart);
        request.setAttribute("total", total);
        request.setAttribute("depositTotal", depositTotal);
        request.setAttribute("upfront30", upfront30);
        request.setAttribute("toPayNow", toPayNow);

        request.getRequestDispatcher("/cart/cart.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            handleAddToCart(request, response, session);
        }
        else if ("remove".equals(action)) {
            handleRemoveFromCart(request, response, session);
        }
        else if ("checkout".equals(action)) {
            handleCheckout(request, response, session);
        }
        else {
            response.sendError(400, "Hành động không hợp lệ");
        }
    }

    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            int bikeId = Integer.parseInt(request.getParameter("bikeId"));
            Date start = Date.valueOf(request.getParameter("start"));
            Date end   = Date.valueOf(request.getParameter("end"));

            if (end.before(start)) {
                session.setAttribute("book_error", "Ngày trả phải sau hoặc bằng ngày nhận.");
                response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+bikeId);
                return;
            }

            MotorbikeListItem b = motorbikeService.getDetail(bikeId);
            if (b == null) throw new IllegalArgumentException("Xe không tồn tại");

            CartItem item = new CartItem(
                    b.getBikeId(),
                    b.getBikeName(),
                    b.getPricePerDay(),
                    b.getTypeName(),
                    start, end
            );

            List<CartItem> cart = getCart(session);
            cart.add(item);

            response.sendRedirect(request.getContextPath()+"/cart");
        } catch (Exception ex) {
            session.setAttribute("book_error", "Không thể thêm vào giỏ: " + ex.getMessage());
            String backId = request.getParameter("bikeId");
            response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+backId);
        }
    }

    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        int index = Integer.parseInt(request.getParameter("index"));
        List<CartItem> cart = getCart(session);
        if (index >= 0 && index < cart.size()) cart.remove(index);
        response.sendRedirect(request.getContextPath()+"/cart");
    }

    private void handleCheckout(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            Account acc = (Account) session.getAttribute("account");
            if (acc == null) {
                session.setAttribute("error", "Vui lòng đăng nhập để thanh toán");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            List<CartItem> cart = getCart(session);
            if (cart == null || cart.isEmpty()) {
                session.setAttribute("error", "Giỏ hàng trống");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            // DEBUG: Log thông tin
            System.out.println("=== DEBUG CHECKOUT START ===");
            System.out.println("Account ID: " + acc.getAccountId());
            System.out.println("Cart items count: " + cart.size());

            // Lấy thông tin customer
            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                session.setAttribute("error", "Không tìm thấy thông tin khách hàng");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            System.out.println("Customer ID: " + customer.getCustomerId());

            // Tạo đơn hàng cho từng item trong cart
            List<Integer> createdOrderIds = new ArrayList<>();
            int successCount = 0;
            int failCount = 0;
            
            for (CartItem item : cart) {
                try {
                    System.out.println("📦 Creating order for bike: " + item.getBikeId() + 
                                     " (" + item.getBikeName() + ")" +
                                     ", dates: " + item.getStartDate() + " to " + item.getEndDate());
                    
                    int orderId = orderService.bookOneBike(
                        customer.getCustomerId(),
                        item.getBikeId(),
                        item.getStartDate(),
                        item.getEndDate()
                    );
                    
                    if (orderId > 0) {
                        createdOrderIds.add(orderId);
                        successCount++;
                        System.out.println("✅ SUCCESS - Created order #" + orderId + " for bike " + item.getBikeId());
                    } else {
                        failCount++;
                        System.err.println("❌ FAILED - Could not create order for bike " + item.getBikeId());
                    }
                } catch (Exception e) {
                    failCount++;
                    System.err.println("❌ ERROR - Bike " + item.getBikeId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("=== DEBUG CHECKOUT RESULTS ===");
            System.out.println("Successful orders: " + successCount);
            System.out.println("Failed orders: " + failCount);
            System.out.println("Total created orders: " + createdOrderIds.size());

            if (createdOrderIds.isEmpty()) {
                session.setAttribute("error", "Không thể tạo đơn hàng. Có thể xe đã được thuê trong khoảng thời gian này.");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            // Xóa cart sau khi tạo đơn thành công
            session.removeAttribute("cart");
            
            // Chuyển hướng đến trang thanh toán với danh sách order IDs
            String ordersParam = String.join(",", 
                createdOrderIds.stream()
                    .map(String::valueOf)
                    .toArray(String[]::new)
            );
            
            System.out.println("🎯 Redirecting to paynow with orders: " + ordersParam);
            System.out.println("=== DEBUG CHECKOUT END ===");
            
            response.sendRedirect(request.getContextPath() + "/paynow?orders=" + ordersParam);

        } catch (Exception e) {
            System.err.println("💥 GENERAL CHECKOUT ERROR: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}