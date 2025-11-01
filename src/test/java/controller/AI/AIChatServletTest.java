//package controller.AI;
//
//import controller.testsupport.TestUtils;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.assertj.core.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.mockito.junit.jupiter.MockitoSettings;
//import org.mockito.quality.Strictness;
//import service.AI.IAIService;
//
//import java.io.BufferedReader;
//import java.io.PrintWriter;
//import java.io.StringReader;
//import java.io.StringWriter;
//
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@MockitoSettings(strictness = Strictness.LENIENT)
//class AIChatServletTest {
//
//    AIChatServlet servlet;
//    @Mock IAIService aiService;
//    @Mock HttpServletRequest req;
//    @Mock HttpServletResponse resp;
//
//    @BeforeEach
//    void setup() {
//        servlet = new AIChatServlet();
//        TestUtils.forceSet(servlet, "aiService", aiService);
//    }
//
//    @Test
//    void post_dbMode_answersFromDatabase() throws Exception {
//        String body = "{\"question\":\"gia xe\"}"; // contains keyword -> db mode
//        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
//        StringWriter sw = new StringWriter();
//        when(resp.getWriter()).thenReturn(new PrintWriter(sw));
//
//        when(aiService.answerFromDatabase(anyString())).thenReturn("DB-ANSWER");
//        servlet.doPost(req, resp);
//        Assertions.assertThat(sw.toString()).contains("DB-ANSWER");
//    }
//
//    @Test
//    void post_emptyQuestion_returnsError() throws Exception {
//        String body = "{\"question\":\"   \"}";
//        when(req.getReader()).thenReturn(new BufferedReader(new StringReader(body)));
//        StringWriter sw = new StringWriter();
//        when(resp.getWriter()).thenReturn(new PrintWriter(sw));
//
//        servlet.doPost(req, resp);
//        Assertions.assertThat(sw.toString()).contains("error");
//    }
//}
//
