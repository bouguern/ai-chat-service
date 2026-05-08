package bouguern.langchain4j.demo.infrastructure.persistence;

import bouguern.langchain4j.demo.domain.model.Conversation;
import bouguern.langchain4j.demo.domain.model.ConversationId;
import bouguern.langchain4j.demo.domain.port.outbound.ConversationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Zero domain or application changes required for that swap.
 */
@Repository
public class InMemoryConversationRepository implements ConversationRepository {

    private final ConcurrentHashMap<String, Conversation> store =
            new ConcurrentHashMap<>();

    @Override
    public void save(Conversation conversation) {
        store.put(conversation.getId().value(), conversation);
    }

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return Optional.ofNullable(store.get(id.value()));
    }
}