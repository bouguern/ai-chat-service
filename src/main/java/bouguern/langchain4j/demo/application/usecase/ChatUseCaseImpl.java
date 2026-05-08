package bouguern.langchain4j.demo.application.usecase;

import bouguern.langchain4j.demo.domain.exception.ConversationNotFoundException;
import bouguern.langchain4j.demo.domain.exception.LanguageModelException;
import bouguern.langchain4j.demo.domain.model.ChatCommand;
import bouguern.langchain4j.demo.domain.model.ChatMessage;
import bouguern.langchain4j.demo.domain.model.ChatResult;
import bouguern.langchain4j.demo.domain.model.Conversation;
import bouguern.langchain4j.demo.domain.model.ConversationId;
import bouguern.langchain4j.demo.domain.port.inbound.ChatUseCase;
import bouguern.langchain4j.demo.domain.port.outbound.ConversationRepository;
import bouguern.langchain4j.demo.domain.port.outbound.LanguageModelPort;
import bouguern.langchain4j.demo.domain.service.ConversationDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class ChatUseCaseImpl implements ChatUseCase {

    private static final Logger log = LoggerFactory.getLogger(ChatUseCaseImpl.class);

    private final LanguageModelPort       languageModelPort;
    private final ConversationRepository  conversationRepository;
    private final ConversationDomainService domainService;

    public ChatUseCaseImpl(
            LanguageModelPort languageModelPort,
            ConversationRepository conversationRepository,
            ConversationDomainService domainService) {
        this.languageModelPort      = languageModelPort;
        this.conversationRepository = conversationRepository;
        this.domainService          = domainService;
    }

    @Override
    public ChatResult chat(ChatCommand command) {
        log.info("Chat request [conversationId={}]",
                command.hasConversationId() ? command.conversationId() : "new");

        // 1. Load existing or create new — NEVER silently create on unknown ID
        Conversation conversation = resolveConversation(command);

        // 2. Validate BEFORE mutation — critical ordering
        domainService.validateReadyForUserMessage(conversation);

        // 3. Record user message
        conversation.addMessage(ChatMessage.userMessage(command.userMessage()));

        // 4. Call AI provider
        String aiReply = generateWithErrorTranslation(conversation);

        // 5. Record assistant reply
        conversation.addMessage(ChatMessage.assistantMessage(aiReply));

        // 6. Persist
        conversationRepository.save(conversation);

        log.info("Chat complete [conversationId={}, messages={}]",
                conversation.getId().value(), conversation.getMessages().size());

        return new ChatResult(
                conversation.getId().value(),
                aiReply,
                conversation.getMessages().size()
        );
    }

    private Conversation resolveConversation(ChatCommand command) {
        if (!command.hasConversationId()) {
            Conversation fresh = new Conversation(ConversationId.generate());
            log.debug("New conversation [id={}]", fresh.getId().value());
            return fresh;
        }
        return conversationRepository
                .findById(ConversationId.of(command.conversationId()))
                .orElseThrow(() -> new ConversationNotFoundException(command.conversationId()));
    }

    private String generateWithErrorTranslation(Conversation conversation) {
        try {
            return languageModelPort.generate(conversation);
        } catch (LanguageModelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Language model port failed [conversationId={}]",
                    conversation.getId().value(), e);
            throw new LanguageModelException("Failed to generate AI response", e);
        }
    }
}