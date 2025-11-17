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

        String fixedQuestion = fixEncoding(question);
        System.out.println("DEBUG: smallTalk - Original: " + question);
        System.out.println("DEBUG: smallTalk - Fixed: " + fixedQuestion);

        String normalized = removeAccents(fixedQuestion.toLowerCase());

        // ====== CÂU TRẢ LỜI CỐ ĐỊNH VỀ DANH TÍNH TRỢ LÝ ======
        if (normalized.contains("ban la ai")
                || normalized.contains("may la ai")
                || normalized.contains("ban la cai gi")
                || normalized.contains("tro ly ai")
                || normalized.contains("ban lam duoc gi")
                || normalized.contains("chuc nang gi")
                || normalized.contains("ban co the giup gi")
                || normalized.contains("gioi thieu ve ban")) {

            return """
                    Xin chào 👋<br/>
                    Mình là <b>trợ lý AI của hệ thống RideNow</b>.<br/><br/>
                    Mình có thể giúp bạn:<br/>
                    • Gợi ý <b>xe số, xe ga, xe phân khối lớn</b> phù hợp nhu cầu<br/>
                    • Tìm xe theo <b>giá, loại, trạng thái còn trống</b><br/>
                    • Xem nhanh thông tin chi tiết xe và mở link đến trang đặt thuê<br/><br/>
                    Các thông tin về xe, giá, trạng thái mình trả lời đều được lấy từ
                    <b>cơ sở dữ liệu RideNow (SQL Server)</b>, nên mình sẽ không tự bịa thêm dữ liệu ngoài hệ thống. 😊
                    """;
        }

        // ====== USER HỎI VỀ Ô TÔ → TỪ CHỐI NHẸ NHÀNG ======
        if (normalized.contains("oto") || normalized.contains("o to")
                || normalized.contains("ô tô") || normalized.contains("xe hoi")
                || normalized.contains("sedan") || normalized.contains("suv")
                || normalized.contains("pickup") || normalized.contains("ban tai")) {

            return """
                    Hiện tại mình chỉ là <b>trợ lý AI cho hệ thống thuê xe máy RideNow</b> 🛵<br/>
                    Mình không tư vấn chi tiết về ô tô, nhưng có thể giúp bạn:<br/>
                    • Gợi ý xe số, xe ga, xe phân khối lớn trong hệ thống RideNow<br/>
                    • Tìm xe theo giá, loại, trạng thái còn trống<br/><br/>
                    Bạn có thể hỏi ví dụ:<br/>
                    • <i>Xe ga nào dưới 180.000đ/ngày?</i><br/>
                    • <i>Top xe phân khối lớn đắt nhất</i><br/>
                    • <i>Xe số nào đang còn available?</i>
                    """;
        }

        // ====== CÂU KIỂU "XE NGON NHẤT" MƠ HỒ → TRẢ LỜI GỢI Ý THEO RIDENOW ======
        if (normalized.contains("xe ngon nhat")
                || normalized.contains("xe nao ngon")
                || normalized.contains("xe nao tot")
                || normalized.contains("xe tot nhat")) {

            return """
                    Câu hỏi <b>\"xe ngon nhất\"</b> hơi rộng vì còn phụ thuộc vào:<br/>
                    • Bạn thích <b>xe số, xe ga hay phân khối lớn</b><br/>
                    • <b>Ngân sách</b> thuê mỗi ngày (ví dụ: dưới 150k, 150k–200k, trên 200k)<br/>
                    • Bạn ưu tiên <b>tiết kiệm, êm, mạnh hay nhìn ngầu</b> 😄<br/><br/>
                    Trong hệ thống <b>RideNow</b>, mình có thể giúp bạn tìm:<br/>
                    • <b>Xe rẻ nhất</b> → hãy thử hỏi: <i>\"xe rẻ nhất\"</i><br/>
                    • <b>Xe đắt nhất / xịn nhất</b> theo giá → hỏi: <i>\"xe đắt nhất\"</i><br/>
                    • Hoặc cụ thể hơn, ví dụ:<br/>
                    &nbsp;&nbsp;• <i>\"Xe ga dưới 180000\"</i><br/>
                    &nbsp;&nbsp;• <i>\"Top 5 xe phân khối lớn\"</i><br/><br/>
                    Bạn thử mô tả rõ hơn nhu cầu (loại xe + tầm giá), mình sẽ gợi ý sát hơn với dữ liệu trong hệ thống nhé 🛵
                    """;
        }

        // ====== CÁC SMALLTALK KHÁC → GỌI LLM NHƯNG CÓ NGỮ CẢNH RIDENOW ======
        String systemPrompt = """
                Bạn đang đóng vai trò là <b>trợ lý AI của hệ thống thuê xe máy RideNow</b>.
                Nguyên tắc:
                - Luôn trả lời bằng tiếng Việt (có thể thêm emoji nhẹ nhàng).
                - Chỉ tư vấn trong bối cảnh thuê <b>xe máy</b> (xe số, xe ga, xe phân khối lớn) và dịch vụ RideNow.
                - Không tư vấn chi tiết, so sánh hay quảng cáo về ô tô, siêu xe, xe không liên quan đến hệ thống.
                - Nếu câu hỏi vượt quá phạm vi (ví dụ: hỏi mua ô tô, chính trị, y tế...), hãy lịch sự nói rằng bạn chỉ hỗ trợ về thuê xe máy RideNow.
                - Luôn trả lời ngắn gọn, thân thiện, gợi ý user hỏi cụ thể hơn nếu cần.
                """;

        String finalPrompt = systemPrompt + "\n\nCâu hỏi của khách: " + fixedQuestion;

        return chatClient.ask(finalPrompt);
    }


        @Override
    public String answerFromDatabase(String question) {
        try {
            String fixedQuestion = fixEncoding(question);
            System.out.println("DEBUG: answerFromDatabase - Original: " + question);
            System.out.println("DEBUG: answerFromDatabase - Fixed: " + fixedQuestion);

            // 1) Intent nhanh theo loại xe
            String typeIntent = detectTypeIntent(fixedQuestion);
            System.out.println("DEBUG: Detected typeIntent: " + typeIntent);

            if (typeIntent != null) {
                List<Map<String, Object>> rows = dao.topBikesByType(typeIntent, 5);
                System.out.println("DEBUG: Found " + (rows == null ? 0 : rows.size()) + " bikes for type: " + typeIntent);

                if (rows == null || rows.isEmpty()) {
                    return "⚠️ Hiện chưa có xe thuộc loại <b>" + escape(typeIntent) + "</b> trong danh sách.";
                }
                return renderTopListWithLinks(typeIntent, rows);
            }

            // 2) Intent phức tạp (giá, trạng thái, rẻ nhất / đắt nhất...)
            String complexIntent = detectComplexIntent(fixedQuestion);
            System.out.println("DEBUG: Detected complexIntent: " + complexIntent);

            if (complexIntent != null) {
                String res = handleComplexIntent(complexIntent, fixedQuestion);
                if (res != null) return res;
            }

            // 3) Tạo schema + policy
            String schemaDoc = getSchemaDoc();
            String policyDoc = """
                    - Chỉ sinh truy vấn SELECT có ? placeholders.
                    - Không dùng DELETE/UPDATE/INSERT/EXEC/DDL.
                    - Nếu có điều kiện, luôn dùng WHERE và JOIN chuẩn.
                    - Không truy cập sysobjects/sp_/INFORMATION_SCHEMA trực tiếp.
                    """;

            // 4) Nhờ Gemini sinh SQL an toàn
            ToolCall t1;
            try {
                t1 = toolClient.turn1_buildSql(fixedQuestion, schemaDoc, policyDoc);
            } catch (Exception ex) {
                ex.printStackTrace();
                // Tool client lỗi → fallback smallTalk cho user vẫn có câu trả lời
                return smallTalk(question);
            }

            if (!t1.isToolCall()) {
                // Không sinh được tool call hợp lệ → hướng dẫn user hỏi rõ hơn
                return "⚠️ Tôi chưa hiểu rõ câu hỏi liên quan đến dữ liệu hệ thống.<br/>" +
                        "Bạn hãy thử hỏi cụ thể hơn, ví dụ:<br/>" +
                        "• \"Top 5 xe ga dưới 180000\"<br/>" +
                        "• \"Danh sách xe số còn available\"<br/>" +
                        "• \"Liệt kê tất cả xe PKL\"";
            }

            // 5) Query DB
            List<Map<String, Object>> rows = dao.select(t1.getSql(), t1.getParams());

            if (rows == null || rows.isEmpty()) {
                return "⚠️ Không tìm thấy dữ liệu phù hợp trong hệ thống cho câu hỏi: \"" +
                        escape(fixedQuestion) + "\"";
            }

            // 6) Giải thích (tuỳ chọn)
            String explain;
            try {
                explain = toolClient.turn2_explainFromRows(fixedQuestion, rows);
            } catch (Exception ex) {
                ex.printStackTrace();
                explain = "Dưới đây là dữ liệu hệ thống phù hợp với câu hỏi của bạn:";
            }

            // 7) Trả về: giải thích + bảng dữ liệu thật
            StringBuilder out = new StringBuilder();
            out.append("✅ ").append(escape(explain)).append("<br/>");
            out.append("<small style=\"color:#9ca3af;\">(Dữ liệu dưới đây được lấy trực tiếp từ hệ thống SQL Server)</small>");
            out.append(buildHtmlTableFromRows(rows));

            return out.toString();

        } catch (Exception e) {
            e.printStackTrace();
            // Không in message raw ra cho user nếu không cần
            return "❌ Đã xảy ra lỗi khi xử lý yêu cầu dữ liệu. Bạn hãy thử lại sau hoặc nhập câu hỏi cụ thể hơn.";
        }
    }


        @Override
    public Map<String, Object> debugDatabaseAnswer(String question) {
        Map<String, Object> debug = new LinkedHashMap<>();
        String fixedQuestion = fixEncoding(question);
        String schemaDoc = getSchemaDoc();
        String policyDoc = "- Chỉ SELECT, có ? placeholders.";

        try {
            ToolCall t1 = toolClient.turn1_buildSql(fixedQuestion, schemaDoc, policyDoc);
            debug.put("toolCall", t1);

            if (t1.isToolCall()) {
                List<Map<String, Object>> rows = dao.select(t1.getSql(), t1.getParams());
                debug.put("rows", rows);

                try {
                    debug.put("explain", toolClient.turn2_explainFromRows(fixedQuestion, rows));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    debug.put("explainError", ex.getMessage());
                }
            } else {
                debug.put("error", t1.getText());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            debug.put("exception", ex.getMessage());
        }

        return debug;
    }


    // ========= Helpers =========

    private String fixEncoding(String text) {
        if (text == null) return null;

        try {
            if (text.matches(".*[�].*") || containsEncodingIssues(text)) {
                System.out.println("DEBUG: Detected encoding issues in: " + text);
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

    /**
     * Nhận diện intent loại xe
     */
    private String detectTypeIntent(String q) {
        if (q == null) return null;
        String normalized = removeAccents(q.toLowerCase().trim());

        // Xe số
        if (normalized.contains("xe so") || normalized.contains("xesố") || normalized.contains("xeso")
                || normalized.contains(" wave") || normalized.contains("future") || normalized.contains("sirius")
                || normalized.contains("jupiter") || normalized.contains("exciter") || normalized.contains("winner")
                || normalized.contains("blade") || normalized.contains("alpha")) {
            return "Xe số";
        }
        // Xe ga
        if (normalized.contains("xe ga") || normalized.contains("xega") || normalized.contains("tay ga")
                || normalized.contains("vision") || normalized.contains("air blade") || normalized.contains("airblade")
                || normalized.contains("vario") || normalized.contains(" sh ") || normalized.endsWith(" sh")
                || normalized.contains("lead")) {
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

    /**
     * Nhận diện intent phức tạp (giá, trạng thái, rẻ nhất / đắt nhất)
     */
    private String detectComplexIntent(String q) {
        if (q == null) return null;
        String normalized = removeAccents(q.toLowerCase().trim());

        // ===== RẺ NHẤT / GIÁ TỐT NHẤT =====
        if (normalized.contains("re nhat")
                || normalized.contains("thap nhat")
                || normalized.contains("gia tot nhat")
                || (normalized.contains("gia") && normalized.contains("tot nhat"))
                || normalized.contains("best price")) {
            return "cheapest";
        }

        // ===== ĐẮT NHẤT / CAO NHẤT =====
        if (normalized.contains("dat nhat")
                || normalized.contains("cao nhat")
                || normalized.contains("gia cao nhat")
                || normalized.contains("max price")) {
            return "most_expensive";
        }

        // ===== Theo khoảng giá có số =====
        // Dưới X
        if (normalized.matches(".*(gia|giá).*(duoi|dưới|thap|thấp|re|rẻ).*")) {
            return extractPriceRange(q, "max");
        }
        // Từ / trên X
        if (normalized.matches(".*(gia|giá).*(tu|từ|tren|trên).*")) {
            return extractPriceRange(q, "min");
        }

        // Tìm tất cả xe available
        if (normalized.contains("co san") || normalized.contains("có sẵn")
                || normalized.contains("available")
                || normalized.contains("con xe") || normalized.contains("còn xe")) {
            return "available";
        }

        return null;
    }

    private String extractPriceRange(String q, String type) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(q);
        if (matcher.find()) {
            String price = matcher.group();
            if (Integer.parseInt(price) < 1000) {
                price = String.valueOf(Integer.parseInt(price) * 1000);
            }
            return type + "_" + price;
        }
        // Không có số → trả về unknown, phần handleComplexIntent sẽ quyết định
        return type + "_unknown";
    }

    private String handleComplexIntent(String intent, String question) {
        Map<String, String> conditions = new HashMap<>();

        // ===== Top xe rẻ nhất =====
        if ("cheapest".equals(intent)) {
            List<Map<String, Object>> rows = dao.findCheapestBikes(5);
            if (rows == null || rows.isEmpty()) {
                return "⚠️ Hiện không có xe nào trong hệ thống.";
            }
            return renderSearchResults("các xe có giá rẻ nhất (top 5)", rows);
        }

        // ===== Top xe đắt nhất =====
        if ("most_expensive".equals(intent)) {
            List<Map<String, Object>> rows = dao.findMostExpensiveBikes(5);
            if (rows == null || rows.isEmpty()) {
                return "⚠️ Hiện không có xe nào trong hệ thống.";
            }
            return renderSearchResults("các xe có giá cao nhất (top 5)", rows);
        }

        // ===== Giá tối đa (dưới X) =====
        if (intent.startsWith("max_")) {
            String price = intent.substring(4);
            if ("unknown".equals(price)) {
                // Không có số cụ thể, fallback: list xe có sẵn
                conditions.put("status", "available");
                List<Map<String, Object>> rows = dao.searchBikes(conditions);
                if (rows.isEmpty()) {
                    return "⚠️ Hiện không có xe nào đang available.";
                }
                return renderSearchResults("xe đang có sẵn", rows);
            }

            conditions.put("max_price", price);
            conditions.put("status", "available");

            List<Map<String, Object>> rows = dao.searchBikes(conditions);
            if (rows.isEmpty()) {
                return "⚠️ Không tìm thấy xe nào dưới " + safeIntK(price) + "k. Hãy thử mức giá cao hơn.";
            }
            return renderSearchResults("xe giá dưới " + safeIntK(price) + "k", rows);
        }

        // ===== Giá tối thiểu (từ X trở lên) =====
        if (intent.startsWith("min_")) {
            String price = intent.substring(4);
            if ("unknown".equals(price)) {
                conditions.put("status", "available");
                List<Map<String, Object>> rows = dao.searchBikes(conditions);
                if (rows.isEmpty()) {
                    return "⚠️ Hiện không có xe nào đang available.";
                }
                return renderSearchResults("xe đang có sẵn", rows);
            }

            conditions.put("min_price", price);
            conditions.put("status", "available");

            List<Map<String, Object>> rows = dao.searchBikes(conditions);
            if (rows.isEmpty()) {
                return "⚠️ Không tìm thấy xe nào từ " + safeIntK(price) + "k trở lên. Hãy thử mức giá thấp hơn.";
            }
            return renderSearchResults("xe giá từ " + safeIntK(price) + "k trở lên", rows);
        }

        // ===== Tất cả xe available =====
        if ("available".equals(intent)) {
            conditions.put("status", "available");
            List<Map<String, Object>> rows = dao.searchBikes(conditions);
            if (rows.isEmpty()) {
                return "⚠️ Hiện không có xe nào đang available.";
            }
            return renderSearchResults("xe có sẵn", rows);
        }

        return null;
    }

    private String safeIntK(String price) {
        try {
            int p = Integer.parseInt(price);
            return String.valueOf(p / 1000);
        } catch (NumberFormatException e) {
        }
        return price;
    }

    /**
     * Loại bỏ dấu tiếng Việt
     */
    private String removeAccents(String s) {
        if (s == null) return null;
        try {
            String temp = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD);
            return temp.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        } catch (Exception e) {
            return s;
        }
    }

    /**
     * Render danh sách top N theo loại kèm link chi tiết (HTML)
     */
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

    /**
     * Render kết quả tìm kiếm
     */
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

    /**
     * Bảng HTML từ rows (cho trường hợp dùng toolClient.select)
     */
    private String buildHtmlTableFromRows(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return "⚠️ Không tìm thấy dữ liệu phù hợp trong hệ thống.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"margin-top:6px;\">");
        sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:13px;\">");

        Map<String, Object> first = rows.get(0);
        sb.append("<thead><tr>");
        for (String col : first.keySet()) {
            sb.append("<th style=\"border-bottom:1px solid #1f2937;padding:4px 6px;text-align:left;color:#9ca3af;\">")
                    .append(escape(col))
                    .append("</th>");
        }
        sb.append("</tr></thead>");

        sb.append("<tbody>");
        for (Map<String, Object> row : rows) {
            sb.append("<tr>");
            for (Object val : row.values()) {
                sb.append("<td style=\"border-bottom:1px solid #111827;padding:4px 6px;color:#e5e7eb;\">")
                        .append(escape(val == null ? "" : String.valueOf(val)))
                        .append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        sb.append("</div>");

        return sb.toString();
    }

    private String getStatusColor(String status) {
        if (status == null) return "#94a3b8";
        switch (status.toLowerCase()) {
            case "available":
                return "#10b981";
            case "rented":
                return "#ef4444";
            case "maintenance":
                return "#f59e0b";
            default:
                return "#94a3b8";
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
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Cache schema mỗi 30 phút để giảm overhead prompt
     */
    private String getSchemaDoc() {
        long now = System.currentTimeMillis();
        if (cachedSchema == null || now - cachedAt > SCHEMA_TTL_MS) {
            cachedSchema = dao.buildSchemaDoc();
            cachedAt = now;
        }
        return cachedSchema;
    }
}
