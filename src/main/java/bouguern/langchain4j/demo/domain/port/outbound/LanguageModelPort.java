package bouguern.langchain4j.demo.domain.port.outbound;

import bouguern.langchain4j.demo.domain.model.Conversation;

public interface LanguageModelPort {
    String generate(Conversation conversation);
}