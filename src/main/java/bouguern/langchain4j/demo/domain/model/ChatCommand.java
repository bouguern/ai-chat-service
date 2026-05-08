package bouguern.langchain4j.demo.domain.model;

import java.util.Objects;

public record ChatCommand(
        String conversationId,
        String userMessage
) {

    public ChatCommand {
        Objects.requireNonNull(userMessage, "userMessage must not be null");
        if (userMessage.isBlank()) {
            throw new IllegalArgumentException("userMessage must not be blank");
        }
    }

    public boolean hasConversationId() {
        return conversationId != null && !conversationId.isBlank();
    }
}