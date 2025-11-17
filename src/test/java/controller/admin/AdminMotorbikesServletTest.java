//package controller.admin;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.RequestDispatcher;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import model.Motorbike;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.IMotorbikeAdminService;
//import service.IOrderService;
//
//import java.util.Collections;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AdminMotorbikesServletTest {
//
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//    @Mock IMotorbikeAdminService motorbikeService;
//    @Mock IOrderService orderService;
//
//    @Test
//    @DisplayName("GET default lists motorbikes and forwards")
//    void get_lists_forwards() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn(null);
//        when(motorbikeService.getAllMotorbikes()).thenReturn(Collections.<Motorbike>emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbikes-management.jsp");
//
//        servlet.doGet(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("motorbikes"), any());
//    }
//
//    @Test
//    @DisplayName("GET action=filter filters by ownerType=partner")
//    void get_filter_owner_partner() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        model.Motorbike a = new model.Motorbike();
//        a.setBikeId(1); a.setPartnerId(100); a.setStatus("available");
//        model.Motorbike b = new model.Motorbike();
//        b.setBikeId(2); b.setPartnerId(null); b.setStatus("available");
//
//        when(req.getParameter("action")).thenReturn("filter");
//        when(req.getParameter("ownerType")).thenReturn("partner");
//        when(req.getParameter("status")).thenReturn("all");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.List.of(a, b));
//
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbikes-management.jsp");
//
//        servlet.doGet(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("motorbikes"), argThat(list -> ((java.util.List<?>) list).size() == 1));
//    }
//
//    @Test
//    @DisplayName("GET action=filter filters by status=available")
//    void get_filter_status_available() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        model.Motorbike a = new model.Motorbike(); a.setStatus("available");
//        model.Motorbike b = new model.Motorbike(); b.setStatus("rented");
//
//        when(req.getParameter("action")).thenReturn("filter");
//        when(req.getParameter("ownerType")).thenReturn("all");
//        when(req.getParameter("status")).thenReturn("available");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.List.of(a, b));
//
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbikes-management.jsp");
//
//        servlet.doGet(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("motorbikes"), argThat(list -> ((java.util.List<?>) list).size() == 1));
//    }
//
//    @Test
//    @DisplayName("GET action=new forwards to form and loads lists")
//    void get_new_forwards_form() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//        when(req.getParameter("action")).thenReturn("new");
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doGet(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("bikeTypes"), any());
//        verify(req).setAttribute(eq("partners"), any());
//    }
//
//    @Test
//    @DisplayName("GET action=edit 404 when motorbike not found")
//    void get_edit_404_when_missing() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//        when(req.getParameter("action")).thenReturn("edit");
//        when(req.getParameter("id")).thenReturn("1");
//        when(motorbikeService.getMotorbikeById(1)).thenReturn(null);
//
//        servlet.doGet(req, resp);
//
//        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND);
//    }
//
//    @Test
//    @DisplayName("GET action=edit forwards when motorbike exists")
//    void get_edit_forwards_when_exists() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//        when(req.getParameter("action")).thenReturn("edit");
//        when(req.getParameter("id")).thenReturn("11");
//        model.Motorbike mb = new model.Motorbike(); mb.setBikeName("N");
//        when(motorbikeService.getMotorbikeById(11)).thenReturn(mb);
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doGet(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("motorbike"), any());
//        verify(req).setAttribute(eq("bikeTypes"), any());
//        verify(req).setAttribute(eq("partners"), any());
//    }
//
//    @Test
//    @DisplayName("POST create with ownerType=partner missing partnerId -> forward form with error")
//    void post_create_partner_missing_partnerId_forwards_error() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("ABC-123");
//        when(req.getParameter("pricePerDay")).thenReturn("100");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("ownerType")).thenReturn("partner");
//        // partnerId intentionally missing
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), anyString());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST create ownerType=admin success -> redirect created")
//    void post_create_admin_success_redirect() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("XYZ-999");
//        when(req.getParameter("pricePerDay")).thenReturn("100");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("ownerType")).thenReturn("admin");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.addMotorbike(any())).thenReturn(true);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=created");
//    }
//
//    @Test
//    @DisplayName("POST create duplicate license plate -> form error forward")
//    void post_create_duplicate_plate_forwards_error() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("DUP");
//        when(req.getParameter("pricePerDay")).thenReturn("100");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("ownerType")).thenReturn("admin");
//        model.Motorbike other = new model.Motorbike(); other.setLicensePlate("DUP");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.List.of(other));
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), contains("Bi"));
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST create service failed -> redirect error=create_failed")
//    void post_create_service_failed_redirect() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("create");
//        when(req.getParameter("bikeName")).thenReturn("Bike X");
//        when(req.getParameter("licensePlate")).thenReturn("XYZ-999");
//        when(req.getParameter("pricePerDay")).thenReturn("100");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("ownerType")).thenReturn("admin");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.addMotorbike(any())).thenReturn(false);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?error=create_failed");
//    }
//
//    @Test
//    @DisplayName("POST update available success -> calls image update safely (typeId=1)")
//    void post_update_available_success_type1() throws Exception {
//        AdminMotorbikesServlet servlet = spy(new AdminMotorbikesServlet());
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        when(motorbikeService.updateMotorbike(any())).thenReturn(true);
//        // Safe FS + no parts
//        jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
//        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("mb-test");
//        when(ctx.getRealPath("/")).thenReturn(tmp.toString());
//        doReturn(ctx).when(servlet).getServletContext();
//        when(req.getParts()).thenReturn(java.util.Collections.emptyList());
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=updated");
//    }
//
//    @Test
//    @DisplayName("POST update available success -> typeId=2 path")
//    void post_update_available_success_type2() throws Exception {
//        AdminMotorbikesServlet servlet = spy(new AdminMotorbikesServlet());
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("10");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("2");
//
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(10)).thenReturn(existing);
//        when(motorbikeService.updateMotorbike(any())).thenReturn(true);
//        jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
//        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("mb-test2");
//        when(ctx.getRealPath("/")).thenReturn(tmp.toString());
//        doReturn(ctx).when(servlet).getServletContext();
//        when(req.getParts()).thenReturn(java.util.Collections.emptyList());
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=updated");
//    }
//
//    @Test
//    @DisplayName("POST update available success -> typeId=3 path")
//    void post_update_available_success_type3() throws Exception {
//        AdminMotorbikesServlet servlet = spy(new AdminMotorbikesServlet());
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("12");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("3");
//
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(12)).thenReturn(existing);
//        when(motorbikeService.updateMotorbike(any())).thenReturn(true);
//        jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
//        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("mb-test3");
//        when(ctx.getRealPath("/")).thenReturn(tmp.toString());
//        doReturn(ctx).when(servlet).getServletContext();
//        when(req.getParts()).thenReturn(java.util.Collections.emptyList());
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=updated");
//    }
//
//    @Test
//    @DisplayName("POST update available success -> default typeId path")
//    void post_update_available_success_default_type() throws Exception {
//        AdminMotorbikesServlet servlet = spy(new AdminMotorbikesServlet());
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("13");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("99");
//
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(13)).thenReturn(existing);
//        when(motorbikeService.updateMotorbike(any())).thenReturn(true);
//        jakarta.servlet.ServletContext ctx = mock(jakarta.servlet.ServletContext.class);
//        java.nio.file.Path tmp = java.nio.file.Files.createTempDirectory("mb-test4");
//        when(ctx.getRealPath("/")).thenReturn(tmp.toString());
//        doReturn(ctx).when(servlet).getServletContext();
//        when(req.getParts()).thenReturn(java.util.Collections.emptyList());
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?success=updated");
//    }
//
//    @Test
//    @DisplayName("POST unknown action falls back to list forward")
//    void post_unknown_action_lists() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//        when(req.getParameter("action")).thenReturn("unknown");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbikes-management.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(rd).forward(req, resp);
//        verify(req).setAttribute(eq("motorbikes"), any());
//    }
//
//    @Test
//    @DisplayName("POST update rented missing dates -> form error forward")
//    void post_update_rented_missing_dates() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        // request params
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("rented");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        // missing rental dates
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(new model.Motorbike());
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), anyString());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST update rented invalid date order -> form error forward")
//    void post_update_rented_invalid_dates() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("rented");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("rentalStartDate")).thenReturn("2025-11-06");
//        when(req.getParameter("rentalEndDate")).thenReturn("2025-11-01");
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), anyString());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST update rented not available -> form error forward")
//    void post_update_rented_not_available() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("rented");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("rentalStartDate")).thenReturn("2025-11-01");
//        when(req.getParameter("rentalEndDate")).thenReturn("2025-11-06");
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        when(orderService.isBikeAvailableForAdmin(eq(9), any(), any())).thenReturn(false);
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), anyString());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST update rented availability SQLException -> form error forward")
//    void post_update_rented_sql_exception() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("rented");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(req.getParameter("rentalStartDate")).thenReturn("2025-11-01");
//        when(req.getParameter("rentalEndDate")).thenReturn("2025-11-06");
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        when(orderService.isBikeAvailableForAdmin(eq(9), any(), any())).thenThrow(new java.sql.SQLException("err"));
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), anyString());
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST update duplicate license plate -> form error forward")
//    void post_update_duplicate_plate() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("DUP");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        model.Motorbike other = new model.Motorbike(); other.setLicensePlate("DUP");
//        when(motorbikeService.getAllMotorbikes()).thenReturn(java.util.List.of(other));
//        when(motorbikeService.getAllBikeTypes()).thenReturn(java.util.Collections.emptyList());
//        when(motorbikeService.getAllPartners()).thenReturn(java.util.Collections.emptyList());
//        RequestDispatcher rd = TestUtils.stubForward(req, "/admin/admin-motorbike-form.jsp");
//
//        servlet.doPost(req, resp);
//
//        verify(req).setAttribute(eq("formError"), contains("Bi"));
//        verify(rd).forward(req, resp);
//    }
//
//    @Test
//    @DisplayName("POST update not found -> redirect error")
//    void post_update_not_found_redirect() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(null);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?error=not_found");
//    }
//
//    @Test
//    @DisplayName("POST update service failed -> redirect error")
//    void post_update_service_failed_redirect() throws Exception {
//        AdminMotorbikesServlet servlet = new AdminMotorbikesServlet();
//        TestUtils.forceSet(servlet, "motorbikeAdminService", motorbikeService);
//        TestUtils.forceSet(servlet, "orderService", orderService);
//
//        when(req.getParameter("action")).thenReturn("update");
//        when(req.getParameter("bikeId")).thenReturn("9");
//        when(req.getParameter("bikeName")).thenReturn("Name");
//        when(req.getParameter("licensePlate")).thenReturn("ABC");
//        when(req.getParameter("pricePerDay")).thenReturn("10");
//        when(req.getParameter("status")).thenReturn("available");
//        when(req.getParameter("description")).thenReturn("desc");
//        when(req.getParameter("typeId")).thenReturn("1");
//        model.Motorbike existing = new model.Motorbike(); existing.setLicensePlate("OLD");
//        when(motorbikeService.getMotorbikeById(9)).thenReturn(existing);
//        when(motorbikeService.updateMotorbike(any())).thenReturn(false);
//        when(req.getContextPath()).thenReturn("/ctx");
//
//        servlet.doPost(req, resp);
//
//        verify(resp).sendRedirect("/ctx/admin/bikes?error=update_failed");
//    }
//}
