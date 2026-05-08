package bouguern.langchain4j.demo.domain.exception;

public class InvalidConversationStateException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InvalidConversationStateException(String message) {
        super(message);
    }
}