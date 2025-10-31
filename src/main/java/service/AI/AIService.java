package service.AI;

import dao.AI.AIDao;
import utils.AI.GeminiClient;
import utils.AI.GeminiToolClient;
import utils.AI.GeminiToolClient.ToolCall;

import java.math.BigDecimal;
import java.util.*;

public class AIService implements IAIService {

    private final GeminiClient chatClient = new GeminiClient();
    private final GeminiToolClient toolClient = new GeminiToolClient();
    private final AIDao dao = new AIDao();

    private static final String DETAIL_URL = "__CTX__/motorbikedetail?id={id}";

    // Cache schema 30 phút
    private static String cachedSchema = null;
    private static long cachedAt = 0;
    private static final long SCHEMA_TTL_MS = 30 * 60 * 1000L;

    @Override
    public String smallTalk(String question) {
        if (question == null || question.isBlank()) return "❌ Bạn chưa nhập câu hỏi nào.";
        
        // Fix encoding trước khi xử lý
        String fixedQuestion = fixEncoding(question);
        System.out.println("DEBUG: smallTalk - Original: " + question);
        System.out.println("DEBUG: smallTalk - Fixed: " + fixedQuestion);
        
        return chatClient.ask(fixedQuestion);
    }

    @Override
    public String answerFromDatabase(String question) {
        try {
            // Fix encoding trước khi xử lý
            String fixedQuestion = fixEncoding(question);
            System.out.println("DEBUG: answerFromDatabase - Original: " + question);
            System.out.println("DEBUG: answerFromDatabase - Fixed: " + fixedQuestion);
            
            // 1) Ưu tiên intent nhanh theo loại xe → trả top 5 + link chi tiết
            String typeIntent = detectTypeIntent(fixedQuestion);
            System.out.println("DEBUG: Detected intent: " + typeIntent);
            
            if (typeIntent != null) {
                List<Map<String, Object>> rows = dao.topBikesByType(typeIntent, 5);
                System.out.println("DEBUG: Found " + (rows == null ? 0 : rows.size()) + " bikes for type: " + typeIntent);
                
                if (rows == null || rows.isEmpty()) {
                    return "⚠️ Hiện chưa có xe thuộc loại " + typeIntent + " có sẵn trong danh sách.";
                }
                return renderTopListWithLinks(typeIntent, rows);
            }

            // 2) Nhận diện intent phức tạp (giá, trạng thái, v.v.)
            String complexIntent = detectComplexIntent(fixedQuestion);
            if (complexIntent != null) {
                return handleComplexIntent(complexIntent, fixedQuestion);
            }

            // 3) Tạo schema và policy an toàn
            String schemaDoc = getSchemaDoc();
            String policyDoc = """
                    - Chỉ sinh truy vấn SELECT có ? placeholders.
                    - Không dùng DELETE/UPDATE/INSERT/EXEC/DDL.
                    - Nếu có điều kiện, luôn dùng WHERE và JOIN chuẩn.
                    - Không truy cập sysobjects/sp_/INFORMATION_SCHEMA trực tiếp.
                    """;

            // 4) Nhờ Gemini sinh SQL an toàn
            ToolCall t1 = toolClient.turn1_buildSql(fixedQuestion, schemaDoc, policyDoc);
            if (!t1.isToolCall()) {
                // Không sinh được tool-call → rẽ sang smalltalk
                return chatClient.ask(fixedQuestion);
            }

            // 5) Query DB (read-only)
            List<Map<String, Object>> rows = dao.select(t1.getSql(), t1.getParams());

            // 6) Không có dữ liệu
            if (rows == null || rows.isEmpty()) {
                return "⚠️ Không tìm thấy dữ liệu phù hợp trong hệ thống.";
            }

            // 7) Nhờ Gemini diễn giải kết quả (chỉ câu trả lời — KHÔNG hiển thị SQL)
            String explain = toolClient.turn2_explainFromRows(fixedQuestion, rows);

            // 8) Trả về câu trả lời gọn, không lộ SQL/params
            return "✅ " + explain;

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Đã xảy ra lỗi khi xử lý yêu cầu: " + e.getMessage();
        }
    }

    @Override
    public Map<String, Object> debugDatabaseAnswer(String question) {
        Map<String, Object> debug = new LinkedHashMap<>();
        String fixedQuestion = fixEncoding(question);
        String schemaDoc = getSchemaDoc();
        String policyDoc = "- Chỉ SELECT, có ? placeholders.";
        ToolCall t1 = toolClient.turn1_buildSql(fixedQuestion, schemaDoc, policyDoc);
        debug.put("toolCall", t1);

        if (t1.isToolCall()) {
            List<Map<String, Object>> rows = dao.select(t1.getSql(), t1.getParams());
            debug.put("rows", rows);
            debug.put("explain", toolClient.turn2_explainFromRows(fixedQuestion, rows));
        } else {
            debug.put("error", t1.getText());
        }
        return debug;
    }

