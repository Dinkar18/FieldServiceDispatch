package com.example.dispatch.ai;

public interface AiProvider {
    /**
     * Sends prompts to the underlying LLM provider and returns the raw JSON string response.
     */
    String getCompletion(String systemPrompt, String userPrompt);
}
