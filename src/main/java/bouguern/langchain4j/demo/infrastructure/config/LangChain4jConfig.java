package bouguern.langchain4j.demo.infrastructure.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LangChain4jConfig {

    private final String ollamaBaseUrl;
    private final String modelName;
    private final int    numPredict;    // Ollama's num_predict param — NOT maxTokens
    private final double temperature;
    private final int    timeoutSeconds;

    public LangChain4jConfig(
            @Value("${app.ai.ollama.base-url}")        String ollamaBaseUrl,
            @Value("${app.ai.ollama.model-name}")      String modelName,
            @Value("${app.ai.ollama.num-predict}")     int numPredict,
            @Value("${app.ai.ollama.temperature}")     double temperature,
            @Value("${app.ai.ollama.timeout-seconds}") int timeoutSeconds) {
        this.ollamaBaseUrl  = ollamaBaseUrl;
        this.modelName      = modelName;
        this.numPredict     = numPredict;
        this.temperature    = temperature;
        this.timeoutSeconds = timeoutSeconds;
    }

    @Bean
    ChatLanguageModel chatLanguageModel() {
        return OllamaChatModel.builder()
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .numPredict(numPredict)   // correct method for LangChain4j 0.31.0
                .temperature(temperature)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    /**
     * Named String bean — consumed by OllamaAdapter via @Qualifier.
     * Avoids @Value in the adapter and keeps it unit-testable without Spring.
     */
    @Bean(name = "ollamaModelName")
    String ollamaModelName() {
        return modelName;
    }
}