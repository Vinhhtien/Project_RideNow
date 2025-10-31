package utils.AI;

public class TestGemini {
    public static void main(String[] args) {
        GeminiClient g = new GeminiClient();
        String out = g.ask("Chào bạn, kiểm tra echo tiếng Việt giúp mình nhé.");
        System.out.println(out);
    }
}
