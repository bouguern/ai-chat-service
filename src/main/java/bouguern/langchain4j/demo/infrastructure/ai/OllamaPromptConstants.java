package bouguern.langchain4j.demo.infrastructure.ai;

public final class OllamaPromptConstants {

    private OllamaPromptConstants() {}

    public static final String SYSTEM_PROMPT = """
            You are a helpful, concise, and professional AI assistant.
            Respond clearly and directly. If you are uncertain, say so.
            Do not invent facts. Keep responses focused and well-structured.
            """;
}