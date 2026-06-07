# Coding Standards — Doctor Assistant

Production coding conventions for the Super Clinic Doctor Assistant backend.

---

## 1. General Principles

| Principle | Guideline |
|---|---|
| **Minimal scope** | One class, one responsibility. Avoid god services. |
| **Immutability** | Prefer `record` for DTOs and value objects. |
| **Explicit over magic** | No field injection; constructor injection only. |
| **Fail fast** | Validate at boundaries (controller DTOs). Domain rules in services. |
| **No PHI in logs** | Never log patient names, symptoms, or appointment details at INFO+. |

---

## 2. Package Structure

```
com.superclinic.doctorassistant
├── config/              # @Configuration, @ConfigurationProperties, beans
├── api/                 # REST layer (inbound)
│   ├── controller/
│   ├── dto/             # Request/response records
│   ├── mapper/          # MapStruct interfaces
│   └── advice/          # @ControllerAdvice, exception handlers
├── domain/              # Business logic (framework-agnostic)
│   ├── appointment/
│   ├── doctor/
│   ├── availability/
│   └── conversation/
├── ai/                  # Spring AI integration
│   ├── config/
│   ├── memory/
│   ├── rag/
│   ├── tools/
│   └── prompt/
├── integration/         # Outbound adapters (MCP, notifications, external APIs)
├── persistence/         # JPA entities, repositories, converters
└── common/              # Shared exceptions, constants, utilities
```

### Dependency rules (strict)

```
api  →  domain  →  persistence
ai   →  domain
integration  →  domain
```

- **Never** import `api` or `persistence` from `domain`.
- **Never** put JPA annotations in `domain` — map in `persistence.entity`.
- **Controllers** delegate to domain services; no business logic in controllers.

---

## 3. Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Classes | PascalCase, noun | `AppointmentService` |
| Interfaces | PascalCase, no `I` prefix | `DoctorRepository` |
| Methods | camelCase, verb-first | `findAvailableSlots()` |
| Constants | UPPER_SNAKE_CASE | `MAX_SLOT_HOLD_MINUTES` |
| Packages | lowercase, singular domain | `domain.appointment` |
| REST paths | kebab-case, versioned | `/api/v1/chat-sessions` |
| DB tables | snake_case, plural | `availability_slots` |
| Flyway scripts | `V{n}__{description}.sql` | `V2__create_doctors.sql` |
| Env vars | UPPER_SNAKE_CASE | `OPENAI_API_KEY` |
| Config props | kebab-case in YAML | `doctor-assistant.rag.top-k` |

---

## 4. Layer Responsibilities

### Controllers (`api.controller`)

- Annotate with `@RestController`, `@RequestMapping("/api/v1/...")`.
- Accept/return DTOs only — never JPA entities.
- Use `@Valid` on request bodies.
- Return `ResponseEntity<T>` for non-200 or custom headers.
- Use SSE (`produces = TEXT_EVENT_STREAM_VALUE`) for streaming chat.

### DTOs (`api.dto`)

- Use Java `record` with Bean Validation annotations.
- Separate request and response types: `BookAppointmentRequest`, `AppointmentResponse`.
- No Lombok on records (unnecessary).

### Domain services (`domain.*`)

- Annotate with `@Service`, `@Transactional` at class or method level.
- Throw domain exceptions (`AppointmentConflictException`), not `ResponseStatusException`.
- Accept/return domain models or primitives — not DTOs or entities.

### Repositories (`persistence.repository`)

