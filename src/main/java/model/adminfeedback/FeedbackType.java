package model.adminfeedback;

public enum FeedbackType {
    STORE, BIKE;

    public static FeedbackType from(String s) {
        if (s == null) return null;
        s = s.trim().toUpperCase();
        if (s.startsWith("S")) return STORE;
        if (s.startsWith("B")) return BIKE;
        return null;
    }
}
