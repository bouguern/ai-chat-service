package bouguern.langchain4j.demo.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Conversation {

    private final ConversationId id;
    private final List<ChatMessage> messages;
    private final Instant createdAt;
    private Instant updatedAt;

    public Conversation(ConversationId id) {
        this.id        = Objects.requireNonNull(id, "id must not be null");
        this.messages  = new ArrayList<>();
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public void addMessage(ChatMessage message) {
        Objects.requireNonNull(message, "message must not be null");
        messages.add(message);
        updatedAt = Instant.now();
    }

    public List<ChatMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public ConversationId getId()  { return id; }
    public Instant getCreatedAt()  { return createdAt; }
    public Instant getUpdatedAt()  { return updatedAt; }
}