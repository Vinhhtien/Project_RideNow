// src/main/java/utils/AI/GeminiClient.java
package utils.AI;

import okhttp3.*;
import com.google.gson.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GeminiClient {

    // Model text-chat nhanh
    private static final String MODEL = "gemini-2.5-flash";
    private static final String API_KEY = "AIzaSyCnAZscEuegrilZj5T_rRrsnlweHutKP94"; // <-- thay key của bạn
    private static final String ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODEL + ":generateContent";

    private final OkHttpClient http = new OkHttpClient.Builder()
            .callTimeout(40, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private final Gson gson = new Gson();

    public String ask(String question) {
        if (API_KEY == null || API_KEY.isBlank()) {
            return "❌ Chưa đặt API key.";
        }

        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", question);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        JsonArray parts = new JsonArray();
        parts.add(userPart);
        userMsg.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(userMsg);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("x-goog-api-key", API_KEY) // header chuẩn
                .post(RequestBody.create(
                        gson.toJson(requestBody),
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response resp = http.newCall(req).execute()) {
            String body = (resp.body() != null) ? resp.body().string() : "";
            if (!resp.isSuccessful()) {
                return "❌ HTTP " + resp.code() + " → " + body;
            }
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            JsonArray candidates = json.getAsJsonArray("candidates");
            if (candidates != null && candidates.size() > 0) {
                JsonObject content = candidates.get(0).getAsJsonObject().getAsJsonObject("content");
                if (content != null) {
                    JsonArray partsArr = content.getAsJsonArray("parts");
                    if (partsArr != null && partsArr.size() > 0) {
                        return partsArr.get(0).getAsJsonObject().get("text").getAsString();
                    }
                }
            }
            return "⚠️ Không có phản hồi text từ Gemini.";
        } catch (IOException e) {
            return "❌ IOException: " + e.getMessage();
        }
    }
}
