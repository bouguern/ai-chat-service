package bouguern.langchain4j.demo.domain.model;

import java.time.Instant;
import java.util.Objects;

public record ChatMessage(
        MessageRole role,
        String content,
        Instant timestamp
) {

    public ChatMessage {
        Objects.requireNonNull(role,      "role must not be null");
        Objects.requireNonNull(content,   "content must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        if (content.isBlank()) {
            throw new IllegalArgumentException("Message content must not be blank");
        }
    }

    public static ChatMessage userMessage(String content) {
        return new ChatMessage(MessageRole.USER, content, Instant.now());
    }

    public static ChatMessage assistantMessage(String content) {
        return new ChatMessage(MessageRole.ASSISTANT, content, Instant.now());
    }

    public static ChatMessage systemMessage(String content) {
        return new ChatMessage(MessageRole.SYSTEM, content, Instant.now());
    }
}