package bouguern.langchain4j.demo.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import bouguern.langchain4j.demo.domain.service.ConversationDomainService;

/**
 * Wires pure-Java domain services as Spring beans.
 * ConversationDomainService has zero Spring annotations — this class
 * is the bridge between Spring's config system and the domain.
 */
@Configuration
public class DomainConfig {

    @Bean
    ConversationDomainService conversationDomainService(
            @Value("${app.conversation.max-history-size}") int maxHistorySize) {
        return new ConversationDomainService(maxHistorySize);
    }
}