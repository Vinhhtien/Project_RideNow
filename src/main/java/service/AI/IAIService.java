// src/main/java/service/AI/IAIService.java
package service.AI;

import java.util.List;
import java.util.Map;

public interface IAIService {
    String smallTalk(String question);

    String answerFromDatabase(String question);

    Map<String, Object> debugDatabaseAnswer(String question);
}
