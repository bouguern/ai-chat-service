package bouguern.langchain4j.demo.domain.model;

import java.util.Objects;
import java.util.UUID;

public record ConversationId(String value) {

    public ConversationId {
        Objects.requireNonNull(value, "ConversationId must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("ConversationId must not be blank");
        }
    }

    public static ConversationId generate() {
        return new ConversationId(UUID.randomUUID().toString());
    }

    public static ConversationId of(String value) {
        return new ConversationId(value);
    }
}