    // ========= Helpers =========

    /** Fix encoding issues từ ISO-8859-1 sang UTF-8 */
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

    /** Kiểm tra xem text có bị lỗi encoding không */
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

    /** Nhận diện intent loại xe */
    private String detectTypeIntent(String q) {
        if (q == null) return null;
        String normalized = removeAccents(q.toLowerCase().trim());

        // Xe số
        if (normalized.contains("xe so") || normalized.contains("xesố") || normalized.contains("xeso") 
                || normalized.contains("so") || normalized.contains("số")
                || normalized.contains("wave") || normalized.contains("future") || normalized.contains("sirius") 
                || normalized.contains("jupiter") || normalized.contains("exciter") || normalized.contains("winner")
                || normalized.contains("blade") || normalized.contains("alpha")) {
            return "Xe số";
        }
        // Xe ga
        if (normalized.contains("xe ga") || normalized.contains("xega") || normalized.contains("tay ga")
                || normalized.contains("vision") || normalized.contains("air blade") || normalized.contains("airblade") 
                || normalized.contains("vario") || normalized.contains("sh") || normalized.contains("lead")) {
            return "Xe ga";
        }
        // PKL
        if (normalized.contains("pkl") || normalized.contains("phan khoi lon") || normalized.contains("pk l")
                || normalized.contains("ninja") || normalized.contains("cbr") || normalized.contains("gsx") 
                || normalized.contains("r15") || normalized.contains("r3") || normalized.contains("r1")
                || normalized.contains("phan khoi") || normalized.contains("xe phan khoi")) {
            return "Phân khối lớn";
        }
        
        return null;
    }

    /** Nhận diện intent phức tạp (giá, trạng thái, v.v.) */
    private String detectComplexIntent(String q) {
        if (q == null) return null;
        String normalized = removeAccents(q.toLowerCase().trim());
        
        // Tìm theo giá
        if (normalized.matches(".*(gia|giá).*(duoi|dưới|thap|rẻ).*")) {
            return extractPriceRange(q, "max");
        }
        if (normalized.matches(".*(gia|giá).*(tu|từ|tren|trên).*")) {
            return extractPriceRange(q, "min"); 
        }
        
        // Tìm theo trạng thái
        if (normalized.contains("co san") || normalized.contains("có sẵn") || normalized.contains("available")) {
            return "available";
        }
        
        return null;
    }

    private String extractPriceRange(String q, String type) {
        // Trích xuất số từ câu hỏi "xe dưới 150k"
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(q);
        if (matcher.find()) {
            String price = matcher.group();
            // Nếu số nhỏ hơn 1000, coi như là nghìn VND (150 -> 150000)
            if (Integer.parseInt(price) < 1000) {
                price = String.valueOf(Integer.parseInt(price) * 1000);
            }
            return type + "_" + price;
        }
        return type + "_unknown";
    }

    private String handleComplexIntent(String intent, String question) {
        Map<String, String> conditions = new HashMap<>();
        
        if (intent.startsWith("max_")) {
            String price = intent.substring(4);
            if (!"unknown".equals(price)) {
                conditions.put("max_price", price);
            }
            conditions.put("status", "available");
            
            List<Map<String, Object>> rows = dao.searchBikes(conditions);
            if (rows.isEmpty()) {
                return "⚠️ Không tìm thấy xe nào dưới " + (Integer.parseInt(price)/1000) + "k. Hãy thử mức giá cao hơn.";
            }
            return renderSearchResults("xe giá dưới " + (Integer.parseInt(price)/1000) + "k", rows);
        }
        
        if ("available".equals(intent)) {
            conditions.put("status", "available");
            List<Map<String, Object>> rows = dao.searchBikes(conditions);
            return renderSearchResults("xe có sẵn", rows);
        }
        
        return null;
    }

    /** Hàm loại bỏ dấu tiếng Việt */
    private String removeAccents(String s) {
        if (s == null) return null;
        try {
            String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
            return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception e) {
            return s;
        }
    }

