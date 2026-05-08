package bouguern.langchain4j.demo.api.rest;

import bouguern.langchain4j.demo.domain.exception.ConversationNotFoundException;
import bouguern.langchain4j.demo.domain.exception.InvalidConversationStateException;
import bouguern.langchain4j.demo.domain.exception.LanguageModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * RFC 7807 ProblemDetail — structured errors, machine-parseable type URIs.
 * Log levels: warn for client errors (4xx), error for infrastructure failures (5xx).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ConversationNotFoundException.class)
    public ProblemDetail handleNotFound(ConversationNotFoundException ex) {
        log.warn("Conversation not found [id={}]", ex.getConversationId());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setType(URI.create("/errors/conversation-not-found"));
        pd.setTitle("Conversation Not Found");
        return pd;
    }

    @ExceptionHandler(InvalidConversationStateException.class)
    public ProblemDetail handleInvalidState(InvalidConversationStateException ex) {
        log.warn("Invalid conversation state: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setType(URI.create("/errors/invalid-conversation-state"));
        pd.setTitle("Invalid Conversation State");
        return pd;
    }

    @ExceptionHandler(LanguageModelException.class)
    public ProblemDetail handleLlmFailure(LanguageModelException ex) {
        log.error("Language model failure: {}", ex.getMessage(), ex);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "The AI service is temporarily unavailable. Please retry.");
        pd.setType(URI.create("/errors/language-model-unavailable"));
        pd.setTitle("AI Service Unavailable");
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.debug("Validation failure: {}", detail);
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setType(URI.create("/errors/validation-failure"));
        pd.setTitle("Validation Failed");
        return pd;
    }
}