package bouguern.langchain4j.demo.domain.model;

import java.util.Objects;

public record ChatResult(
        String conversationId,
        String assistantMessage,
        long messageCount
) {

    public ChatResult {
        Objects.requireNonNull(conversationId,   "conversationId must not be null");
        Objects.requireNonNull(assistantMessage, "assistantMessage must not be null");
    }
}