- Spring Data JPA interfaces extending `JpaRepository`.
- Custom queries via `@Query` (JPQL) or method naming — prefer JPQL for readability.
- Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` for slot booking paths.

### AI tools (`ai.tools`)

- One class per tool, annotated with Spring AI `@Tool` or registered as `FunctionCallback`.
- Tools delegate to domain services — no direct repository access.
- Return structured JSON-friendly objects, not raw strings.

---

## 5. Configuration

- Externalize all secrets via environment variables (see `.env.example`).
- Bind custom properties with `@ConfigurationProperties(prefix = "doctor-assistant")` on a `@Configuration` record/class.
- Use profile-specific YAML: `application-dev.yml`, `application-prod.yml`.
- Disable Swagger in production via `SWAGGER_ENABLED=false`.

---

## 6. Validation

- **Request DTOs**: Jakarta Validation (`@NotBlank`, `@NotNull`, `@Size`, `@Future`).
- **Custom constraints**: Place validators in `common.validation`.
- **Domain validation**: Business rules (slot conflicts, doctor inactive) in domain services.
- Enable `@Validated` on controllers when using constraint groups.

---

## 7. Exception Handling

- Define hierarchy under `common.exception`:
  - `DoctorAssistantException` (base, runtime)
  - `ResourceNotFoundException`
  - `ConflictException`
  - `ValidationException`
- Single `@RestControllerAdvice` in `api.advice` maps exceptions to RFC 7807 `ProblemDetail`.
- Never expose stack traces or internal messages in production.

---

## 8. Logging & Observability

- Use SLF4J (`@Slf4j`) — never `System.out`.
- Structured JSON logging in production (see `logback-spring.xml`).
- Propagate trace IDs via MDC (`traceId`, `spanId`).
- Log at boundaries: controller entry (DEBUG), service outcomes (INFO), failures (WARN/ERROR).
- Micrometer metrics for: chat requests, tool invocations, appointment bookings, RAG latency.

---

## 9. Database & Migrations

- **Flyway only** — never `ddl-auto: create` in any environment.
- All schema changes via versioned SQL in `db/migration/`.
- Enable pgvector extension in `V1__enable_extensions.sql`.
- Use UUID primary keys (`gen_random_uuid()`).
- Timestamps as `TIMESTAMPTZ`, always UTC.
- Index every FK column and frequently filtered columns.

---

## 10. Spring AI Conventions

- Centralize prompt templates in `ai.prompt` (classpath resources or constants).
- Chat memory keyed by `conversationSessionId`.
- RAG retrieval wrapped in a dedicated `RagRetriever` service — not inline in controllers.
- Tool definitions must include clear descriptions for the LLM (name, description, parameter schema).
- Set low temperature (0.2–0.4) for factual clinic responses.
- Always append medical disclaimer for symptom-to-specialty recommendations.

---

## 11. Testing Standards

| Layer | Approach |
|---|---|
| Unit | JUnit 5 + Mockito; test domain services in isolation |
| Integration | `@SpringBootTest` + Testcontainers PostgreSQL |
| API | `@WebMvcTest` for controllers; MockMvc |
| AI | Mock `ChatClient` / `VectorStore` in unit tests; integration tests opt-in |

- Test profile: `application-test.yml` with Testcontainers JDBC URL.
- Naming: `{ClassName}Test`, `{ClassName}IntegrationTest`.
- Use `@DisplayName` for readable test descriptions.

---

## 12. Git & Code Review Checklist

- [ ] No secrets or API keys in source
- [ ] Flyway migration included for schema changes
- [ ] DTOs validated; entities not exposed via REST
- [ ] Domain logic not in controllers
- [ ] No PHI in log statements
- [ ] Actuator health unaffected by new dependencies
- [ ] OpenAPI annotations on new endpoints

---

## 13. Recommended Tooling

| Tool | Purpose |
|---|---|
| **MapStruct** | DTO ↔ entity mapping (compile-time, no reflection) |
| **springdoc-openapi** | API documentation |
| **Flyway** | Schema migrations |
| **Testcontainers** | Integration tests with real PostgreSQL |
| **Spotless** *(optional)* | Code formatting enforcement in CI |
| **Error Prone / NullAway** *(optional)* | Static null-safety analysis |

---

## 14. API Versioning

- URI versioning: `/api/v1/...`
- Breaking changes require `/api/v2/...`; maintain v1 until clients migrate.
- Deprecation signalled via `Sunset` response header and release notes.
