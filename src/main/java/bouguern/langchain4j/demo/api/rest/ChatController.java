package bouguern.langchain4j.demo.api.rest;

import bouguern.langchain4j.demo.api.dto.ChatRequest;
import bouguern.langchain4j.demo.api.dto.ChatResponse;
import bouguern.langchain4j.demo.domain.model.ChatCommand;
import bouguern.langchain4j.demo.domain.model.ChatResult;
import bouguern.langchain4j.demo.domain.port.inbound.ChatUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Three jobs only:
 *  1. Validate HTTP input (@Valid)
 *  2. Map ChatRequest → ChatCommand
 *  3. Map ChatResult  → ChatResponse
 * No business logic. No domain knowledge. No infrastructure awareness.
 */
@RestController
@RequestMapping("/api/v1")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatUseCase chatUseCase;

    public ChatController(ChatUseCase chatUseCase) {
        this.chatUseCase = chatUseCase;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        log.info("POST /api/v1/chat [conversationId={}]",
                request.conversationId() != null ? request.conversationId() : "new");

        ChatResult result = chatUseCase.chat(
                new ChatCommand(request.conversationId(), request.message())
        );

        return ResponseEntity.ok(new ChatResponse(
                result.conversationId(),
                result.assistantMessage(),
                result.messageCount()
        ));
    }
}