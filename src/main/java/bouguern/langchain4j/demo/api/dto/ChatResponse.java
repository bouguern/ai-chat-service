package bouguern.langchain4j.demo.api.dto;

public record ChatResponse(
        String conversationId,
        String message,
        long   messageCount
) {}