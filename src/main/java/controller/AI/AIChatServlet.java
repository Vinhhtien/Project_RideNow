// src/main/java/controller/AI/AIChatServlet.java
package controller.AI;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.*;
import com.google.gson.*;
import service.AI.AIService;
import service.AI.IAIService;

@WebServlet(name = "AIChatServlet", urlPatterns = {"/ai/chat"})
public class AIChatServlet extends HttpServlet {

    private final IAIService aiService = new AIService();
    private final Gson gson = new Gson();

    private String inferMode(String q) {
        if (q == null) return "smalltalk";
        String t = q.toLowerCase();
        
        // Mở rộng từ khóa để nhận diện câu hỏi về database
        String[] dbHints = {
            "giá", "dưới", "trên", "từ", "đến", "ở", "tại", "đà nẵng", "hà nội", "hồ chí minh",
            "còn xe", "tồn kho", "loại", "status", "type", "đặt", "ngày", "số lượng",
            "where", "select", "between", "mẫu", "xe số", "xe ga", "pkl", "phân khối lớn",
            "danh sách", "liệt kê", "có những", "nào", "gì", "bao nhiêu", "tìm", "kiếm",
            "wave", "future", "sirius", "jupiter", "exciter", "winner", "vision", "air blade",
            "vario", "sh", "lead", "ninja", "cbr", "gsx", "r15", "r3", "r1", "honda", "yamaha", "suzuki",
            "biển số", "giấy tờ", "thuê", "cho thuê", "cửa hàng", "đối tác", "admin"
        };
        for (String k : dbHints) {
            if (t.contains(k)) return "db";
        }
        return "smalltalk";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // Set encoding ngay từ đầu
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        JsonObject jsonResp = new JsonObject();
        String question = null;
        String mode = null;
        boolean debug = "1".equals(req.getParameter("debug"));

        try (BufferedReader reader = req.getReader()) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            
            if (body.length() > 0) {
                JsonObject json = gson.fromJson(body.toString(), JsonObject.class);
                if (json != null && json.has("question")) {
                    question = json.get("question").getAsString().trim();
                }
            }
            
            mode = req.getParameter("mode");
        } catch (Exception ex) {
            jsonResp.addProperty("error", "Không đọc được yêu cầu: " + ex.getMessage());
            resp.getWriter().write(gson.toJson(jsonResp));
            return;
        }

        if (question == null || question.isBlank()) {
            jsonResp.addProperty("error", "⚠️ Câu hỏi không được để trống.");
            resp.getWriter().write(gson.toJson(jsonResp));
            return;
        }

        // Fix encoding issues trước khi xử lý
        String fixedQuestion = fixEncoding(question);
        System.out.println("AIChatServlet: Original question: " + question);
        System.out.println("AIChatServlet: Fixed question: " + fixedQuestion);

        if (mode == null || mode.isBlank()) {
            mode = inferMode(fixedQuestion);
        }

        System.out.println("AIChatServlet: mode=" + mode + ", q=" + fixedQuestion);

        try {
            String answer;
            if ("db".equalsIgnoreCase(mode)) {
                answer = aiService.answerFromDatabase(fixedQuestion);
            } else {
                answer = aiService.smallTalk(fixedQuestion);
            }
            
            // Đảm bảo answer không null
            if (answer == null || answer.isBlank()) {
                answer = "⚠️ Không có phản hồi, hãy thử lại sau.";
            }
            
            jsonResp.addProperty("answer", answer);
            
        } catch (Exception e) {
            // In stack trace để debug
            e.printStackTrace();
            String errorMsg = "❌ Lỗi hệ thống: " + e.getMessage();
            // Nếu là debug mode, thêm thông tin chi tiết
            if (debug) {
                errorMsg += "\nStack trace: " + getStackTrace(e);
            }
            jsonResp.addProperty("error", errorMsg);
        }

        resp.getWriter().write(gson.toJson(jsonResp));
    }

    /**
     * Fix encoding issues từ ISO-8859-1 sang UTF-8
     */
    private String fixEncoding(String text) {
        if (text == null) return null;
        
        try {
            // Kiểm tra xem có phải là text bị lỗi encoding không
            if (text.matches(".*[�].*") || containsEncodingIssues(text)) {
                System.out.println("DEBUG: Detected encoding issues in: " + text);
                
                // Thử fix common encoding issues
                byte[] bytes = text.getBytes("ISO-8859-1");
                String fixed = new String(bytes, "UTF-8");
                System.out.println("DEBUG: After encoding fix: " + fixed);
                return fixed;
            }
        } catch (Exception e) {
            System.out.println("DEBUG: Encoding fix failed: " + e.getMessage());
        }
        
        return text;
    }

    /**
     * Kiểm tra xem text có bị lỗi encoding không
     */
    private boolean containsEncodingIssues(String text) {
        // Các pattern thường gặp khi lỗi encoding Vietnamese
        String[] issuePatterns = {
            "Nh?ng", "m?u", "xe s?", "c?a", "b?n", "l�", "g�"
        };
        
        for (String pattern : issuePatterns) {
            if (text.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Lấy stack trace dưới dạng string
     */
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
}