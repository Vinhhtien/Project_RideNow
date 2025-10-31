package utils.AI;

import okhttp3.*;
import com.google.gson.*;

public class TestListModels {
    // dùng KEY Google Cloud (dạng AIza…)
    private static final String API_KEY = "AIzaSyCnAZscEuegrilZj5T_rRrsnlweHutKP94"; // <-- dán key mới
    private static final String ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models?key=" + API_KEY;

    public static void main(String[] args) throws Exception {
        OkHttpClient http = new OkHttpClient();
        Request req = new Request.Builder().url(ENDPOINT).get().build();
        try (Response resp = http.newCall(req).execute()) {
            String body = resp.body() == null ? "" : resp.body().string();
            System.out.println("HTTP " + resp.code());
            System.out.println(body);
        }
    }
}
