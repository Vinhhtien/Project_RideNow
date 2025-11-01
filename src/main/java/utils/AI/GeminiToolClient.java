// src/main/java/utils/AI/GeminiToolClient.java
package utils.AI;

import okhttp3.*;
import com.google.gson.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class GeminiToolClient {

    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_KEY = "AIzaSyCnAZscEuegrilZj5T_rRrsnlweHutKP94"; // <-- thay key của bạn

    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private final OkHttpClient http = new OkHttpClient.Builder()
            .callTimeout(50, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(40, TimeUnit.SECONDS)
            .writeTimeout(40, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    public ToolCall turn1_buildSql(String question, String schemaDoc, String policyDoc) {
        JsonObject req = new JsonObject();
        req.add("tools", buildTools());

        JsonObject toolCfg = new JsonObject();
        JsonObject fnCfg = new JsonObject();
        fnCfg.addProperty("mode", "ANY");
        toolCfg.add("functionCallingConfig", fnCfg);
        req.add("toolConfig", toolCfg);

        JsonArray contents = new JsonArray();
        contents.add(msg("user",
                "Bạn là trợ lý RideNow. Dùng tool runSql để sinh truy vấn SQL Server SELECT.\n"
                        + "SCHEMA:\n" + schemaDoc + "\nPOLICY:\n" + policyDoc + "\nQUESTION:\n" + question));
        req.add("contents", contents);

        JsonObject gen = new JsonObject();
        gen.addProperty("temperature", 0.2);
        gen.addProperty("maxOutputTokens", 512);
        req.add("generationConfig", gen);

        Request httpReq = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("x-goog-api-key", API_KEY)
                .post(RequestBody.create(gson.toJson(req), MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response resp = http.newCall(httpReq).execute()) {
            String body = resp.body() == null ? "" : resp.body().string();
            if (!resp.isSuccessful()) {
                System.err.println("Gemini HTTP " + resp.code() + " → " + body);
                return ToolCall.plain("HTTP " + resp.code());
            }
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return extractToolCallOrText(json);
        } catch (IOException e) {
            return ToolCall.plain("IOException: " + e.getMessage());
        }
    }

    public String turn2_explainFromRows(String question, List<Map<String, Object>> rows) {
        JsonObject req = new JsonObject();

        JsonArray contents = new JsonArray();
        contents.add(msg("user",
                "Trả lời ngắn gọn bằng tiếng Việt dựa trên dữ liệu ROWS(JSON):\n"
                        + gson.toJson(rows) + "\nQUESTION:\n" + question));
        req.add("contents", contents);

        JsonObject gen = new JsonObject();
        gen.addProperty("temperature", 0.4);
        gen.addProperty("maxOutputTokens", 700);
        req.add("generationConfig", gen);

        Request httpReq = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("x-goog-api-key", API_KEY)
                .post(RequestBody.create(gson.toJson(req), MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response resp = http.newCall(httpReq).execute()) {
            String body = resp.body() == null ? "" : resp.body().string();
            if (!resp.isSuccessful()) {
                System.err.println("Gemini HTTP " + resp.code() + " → " + body);
                return "[Gemini] HTTP " + resp.code();
            }
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            return extractText(json);
        } catch (IOException e) {
            return "IOException: " + e.getMessage();
        }
    }

    private JsonObject msg(String role, String text) {
        JsonObject m = new JsonObject();
        m.addProperty("role", role);
        JsonArray parts = new JsonArray();
        JsonObject t = new JsonObject();
        t.addProperty("text", text);
        parts.add(t);
        m.add("parts", parts);
        return m;
    }

    private JsonArray buildTools() {
        JsonArray tools = new JsonArray();
        JsonObject tool = new JsonObject();
        JsonArray fns = new JsonArray();

        JsonObject fn = new JsonObject();
        fn.addProperty("name", "runSql");

        JsonObject params = new JsonObject();
        params.addProperty("type", "OBJECT");

        JsonObject props = new JsonObject();
        JsonObject st = new JsonObject();
        st.addProperty("type", "STRING");
        st.addProperty("description", "SQL Server SELECT với ? placeholders.");
        props.add("statement", st);

        JsonObject pa = new JsonObject();
        pa.addProperty("type", "ARRAY");
        JsonObject items = new JsonObject();
        items.addProperty("type", "STRING");
        pa.add("items", items);
        pa.addProperty("description", "Danh sách tham số bind vào ? theo thứ tự.");
        props.add("params", pa);

        params.add("properties", props);
        JsonArray required = new JsonArray();
        required.add("statement");
        params.add("required", required);

        fn.add("parameters", params);
        fns.add(fn);
        tool.add("functionDeclarations", fns);
        tools.add(tool);

        return tools;
    }

    private ToolCall extractToolCallOrText(JsonObject json) {
        var cands = json.getAsJsonArray("candidates");
        if (cands != null && !cands.isEmpty()) {
            var content = cands.get(0).getAsJsonObject().getAsJsonObject("content");
            if (content != null) {
                var parts = content.getAsJsonArray("parts");
                for (var p : parts) {
                    var obj = p.getAsJsonObject();
                    if (obj.has("functionCall")) {
                        var fc = obj.getAsJsonObject("functionCall");
                        if ("runSql".equals(fc.get("name").getAsString())) {
                            var args = fc.getAsJsonObject("args");
                            String statement = args.get("statement").getAsString();
                            List<String> params = new ArrayList<>();
                            if (args.has("params")) {
                                for (var el : args.getAsJsonArray("params")) params.add(el.getAsString());
                            }
                            return ToolCall.tool(statement, params);
                        }
                    }
                }
                return ToolCall.plain(extractText(json));
            }
        }
        return ToolCall.plain("No candidates.");
    }

    private String extractText(JsonObject json) {
        var cands = json.getAsJsonArray("candidates");
        if (cands != null && !cands.isEmpty()) {
            var content = cands.get(0).getAsJsonObject().getAsJsonObject("content");
            if (content != null && content.has("parts")) {
                var parts = content.getAsJsonArray("parts");
                if (!parts.isEmpty()) {
                    var t = parts.get(0).getAsJsonObject().get("text");
                    if (t != null) return t.getAsString();
                }
            }
        }
        return "";
    }

    public static class ToolCall {
        private final boolean tool;
        private final String statement;
        private final List<String> params;
        private final String text;

        private ToolCall(boolean tool, String st, List<String> ps, String tx) {
            this.tool = tool;
            this.statement = st;
            this.params = ps;
            this.text = tx;
        }

        public static ToolCall tool(String st, List<String> ps) {
            return new ToolCall(true, st, ps, null);
        }

        public static ToolCall plain(String tx) {
            return new ToolCall(false, null, null, tx);
        }

        public boolean isToolCall() {
            return tool;
        }

        public String getSql() {
            return statement;
        }

        public List<String> getParams() {
            return params == null ? List.of() : params;
        }

        public String getText() {
            return text;
        }
    }
}
