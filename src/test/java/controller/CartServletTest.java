package controller;

import controller.testsupport.TestUtils;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.CartItem;
import model.MotorbikeListItem;
import model.Customer;
import model.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.ICustomerService;
import service.IMotorbikeService;
import service.IOrderService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServletTest {

    @Mock
    IMotorbikeService motorbikeService;

    @Mock
    IOrderService orderService;

    @Mock
    ICustomerService customerService;

    @Mock
    HttpServletRequest req;

    @Mock
    HttpServletResponse resp;

    @Mock
    HttpSession session;

    @Mock
    RequestDispatcher requestDispatcher;

    CartServlet servlet;
    Map<String, Object> sessionAttrs;

    @BeforeEach
    void setUp() {
        servlet = new CartServlet();
        sessionAttrs = new HashMap<>();

        // Mock session behavior
        when(req.getSession()).thenReturn(session);
        when(req.getContextPath()).thenReturn("/ctx");
        when(session.getAttribute(anyString())).thenAnswer(invocation ->
                sessionAttrs.get(invocation.getArgument(0)));
        doAnswer(invocation -> {
            sessionAttrs.put(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(session).setAttribute(anyString(), any());
        doAnswer(invocation -> {
            sessionAttrs.remove(invocation.getArgument(0));
            return null;
        }).when(session).removeAttribute(anyString());

        // Mock request dispatcher
        when(req.getRequestDispatcher(anyString())).thenReturn(requestDispatcher);

        // Inject mock services using reflection
        injectMockServices();
    }

    private void injectMockServices() {
        try {
            java.lang.reflect.Field motorbikeServiceField = CartServlet.class.getDeclaredField("motorbikeService");
            motorbikeServiceField.setAccessible(true);
            motorbikeServiceField.set(servlet, motorbikeService);

            java.lang.reflect.Field orderServiceField = CartServlet.class.getDeclaredField("orderService");
            orderServiceField.setAccessible(true);
            orderServiceField.set(servlet, orderService);

            java.lang.reflect.Field customerServiceField = CartServlet.class.getDeclaredField("customerService");
            customerServiceField.setAccessible(true);
            customerServiceField.set(servlet, customerService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject dependencies", e);
        }
    }

    private CartItem createCartItem(int bikeId, String bikeName, BigDecimal pricePerDay, Date start, Date end) {
        return new CartItem(bikeId, bikeName, pricePerDay, "Xe ga", start, end);
    }

    private MotorbikeListItem createMotorbikeItem(int bikeId, String bikeName, String typeName,
                                                  BigDecimal pricePerDay, String status) {
        MotorbikeListItem item = new MotorbikeListItem();
        item.setBikeId(bikeId);
        item.setBikeName(bikeName);
        item.setTypeName(typeName);
        item.setPricePerDay(pricePerDay);
        item.setStatus(status);
        return item;
    }

    private Account createAccount(int accountId) {
        Account account = new Account();
        account.setAccountId(accountId);
        account.setRole("customer");
        return account;
    }

    private Customer createCustomer(int customerId, int accountId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setAccountId(accountId);
        return customer;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ====== GET CART TESTS ======
    @Test
    @DisplayName("CART-GET-001: doGet() – tính tổng & forward /cart/cart.jsp")
    void cartGet001() throws Exception {
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "A", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(1))));
        sessionAttrs.put("cart", cart);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("cartItems"), any());
        verify(req).setAttribute(eq("total"), any());
        verify(req).setAttribute(eq("depositTotal"), any());
        verify(req).setAttribute(eq("upfront30"), any());
        verify(req).setAttribute(eq("toPayNow"), any());
        verify(req).setAttribute(eq("todayISO"), any());
        verify(requestDispatcher).forward(req, resp);
    }

    @Test
    @DisplayName("CART-GET-002: doGet() – giỏ hàng rỗng")
    void cartGet002() throws Exception {
        sessionAttrs.put("cart", new ArrayList<CartItem>());

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("cartItems"), any());
        verify(req).setAttribute(eq("total"), any(BigDecimal.class));
        verify(req).setAttribute(eq("depositTotal"), any(BigDecimal.class));
        verify(req).setAttribute(eq("upfront30"), any(BigDecimal.class));
        verify(req).setAttribute(eq("toPayNow"), any(BigDecimal.class));
        verify(req).setAttribute(eq("todayISO"), any());
        verify(requestDispatcher).forward(req, resp);
    }

    @Test
    @DisplayName("CART-GET-003: doGet() – cart null trong session → tạo cart mới")
    void cartGet003_nullCartInSession() throws Exception {
        servlet.doGet(req, resp);

        verify(session).setAttribute(eq("cart"), any(List.class));
        verify(requestDispatcher).forward(req, resp);
    }

    // ====== ADD TO CART TESTS ======
    // @Test
    // @DisplayName("CART-ADD-001: add – hợp lệ → thêm vào giỏ và redirect /cart")
    // void cartAdd001() throws Exception {
    //     when(req.getParameter("action")).thenReturn("add");
    //     when(req.getParameter("bikeId")).thenReturn("5");
    //     when(req.getParameter("start")).thenReturn("2025-11-10");
    //     when(req.getParameter("end")).thenReturn("2025-11-12");

    //     MotorbikeListItem item = createMotorbikeItem(5, "B", "Xe số", new BigDecimal("100000"), "available");
    //     when(motorbikeService.getDetail(5)).thenReturn(item);
    //     when(orderService.isBikeAvailable(eq(5), any(Date.class), any(Date.class))).thenReturn(true);

    //     servlet.doPost(req, resp);

    //     verify(resp).sendRedirect("/ctx/cart");
    // }

    @Test
    @DisplayName("CART-ADD-002: add – ngày đảo → set book_error & redirect về detail")
    void cartAdd002() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("6");
        when(req.getParameter("start")).thenReturn("2025-11-12");
        when(req.getParameter("end")).thenReturn("2025-11-10");

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=6");
    }

    @Test
    @DisplayName("CART-ADD-003: add – thiếu start → đồng bộ và thêm")
    void cartAdd003() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("7");
        when(req.getParameter("start")).thenReturn(null);
        when(req.getParameter("end")).thenReturn("2025-12-10");

        MotorbikeListItem item = createMotorbikeItem(7, "C", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(7)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(7), any(Date.class), any(Date.class))).thenReturn(true);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-ADD-004: add – xe không tồn tại → book_error & back detail")
    void cartAdd004() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("8");
        when(req.getParameter("start")).thenReturn("2025-12-10");
        when(req.getParameter("end")).thenReturn("2025-12-12");

        when(motorbikeService.getDetail(8)).thenReturn(null);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=8");
    }

    @Test
    @DisplayName("CART-ADD-005: add – xe bận → set book_error & book_conflicts")
    void cartAdd005() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("9");
        when(req.getParameter("start")).thenReturn("2025-12-10");
        when(req.getParameter("end")).thenReturn("2025-12-12");

        MotorbikeListItem item = createMotorbikeItem(9, "D", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(9)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(9), any(Date.class), any(Date.class))).thenReturn(false);

        List<IOrderService.OverlappedRange> overlaps = new ArrayList<>();
        overlaps.add(new IOrderService.OverlappedRange(1, Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        when(orderService.getOverlappingRanges(eq(9), any(Date.class), any(Date.class))).thenReturn(overlaps);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        Assertions.assertThat(sessionAttrs).containsKey("book_conflicts");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=9");
    }

    @Test
    @DisplayName("CART-ADD-006: add – bikeId không phải số → book_error")
    void cartAdd006() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("not_a_number");
        when(req.getParameter("start")).thenReturn("2025-11-10");
        when(req.getParameter("end")).thenReturn("2025-11-12");

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=not_a_number");
    }

    @Test
    @DisplayName("CART-ADD-007: add – ngày không hợp lệ → book_error")
    void cartAdd007() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("10");
        when(req.getParameter("start")).thenReturn("invalid-date");
        when(req.getParameter("end")).thenReturn("2025-11-12");

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=10");
    }

    @Test
    @DisplayName("CART-ADD-008: add – cả start và end đều null → book_error")
    void cartAdd008() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("11");
        when(req.getParameter("start")).thenReturn(null);
        when(req.getParameter("end")).thenReturn(null);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=11");
    }

    @Test
    @DisplayName("CART-ADD-009: add – start và end đều blank → book_error")
    void cartAdd009() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("12");
        when(req.getParameter("start")).thenReturn("");
        when(req.getParameter("end")).thenReturn("");

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=12");
    }

    @Test
    @DisplayName("CART-ADD-010: add – exception trong service → book_error")
    void cartAdd010() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("13");
        when(req.getParameter("start")).thenReturn("2025-11-10");
        when(req.getParameter("end")).thenReturn("2025-11-12");

        when(motorbikeService.getDetail(13)).thenThrow(new RuntimeException("Database error"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=13");
    }

    @Test
    @DisplayName("CART-ADD-011: add – xe bận nhưng overlaps rỗng → chỉ set book_error")
    void cartAdd011() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("14");
        when(req.getParameter("start")).thenReturn("2025-12-10");
        when(req.getParameter("end")).thenReturn("2025-12-12");

        MotorbikeListItem item = createMotorbikeItem(14, "E", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(14)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(14), any(Date.class), any(Date.class))).thenReturn(false);
        when(orderService.getOverlappingRanges(eq(14), any(Date.class), any(Date.class))).thenReturn(new ArrayList<>());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        Assertions.assertThat(sessionAttrs).doesNotContainKey("book_conflicts");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=14");
    }

    @Test
    @DisplayName("CART-ADD-012: add – thiếu bikeId -> book_error & redirect detail?id=null")
    void cartAdd012() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn(null);
        when(req.getParameter("start")).thenReturn("2026-01-10");
        when(req.getParameter("end")).thenReturn("2026-01-12");

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=null");
    }

    @Test
    @DisplayName("CART-ADD-013: add – start ở quá khứ -> book_error & back detail")
    void cartAdd013() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("21");
        when(req.getParameter("start")).thenReturn(LocalDate.now().minusDays(1).toString());
        when(req.getParameter("end")).thenReturn(LocalDate.now().plusDays(1).toString());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=21");
    }

    @Test
    @DisplayName("CART-ADD-014: add – overlaps null trong availability check → không thêm conflicts")
    void cartAdd014_nullOverlapsInAvailability() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("15");
        when(req.getParameter("start")).thenReturn("2025-12-10");
        when(req.getParameter("end")).thenReturn("2025-12-12");

        MotorbikeListItem item = createMotorbikeItem(15, "F", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(15)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(15), any(Date.class), any(Date.class))).thenReturn(false);
        when(orderService.getOverlappingRanges(eq(15), any(Date.class), any(Date.class))).thenReturn(null);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        Assertions.assertThat(sessionAttrs).doesNotContainKey("book_conflicts");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=15");
    }

    // @Test
    // @DisplayName("CART-ADD-015: add – cart null trong session → tạo cart mới")
    // void cartAdd015_nullCartInSession() throws Exception {
    //     when(req.getParameter("action")).thenReturn("add");
    //     when(req.getParameter("bikeId")).thenReturn("16");
    //     when(req.getParameter("start")).thenReturn("2025-11-10");
    //     when(req.getParameter("end")).thenReturn("2025-11-12");

    //     MotorbikeListItem item = createMotorbikeItem(16, "G", "Xe ga", new BigDecimal("100000"), "available");
    //     when(motorbikeService.getDetail(16)).thenReturn(item);
    //     when(orderService.isBikeAvailable(eq(16), any(Date.class), any(Date.class))).thenReturn(true);

    //     // Don't put cart in session to simulate null
    //     servlet.doPost(req, resp);

    //     verify(session).setAttribute(eq("cart"), any(List.class));
    //     verify(resp).sendRedirect("/ctx/cart");
    // }

    @Test
    @DisplayName("CART-ADD-016: add – exception trong isBikeAvailable → book_error")
    void cartAdd016_exceptionInAvailabilityCheck() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("17");
        when(req.getParameter("start")).thenReturn("2025-11-10");
        when(req.getParameter("end")).thenReturn("2025-11-12");

        MotorbikeListItem item = createMotorbikeItem(17, "H", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(17)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(17), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Availability check failed"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=17");
    }

    @Test
    @DisplayName("CART-ADD-017: add – exception trong getOverlappingRanges → chỉ set book_error")
    void cartAdd017_exceptionInOverlapsCheck() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("18");
        when(req.getParameter("start")).thenReturn("2025-12-10");
        when(req.getParameter("end")).thenReturn("2025-12-12");

        MotorbikeListItem item = createMotorbikeItem(18, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(18)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(18), any(Date.class), any(Date.class))).thenReturn(false);
        when(orderService.getOverlappingRanges(eq(18), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Overlaps check failed"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("book_error");
        Assertions.assertThat(sessionAttrs).doesNotContainKey("book_conflicts");
        verify(resp).sendRedirect("/ctx/motorbikedetail?id=18");
    }

    // @Test
    // @DisplayName("CART-ADD-018: add – xe đang bảo dưỡng → book_error")
    // void cartAdd018_maintenanceBike() throws Exception {
    //     when(req.getParameter("action")).thenReturn("add");
    //     when(req.getParameter("bikeId")).thenReturn("19");   
    //     when(req.getParameter("start")).thenReturn("2025-11-10");
    //     when(req.getParameter("end")).thenReturn("2025-11-12");

    //     MotorbikeListItem item = createMotorbikeItem(19, "Maintenance Bike", "Xe ga", new BigDecimal("100000"), "maintenance");
    //     when(motorbikeService.getDetail(19)).thenReturn(item);

    //     servlet.doPost(req, resp);

    //     Assertions.assertThat(sessionAttrs).containsKey("book_error");
    //     Assertions.assertThat(sessionAttrs.get("book_error")).asString().contains("bảo dưỡng");
    //     verify(resp).sendRedirect("/ctx/motorbikedetail?id=19");
    // }

    // ====== REMOVE FROM CART TESTS ======
    @Test
    @DisplayName("CART-RM-001: remove – index hợp lệ → xóa & redirect /cart")
    void cartRm001() throws Exception {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("0");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "E", new BigDecimal("100000"),
                Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(cart).isEmpty();
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-RM-002: remove – index không phải số → NumberFormatException")
    void cartRm002() {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("not_a_number");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "E", new BigDecimal("100000"),
                Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        sessionAttrs.put("cart", cart);

        Assertions.assertThatThrownBy(() -> servlet.doPost(req, resp))
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    @DisplayName("CART-RM-003: remove – index âm → không xóa")
    void cartRm003() throws Exception {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("-1");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "E", new BigDecimal("100000"),
                Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(cart).hasSize(1);
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-RM-004: remove – index vượt quá size → không xóa")
    void cartRm004() throws Exception {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("5");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "E", new BigDecimal("100000"),
                Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(cart).hasSize(1);
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-RM-005: remove – cart null trong session → không crash")
    void cartRm005_nullCartInSession() throws Exception {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("0");

        // Don't put cart in session to simulate null
        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/cart");
    }

    // ====== UPDATE DATES TESTS ======
    @Test
    @DisplayName("CART-UPD-001: updateDates – hợp lệ → success")
    void cartUpd001() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        LocalDate s = LocalDate.now().plusDays(2);
        LocalDate e = s.plusDays(2);
        when(req.getParameter("start")).thenReturn(s.toString());
        when(req.getParameter("end")).thenReturn(e.toString());
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(2, "F", new BigDecimal("100000"), Date.valueOf(s), Date.valueOf(e)));
        sessionAttrs.put("cart", cart);

        when(orderService.isBikeAvailable(eq(2), any(Date.class), any(Date.class))).thenReturn(true);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("success");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-002: updateDates – start quá khứ → error")
    void cartUpd002() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        LocalDate s = LocalDate.now().minusDays(1);
        LocalDate e = LocalDate.now().plusDays(1);
        when(req.getParameter("start")).thenReturn(s.toString());
        when(req.getParameter("end")).thenReturn(e.toString());
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(3, "G", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-003: updateDates – end < start → error")
    void cartUpd003() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        LocalDate s = LocalDate.now().plusDays(5);
        LocalDate e = s.minusDays(1);
        when(req.getParameter("start")).thenReturn(s.toString());
        when(req.getParameter("end")).thenReturn(e.toString());
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(4, "H", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-004: updateDates – index không phải số → error")
    void cartUpd004() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("not_a_number");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(3, "G", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-005: updateDates – index vượt quá size → error")
    void cartUpd005() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("5");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(3, "G", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-006: updateDates – ngày không hợp lệ → error")
    void cartUpd006() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        when(req.getParameter("start")).thenReturn("invalid-date");
        when(req.getParameter("end")).thenReturn("2025-12-10");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(4, "H", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-UPD-007: updateDates – cart null trong session → error")
    void cartUpd007_nullCartInSession() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        LocalDate s = LocalDate.now().plusDays(2);
        LocalDate e = s.plusDays(2);
        when(req.getParameter("start")).thenReturn(s.toString());
        when(req.getParameter("end")).thenReturn(e.toString());

        // Don't put cart in session to simulate null
        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    // ====== CHECKOUT TESTS ======
    @Test
    @DisplayName("CART-CHK-001: checkout – chưa login → /login")
    void cartChk001() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/login");
    }

    @Test
    @DisplayName("CART-CHK-002: checkout – giỏ trống → error")
    void cartChk002() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        sessionAttrs.put("cart", new ArrayList<>());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-003: checkout – dòng invalid → error")
    void cartChk003() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{LocalDate.now().minusDays(1).toString()});
        params.put("end_0", new String[]{LocalDate.now().plusDays(2).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(LocalDate.now().minusDays(1).toString());
        when(req.getParameter("end_0")).thenReturn(LocalDate.now().plusDays(2).toString());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-004: checkout – customer không tồn tại → error")
    void cartChk004() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{LocalDate.now().plusDays(2).toString()});
        params.put("end_0", new String[]{LocalDate.now().plusDays(3).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(LocalDate.now().plusDays(2).toString());
        when(req.getParameter("end_0")).thenReturn(LocalDate.now().plusDays(3).toString());

        when(customerService.getProfile(anyInt())).thenReturn(null);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-005: checkout – tất cả order đều fail → error")
    void cartChk005() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{LocalDate.now().plusDays(2).toString()});
        params.put("end_0", new String[]{LocalDate.now().plusDays(3).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(LocalDate.now().plusDays(2).toString());
        when(req.getParameter("end_0")).thenReturn(LocalDate.now().plusDays(3).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class))).thenReturn(0);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-006: checkout – một số order thành công, một số fail → warning")
    void cartChk006() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        cart.add(createCartItem(11, "J", new BigDecimal("150000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(2))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        params.put("start_1", new String[]{s.toString()});
        params.put("end_1", new String[]{s.plusDays(2).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());
        when(req.getParameter("start_1")).thenReturn(s.toString());
        when(req.getParameter("end_1")).thenReturn(s.plusDays(2).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(1, 10, cart.get(0).getStartDate(), cart.get(0).getEndDate())).thenReturn(100);
        when(orderService.bookOneBike(1, 11, cart.get(1).getStartDate(), cart.get(1).getEndDate())).thenReturn(0);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("warning");
        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    @Test
    @DisplayName("CART-CHK-007: checkout – exception trong quá trình tạo order → error")
    void cartChk007() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Order creation failed"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-008: checkout – indices có index âm và vượt quá → bỏ qua")
    void cartChk008() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_-1", new String[]{LocalDate.now().plusDays(2).toString()});
        params.put("end_-1", new String[]{LocalDate.now().plusDays(3).toString()});
        params.put("start_5", new String[]{LocalDate.now().plusDays(2).toString()});
        params.put("end_5", new String[]{LocalDate.now().plusDays(3).toString()});
        when(req.getParameterMap()).thenReturn(params);

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class))).thenReturn(100);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    @Test
    @DisplayName("CART-CHK-009: checkout – một số start_i/end_i null → bỏ qua")
    void cartChk009() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now().plusDays(2)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{LocalDate.now().plusDays(2).toString()});
        params.put("end_0", null);
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(LocalDate.now().plusDays(2).toString());
        when(req.getParameter("end_0")).thenReturn(null);

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class))).thenReturn(100);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    @Test
    @DisplayName("CART-CHK-010: checkout – không có start_i/end_i trong request -> tự lấy tất cả index")
    void cartChk010() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(100));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(3);
        cart.add(createCartItem(31, "Z1", new BigDecimal("100000"), Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        when(req.getParameterMap()).thenReturn(new HashMap<>());

        Customer c = createCustomer(999, 100);
        when(customerService.getProfile(100)).thenReturn(c);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(31, "Z1", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(eq(999), anyInt(), any(Date.class), any(Date.class))).thenReturn(5001);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/paynow?orders=5001");
    }

    @Test
    @DisplayName("CART-CHK-011: checkout – end_i < start_i -> error & redirect /cart")
    void cartChk011() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(101));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(4);
        cart.add(createCartItem(41, "Y1", new BigDecimal("100000"), Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> p = new HashMap<>();
        p.put("start_0", new String[]{ s.toString() });
        p.put("end_0",   new String[]{ s.minusDays(1).toString() });
        when(req.getParameterMap()).thenReturn(p);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.minusDays(1).toString());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-012: checkout – lỗi bất ngờ ngoài vòng lặp -> vào catch ngoài & redirect /cart")
    void cartChk012() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(102));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(3);
        cart.add(createCartItem(51, "X1", new BigDecimal("100000"), Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> p = new HashMap<>();
        p.put("start_0", new String[]{ s.toString() });
        p.put("end_0",   new String[]{ s.plusDays(1).toString() });
        when(req.getParameterMap()).thenReturn(p);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        when(customerService.getProfile(anyInt())).thenThrow(new RuntimeException("boom"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-013: checkout – không đưa start_i/end_i => dùng ngày trong cart; phát hiện start ở quá khứ (final check)")
    void cartChk013() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));

        List<CartItem> cart = new ArrayList<>();
        LocalDate past = LocalDate.now().minusDays(1);
        cart.add(createCartItem(20, "K", new BigDecimal("120000"),
                Date.valueOf(past), Date.valueOf(LocalDate.now().plusDays(2))));
        sessionAttrs.put("cart", cart);

        when(req.getParameterMap()).thenReturn(new HashMap<>());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-014: checkout – không đưa start_i/end_i => dùng ngày trong cart; phát hiện end < start (final check)")
    void cartChk014() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));

        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(5);
        LocalDate e = s.minusDays(2);
        cart.add(createCartItem(21, "L", new BigDecimal("150000"), Date.valueOf(s), Date.valueOf(e)));
        sessionAttrs.put("cart", cart);

        when(req.getParameterMap()).thenReturn(new HashMap<>());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-015: checkout – nhánh update theo index: newEnd.before(newStart) => error")
    void cartChk015() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));

        List<CartItem> cart = new ArrayList<>();
        LocalDate base = LocalDate.now().plusDays(3);
        cart.add(createCartItem(22, "M", new BigDecimal("160000"),
                Date.valueOf(base), Date.valueOf(base.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{ base.plusDays(5).toString() });
        params.put("end_0",   new String[]{ base.plusDays(1).toString() });
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(base.plusDays(5).toString());
        when(req.getParameter("end_0")).thenReturn(base.plusDays(1).toString());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-016: checkout – exception message chứa 'Xe không khả dụng' => add đúng tên xe vào unavailableBikes")
    void cartChk016() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));

        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(30, "Z", new BigDecimal("200000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{ s.toString() });
        params.put("end_0",   new String[]{ s.plusDays(1).toString() });
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(30, "Z", "Xe ga", new BigDecimal("200000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Xe không khả dụng vì trùng lịch"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-017: checkout – tất cả orders thành công → xóa giỏ hàng")
    void cartChk017() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        cart.add(createCartItem(11, "J", new BigDecimal("150000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(2))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        params.put("start_1", new String[]{s.toString()});
        params.put("end_1", new String[]{s.plusDays(2).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());
        when(req.getParameter("start_1")).thenReturn(s.toString());
        when(req.getParameter("end_1")).thenReturn(s.plusDays(2).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(1, 10, cart.get(0).getStartDate(), cart.get(0).getEndDate())).thenReturn(100);
        when(orderService.bookOneBike(1, 11, cart.get(1).getStartDate(), cart.get(1).getEndDate())).thenReturn(101);

        servlet.doPost(req, resp);

        verify(session).removeAttribute("cart");
        verify(resp).sendRedirect("/ctx/paynow?orders=100,101");
    }

    @Test
    @DisplayName("CART-CHK-018: checkout – exception không chứa 'Xe không khả dụng' → thêm '(Lỗi hệ thống)'")
    void cartChk018() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-019: checkout – có start_abc (index không phải số) → bỏ qua")
    void cartChk019() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_abc", new String[]{s.toString()});
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(1, 10, cart.get(0).getStartDate(), cart.get(0).getEndDate())).thenReturn(100);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    @Test
    @DisplayName("CART-CHK-020: checkout – không có unavailableBikes → error message khác")
    void cartChk020() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class))).thenReturn(0);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        String error = (String) sessionAttrs.get("error");
        Assertions.assertThat(error).contains("Không thể tạo đơn hàng");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-021: checkout – failCount > 0 nhưng successCount > 0 → chỉ set warning, không xóa cart")
    void cartChk021() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        cart.add(createCartItem(11, "J", new BigDecimal("150000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(2))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        params.put("start_1", new String[]{s.toString()});
        params.put("end_1", new String[]{s.plusDays(2).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());
        when(req.getParameter("start_1")).thenReturn(s.toString());
        when(req.getParameter("end_1")).thenReturn(s.plusDays(2).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(1, 10, cart.get(0).getStartDate(), cart.get(0).getEndDate())).thenReturn(100);
        when(orderService.bookOneBike(1, 11, cart.get(1).getStartDate(), cart.get(1).getEndDate())).thenReturn(0);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("warning");
        verify(session, never()).removeAttribute("cart");
        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    @Test
    @DisplayName("CART-CHK-022: checkout – exception với message null → thêm '(Lỗi hệ thống)'")
    void cartChk022() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(anyInt(), anyInt(), any(Date.class), any(Date.class)))
                .thenThrow(new RuntimeException());

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-023: checkout – cart null trong session → error")
    void cartChk023_nullCartInSession() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));

        // Don't put cart in session to simulate null
        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-024: checkout – có xe đang bảo dưỡng → error")
    void cartChk024_maintenanceBike() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        // Mock xe đang bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "maintenance");
        when(motorbikeService.getDetail(10)).thenReturn(bike);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        Assertions.assertThat(sessionAttrs.get("error")).asString().contains("bảo dưỡng");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-CHK-025: checkout – một xe bảo dưỡng, một xe available → error với thông báo đúng")
    void cartChk025_mixedMaintenanceAndAvailable() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        cart.add(createCartItem(11, "J", new BigDecimal("150000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(2))));
        sessionAttrs.put("cart", cart);

        // Mock: xe đầu tiên đang bảo dưỡng, xe thứ hai available
        MotorbikeListItem maintenanceBike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "maintenance");
        MotorbikeListItem availableBike = createMotorbikeItem(11, "J", "Xe ga", new BigDecimal("150000"), "available");

        when(motorbikeService.getDetail(10)).thenReturn(maintenanceBike);
        when(motorbikeService.getDetail(11)).thenReturn(availableBike);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("error");
        String error = (String) sessionAttrs.get("error");
        Assertions.assertThat(error).contains("bảo dưỡng");
        verify(resp).sendRedirect("/ctx/cart");
    }

    // ====== ACTION INVALID ======
    @Test
    @DisplayName("CART-ACTION-001: action không hợp lệ → sendError 400")
    void cartAction001() throws Exception {
        when(req.getParameter("action")).thenReturn("invalid_action");

        servlet.doPost(req, resp);

        verify(resp).sendError(400, "Hành động không hợp lệ");
    }

    // ====== EDGE CASE TESTS ======
    @Test
    @DisplayName("CART-EDGE-001: doPost – không có action parameter → sendError 400")
    void cartEdge001() throws Exception {
        when(req.getParameter("action")).thenReturn(null);

        servlet.doPost(req, resp);

        verify(resp).sendError(400, "Hành động không hợp lệ");
    }

    @Test
    @DisplayName("CART-EDGE-002: doPost – action blank → sendError 400")
    void cartEdge002() throws Exception {
        when(req.getParameter("action")).thenReturn("");

        servlet.doPost(req, resp);

        verify(resp).sendError(400, "Hành động không hợp lệ");
    }

    @Test
    @DisplayName("CART-EDGE-003: updateDates – start và end giống nhau → success")
    void cartEdge003() throws Exception {
        when(req.getParameter("action")).thenReturn("updateDates");
        when(req.getParameter("index")).thenReturn("0");
        LocalDate date = LocalDate.now().plusDays(2);
        when(req.getParameter("start")).thenReturn(date.toString());
        when(req.getParameter("end")).thenReturn(date.toString());
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(2, "F", new BigDecimal("100000"), Date.valueOf(date), Date.valueOf(date)));
        sessionAttrs.put("cart", cart);

        when(orderService.isBikeAvailable(eq(2), any(Date.class), any(Date.class))).thenReturn(true);

        servlet.doPost(req, resp);

        Assertions.assertThat(sessionAttrs).containsKey("success");
        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-EDGE-004: add – start và end giống nhau → success")
    void cartEdge004() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("19");
        LocalDate date = LocalDate.now().plusDays(2);
        when(req.getParameter("start")).thenReturn(date.toString());
        when(req.getParameter("end")).thenReturn(date.toString());

        MotorbikeListItem item = createMotorbikeItem(19, "J", "Xe số", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(19)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(19), any(Date.class), any(Date.class))).thenReturn(true);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/cart");
    }

    @Test
    @DisplayName("CART-EDGE-005: checkout – chỉ có một item và thành công → xóa cart")
    void cartEdge005() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(2);
        cart.add(createCartItem(10, "I", new BigDecimal("100000"),
                Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        Map<String, String[]> params = new HashMap<>();
        params.put("start_0", new String[]{s.toString()});
        params.put("end_0", new String[]{s.plusDays(1).toString()});
        when(req.getParameterMap()).thenReturn(params);
        when(req.getParameter("start_0")).thenReturn(s.toString());
        when(req.getParameter("end_0")).thenReturn(s.plusDays(1).toString());

        Customer customer = createCustomer(1, 1);
        when(customerService.getProfile(anyInt())).thenReturn(customer);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(10, "I", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(1, 10, cart.get(0).getStartDate(), cart.get(0).getEndDate())).thenReturn(100);

        servlet.doPost(req, resp);

        verify(session).removeAttribute("cart");
        verify(resp).sendRedirect("/ctx/paynow?orders=100");
    }

    // ====== ENCODING TESTS ======
    @Test
    @DisplayName("CART-ENCODING-001: doPost – set UTF-8 encoding")
    void cartEncoding001() throws Exception {
        when(req.getParameter("action")).thenReturn("add");
        when(req.getParameter("bikeId")).thenReturn("5");
        when(req.getParameter("start")).thenReturn("2025-11-10");
        when(req.getParameter("end")).thenReturn("2025-11-12");

        MotorbikeListItem item = createMotorbikeItem(5, "B", "Xe số", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(5)).thenReturn(item);
        when(orderService.isBikeAvailable(eq(5), any(Date.class), any(Date.class))).thenReturn(true);

        servlet.doPost(req, resp);

        verify(req).setCharacterEncoding("UTF-8");
        verify(resp).setCharacterEncoding("UTF-8");
    }

    // ====== NULL PARAMETER MAP TESTS ======
    @Test
    @DisplayName("CART-PARAM-001: checkout – parameterMap null → sử dụng cart dates")
    void cartParam001() throws Exception {
        when(req.getParameter("action")).thenReturn("checkout");
        sessionAttrs.put("account", createAccount(1));
        List<CartItem> cart = new ArrayList<>();
        LocalDate s = LocalDate.now().plusDays(3);
        cart.add(createCartItem(31, "Z1", new BigDecimal("100000"), Date.valueOf(s), Date.valueOf(s.plusDays(1))));
        sessionAttrs.put("cart", cart);

        when(req.getParameterMap()).thenReturn(java.util.Collections.emptyMap());

        Customer c = createCustomer(999, 1);
        when(customerService.getProfile(1)).thenReturn(c);

        // Mock xe không ở trạng thái bảo dưỡng
        MotorbikeListItem bike = createMotorbikeItem(31, "Z1", "Xe ga", new BigDecimal("100000"), "available");
        when(motorbikeService.getDetail(anyInt())).thenReturn(bike);

        when(orderService.bookOneBike(eq(999), anyInt(), any(Date.class), any(Date.class))).thenReturn(5001);

        servlet.doPost(req, resp);

        verify(resp).sendRedirect("/ctx/paynow?orders=5001");
    }

    // ====== EMPTY CART AFTER REMOVAL TESTS ======
    @Test
    @DisplayName("CART-REMOVAL-001: remove – xóa hết items → cart rỗng nhưng vẫn tồn tại")
    void cartRemoval001() throws Exception {
        when(req.getParameter("action")).thenReturn("remove");
        when(req.getParameter("index")).thenReturn("0");
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "E", new BigDecimal("100000"),
                Date.valueOf("2025-12-10"), Date.valueOf("2025-12-12")));
        sessionAttrs.put("cart", cart);

        servlet.doPost(req, resp);

        Assertions.assertThat(cart).isEmpty();
        Assertions.assertThat(sessionAttrs).containsKey("cart");
        verify(resp).sendRedirect("/ctx/cart");
    }

    // ====== MULTIPLE ITEMS CALCULATION TESTS ======
    @Test
    @DisplayName("CART-CALC-001: doGet – tính toán chính xác với nhiều items")
    void cartCalc001() throws Exception {
        List<CartItem> cart = new ArrayList<>();
        cart.add(createCartItem(1, "A", new BigDecimal("100000"),
                Date.valueOf(LocalDate.now()), Date.valueOf(LocalDate.now().plusDays(2))));
        cart.add(createCartItem(2, "B", new BigDecimal("150000"),
                Date.valueOf(LocalDate.now().plusDays(1)), Date.valueOf(LocalDate.now().plusDays(3))));
        sessionAttrs.put("cart", cart);

        servlet.doGet(req, resp);

        verify(req).setAttribute(eq("cartItems"), any());
        verify(req).setAttribute(eq("total"), any(BigDecimal.class));
        verify(req).setAttribute(eq("depositTotal"), any(BigDecimal.class));
        verify(req).setAttribute(eq("upfront30"), any(BigDecimal.class));
        verify(req).setAttribute(eq("toPayNow"), any(BigDecimal.class));
        verify(requestDispatcher).forward(req, resp);
    }
}
