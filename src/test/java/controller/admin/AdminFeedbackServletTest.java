package controller.admin;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.adminfeedback.AdminFeedbackItem;
import model.adminfeedback.AdminFeedbackSummary;
import model.adminfeedback.FeedbackType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import service.IAdminFeedbackService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminFeedbackServletTest {

    @Mock HttpServletRequest req;
    @Mock HttpServletResponse resp;
    @Mock IAdminFeedbackService service;

    @Test
    @DisplayName("Export details sets CSV headers and writes output")
    void export_details_headers() throws Exception {
        AdminFeedbackServlet servlet = new AdminFeedbackServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        when(req.getParameter("view")).thenReturn("details");
        when(req.getParameter("action")).thenReturn("export");
        when(service.countAll(any(), any(), any(), any())).thenReturn(0);
        when(service.findAll(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(Collections.emptyList());

        StringWriter sw = new StringWriter();
        when(resp.getWriter()).thenReturn(new PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp).setContentType(contains("text/csv"));
        verify(resp).setHeader(eq("Content-Disposition"), contains("attachment; filename*="));
    }

    @Test
    @DisplayName("Export details writes BOM and sets X-AFS header")
    void export_details_writes_bom() throws Exception {
        AdminFeedbackServlet servlet = new AdminFeedbackServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        when(req.getParameter("view")).thenReturn("details");
        when(req.getParameter("action")).thenReturn("export");
        when(service.countAll(any(), any(), any(), any())).thenReturn(0);
        when(service.findAll(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());

        java.io.StringWriter sw = new java.io.StringWriter();
        when(resp.getWriter()).thenReturn(new java.io.PrintWriter(sw));

        servlet.doGet(req, resp);

        verify(resp, atLeastOnce()).setHeader(eq("X-AFS"), anyString());
        String out = sw.toString();
        org.assertj.core.api.Assertions.assertThat(out).isNotEmpty();
        // BOM character at start
        org.assertj.core.api.Assertions.assertThat(out.charAt(0)).isEqualTo('\uFEFF');
    }

    @Test
    @DisplayName("Details view clamps star and sanitizes type")
    void details_clamp_star_and_type() throws Exception {
        AdminFeedbackServlet servlet = new AdminFeedbackServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        when(req.getParameter("view")).thenReturn("details");
        when(req.getParameter("type")).thenReturn("overview"); // should be ignored -> null
        when(req.getParameter("star")).thenReturn("0"); // should clamp to 1
        when(service.countAll(any(), any(), any(), any())).thenReturn(0);
        when(service.findAll(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/feedback.jsp");

        servlet.doGet(req, resp);

        org.mockito.ArgumentCaptor<model.adminfeedback.FeedbackType> typeCap = org.mockito.ArgumentCaptor.forClass(model.adminfeedback.FeedbackType.class);
        org.mockito.ArgumentCaptor<Integer> starCap = org.mockito.ArgumentCaptor.forClass(Integer.class);
        verify(service).countAll(any(), any(), typeCap.capture(), starCap.capture());
        org.assertj.core.api.Assertions.assertThat(typeCap.getValue()).isNull();
        org.assertj.core.api.Assertions.assertThat(starCap.getValue()).isEqualTo(1);
        verify(rd).forward(req, resp);
    }

    @Test
    @DisplayName("Resolve JSP picks admin-feedback.jsp when resource exists")
    void resolve_jsp_admin_feedback_exists() throws Exception {
        AdminFeedbackServlet servlet = org.mockito.Mockito.spy(new AdminFeedbackServlet());
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        jakarta.servlet.ServletContext ctx = org.mockito.Mockito.mock(jakarta.servlet.ServletContext.class);
        java.net.URL dummy = new java.net.URL("http://example.com");
        when(ctx.getResource("/admin/admin-feedback.jsp")).thenReturn(dummy);
        org.mockito.Mockito.doReturn(ctx).when(servlet).getServletContext();

        when(req.getParameter("view")).thenReturn("overview");
        when(service.getSummary(any(), any())).thenReturn(new AdminFeedbackSummary());

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/admin-feedback.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
    }
    @Test
    @DisplayName("Overview view forwards to resolved JSP with attributes")
    void overview_forwards_with_attrs() throws Exception {
        AdminFeedbackServlet servlet = new AdminFeedbackServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        when(req.getParameter("view")).thenReturn(null); // default overview
        when(service.getSummary(any(), any())).thenReturn(new AdminFeedbackSummary());

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/feedback.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("summary"), any());
        verify(req).setAttribute(eq("view"), eq("overview"));
    }

    @Test
    @DisplayName("Details view forwards and sets paging attrs")
    void details_forwards_with_paging() throws Exception {
        AdminFeedbackServlet servlet = new AdminFeedbackServlet();
        controller.testsupport.TestUtils.forceSet(servlet, "service", service);

        when(req.getParameter("view")).thenReturn("details");
        when(req.getParameter("page")).thenReturn("2");
        when(req.getParameter("size")).thenReturn("5");
        when(service.countAll(any(), any(), any(), any())).thenReturn(0);
        when(service.findAll(any(), any(), any(), any(), anyInt(), anyInt())).thenReturn(java.util.Collections.emptyList());

        jakarta.servlet.RequestDispatcher rd = controller.testsupport.TestUtils.stubForward(req, "/admin/feedback.jsp");

        servlet.doGet(req, resp);

        verify(rd).forward(req, resp);
        verify(req).setAttribute(eq("view"), eq("details"));
        verify(req).setAttribute(eq("page"), any());
        verify(req).setAttribute(eq("items"), any());
    }
}
