# ── Stage 1: Build ─────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

# Copy wrapper first — cached layer, only invalidated when wrapper changes
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# chmod required: Docker COPY does not guarantee execute permission
RUN chmod +x mvnw

# Download deps layer — only re-runs when pom.xml changes
RUN ./mvnw dependency:go-offline -q

# Build — re-runs on source changes only
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Non-root: production security baseline
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8890
ENTRYPOINT ["java", "-jar", "app.jar"]