package utils;

import model.GoogleUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleUtils {

    public static String buildAuthURL() {
        try {
            String base = GoogleConstants.AUTH_URI
                    + "?client_id=" + URLEncoder.encode(GoogleConstants.CLIENT_ID, "UTF-8")
                    + "&redirect_uri=" + URLEncoder.encode(GoogleConstants.REDIRECT_URI, "UTF-8")
                    + "&response_type=code"
                    + "&scope=" + URLEncoder.encode(GoogleConstants.SCOPE, "UTF-8")
                    + "&access_type=offline"
                    + "&prompt=select_account";
            return base;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String exchangeCodeForToken(String code) throws IOException {
        String data = "code=" + enc(code)
                + "&client_id=" + enc(GoogleConstants.CLIENT_ID)
                + "&client_secret=" + enc(GoogleConstants.CLIENT_SECRET)
                + "&redirect_uri=" + enc(GoogleConstants.REDIRECT_URI)
                + "&grant_type=" + enc(GoogleConstants.GRANT_TYPE);

        HttpURLConnection conn = (HttpURLConnection) new URL(GoogleConstants.TOKEN_URI).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Accept", "application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data.getBytes(StandardCharsets.UTF_8));
        }

        String json = readAll(conn);
        // Log hỗ trợ debug
        // System.out.println("[GoogleUtils] token json = " + json);

        // Tìm access_token bằng regex
        String token = findJsonString(json, "access_token");
        if (token == null || token.isBlank()) {
            // Thường nếu sai test user / sai redirect uri / sai secret -> json sẽ có "error" / "error_description"
            String err = findJsonString(json, "error_description");
            if (err == null) err = findJsonString(json, "error");
            System.err.println("[GoogleUtils] Không lấy được access_token, response: " + json);
            if (err != null) System.err.println("[GoogleUtils] error: " + err);
        }
        return token;
    }

    public static GoogleUser fetchUserInfo(String accessToken) throws IOException {
        // Dùng v3 ổn định hơn
        String endpoint = GoogleConstants.USERINFO_URI; // v3
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Accept", "application/json");

        String json = readAll(conn);
        // System.out.println("[GoogleUtils] userinfo json = " + json);

        GoogleUser u = new GoogleUser();
        u.setId(findJsonString(json, "sub"));                      // v3 dùng "sub" làm id
        if (u.getId() == null) u.setId(findJsonString(json, "id"));// dự phòng
        u.setEmail(findJsonString(json, "email"));
        String ve = findJsonString(json, "email_verified");
        if (ve != null) u.setVerifiedEmail("true".equalsIgnoreCase(ve));
        u.setName(findJsonString(json, "name"));
        u.setGivenName(findJsonString(json, "given_name"));
        u.setFamilyName(findJsonString(json, "family_name"));
        u.setPicture(findJsonString(json, "picture"));
        return u;
    }

    // ===== helpers =====
    private static String enc(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return "";
        }
    }

    private static String readAll(HttpURLConnection conn) throws IOException {
        InputStream is = (conn.getResponseCode() >= 400) ? conn.getErrorStream() : conn.getInputStream();
        if (is == null) return "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    // Regex: "key" : "value"  hoặc  "key" : true/false/number
    private static String findJsonString(String json, String key) {
        if (json == null) return null;
        // nhóm 1: value dạng "..."
        Pattern p1 = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher m1 = p1.matcher(json);
        if (m1.find()) return m1.group(1);

        // nhóm 2: value dạng boolean/number
        Pattern p2 = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([^,}\\s]+)");
        Matcher m2 = p2.matcher(json);
        if (m2.find()) return m2.group(1);
        return null;
    }
}
