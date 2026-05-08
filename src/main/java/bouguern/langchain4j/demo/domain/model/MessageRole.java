package bouguern.langchain4j.demo.domain.model;

/**
 * Enumerates who authored a message in a conversation.
 *
 * "A user message must not follow another user message without an
 * intervening assistant reply" is expressed in terms of MessageRole.
 * That rule lives in ConversationDomainService.
 */
public enum MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}