# Snippet Manager

Kotlin / Spring Boot backend for managing code snippets in the **Snippet Searcher** university team project at Universidad Austral.

This service coordinates snippet metadata, access permissions, source assets and PrintScript operations. It provides a concrete example of a backend working across relational storage, HTTP services and asynchronous messaging.

## Responsibilities

- Create, retrieve, edit and delete snippets, with paginated user listings.
- Delegate access checks to the permission service and source storage to the asset service.
- Coordinate execution and tests through the PrintScript service.
- Store per-user formatting and linting configuration.
- Publish formatting and linting requests through Redis Streams and track lint compliance.
- Validate OAuth2 JWT issuer/audience and attach correlation IDs to operational logs.

## Architecture

```text
Client → Spring REST controllers → Application services → PostgreSQL
                                       ├── HTTP → Permission Manager
                                       ├── HTTP → Asset Service / Azurite
                                       ├── HTTP → PrintScript Service
                                       └── Redis Streams → format / lint processing
```

**Stack:** Kotlin, Spring Boot 3.3, Spring Data JPA, PostgreSQL, Spring Security OAuth2, Redis Streams, Gradle, Docker Compose and GitHub Actions. Tests use Spring Boot Test, JUnit 5, Mockito Kotlin and H2.

## Local setup

Requires **JDK 21**, Docker with Compose, and access to the project's Auth0 configuration and GitHub Packages dependency.

1. Clone the repository and prepare local configuration:

   ```bash
   git clone https://github.com/IngSis15/snippet-manager.git
   cd snippet-manager
   cp .env.example .env
   ```

2. Fill in database and Auth0 values. Gradle resolves `org.austral.ingsis:redis-streams-mvc` from GitHub Packages using `GITHUB_USERNAME` and `GITHUB_TOKEN`; configure them in your environment with permission to read that package.

3. Start the supporting services:

   ```bash
   docker compose up -d
   ```

   Compose starts PostgreSQL, the permission database/service, the asset service with Azurite, the PrintScript service and Redis. **It does not start Snippet Manager itself.** Registry access may be required for the service images.

4. Export the required values into the application process, then start it:

   ```bash
   ./gradlew bootRun
   ```

   Docker Compose reads `.env` for its containers. Spring Boot does not automatically load that file into a host-side `bootRun` process; export the variables or use your IDE's environment configuration.

### Configuration reference

See [application.yaml](src/main/resources/application.yaml) for the authoritative names and defaults.

| Variables | Purpose |
| --- | --- |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Snippet metadata database |
| `PM_DB_NAME`, `PM_DB_USERNAME`, `PM_DB_PASSWORD` | Permission database in Compose |
| `AUTH_SERVER_URI`, `AUTH0_AUDIENCE`, `AUTH_CLIENT_ID`, `AUTH_CLIENT_SECRET` | Application Auth0 configuration |
| `AUTH0_AUDIENCE_API`, `AUTH_CLIENT_ID_API`, `AUTH_CLIENT_SECRET_API` | Additional permission-service configuration in Compose |
| `PRINTSCRIPT_HOST`, `ASSET_HOST`, `PERMISSION_HOST` | Downstream service URLs |
| `REDIS_HOST`, `REDIS_PORT` | Redis connection |
| `SERVER_PORT` | HTTP port; defaults to 8080 |

The example file currently uses `PRINTSCRIPT_URL` and `ASSET_URL`; the application reads **`PRINTSCRIPT_HOST` and `ASSET_HOST`**. Use the latter for overrides. Defaults target the exposed local service ports.

## API and checks

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html).
- OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).
- Health endpoint: `GET /health`.
- Most application routes require a valid bearer JWT; consult Swagger for request/response contracts.

```bash
./gradlew test
./gradlew ktlintCheck
./gradlew koverVerify
./gradlew build
```

Tests use an H2-backed test configuration and mocked downstream URLs. Kover has an 80% minimum for included code, with exclusions defined in [build.gradle](build.gradle); this is a configured threshold, not a current coverage result. The build also installs a pre-commit hook.

## Reading the code

Start with [SnippetService](src/main/kotlin/edu/ingsis/snippetmanager/snippet/SnippetService.kt), the [HTTP integrations](src/main/kotlin/edu/ingsis/snippetmanager/external), and the [format](src/main/kotlin/edu/ingsis/snippetmanager/format) / [lint](src/main/kotlin/edu/ingsis/snippetmanager/lint) producers. Correlation-ID and request logging filters live under [server](src/main/kotlin/edu/ingsis/snippetmanager/server).

## Project context

This is a collaborative academic service, not a standalone hosted product. Running the complete system requires external services, registry access and authentication configuration. See the [contributors](https://github.com/IngSis15/snippet-manager/graphs/contributors) and commit history for attribution.

