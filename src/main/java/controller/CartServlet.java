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
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal depositTotal = BigDecimal.ZERO;
        for (CartItem it : cart) {
            total = total.add(it.getSubtotal());
            depositTotal = depositTotal.add(it.getDeposit());
        }
        BigDecimal upfront30 = total.multiply(BigDecimal.valueOf(0.3));
        BigDecimal toPayNow  = upfront30.add(depositTotal);

        request.setAttribute("todayISO", LocalDate.now().toString());
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
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("add".equals(action)) {
            handleAddToCart(request, response, session);
        } else if ("remove".equals(action)) {
            handleRemoveFromCart(request, response, session);
        } else if ("updateDates".equals(action)) { // SAVE 1 dòng
            handleUpdateDates(request, response, session);
        } else if ("checkout".equals(action)) {
            handleCheckout(request, response, session);
        } else {
            response.sendError(400, "Hành động không hợp lệ");
        }
    }

//    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
//            throws IOException {
//        try {
//            int bikeId = Integer.parseInt(request.getParameter("bikeId"));
//            Date start = Date.valueOf(request.getParameter("start"));
//            Date end   = Date.valueOf(request.getParameter("end"));
//            if (end.before(start)) {
//                session.setAttribute("book_error", "Ngày trả phải sau hoặc bằng ngày nhận.");
//                response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+bikeId);
//                return;
//            }
//            if (start.toLocalDate().isBefore(LocalDate.now())) {
//                session.setAttribute("book_error", "Ngày nhận không được ở quá khứ.");
//                response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+bikeId);
//                return;
//            }
//
//            MotorbikeListItem b = motorbikeService.getDetail(bikeId);
//            if (b == null) throw new IllegalArgumentException("Xe không tồn tại");
//            
//            if (!orderService.isBikeAvailable(bikeId, start, end)) {
//                session.setAttribute("book_error",
//                    "Xe đã được xác nhận cho khoảng ngày này. Vui lòng chọn ngày khác.");
//                response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+bikeId);
//                return;
//            }
//            
//            
//            CartItem item = new CartItem(
//                    b.getBikeId(), b.getBikeName(), b.getPricePerDay(), b.getTypeName(), start, end
//            );
//            getCart(session).add(item);
//            response.sendRedirect(request.getContextPath()+"/cart");
//        } catch (Exception ex) {
//            session.setAttribute("book_error", "Không thể thêm vào giỏ: " + ex.getMessage());
//            String backId = request.getParameter("bikeId");
//            response.sendRedirect(request.getContextPath()+"/motorbikedetail?id="+backId);
//        }
//    }

    private void handleAddToCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
        throws IOException {
    int bikeId = -1;
    try {
        // ===== 1) Lấy & chuẩn hoá tham số =====
        String bikeIdStr = request.getParameter("bikeId");
        String startStr  = request.getParameter("start");
        String endStr    = request.getParameter("end");

        if (bikeIdStr != null) bikeIdStr = bikeIdStr.trim();
        if (startStr  != null) startStr  = startStr.trim();
        if (endStr    != null) endStr    = endStr.trim();

        System.out.println("[ADD] raw params => bikeId=" + bikeIdStr + ", start=" + startStr + ", end=" + endStr);

        if (bikeIdStr == null || bikeIdStr.isBlank()) {
            throw new IllegalArgumentException("Thiếu mã xe.");
        }
        bikeId = Integer.parseInt(bikeIdStr);

        // Cho phép chọn 1 ngày: nếu thiếu start hay end thì dùng giá trị còn lại
        if ((startStr == null || startStr.isBlank()) && (endStr == null || endStr.isBlank())) {
            throw new IllegalArgumentException("Vui lòng chọn ngày nhận (và/hoặc ngày trả).");
        }
        if (startStr == null || startStr.isBlank()) startStr = endStr;
        if (endStr   == null || endStr.isBlank())   endStr   = startStr;

        // Parse yyyy-MM-dd an toàn
        java.sql.Date start, end;
        try {
            start = java.sql.Date.valueOf(startStr); // yêu cầu định dạng yyyy-MM-dd
            end   = java.sql.Date.valueOf(endStr);
        } catch (IllegalArgumentException badFmt) {
            throw new IllegalArgumentException("Định dạng ngày không hợp lệ. Vui lòng chọn lại (yyyy-MM-dd).");
        }

        // ===== 2) Validate ngày =====
        java.time.LocalDate today = java.time.LocalDate.now();
        if (end.before(start)) {
            session.setAttribute("book_error", "Ngày trả phải sau hoặc bằng ngày nhận.");
            response.sendRedirect(request.getContextPath() + "/motorbikedetail?id=" + bikeId);
            return;
        }
        if (start.toLocalDate().isBefore(today)) {
            session.setAttribute("book_error", "Ngày nhận không được ở quá khứ.");
            response.sendRedirect(request.getContextPath() + "/motorbikedetail?id=" + bikeId);
            return;
        }

        // ===== 3) Kiểm tra xe & lịch =====
        MotorbikeListItem b = motorbikeService.getDetail(bikeId);
        if (b == null) throw new IllegalArgumentException("Xe không tồn tại.");

        if (!orderService.isBikeAvailable(bikeId, start, end)) {
            // Lấy các khoảng bị trùng để hiển thị
            java.util.List<service.IOrderService.OverlappedRange> overlaps =
                    orderService.getOverlappingRanges(bikeId, start, end);

            java.util.List<String> conflictStrings = new java.util.ArrayList<>();
            java.time.format.DateTimeFormatter DF = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
            if (overlaps != null && !overlaps.isEmpty()) {
                for (service.IOrderService.OverlappedRange o : overlaps) {
                    String s = o.start.toLocalDate().format(DF);
                    String e = o.end.toLocalDate().format(DF);
                    conflictStrings.add("#" + o.orderId + ": " + s + " → " + e);
                }
            }

            session.setAttribute("book_error",
                    "Xe đã được xác nhận cho một khoảng ngày đè lên khung bạn chọn. Vui lòng chọn thời gian khác.");
            if (!conflictStrings.isEmpty()) {
                session.setAttribute("book_conflicts", conflictStrings); // detail.jsp sẽ render danh sách này
            }
            response.sendRedirect(request.getContextPath() + "/motorbikedetail?id=" + bikeId);
            return;
        }

        // ===== 4) Add vào giỏ =====
        CartItem item = new CartItem(
                b.getBikeId(), b.getBikeName(), b.getPricePerDay(), b.getTypeName(), start, end
        );
        getCart(session).add(item);
        response.sendRedirect(request.getContextPath() + "/cart");

    } catch (IllegalArgumentException iae) {
        session.setAttribute("book_error", iae.getMessage());
        response.sendRedirect(request.getContextPath() + "/motorbikedetail?id=" +
                (bikeId == -1 ? request.getParameter("bikeId") : bikeId));
    } catch (Exception ex) {
        ex.printStackTrace();
        session.setAttribute("book_error", "Không thể thêm vào giỏ: " + ex.getMessage());
        String backId = request.getParameter("bikeId");
        response.sendRedirect(request.getContextPath() + "/motorbikedetail?id=" + backId);
    }
}


    
    private void handleRemoveFromCart(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        int index = Integer.parseInt(request.getParameter("index"));
        List<CartItem> cart = getCart(session);
        if (index >= 0 && index < cart.size()) cart.remove(index);
        response.sendRedirect(request.getContextPath()+"/cart");
    }

    // SAVE ngày thuê 1 dòng (cập nhật session)
    private void handleUpdateDates(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException {
        try {
            int index = Integer.parseInt(request.getParameter("index"));
            List<CartItem> cart = getCart(session);
            if (index < 0 || index >= cart.size()) {
                session.setAttribute("error", "Mục giỏ hàng không hợp lệ.");
                response.sendRedirect(request.getContextPath()+"/cart");
                return;
            }
            Date start = Date.valueOf(request.getParameter("start"));
            Date end   = Date.valueOf(request.getParameter("end"));

            LocalDate today = LocalDate.now();
            if (start.toLocalDate().isBefore(today)) {
                session.setAttribute("error", "Ngày nhận không được ở quá khứ.");
                response.sendRedirect(request.getContextPath()+"/cart");
                return;
            }
            if (end.before(start)) {
                session.setAttribute("error", "Ngày trả phải sau hoặc bằng ngày nhận.");
                response.sendRedirect(request.getContextPath()+"/cart");
                return;
            }

            CartItem item = cart.get(index);
            item.setStartDate(start);
            item.setEndDate(end);

            session.setAttribute("success", "Đã lưu ngày thuê cho \"" + item.getBikeName() + "\".");
            response.sendRedirect(request.getContextPath()+"/cart");
        } catch (Exception e) {
            session.setAttribute("error", "Không thể cập nhật ngày thuê: " + e.getMessage());
            response.sendRedirect(request.getContextPath()+"/cart");
        }
    }

    // CHECKOUT: đọc trực tiếp start_i/end_i từ input date (form="checkoutForm") → không cần bấm Save
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

            // Lấy đúng danh sách index có trong request (start_0, start_3, ...)
            Map<String, String[]> params = request.getParameterMap();
            List<Integer> indices = new ArrayList<>();
            for (String k : params.keySet()) {
                if (k.startsWith("start_")) {
                    try { indices.add(Integer.parseInt(k.substring("start_".length()))); }
                    catch (NumberFormatException ignore) {}
                }
            }
            if (indices.isEmpty()) {
                for (int i = 0; i < cart.size(); i++) indices.add(i);
            }
            indices.sort(Integer::compareTo);

            // Cập nhật ngày từ request vào cart (không cần Save)
            LocalDate today = LocalDate.now();
            for (Integer i : indices) {
                if (i < 0 || i >= cart.size()) continue;
                String s = request.getParameter("start_" + i);
                String e = request.getParameter("end_" + i);
                if (s == null || e == null) continue;

                Date newStart = Date.valueOf(s);
                Date newEnd   = Date.valueOf(e);

                if (newEnd.before(newStart)) {
                    session.setAttribute("error", "Dòng #" + (i + 1) + ": Ngày trả phải sau hoặc bằng ngày nhận.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                if (newStart.toLocalDate().isBefore(today)) {
                    session.setAttribute("error", "Dòng #" + (i + 1) + ": Ngày nhận không được ở quá khứ.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }

                cart.get(i).setStartDate(newStart);
                cart.get(i).setEndDate(newEnd);
            }

            // Kiểm tra cuối
            for (CartItem it : cart) {
                if (it.getStartDate().toLocalDate().isBefore(LocalDate.now())) {
                    session.setAttribute("error", "Một hoặc nhiều xe có ngày nhận ở quá khứ. Vui lòng cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
                if (it.getEndDate().before(it.getStartDate())) {
                    session.setAttribute("error", "Một hoặc nhiều xe có ngày trả trước ngày nhận. Vui lòng cập nhật.");
                    response.sendRedirect(request.getContextPath() + "/cart");
                    return;
                }
            }

            // Create orders
            Customer customer = customerService.getProfile(acc.getAccountId());
            if (customer == null) {
                session.setAttribute("error", "Không tìm thấy thông tin khách hàng");
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }

            List<Integer> createdOrderIds = new ArrayList<>();
            List<String> unavailableBikes = new ArrayList<>();
            int successCount = 0, failCount = 0;

            for (CartItem item : cart) {
                try {
                    int orderId = orderService.bookOneBike(
                            customer.getCustomerId(),
                            item.getBikeId(),
                            item.getStartDate(),
                            item.getEndDate()
                    );
                    if (orderId > 0) {
                        createdOrderIds.add(orderId);
                        successCount++;
                    } else {
                        failCount++;
                        unavailableBikes.add(item.getBikeName() + " (Không thể tạo đơn hàng)");
                    }
                } catch (Exception e) {
                    failCount++;
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("Xe không khả dụng")) {
                        unavailableBikes.add(item.getBikeName());
                    } else {
                        unavailableBikes.add(item.getBikeName() + " (Lỗi hệ thống)");
                    }
                }
            }

//            if (createdOrderIds.isEmpty()) {
//                String msg;
//                if (!unavailableBikes.isEmpty()) {
//                    msg = "Các xe sau không khả dụng: " +
//                            unavailableBikes.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "))
//                            + ". Vui lòng chọn thời gian khác.";
//                } else msg = "Không thể tạo đơn hàng. Vui lòng thử lại sau.";
//                session.setAttribute("error", msg);
//                response.sendRedirect(request.getContextPath() + "/cart");
//                return;
//            }

            if (createdOrderIds.isEmpty()) {
                String msg = "Các xe sau không khả dụng: "
                        + unavailableBikes.stream()
                        .map(s -> "'" + s + "'")
                        .collect(Collectors.joining(", "))
                        + ". Vui lòng chọn thời gian khác.";
                session.setAttribute("error", msg);
                response.sendRedirect(request.getContextPath() + "/cart");
                return;
            }



            if (failCount > 0) {
                String warn = "Đã tạo thành công " + successCount + " đơn hàng. "
                        + "Một số xe không khả dụng: "
                        + unavailableBikes.stream().map(s -> "'" + s + "'").collect(Collectors.joining(", "))
                        + ".";
                session.setAttribute("warning", warn);
            }

            if (failCount == 0) session.removeAttribute("cart");

            String ordersParam = createdOrderIds.stream().map(String::valueOf).collect(Collectors.joining(","));
            response.sendRedirect(request.getContextPath() + "/paynow?orders=" + ordersParam);

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("error", "Hệ thống đang gặp sự cố. Vui lòng thử lại sau.");
            response.sendRedirect(request.getContextPath() + "/cart");
        }
    }
}
