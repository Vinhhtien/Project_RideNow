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
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import utils.DBConnection;

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
        response.setCharacterEncoding("UTF-8"); // THÊM DÒNG NÀY
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
        List<String> unavailableBikes = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (CartItem item : cart) {
            try {
                // DEBUG: Kiểm tra availability trước khi tạo order
                debugBikeAvailability(item.getBikeId(), item.getStartDate());

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
                    unavailableBikes.add(item.getBikeName() + " (Không thể tạo đơn hàng)");
                }
            } catch (Exception e) {
                 failCount++;
                System.err.println("❌ ERROR - Bike " + item.getBikeId() + ": " + e.getMessage());

                String errorMessage = e.getMessage();
                if (errorMessage.contains("Xe không khả dụng")) {
                    // SỬA: Chỉ thêm tên xe, không thêm thông báo lỗi từ service
                    unavailableBikes.add(item.getBikeName());
                } else {
                    unavailableBikes.add(item.getBikeName() + " (Lỗi hệ thống)");
                }
                e.printStackTrace();
            }
        }

        System.out.println("=== DEBUG CHECKOUT RESULTS ===");
        System.out.println("Successful orders: " + successCount);
        System.out.println("Failed orders: " + failCount);
        System.out.println("Total created orders: " + createdOrderIds.size());

        // SỬA: Xử lý thông báo chi tiết và chuyên nghiệp hơn
        if (createdOrderIds.isEmpty()) {
            StringBuilder errorMsg = new StringBuilder();
            if (!unavailableBikes.isEmpty()) {
                if (unavailableBikes.size() == 1) {
                    // SỬA: Thông báo đơn giản, rõ ràng
                    errorMsg.append("Xe '").append(unavailableBikes.get(0))
                           .append("' không khả dụng trong khoảng thời gian đã chọn. ")
                           .append("Vui lòng chọn thời gian khác hoặc xe khác.");
                } else {
                    errorMsg.append("Các xe sau không khả dụng trong khoảng thời gian đã chọn: ");
                    for (int i = 0; i < unavailableBikes.size(); i++) {
                        if (i > 0) errorMsg.append(i == unavailableBikes.size() - 1 ? " và " : ", ");
                        errorMsg.append("'").append(unavailableBikes.get(i)).append("'");
                    }
                    errorMsg.append(". Vui lòng chọn khoảng thời gian khác hoặc xe khác.");
                }
            } else {
                errorMsg.append("Không thể tạo đơn hàng. Vui lòng thử lại sau.");
            }
            session.setAttribute("error", errorMsg.toString());
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        // SỬA: Thông báo warning chuyên nghiệp hơn
        if (failCount > 0) {
            StringBuilder warningMsg = new StringBuilder();
            warningMsg.append("Đã tạo thành công ").append(successCount).append(" đơn hàng. ");
            
            if (!unavailableBikes.isEmpty()) {
                warningMsg.append("Tuy nhiên, ");
                if (unavailableBikes.size() == 1) {
                    warningMsg.append("xe '").append(unavailableBikes.get(0)).append("' không khả dụng.");
                } else {
                    warningMsg.append(unavailableBikes.size()).append(" xe không khả dụng: ");
                    for (int i = 0; i < unavailableBikes.size(); i++) {
                        if (i > 0) warningMsg.append(i == unavailableBikes.size() - 1 ? " và " : ", ");
                        warningMsg.append("'").append(unavailableBikes.get(i)).append("'");
                    }
                }
                warningMsg.append(" Vui lòng chọn khoảng thời gian khác cho các xe này.");
            }
            
            session.setAttribute("warning", warningMsg.toString());
        }

        // Xóa cart sau khi tạo đơn thành công
        if (failCount == 0) {
            session.removeAttribute("cart");
        }
        
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
        
        // SỬA: Thông báo lỗi tổng quát chuyên nghiệp hơn
        String userFriendlyError = "Hệ thống đang gặp sự cố. Vui lòng thử lại sau hoặc liên hệ hỗ trợ nếu sự cố tiếp diễn.";
        session.setAttribute("error", userFriendlyError);
        response.sendRedirect(request.getContextPath() + "/cart");
    }
}
    private String extractMainMessage(String fullMessage) {
    if (fullMessage.contains("Xe không khả dụng")) {
        // Trích xuất phần chính của thông báo (bỏ qua chi tiết các đơn hàng nếu có)
        int detailIndex = fullMessage.indexOf("Xe đang được thuê");
        if (detailIndex > 0) {
            return fullMessage.substring(0, detailIndex).trim();
        }
    }
    return fullMessage;
}
    
    
    // DEBUG: Method kiểm tra availability
    private void debugBikeAvailability(int bikeId, Date date) {
        try {
            String sql = """
                SELECT 
                    ro.order_id, ro.status, ro.pickup_status, ro.return_status,
                    ro.start_date, ro.end_date, b.bike_name, c.full_name
                FROM RentalOrders ro
                JOIN OrderDetails od ON ro.order_id = od.order_id  
                JOIN Motorbikes b ON od.bike_id = b.bike_id
                JOIN Customers c ON ro.customer_id = c.customer_id
                WHERE od.bike_id = ?
                    AND ? BETWEEN ro.start_date AND ro.end_date
                    AND ro.status = 'confirmed'
                    AND ro.return_status IN ('not_returned', 'none')
                """;
            
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, bikeId);
                ps.setDate(2, date);
                
                ResultSet rs = ps.executeQuery();
                System.out.println("🔍 DEBUG BIKE AVAILABILITY FOR BIKE " + bikeId + " ON " + date + ":");
                boolean hasOrders = false;
                while (rs.next()) {
                    hasOrders = true;
                    System.out.println("   🚫 CONFLICT - Order #" + rs.getInt("order_id") + 
                                     ", Customer: " + rs.getString("full_name") +
                                     ", Status: " + rs.getString("status") +
                                     ", Pickup: " + rs.getString("pickup_status") +
                                     ", Return: " + rs.getString("return_status") +
                                     ", Dates: " + rs.getDate("start_date") + " to " + rs.getDate("end_date"));
                }
                if (!hasOrders) {
                    System.out.println("   ✅ No conflicting orders found - BIKE IS AVAILABLE");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in debugBikeAvailability: " + e.getMessage());
        }
    }
}


