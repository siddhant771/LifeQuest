# LifeQuest Backend — deployment, testing and interview guide

## Architecture
HTTP -> Spring Security JWT filter -> Controller -> DTO validation -> Service -> Repository/JPA -> PostgreSQL. Redis caches weekly leaderboard reads. Flyway versions the schema. Actuator exposes health/info.

Controller owns HTTP mapping and principal extraction. Service owns business rules and transaction boundaries. Repository owns persistence queries and locking. Entity is persisted state. DTO is the external contract. Security filter verifies tokens and establishes identity.

## Authentication and JWT
Registration normalizes email, validates input and hashes the password with BCrypt. Login verifies the hash and issues a signed JWT with user ID subject and expiry. Protected requests send Authorization: Bearer token. The filter verifies signature/expiry and sets SecurityContext. Controller must derive user ID from Authentication, never trust a body-supplied user ID. JWT is signed, not encrypted. Never put secrets in claims. Use TLS and short-lived access tokens. A refresh-token design needs secure storage, rotation and revocation.

## Quest completion correctness
Completion should atomically lock a quest owned by the authenticated user, reject an already completed quest, calculate XP on server, update quest and user, update streak/achievements, insert a completion event, then commit. A transaction rolls back all database writes if a step fails. Pessimistic locking prevents concurrent transactions both seeing an incomplete quest. A unique DB constraint remains the final integrity boundary. Add a unique client idempotency key and handle duplicate-key conflicts as a stable response. Test real PostgreSQL locking; H2 is not a perfect substitute.

## Mockito vs integration tests
Mockito unit tests isolate service logic: arrange mocks, call service, assert result/state, verify interactions. They do not prove SQL, Flyway, JPA mappings or lock behaviour. Add Spring integration tests with PostgreSQL Testcontainers for migrations, constraints and concurrency. Submit two completion requests concurrently and assert one event and one XP award.

Commands:
~~~bash
./mvnw test
./mvnw -Dtest=QuestServiceTest test
./mvnw -Dtest=QuestServiceTest#duplicateCompletionIsRejectedWithoutMoreXp test
./mvnw clean verify
~~~

## Docker local deployment
Requirements: Docker Engine and Compose plugin. Copy .env.example to .env; generate unique local secrets; never commit .env. Build JAR from lifequest-backend using ./mvnw -B clean package -DskipTests. From repository root run docker compose up --build -d. Inspect docker compose ps and docker compose logs -f api. Check curl http://localhost:8080/actuator/health. Stop with docker compose down. The named database volume persists. docker compose down -v deletes local persisted data.

Compose binds API to localhost only. Put a TLS reverse proxy/load balancer in front before remote access. Never expose Postgres/Redis publicly.

## Environment variables
DB_URL is JDBC URL; in Compose use hostname postgres, not localhost. DB_USERNAME and DB_PASSWORD are database credentials. JWT_SECRET must be high entropy and at least 32 bytes. JWT_EXPIRATION_MS controls access token lifetime. REDIS_HOST/PORT select Redis. SERVER_PORT selects listener. Use cloud secret manager in production. Rotate exposed credentials. Never log secrets.

## Flyway
Migrations V1..V4 are ordered and immutable after shared deployment. Flyway records applied versions in flyway_schema_history. Add V5 for new changes; do not edit an applied migration. Test both empty database and upgrade from previous release. Hibernate ddl-auto=validate checks entity/schema agreement; Flyway owns schema evolution.

## CI
Backend workflow sets up JDK 21, runs Maven tests, packages executable JAR and builds Docker image. It does not publish an image or deploy to a cloud provider. Registry authentication, image scanning, immutable tags, approvals, smoke checks and rollback require a selected provider and secrets.

## Production topology
Android clients -> HTTPS load balancer -> multiple stateless Spring Boot instances -> managed PostgreSQL and Redis. Add secret manager, centralized logs/metrics/traces, Android crash reporting, CI image registry, staging smoke tests, manual production approval. Configure TLS, least-privilege DB users, firewall rules, backups and restore drills.

## Observability
Actuator health is a basic signal. Add Micrometer, Prometheus, Grafana, OpenTelemetry and structured JSON logs with request IDs. Never log Authorization headers or sensitive bodies. Measure p50/p95/p99 latency, request/error rates, DB pool, slow SQL, Redis latency, JVM memory and GC. Report actual load-test workload and results.

## Scaling trade-offs
Stateless API instances scale horizontally; JWT avoids server sessions but complicates revocation. PostgreSQL is source of truth; Redis is rebuildable cache. Cache-aside improves reads but introduces staleness/invalidation. Pessimistic locking is simple for quest completion but contention limits throughput. Unique constraints protect integrity. Completion events enable audit/analytics; high volume may require partitioning. Kafka is not needed at current scale; add it for async fan-out, replay or independent consumers, not as decoration.

## Interview questions
1. Trace request from JWT filter to SQL.
2. Why derive identity from Authentication?
3. Why BCrypt rather than plain SHA-256?
4. Is JWT encrypted?
5. What happens on token expiry?
6. Why @Transactional?
7. What is isolation level?
8. Pessimistic vs optimistic locking?
9. Why unique completion constraint?
10. Why completion event plus completed boolean?
11. Why DTOs?
12. Flyway vs ddl-auto update?
13. PostgreSQL vs H2?
14. Why Redis leaderboard cache?
15. What if Redis fails?
16. What if DB commits but response is lost?
17. How to rate limit?
18. How to scale instances?
19. How to deploy without downtime?
20. How to recover from bad migration?

## Scope boundary
This adds local Docker deployment and CI image build, not cloud deployment. Cloud deployment requires provider, domain, TLS, managed data services, secret manager, monitoring, backups and real credentials. Android API URLs remain placeholders until real endpoints exist.
