package bouguern.langchain4j.demo.domain.exception;

public class ConversationNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private final String conversationId;

    public ConversationNotFoundException(String conversationId) {
        super("Conversation not found: " + conversationId);
        this.conversationId = conversationId;
    }

    public String getConversationId() {
        return conversationId;
    }
}