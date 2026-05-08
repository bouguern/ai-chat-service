package bouguern.langchain4j.demo.domain.service;

import bouguern.langchain4j.demo.domain.exception.InvalidConversationStateException;
import bouguern.langchain4j.demo.domain.model.ChatMessage;
import bouguern.langchain4j.demo.domain.model.Conversation;
import bouguern.langchain4j.demo.domain.model.MessageRole;

import java.util.ArrayList;
import java.util.List;

/**
 * Pure Java — zero framework imports.
 * Wired as a Spring bean by DomainConfig in the infrastructure layer.
 */
public class ConversationDomainService {

    private final int maxHistorySize;

    public ConversationDomainService(int maxHistorySize) {
        if (maxHistorySize < 1) {
            throw new IllegalArgumentException("maxHistorySize must be at least 1");
        }
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Returns the trimmed context window.
     * Always preserves a leading SYSTEM message.
     * Returns a defensive copy — callers may mutate the list freely.
     */
    public List<ChatMessage> getContextWindow(Conversation conversation) {
        List<ChatMessage> all = conversation.getMessages();

        if (all.size() <= maxHistorySize) {
            return new ArrayList<>(all);
        }

        boolean hasSystemHeader =
                !all.isEmpty() && all.get(0).role() == MessageRole.SYSTEM;

        List<ChatMessage> window = new ArrayList<>();

        if (hasSystemHeader) {
            window.add(all.get(0));
            int from = all.size() - (maxHistorySize - 1);
            window.addAll(all.subList(from, all.size()));
        } else {
            int from = all.size() - maxHistorySize;
            window.addAll(all.subList(from, all.size()));
        }

        return window;
    }

    /**
     * MUST be called BEFORE addMessage(userMessage).
     * Throws if last message is already USER — prevents unanswered double sends.
     */
    public void validateReadyForUserMessage(Conversation conversation) {
        List<ChatMessage> messages = conversation.getMessages();

        if (messages.isEmpty()) {
            return;
        }

        ChatMessage last = messages.get(messages.size() - 1);
        if (last.role() == MessageRole.USER) {
            throw new InvalidConversationStateException(
                    "Cannot send: previous user message in conversation ["
                    + conversation.getId().value() + "] has not been answered yet.");
        }
    }
}