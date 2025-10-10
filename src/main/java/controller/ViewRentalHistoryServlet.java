//quy (fixed harden)
package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import model.Account;
import model.Partner;
import model.Motorbike;
import model.OrderDetail;
import model.RentalOrder;
import model.Customer;

import service.IPartnerService;
import service.IMotorbikeService;
import service.ICustomerService;
import service.IRentalOrderService;
import service.IOrderDetailService;

import service.PartnerService;
import service.MotorbikeService;
import service.CustomerService;
import service.RentalOrderService;
import service.OrderDetailService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@WebServlet(name = "ViewRentalHistoryServlet", urlPatterns = {"/rentalhistory"})
public class ViewRentalHistoryServlet extends HttpServlet {

    private final IPartnerService partnerService = new PartnerService();
    private final IMotorbikeService motorbikeService = new MotorbikeService();
    private final ICustomerService customerService = new CustomerService();
    private final IRentalOrderService rentalOrderService = new RentalOrderService();
    private final IOrderDetailService orderDetailService = new OrderDetailService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Account acc = (session != null) ? (Account) session.getAttribute("account") : null;

        if (acc == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!"partner".equalsIgnoreCase(acc.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập lịch sử thuê xe.");
            return;
        }

        try {
            Partner partner = partnerService.getByAccountId(acc.getAccountId());
            if (partner == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông tin đối tác.");
                return;
            }

            // ===== 1) Lấy danh sách xe thuộc partner (3 lớp dự phòng) =====
            List<Motorbike> motorbikes = Optional.ofNullable(
                    motorbikeService.findAllByOwnerAccount(acc.getAccountId(), acc.getRole())
            ).orElseGet(ArrayList::new);

            if (motorbikes.isEmpty()) {
                // Fallback 1: theo partnerId
                motorbikes = Optional.ofNullable(
                        motorbikeService.getByPartnerId(partner.getPartnerId())
                ).orElseGet(ArrayList::new);
            }

            if (motorbikes.isEmpty()) {
                // Fallback 2: lấy toàn bộ xe rồi lọc theo partnerId (bền vững nhất)
                List<Motorbike> allBikes = Optional.ofNullable(motorbikeService.findAll())
                        .orElseGet(ArrayList::new);
                int pid = partner.getPartnerId();
                motorbikes = allBikes.stream()
                        .filter(b -> b.getPartnerId() != null && b.getPartnerId() == pid)
                        .collect(Collectors.toList());
            }

            Set<Integer> myBikeIds = motorbikes.stream()
                    .map(Motorbike::getBikeId)
                    .collect(Collectors.toSet());

            // ===== 2) Tất cả OrderDetail, chỉ giữ chi tiết thuộc xe của partner =====
            List<OrderDetail> allDetails = Optional.ofNullable(orderDetailService.getAll())
                    .orElseGet(ArrayList::new);

            Map<Integer, List<OrderDetail>> detailsByOrder = allDetails.stream()
                    .filter(d -> myBikeIds.contains(d.getBikeId()))
                    .collect(Collectors.groupingBy(OrderDetail::getOrderId));

            Set<Integer> relevantOrderIds = detailsByOrder.keySet();

            // ===== 3) Tất cả RentalOrder, giữ các order có chi tiết thuộc partner =====
            List<RentalOrder> allOrders = Optional.ofNullable(rentalOrderService.getAll())
                    .orElseGet(ArrayList::new);

            List<RentalOrder> filteredOrders = allOrders.stream()
                    .filter(o -> relevantOrderIds.contains(o.getOrderId()))
                    .collect(Collectors.toList());

            // Mới -> cũ theo createdAt (null đặt cuối)
            filteredOrders.sort(Comparator.comparing(
                    RentalOrder::getCreatedAt,
                    Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed());

            // ===== 4) Map khách hàng =====
            List<Customer> customers = Optional.ofNullable(customerService.getAll())
                    .orElseGet(ArrayList::new);

            Map<Integer, String> customerNames = new HashMap<>();
            for (Customer c : customers) customerNames.put(c.getCustomerId(), c.getFullName());

            // ===== 5) Chuẩn bị dữ liệu hiển thị =====
            List<Map<String, Object>> displayOrders = new ArrayList<>();
            for (RentalOrder order : filteredOrders) {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getOrderId());
                map.put("customerName", customerNames.getOrDefault(order.getCustomerId(), "Khách #" + order.getCustomerId()));
                map.put("startDate", order.getStartDate());
                map.put("endDate", order.getEndDate());
                map.put("totalPrice", order.getTotalPrice());
                map.put("status", order.getStatus());
                map.put("createdAt", order.getCreatedAt());
                map.put("depositAmount", order.getDepositAmount());
                map.put("depositStatus", order.getDepositStatus());
                map.put("paymentSubmitted", order.isPaymentSubmitted());
                map.put("returnStatus", order.getReturnStatus());
                map.put("returnedAt", order.getReturnedAt());

                List<OrderDetail> ods = detailsByOrder.getOrDefault(order.getOrderId(), Collections.emptyList());
                map.put("details", ods);
                map.put("bikeCount", ods.size());

                String bikeSummary = ods.stream()
                        .map(d -> "#" + d.getBikeId())
                        .distinct()
                        .collect(Collectors.joining(", "));
                map.put("bikeSummary", bikeSummary.isEmpty() ? "—" : bikeSummary);

                displayOrders.add(map);
            }

            // ===== 6) Gán attribute (alias cho JSP) =====
            request.setAttribute("partner", partner);
            request.setAttribute("displayOrders", displayOrders);
            request.setAttribute("rentalList", displayOrders);
            request.setAttribute("histories", displayOrders);
            request.setAttribute("orders", displayOrders);

            // orderId để highlight (?orderId=)
            request.setAttribute("orderIdParam", request.getParameter("orderId"));

            // ===== 7) Chọn view =====
            String view = "/partners/rentalhistory.jsp";
            if (getServletContext().getResource(view) == null) view = "/partners/rentalhistorylist.jsp";
            request.getRequestDispatcher(view).forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage());
        }
    }
}
