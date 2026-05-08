package bouguern.langchain4j.demo.infrastructure.ai;

import bouguern.langchain4j.demo.domain.exception.LanguageModelException;
import bouguern.langchain4j.demo.domain.model.Conversation;
import bouguern.langchain4j.demo.domain.model.MessageRole;
import bouguern.langchain4j.demo.domain.port.outbound.LanguageModelPort;
import bouguern.langchain4j.demo.domain.service.ConversationDomainService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Naming collision note:
 *   dev.langchain4j.data.message.ChatMessage  → imported as ChatMessage (short name)
 *   com.yourname.aichat.domain.model.ChatMessage → always fully qualified
 */
@Component
public class OllamaAdapter implements LanguageModelPort {

    private static final Logger log = LoggerFactory.getLogger(OllamaAdapter.class);

    private final ChatLanguageModel       chatLanguageModel;
    private final ConversationDomainService domainService;
    private final Timer                   llmTimer;
    private final String                  modelName;

    public OllamaAdapter(
            ChatLanguageModel chatLanguageModel,
            ConversationDomainService domainService,
            MeterRegistry meterRegistry,
            @Qualifier("ollamaModelName") String modelName) {

        this.chatLanguageModel = chatLanguageModel;
        this.domainService     = domainService;
        this.modelName         = modelName;

        // Built ONCE — not per-request. Avoids allocation churn under load.
        this.llmTimer = Timer.builder("ai.llm.request.duration")
                .description("LLM generation request latency")
                .tag("model", modelName)
                .register(meterRegistry);
    }

    @Override
    public String generate(Conversation conversation) {
        List<bouguern.langchain4j.demo.domain.model.ChatMessage> context =
                domainService.getContextWindow(conversation);

        List<ChatMessage> lc4jMessages = buildMessages(context);

        log.debug("Sending {} messages to Ollama [conversationId={}, model={}]",
                lc4jMessages.size(), conversation.getId().value(), modelName);

        Timer.Sample sample = Timer.start();
        try {
            Response<AiMessage> response = chatLanguageModel.generate(lc4jMessages);
            String text = response.content().text();

            if (text == null || text.isBlank()) {
                throw new LanguageModelException(
                        "Model returned empty response [conversationId="
                        + conversation.getId().value() + "]");
            }

            log.debug("Response [length={}, conversationId={}]",
                    text.length(), conversation.getId().value());
            return text;

        } catch (LanguageModelException e) {
            throw e;
        } catch (Exception e) {
            throw new LanguageModelException(
                    "Ollama call failed [conversationId=" + conversation.getId().value() + "]", e);
        } finally {
            // Always record — failed calls are often the slowest
            sample.stop(llmTimer);
        }
    }

    private List<ChatMessage> buildMessages(
            List<bouguern.langchain4j.demo.domain.model.ChatMessage> domainMessages) {

        List<ChatMessage> messages = new ArrayList<>(domainMessages.size() + 1);

        boolean hasSystemMessage = !domainMessages.isEmpty()
                && domainMessages.get(0).role() == MessageRole.SYSTEM;

        if (!hasSystemMessage) {
            messages.add(SystemMessage.from(OllamaPromptConstants.SYSTEM_PROMPT));
        }

        for (bouguern.langchain4j.demo.domain.model.ChatMessage msg : domainMessages) {
            messages.add(toLC4J(msg));
        }

        return messages;
    }

    // Exhaustive switch — compile error if MessageRole gains a new variant
    private ChatMessage toLC4J(bouguern.langchain4j.demo.domain.model.ChatMessage msg) {
        return switch (msg.role()) {
            case USER      -> UserMessage.from(msg.content());
            case ASSISTANT -> AiMessage.from(msg.content());
            case SYSTEM    -> SystemMessage.from(msg.content());
        };
    }
}