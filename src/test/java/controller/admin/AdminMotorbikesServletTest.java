//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import jakarta.servlet.http.HttpSession;
//import jakarta.servlet.http.Part;
//import model.Motorbike;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IMotorbikeAdminService;
//import service.IOrderService;
//
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.*;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminMotorbikesServletTest {
//
//    @BeforeAll
//    static void initEnv() {
//        Locale.setDefault(Locale.US);
//        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
//    }
//
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    @Test
//    void ADMIN-MOTORBIKES-001_get_list_happy_forwards() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        IMotorbikeAdminService svc = mock(IMotorbikeAdminService.class);
//        Motorbike mb = new Motorbike(); mb.setBikeId(1); mb.setBikeName("B1");
//        when(svc.getAllMotorbikes()).thenReturn(List.of(mb));
//
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
//
//        servlet.doGet(req, resp);
//
//        verify(req).setAttribute(eq("motorbikes"), any());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-MOTORBIKES-002_post_create_happy_redirects_success() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("ABC-123");
//        when(req.getParameter("pricePerDay")).thenReturn("100.00");
//        when(req.getParameter("typeId")).thenReturn("2");
//        when(req.getParameter("ownerType")).thenReturn("admin");
//
//        // Optional mocked parts (won't be used since bikeId remains 0 => no FS)
//        Part part = TestUtils.mockPart("images", "a.jpg", "image/jpeg", new byte[]{1,2,3});
//        when(req.getParts()).thenReturn(Collections.singleton(part));
//
//        IMotorbikeAdminService svc = mock(IMotorbikeAdminService.class);
//        when(svc.addMotorbike(any())).thenAnswer(inv -> true); // bikeId stays default 0 to avoid FS
//
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
//
//        servlet.doPost(req, resp);
//
//        verify(svc).addMotorbike(any());
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=created");
//    }
//
//    @Test
//    void ADMIN-MOTORBIKES-003_post_create_duplicate_plate_forwards_error() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("DUP-1");
//        when(req.getParameter("pricePerDay")).thenReturn("100.00");
//        when(req.getParameter("typeId")).thenReturn("2");
//
//        // licensePlateExists -> true via getAllMotorbikes
//        IMotorbikeAdminService svc = mock(IMotorbikeAdminService.class);
//        Motorbike exists = new Motorbike(); exists.setLicensePlate("DUP-1");
//        when(svc.getAllMotorbikes()).thenReturn(List.of(exists));
//
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), any());
//        verify(rd).forward(req, resp);
//        verify(resp, never()).sendRedirect(anyString());
//    }
//
//    @Test
//    void ADMIN-MOTORBIKES-004_post_update_happy_redirects_success() throws Exception {
//        when(req.getContextPath()).thenReturn("/ctx");
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("5");
//        when(req.getParameter("bikeName")).thenReturn("Renamed");
//        when(req.getParameter("licensePlate")).thenReturn("PLATE");
//        when(req.getParameter("pricePerDay")).thenReturn("150.00");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("3");
//
//        IMotorbikeAdminService svc = mock(IMotorbikeAdminService.class);
//        Motorbike existing = new Motorbike(); existing.setBikeId(5); existing.setLicensePlate("OLD");
//        when(svc.getMotorbikeById(5)).thenReturn(existing);
//        when(svc.updateMotorbike(any())).thenReturn(true);
//
//        IOrderService orderSvc = mock(IOrderService.class);
//
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
//        TestUtils.forceSet(servlet, "orderService", orderSvc);
//
//        servlet.doPost(req, resp);
//
//        verify(svc).updateMotorbike(any(Motorbike.class));
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=updated");
//    }
//
//    @Test
//    void ADMIN-MOTORBIKES-005_post_update_rented_dates_missing_forwards_error() throws Exception {
//        RequestDispatcher rd = TestUtils.mockDispatcher(req);
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("6");
//        when(req.getParameter("bikeName")).thenReturn("B");
//        when(req.getParameter("licensePlate")).thenReturn("X");
//        when(req.getParameter("pricePerDay")).thenReturn("50.00");
//        when(req.getParameter("status")).thenReturn("rented");
//        when(req.getParameter("description")).thenReturn("d");
//        when(req.getParameter("typeId")).thenReturn("1");
//
//        IMotorbikeAdminService svc = mock(IMotorbikeAdminService.class);
//        when(svc.getMotorbikeById(6)).thenReturn(new Motorbike());
//
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", svc);
//        TestUtils.forceSet(servlet, "orderService", mock(IOrderService.class));
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), any());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    void ADMIN-MOTORBIKES-006_get_delete_happy_redirects_success() throws Exception {
//        when(req.getParameter("action")).thenReturn("delete");
//        when(req.getParameter("id")).thenReturn("9");
//
//        // delete handler instantiates service and hits DB; no real DB allowed
//        // We don't need to assert DB parameters here; just ensure redirect branch
//        // by letting service report success via a simplified path: hasOrderHistory -> false, delete -> true.
//
//        try (var db = org.mockito.Mockito.mockStatic(utils.DBConnection.class)) {
//            java.sql.Connection con = mock(java.sql.Connection.class);
//            java.sql.PreparedStatement ps = mock(java.sql.PreparedStatement.class);
//            java.sql.ResultSet rs = mock(java.sql.ResultSet.class);
//            db.when(utils.DBConnection::getConnection).thenReturn(con);
//            when(con.prepareStatement(anyString())).thenReturn(ps);
//
//            // hasOrderHistory -> COUNT(*) = 0
//            when(ps.executeQuery()).thenReturn(rs);
//            when(rs.next()).thenReturn(true);
//            when(rs.getInt(1)).thenReturn(0);
//
//            // delete -> executeUpdate > 0
//            when(ps.executeUpdate()).thenReturn(1);
//
//            new AdminMotorbikesServlet().doGet(req, resp);
//            verify(resp).sendRedirect("bikes?success=deleted");
//        }
//    }
//}
//
