package com.example.dispatch.ai;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock")
public class MockAiProvider implements AiProvider {

    @Override
    public String getCompletion(String systemPrompt, String userPrompt) {
        // Fallback for tests or local dev without an API key
        return """
        {
          "assignments": [
            {
              "requestId": 1,
              "technicianId": 1,
              "startTime": "10:00:00",
              "endTime": "12:00:00"
            }
          ],
          "unassignedRequests": [],
          "tradeoffs": ["Mock AI assigned Request 1 to Tech 1 based on minimal workload."],
          "risks": ["Mock AI suggests monitoring load for afternoon."]
        }
        """;
    }
}
