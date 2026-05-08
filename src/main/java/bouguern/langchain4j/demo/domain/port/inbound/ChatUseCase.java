package bouguern.langchain4j.demo.domain.port.inbound;

import bouguern.langchain4j.demo.domain.model.ChatCommand;
import bouguern.langchain4j.demo.domain.model.ChatResult;

public interface ChatUseCase {
    ChatResult chat(ChatCommand command);
}