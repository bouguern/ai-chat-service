package bouguern.langchain4j.demo.domain.port.outbound;

import bouguern.langchain4j.demo.domain.model.Conversation;
import bouguern.langchain4j.demo.domain.model.ConversationId;

import java.util.Optional;

public interface ConversationRepository {
    void save(Conversation conversation);
    Optional<Conversation> findById(ConversationId id);
}