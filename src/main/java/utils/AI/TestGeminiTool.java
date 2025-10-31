package utils.AI;

import java.util.*;

public class TestGeminiTool {
    public static void main(String[] args) {
        GeminiToolClient ai = new GeminiToolClient();

        String schema = """
                Tables:
                - Motorbikes(Id INT, BikeName NVARCHAR, PricePerDay DECIMAL, Status NVARCHAR, TypeId INT, City NVARCHAR)
                - BikeTypes(Id INT, TypeName NVARCHAR)
                """;

        String policy = """
                - Chỉ tạo SELECT có ? placeholders.
                - Không bao giờ dùng DELETE/UPDATE/INSERT.
                - Nếu có điều kiện, luôn dùng WHERE.
                """;

        String question = "Liệt kê các xe tay ga giá dưới 150000 ở Đà Nẵng.";

        var t1 = ai.turn1_buildSql(question, schema, policy);

        if (!t1.isToolCall()) {
            System.out.println("AI Text: " + t1.getText());
        } else {
            System.out.println("SQL: " + t1.getSql());
            System.out.println("Params: " + t1.getParams());
        }
    }
}
