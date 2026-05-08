# AI Chat Service — Production-Grade LLM Integration in Java

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![LangChain4j](https://img.shields.io/badge/LangChain4j-0.31.0-blue?style=flat-square)](https://github.com/langchain4j/langchain4j)
[![Ollama](https://img.shields.io/badge/Ollama-local%20LLM-black?style=flat-square)](https://ollama.com)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)](LICENSE)
[![Tests](https://img.shields.io/badge/Tests-31%20passing-success?style=flat-square)]()

> Read the full article on Medium → [Medium Article](https://medium.com/@bouguern.mohamed/designing-a-production-ready-ai-chat-service-with-spring-boot-langchain4j-ollama-and-hexagonal-d5cf11d61bb8)

---

## What this is

Most Java + AI tutorials show you how to call an LLM and print the result. This is not that.

This is a **stateful conversational AI service** built the way you would build it at a company — with strict architectural boundaries, multi-turn memory, observability, graceful error handling, and code structured to survive a provider change without a rewrite.

**The core question this project answers:** where does `dev.langchain4j` get imported?

Answer: only in `infrastructure.ai`. The domain layer has zero framework imports. The application layer has zero infrastructure knowledge. Swapping Ollama for OpenAI means writing one new adapter class. Every test, every domain rule, every use case stays untouched.

---

## Features

- **Stateful multi-turn conversations** — the AI remembers everything said in the session
- **Hexagonal architecture** — domain, application, infrastructure, and API layers with strict dependency rules
- **Provider-agnostic** — `LanguageModelPort` abstracts the LLM; swap providers without touching business logic
- **Runs 100% locally** — Ollama in Docker, no API keys, no data leaving your machine
- **RFC 7807 error responses** — structured `ProblemDetail` on every error, not raw stack traces
- **LLM latency metrics** — Micrometer timer exposed at `/actuator/prometheus`
- **Context window management** — configurable sliding window keeps token usage bounded
- **31 unit tests** — pure JUnit 5 + Mockito, zero Spring context loaded in tests

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  API Layer          ChatController                  │
│                     GlobalExceptionHandler          │
│                     ChatRequest · ChatResponse      │
└──────────────────────────┬──────────────────────────┘
                           │ calls inbound port
┌──────────────────────────▼──────────────────────────┐
│  Application Layer  ChatUseCaseImpl                 │
│                     Orchestration only — no logic   │
└──────────────────────────┬──────────────────────────┘
                           │ calls outbound ports
┌──────────────────────────▼──────────────────────────┐
│  Domain Layer       Conversation · ChatMessage      │  ← pure Java
│  (zero framework)   ConversationDomainService       │     no Spring
│                     ChatUseCase · LanguageModelPort │     no LangChain4j
│                     ConversationRepository          │
└──────────────────────────┬──────────────────────────┘
                           │ implemented by
┌──────────────────────────▼──────────────────────────┐
│  Infrastructure     OllamaAdapter (LangChain4j)     │
│                     InMemoryConversationRepository  │
│                     LangChain4jConfig · DomainConfig│
└──────────────────────────┬──────────────────────────┘
                           │ HTTP :11434
                    Ollama (Docker)
                    llama3.2:1b · local LLM
```

**Dependency rule:** every arrow points inward. Infrastructure knows about the domain. The domain knows about nothing.

---

## Tech Stack

| Concern | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.3.0 |
| AI Library | LangChain4j | 0.31.0 |
| LLM Runtime | Ollama | latest |
| LLM Model | llama3.2:1b | — |
| Metrics | Micrometer + Prometheus | — |
| Containerisation | Docker + Compose | — |
| Build | Maven | 3.9+ |
| Testing | JUnit 5 + Mockito + AssertJ | — |

---

## Project Structure

```
src/main/java/bouguern/langchain4j/demo/
│
├── AiChatServiceApplication.java
│
├── domain/                          # Pure Java — zero framework imports
│   ├── model/
│   │   ├── Conversation.java        # Aggregate root
│   │   ├── ChatMessage.java         # Value object (record)
│   │   ├── ConversationId.java      # Value object — no primitive obsession
│   │   ├── MessageRole.java         # Enum: USER · ASSISTANT · SYSTEM
│   │   ├── ChatCommand.java         # Inbound domain command
│   │   └── ChatResult.java          # Outbound domain result
│   ├── port/
│   │   ├── inbound/
│   │   │   └── ChatUseCase.java     # Inbound port (interface)
│   │   └── outbound/
│   │       ├── LanguageModelPort.java     # AI provider contract
│   │       └── ConversationRepository.java # Persistence contract
│   ├── service/
│   │   └── ConversationDomainService.java # Context window · validation
│   └── exception/
│       ├── ConversationNotFoundException.java
│       ├── InvalidConversationStateException.java
│       └── LanguageModelException.java
│
├── application/
│   └── usecase/
│       └── ChatUseCaseImpl.java     # Orchestration: resolve→validate→generate→save
│
├── infrastructure/
│   ├── ai/
│   │   ├── OllamaAdapter.java       # LangChain4j implementation of LanguageModelPort
│   │   └── OllamaPromptConstants.java
│   ├── persistence/
│   │   └── InMemoryConversationRepository.java  # Replaced by Redis in Day 5
│   └── config/
│       ├── LangChain4jConfig.java   # OllamaChatModel bean wiring
│       └── DomainConfig.java        # Wires pure-Java domain services
│
└── api/
    ├── dto/
    │   ├── ChatRequest.java         # Inbound HTTP body (record)
    │   └── ChatResponse.java        # Outbound HTTP body (record)
    └── rest/
        ├── ChatController.java      # Thin — maps DTOs, delegates to use case
        └── GlobalExceptionHandler.java  # RFC 7807 ProblemDetail

src/test/java/bouguern/langchain4j/demo/
├── domain/
│   ├── model/ConversationTest.java
│   └── service/ConversationDomainServiceTest.java
├── application/
│   └── usecase/ChatUseCaseImplTest.java
└── infrastructure/
    ├── ai/OllamaAdapterTest.java
    └── persistence/InMemoryConversationRepositoryTest.java
```

---

## Prerequisites

| Tool | Version | Check |
|---|---|---|
| Java JDK | 21+ | `java -version` |
| Maven | 3.9+ | `./mvnw -version` |
| Docker Desktop | latest | `docker info` |
| Git | any | `git --version` |

> **Windows users:** ensure Docker Desktop is running before any `docker` command. Look for the whale icon in the system tray.

---

## Quick Start

### 1. Clone

```bash
git clone https://github.com/bouguern/ai-chat-service.git
cd ai-chat-service
```

### 2. Start Ollama

```bash
docker compose up ollama -d
```

Wait for the container to be healthy (15–20 seconds), then pull the model:

```bash
docker exec -it ollama ollama pull llama3.2:1b
```

> **First run only.** The model (~600MB) downloads once and is cached in the `ollama_data` Docker volume. All subsequent starts are instant.

### 3. Run the application

```bash
./mvnw spring-boot:run
```

You should see:

```
Started AiChatServiceApplication on port 8890
```

### 4. Send your first message

```bash
curl -s -X POST http://localhost:8890/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "What is hexagonal architecture?"}' | jq .
```

```json
{
  "conversationId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "message": "Hexagonal architecture, also known as Ports and Adapters...",
  "messageCount": 2
}
```

### 5. Continue the conversation

```bash
curl -s -X POST http://localhost:8890/api/v1/chat \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "message": "Give me a Java code example"
  }' | jq .
```

The AI remembers the previous question. History is maintained server-side.

---

## API Reference

### `POST /api/v1/chat`

Start a new conversation or continue an existing one.

**Request body**

```json
{
  "conversationId": "optional — omit to start a new conversation",
  "message": "Your message (required, max 4000 characters)"
}
```

**Response — 200 OK**

```json
{
  "conversationId": "f47ac10b-...",
  "message": "AI response text",
  "messageCount": 2
}
```

**Error responses (RFC 7807)**

| HTTP Status | type URI | When |
|---|---|---|
| `400 Bad Request` | `/errors/validation-failure` | Blank message or over 4000 chars |
| `404 Not Found` | `/errors/conversation-not-found` | Unknown `conversationId` |
| `409 Conflict` | `/errors/invalid-conversation-state` | Double user message without reply |
| `503 Service Unavailable` | `/errors/language-model-unavailable` | Ollama down or model failure |

**Error response shape (all errors)**

```json
{
  "type": "/errors/conversation-not-found",
  "title": "Conversation Not Found",
  "status": 404,
  "detail": "Conversation not found: f47ac10b-..."
}
```

---

## Configuration

All configuration lives in `src/main/resources/application.yml`.

```yaml
app:
  ai:
    ollama:
      base-url: http://localhost:11434   # Override in Docker: http://ollama:11434
      model-name: llama3.2:1b            # Model must be pulled first
      num-predict: 1024                  # Max tokens generated per response
      temperature: 0.7                   # 0.0 = deterministic, 1.0 = creative
      timeout-seconds: 300               # CPU inference can be slow — be generous

  conversation:
    max-history-size: 20                 # Sliding window sent to model per request
```

### Environment variable overrides

Spring Boot maps `APP_AI_OLLAMA_BASE_URL` → `app.ai.ollama.base-url` automatically.

```bash
# Override base URL for Docker Compose networking
APP_AI_OLLAMA_BASE_URL=http://ollama:11434 ./mvnw spring-boot:run
```

---

## Running Tests

```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=ChatUseCaseImplTest

# With coverage report
./mvnw test jacoco:report
# Report at: target/site/jacoco/index.html
```

Expected output:

```
[INFO] Tests run: 31, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Test coverage by layer:**

| Layer | Test Class | Tests |
|---|---|---|
| Domain model | `ConversationTest` | 5 |
| Domain service | `ConversationDomainServiceTest` | 7 |
| Application | `ChatUseCaseImplTest` | 6 |
| Infrastructure AI | `OllamaAdapterTest` | 8 |
| Infrastructure persistence | `InMemoryConversationRepositoryTest` | 5 |

All tests are pure unit tests — no Spring context, no Docker, no network. They run in under 3 seconds.

---

## Observability

### Health check

```bash
curl http://localhost:8890/actuator/health
```

```json
{ "status": "UP" }
```

### LLM latency metric

```bash
curl -s http://localhost:8890/actuator/prometheus | grep ai_llm
```

```
ai_llm_request_duration_seconds_count{model="llama3.2:1b"} 3.0
ai_llm_request_duration_seconds_sum{model="llama3.2:1b"}   45.2
```

This gives you average latency per request, p99, and failure rate when connected to Prometheus + Grafana.

### All exposed endpoints

| Endpoint | Description |
|---|---|
| `GET /actuator/health` | Service health status |
| `GET /actuator/metrics` | All registered metrics |
| `GET /actuator/prometheus` | Prometheus scrape endpoint |
| `GET /actuator/info` | Application info |

---

## Docker — Full Stack

To run everything in containers (app + Ollama):

```bash
docker compose up --build
```

This builds the app image, starts Ollama, pulls the model, and starts the service. The app is available at `http://localhost:8890`.

> **First run:** the model pull (~600MB) happens automatically via the `ollama-pull` service. Watch progress with `docker compose logs -f ollama-pull`.

```bash
# Stop everything
docker compose down

# Stop and remove volumes (deletes downloaded model)
docker compose down -v
```

---

## Key Design Decisions

### Why hexagonal architecture?

The domain layer has zero LangChain4j imports. If LangChain4j releases a breaking change (it has, multiple times), the blast radius is one adapter class. Every domain rule, every test, every use case is untouched.

### Why `ChatCommand` and `ChatResult` instead of using DTOs directly?

`ChatUseCase` is a domain port. If it accepted `ChatRequest` (an `api.dto` class), the domain would import from the API layer — a dependency pointing the wrong direction. `ChatCommand` and `ChatResult` are the domain's own input/output contracts. The controller maps DTOs → commands on the way in, and results → DTOs on the way out.

### Why not use LangChain4j's `AiServices`?

`AiServices` with `@AiService` / `@SystemMessage` is clean for stateless Q&A. For a stateful service where we manage conversation history ourselves, it bypasses our `ConversationRepository` port entirely and moves context window management into LangChain4j internals — breaking the hexagonal boundary. We use `ChatLanguageModel.generate(List<ChatMessage>)` directly. We own state.

### Why is `InMemoryConversationRepository` not a temporary hack?

It is — intentionally. The known limitations (no persistence on restart, single JVM) are documented in the class. Day 5 of this series replaces it with `RedisConversationRepository`. Zero domain changes required. That absence of upstream change is the proof the port abstraction works.

### Why constructor injection everywhere?

Field injection (`@Autowired`) produces objects that can be partially initialised — a subtle category of runtime bugs. Constructor injection forces all dependencies to be present at construction time, making the object graph explicit and every class trivially testable without a Spring context.

---

## Contributing

This repository accompanies a blog series — the code intentionally evolves from article to article. Found a bug or want to suggest an improvement?

1. Fork the repository
2. Create a branch: `git checkout -b fix/your-fix-name`
3. Commit your changes with a clear message
4. Open a Pull Request with a description of what and why

---

## Author

**Mohamed Bouguern** — Java and AI Engineer

Building production Java + AI systems and writing about the patterns that actually matter in enterprise environments.

- [Medium](https://medium.com/@bouguern.mohamed)
- [LinkedIn](https://www.linkedin.com/in/mohamed-bouguern/)

---

*Currently open to Java / AI engineering roles. If you're building systems where these patterns matter, feel free to reach out.*

---

## License

MIT — see [LICENSE](LICENSE) for details.
