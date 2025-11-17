//// src/main/java/controller/AI/AIChatServlet.java
//package controller.AI;
//
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//
//import java.io.*;
//
//import com.google.gson.*;
//import service.AI.AIService;
//import service.AI.IAIService;
//
//@WebServlet(name = "AIChatServlet", urlPatterns = {"/ai/chat"})
//public class AIChatServlet extends HttpServlet {
//
//    private final IAIService aiService = new AIService();
//    private final Gson gson = new Gson();
//
//    private String inferMode(String q) {
//        if (q == null) return "smalltalk";
//        String t = q.toLowerCase();
//
//        // Mở rộng từ khóa để nhận diện câu hỏi về database
//        String[] dbHints = {
//                "giá", "dưới", "trên", "từ", "đến", "ở", "tại", "đà nẵng", "hà nội", "hồ chí minh",
//                "còn xe", "tồn kho", "loại", "status", "type", "đặt", "ngày", "số lượng",
//                "where", "select", "between", "mẫu", "xe số", "xe ga", "pkl", "phân khối lớn",
//                "danh sách", "liệt kê", "có những", "nào", "gì", "bao nhiêu", "tìm", "kiếm",
//                "wave", "future", "sirius", "jupiter", "exciter", "winner", "vision", "air blade",
//                "vario", "sh", "lead", "ninja", "cbr", "gsx", "r15", "r3", "r1", "honda", "yamaha", "suzuki",
//                "biển số", "giấy tờ", "thuê", "cho thuê", "cửa hàng", "đối tác", "admin",
//                "xe ", "motor", "motorbike", "rent", "rental"
//        };
//        for (String k : dbHints) {
//            if (t.contains(k)) return "db";
//        }
//        return "smalltalk";
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//            throws ServletException, IOException {
//
//        // Set encoding ngay từ đầu
//        req.setCharacterEncoding("UTF-8");
//        resp.setCharacterEncoding("UTF-8");
//        resp.setContentType("application/json; charset=UTF-8");
//
//        JsonObject jsonResp = new JsonObject();
//        String question = null;
//
//        try (BufferedReader reader = req.getReader()) {
//            StringBuilder body = new StringBuilder();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                body.append(line);
//            }
//
//            if (body.length() > 0) {
//                JsonObject json = gson.fromJson(body.toString(), JsonObject.class);
//                if (json != null && json.has("question")) {
//                    question = json.get("question").getAsString().trim();
//                }
//            }
//        } catch (Exception ex) {
//            jsonResp.addProperty("error", "Không đọc được yêu cầu: " + ex.getMessage());
//            resp.getWriter().write(gson.toJson(jsonResp));
//            return;
//        }
//
//        if (question == null || question.isBlank()) {
//            jsonResp.addProperty("error", "⚠️ Câu hỏi không được để trống.");
//            resp.getWriter().write(gson.toJson(jsonResp));
//            return;
//        }
//
//        // Fix encoding issues trước khi xử lý
//        String fixedQuestion = fixEncoding(question);
//        System.out.println("[AIChatServlet] Question: " + fixedQuestion);
//
//        String mode = inferMode(fixedQuestion);
//        System.out.println("[AIChatServlet] Inferred mode=" + mode);
//
//        try {
//            String answer;
//            if ("db".equalsIgnoreCase(mode)) {
//                answer = aiService.answerFromDatabase(fixedQuestion);
//            } else {
//                answer = aiService.smallTalk(fixedQuestion);
//            }
//
//            if (answer == null || answer.isBlank()) {
//                answer = "⚠️ Không có phản hồi, hãy thử lại sau.";
//            }
//
//            jsonResp.addProperty("answer", answer);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            String errorMsg = "❌ Lỗi hệ thống: " + e.getMessage();
//            jsonResp.addProperty("error", errorMsg);
//        }
//
//        resp.getWriter().write(gson.toJson(jsonResp));
//    }
//
//    /**
//     * Fix encoding issues từ ISO-8859-1 sang UTF-8
//     */
//    private String fixEncoding(String text) {
//        if (text == null) return null;
//
//        try {
//            if (text.matches(".*[�].*") || containsEncodingIssues(text)) {
//                System.out.println("[AIChatServlet] Detected encoding issues in: " + text);
//                byte[] bytes = text.getBytes("ISO-8859-1");
//                String fixed = new String(bytes, "UTF-8");
//                System.out.println("[AIChatServlet] After encoding fix: " + fixed);
//                return fixed;
//            }
//        } catch (Exception e) {
//            System.out.println("[AIChatServlet] Encoding fix failed: " + e.getMessage());
//        }
//
//        return text;
//    }
//
//    private boolean containsEncodingIssues(String text) {
//        String[] issuePatterns = {
//                "Nh?ng", "m?u", "xe s?", "c?a", "b?n", "l�", "g�"
//        };
//
//        for (String pattern : issuePatterns) {
//            if (text.contains(pattern)) {
//                return true;
//            }
//        }
//        return false;
//    }
//}


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

        // 1. Câu mang tính chủ quan → smalltalk
        String[] subjectiveHints = {
                "ngon nhat", "ngon nhất",
                "dep nhat", "đẹp nhất",
                "xin nhat", "xịn nhất",
                "phu hop", "phù hợp",
                "hợp với", "hop voi"
        };
        for (String k : subjectiveHints) {
            if (t.contains(k)) {
                return "smalltalk";
            }
        }

        // 2. Từ khóa gợi ý truy vấn dữ liệu
        String[] dbHints = {
                "giá", "gia",
                "dưới", "duoi",
                "trên", "tren",
                "từ", "tu ",
                "đến", "den ",
                "rẻ nhất", "re nhat",
                "đắt nhất", "dat nhat",
                "cao nhất", "cao nhat",
                "best price",

                "ở ", "tai ",
                "đà nẵng", "da nang",
                "hà nội", "ha noi",
                "hồ chí minh", "ho chi minh",
                "còn xe", "con xe",
                "tồn kho", "ton kho",
                "available", "status", "type",
                "ngày", "ngay", "số lượng", "so luong",

                "where", "select", "between",
                "mẫu", "mau ",
                "xe số", "xe so",
                "xe ga",
                "pkl", "phân khối lớn", "phan khoi lon",
                "danh sách", "danh sach",
                "liệt kê", "liet ke",
                "có những", "co nhung",
                "nào", "nao ",
                "gì", "gi ",
                "bao nhiêu", "bao nhieu",
                "tìm", "tim ",
                "kiếm", "kiem ",

                "wave", "future", "sirius", "jupiter", "exciter", "winner",
                "vision", "air blade", "airblade",
                "vario", " sh ", " lead ",
                "ninja", "cbr", "gsx", "r15", "r3", "r1",
                "honda", "yamaha", "suzuki",

                "biển số", "bien so",
                "giấy tờ", "giay to",
                "thuê", "thue",
                "cho thuê", "cho thue",
                "cửa hàng", "cua hang",
                "đối tác", "doi tac",
                "admin",
                "motor", "motorbike",
                "rent", "rental"
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
        System.out.println("[AIChatServlet] Question: " + fixedQuestion);

        String mode = inferMode(fixedQuestion);
        System.out.println("[AIChatServlet] Inferred mode=" + mode);

        try {
            String answer;
            if ("db".equalsIgnoreCase(mode)) {
                answer = aiService.answerFromDatabase(fixedQuestion);
            } else {
                answer = aiService.smallTalk(fixedQuestion);
            }

            if (answer == null || answer.isBlank()) {
                answer = "⚠️ Không có phản hồi, hãy thử lại sau.";
            }

            // Thay __CTX__ bằng context path thực tế
            answer = answer.replace("__CTX__", req.getContextPath());

            jsonResp.addProperty("answer", answer);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "❌ Lỗi hệ thống: " + e.getMessage();
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
            if (text.matches(".*[�].*") || containsEncodingIssues(text)) {
                System.out.println("[AIChatServlet] Detected encoding issues in: " + text);
                byte[] bytes = text.getBytes("ISO-8859-1");
                String fixed = new String(bytes, "UTF-8");
                System.out.println("[AIChatServlet] After encoding fix: " + fixed);
                return fixed;
            }
        } catch (Exception e) {
            System.out.println("[AIChatServlet] Encoding fix failed: " + e.getMessage());
        }

        return text;
    }

    private boolean containsEncodingIssues(String text) {
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
}
