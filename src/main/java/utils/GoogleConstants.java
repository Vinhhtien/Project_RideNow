package utils;

public class GoogleConstants {
    public static final String CLIENT_ID = "544893903492-fedsn6t5jeks8fm0aus5gete8ckv7k1r.apps.googleusercontent.com";
    public static final String CLIENT_SECRET = "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";
    public static final String REDIRECT_URI = "http://localhost:8080/Project_RideNow/logingoogle";

    public static final String AUTH_URI = "https://accounts.google.com/o/oauth2/auth";
    public static final String TOKEN_URI = "https://oauth2.googleapis.com/token";
    public static final String USERINFO_URI = "https://www.googleapis.com/oauth2/v2/userinfo";

    public static final String SCOPE = "email profile";
    public static final String GRANT_TYPE = "authorization_code";
}