    /** Render danh sách top N theo loại kèm link chi tiết (HTML) */
    private String renderTopListWithLinks(String typeName, List<Map<String, Object>> rows) {
        StringBuilder out = new StringBuilder();
        out.append("✅ Tìm thấy ").append(rows.size())
                .append(" mẫu <b>").append(escape(typeName)).append("</b> có sẵn:<br/>");
        out.append("<ul style=\"margin:6px 0 0 16px; padding:0;\">");

        for (Map<String, Object> r : rows) {
            int id = ((Number) r.get("bike_id")).intValue();
            String name = String.valueOf(r.get("bike_name"));
            BigDecimal price = new BigDecimal(String.valueOf(r.get("price_per_day")));
            String description = String.valueOf(r.get("description"));
            String licensePlate = String.valueOf(r.get("license_plate"));
            String status = String.valueOf(r.get("status"));

            String href = DETAIL_URL.replace("{id}", String.valueOf(id));
            out.append("<li style=\"margin:8px 0; padding:8px; background:rgba(255,255,255,0.05); border-radius:8px;\">")
                    .append("<a href=\"").append(href).append("\" style=\"font-weight:600; color:#3b82f6; text-decoration:none;\" target=\"_blank\">")
                    .append(escape(name)).append("</a>")
                    .append("<br/><span style=\"color:#94a3b8; font-size:14px;\">")
                    .append(formatVnd(price)).append("/ngày")
                    .append(" • Biển số: ").append(escape(licensePlate))
                    .append(" • Trạng thái: <span style=\"color:").append(getStatusColor(status)).append("\">").append(escape(status)).append("</span>")
                    .append("</span>");
            
            if (description != null && !description.equals("null") && !description.trim().isEmpty()) {
                out.append("<br/><span style=\"color:#cbd5e1; font-size:13px;\">")
                   .append(escape(description))
                   .append("</span>");
            }
            out.append("</li>");
        }
        out.append("</ul>");
        out.append("<br/><small style=\"color:#64748b;\">🔗 Click vào tên xe để xem chi tiết và đặt thuê (mở tab mới)</small>");
        return out.toString();
    }

    /** Render kết quả tìm kiếm */
    private String renderSearchResults(String searchType, List<Map<String, Object>> rows) {
        StringBuilder out = new StringBuilder();
        out.append("✅ Tìm thấy ").append(rows.size())
                .append(" mẫu <b>").append(escape(searchType)).append("</b>:<br/>");
        out.append("<ul style=\"margin:6px 0 0 16px; padding:0;\">");

        for (Map<String, Object> r : rows) {
            int id = ((Number) r.get("bike_id")).intValue();
            String name = String.valueOf(r.get("bike_name"));
            BigDecimal price = new BigDecimal(String.valueOf(r.get("price_per_day")));
            String typeName = String.valueOf(r.get("type_name"));
            String licensePlate = String.valueOf(r.get("license_plate"));
            String status = String.valueOf(r.get("status"));

            String href = DETAIL_URL.replace("{id}", String.valueOf(id));
            out.append("<li style=\"margin:8px 0; padding:8px; background:rgba(255,255,255,0.05); border-radius:8px;\">")
                    .append("<a href=\"").append(href).append("\" style=\"font-weight:600; color:#3b82f6; text-decoration:none;\" target=\"_blank\">")
                    .append(escape(name)).append("</a>")
                    .append("<br/><span style=\"color:#94a3b8; font-size:14px;\">")
                    .append(formatVnd(price)).append("/ngày")
                    .append(" • Loại: ").append(escape(typeName))
                    .append(" • Biển số: ").append(escape(licensePlate))
                    .append(" • Trạng thái: <span style=\"color:").append(getStatusColor(status)).append("\">").append(escape(status)).append("</span>")
                    .append("</span>");
            out.append("</li>");
        }
        out.append("</ul>");
        out.append("<br/><small style=\"color:#64748b;\">🔗 Click vào tên xe để xem chi tiết và đặt thuê</small>");
        return out.toString();
    }

    /** Màu sắc cho trạng thái xe */
    private String getStatusColor(String status) {
        if (status == null) return "#94a3b8";
        switch (status.toLowerCase()) {
            case "available": return "#10b981"; // green
            case "rented": return "#ef4444";    // red
            case "maintenance": return "#f59e0b"; // yellow
            default: return "#94a3b8";          // gray
        }
    }

    private String formatVnd(BigDecimal v) {
        if (v == null) return "0đ";
        String s = v.setScale(0, BigDecimal.ROUND_HALF_UP).toPlainString();
        StringBuilder sb = new StringBuilder(s);
        for (int i = sb.length() - 3; i > 0; i -= 3) sb.insert(i, ',');
        return sb.append('đ').toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    /** Cache schema mỗi 30 phút để giảm overhead prompt */
    private String getSchemaDoc() {
        long now = System.currentTimeMillis();
        if (cachedSchema == null || now - cachedAt > SCHEMA_TTL_MS) {
            cachedSchema = dao.buildSchemaDoc();
            cachedAt = now;
        }
        return cachedSchema;
    }
}