package bouguern.langchain4j.demo.domain.exception;

public class LanguageModelException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/** Use when wrapping an underlying infrastructure exception. */
    public LanguageModelException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Use for semantic failures with no underlying exception (e.g. empty response). */
    public LanguageModelException(String message) {
        super(message);
    }
}