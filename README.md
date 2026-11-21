# LifeQuest

LifeQuest is a habit and goal-tracking application that turns personal tasks into quests. The repository contains the Android client and Spring Boot API in one project tree.

## Features

### Android client
- Kotlin, Jetpack Compose and Material 3 UI.
- Email registration and JWT sign-in.
- Create, filter and complete quests with difficulty-based XP.
- Local Room persistence with offline-first quest creation/completion.
- WorkManager background sync with retry and idempotency keys.
- Profile progress, streaks, achievements, weekly leaderboard and friend/team challenges.
- Hilt dependency injection, Retrofit/OkHttp networking and DataStore preferences.
- Access tokens encrypted with Android Keystore AES-GCM.

### Backend API
- Java 21 and Spring Boot REST API.
- Stateless JWT authentication with BCrypt password hashing.
- PostgreSQL persistence, Flyway schema migrations and Redis-backed leaderboard caching.
- Server-authoritative XP awards, quest completion events, streaks, achievements and social challenges.
- Dockerfile and Docker Compose for local deployment.

## Repository layout

```text
app/                         Android application (Kotlin / Compose)
lifequest-backend/           Spring Boot API (Java 21)
compose.yaml                 Local PostgreSQL + Redis + API stack
.env.example                 Local backend configuration template
docs/                        Android and backend implementation guides
.github/workflows/           Android and backend CI workflows
```

## Requirements

- Android Studio with Android SDK 36 and JDK 21 for the Android app.
- Docker Engine and Docker Compose plugin for the backend stack.
- Or Java 21 plus PostgreSQL and Redis when running the backend outside Docker.

## Run the backend locally

1. Copy `.env.example` to `.env`.
2. Replace both placeholder secrets. Use a strong database password and a randomly generated JWT secret with at least 32 bytes. Never commit `.env`.
3. Start the stack:

```bash
docker compose up --build -d
docker compose ps
docker compose logs -f api
```

4. Check the API health endpoint:

```bash
curl http://localhost:8080/actuator/health
```

Stop the stack with `docker compose down`. The named PostgreSQL and Redis volumes persist. Use `docker compose down -v` only when you intentionally want to delete local database/cache data.

### Run backend tests without Docker Compose

From the repository root:

```bash
cd lifequest-backend
./mvnw -B test
./mvnw -B clean verify
```

On Windows, use `mvnw.cmd` instead of `./mvnw`.

## Run the Android app

Open the repository root in Android Studio and allow Gradle sync to finish. The app has staging and production product flavors.

For an Android emulator using a local API on the host machine, build the debug variant:

```bash
./gradlew :app:testStagingDebugUnitTest
./gradlew :app:assembleStagingDebug
```

The default debug build points to `http://10.0.2.2:8080/`, which reaches port 8080 on the host from the standard Android emulator. Start the backend stack first.

For a physical Android phone on the same trusted Wi-Fi network, set `API_BIND_ADDRESS=0.0.0.0` in the local `.env` file, start the backend, and use the computer's LAN IPv4 address as the debug API URL. For example, run `./gradlew :app:installStagingDebug -PlifequestApiBaseUrl=http://192.168.1.10:8080/` after replacing the example address with the computer's actual IPv4 address. Allow inbound TCP port 8080 through the computer firewall only on the private network profile. Do not expose this development HTTP endpoint to the public internet. Restore `API_BIND_ADDRESS=127.0.0.1` when finished.

The staging and production flavor URLs in `app/build.gradle.kts` are intentionally example hosts until a real API domain is deployed. Before a release build, replace those URLs with your HTTPS API endpoints and verify the endpoint from a physical device and the Play Store signing track. Do not ship `example.com` values.

## Build variants and CI

- Android CI runs staging unit tests and assembles a staging debug APK on pushes and pull requests targeting `master`.
- Backend CI runs Maven tests, packages the executable JAR and builds the Docker image when backend files change on `master` or `backend`.
- CI validates compilation/tests/package/image construction. It does **not** provision cloud infrastructure, publish a container image, or deploy a public API.

## Security and deployment notes

- Android access tokens are encrypted with an Android Keystore AES-GCM key; Android app backup is disabled to avoid exporting app-private token data.
- Cleartext HTTP is enabled only for the debug emulator build. Release builds must use HTTPS.
- JWT is signed, not encrypted. Keep `JWT_SECRET`, `DB_PASSWORD` and any deployment credentials in a secret manager or deployment environment, never in source control.
- The Compose API port is bound to localhost. For remote access, deploy behind a TLS reverse proxy/load balancer and keep PostgreSQL/Redis private.
- Configure database backups and restore tests, log/metric monitoring, rate limits, dependency scanning and a rollback plan before a public production launch.

## Further documentation

- [Android architecture, sync and interview guide](docs/ANDROID_IMPLEMENTATION_AND_INTERVIEW.md)
- [Backend architecture, deployment and interview guide](docs/BACKEND_DEPLOYMENT_AND_INTERVIEW.md)

## Current release status

The codebase includes the Android client and backend implementation with local Docker support and CI. A public production release is not configured by this repository alone: it still needs a real API host/domain, production database/Redis, TLS, secrets, monitoring, deployment pipeline and matching Android flavor URLs. No live production endpoint is implied by the example configuration.
