package com.example.dispatch.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.dispatch.config.GroqProperties;
import com.example.dispatch.exception.AiPlanningException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "groq", matchIfMissing = true)
public class GroqAiProvider implements AiProvider {

    private final GroqProperties groqProperties;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GroqAiProvider(GroqProperties groqProperties) {
        this.groqProperties = groqProperties;
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Override
    public String getCompletion(String systemPrompt, String userPrompt) {
        if (groqProperties.getApiKey() == null || groqProperties.getApiKey().isBlank()) {
            throw new AiPlanningException("Groq API key is missing. Please set ai.groq.api.key in configuration.");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", groqProperties.getModel(),
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.2 // Low temperature for deterministic planning
            );

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(groqProperties.getApiUrl()))
                    .header("Authorization", "Bearer " + groqProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new AiPlanningException("Groq API failed with status " + response.statusCode() + ": " + response.body());
            }

            JsonNode rootNode = objectMapper.readTree(response.body());
            return rootNode.path("choices").get(0).path("message").path("content").asText();

        } catch (AiPlanningException e) {
            throw e; // Rethrow custom exception without wrapping
        } catch (Exception e) {
            throw new AiPlanningException("Failed to communicate with Groq AI API", e);
        }
    }
}
