# LifeQuest Android — implementation and interview guide

This guide describes the Android branch and provides a practical explanation of its main design choices. Repository source is authoritative; snippets are teaching-sized examples.

## 1. Architecture and request lifecycle

The client uses Kotlin, Jetpack Compose, MVVM, Hilt, Room, DataStore, Retrofit/OkHttp and WorkManager.

Quest completion flow:
1. Compose screen invokes a ViewModel action.
2. ViewModel queues the operation in Room.
3. Repository/Retrofit sends it when authenticated and online.
4. OkHttp attaches the bearer token.
5. Spring Boot validates the token and derives identity from the authenticated principal.
6. Backend transaction validates ownership and awards XP once.
7. Android reconciles the response into Room.
8. Room Flow emits state and Compose renders it.

Compose renders state; ViewModel coordinates UI work; Repository abstracts data sources; Room provides durable local state; Retrofit defines HTTP contracts; WorkManager handles deferrable persistent work.

## 2. StateFlow and ViewModel

StateFlow exposes the latest UI state. Keep mutation private and expose an immutable StateFlow.

~~~kotlin
data class QuestScreenState(
    val loading: Boolean = false,
    val quests: List<Quest> = emptyList(),
    val error: String? = null
)

class QuestViewModel(
    private val repository: QuestRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuestScreenState())
    val state = _state.asStateFlow()

    fun refresh() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            repository.refresh()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            _state.update { it.copy(error = "Unable to refresh quests") }
        } finally {
            _state.update { it.copy(loading = false) }
        }
    }
}
~~~

Interview defence:
- viewModelScope is cancelled when the ViewModel is cleared.
- Rethrow CancellationException; swallowing it breaks structured concurrency.
- Represent loading, data and error explicitly.
- StateFlow represents current state; SharedFlow is useful for transient one-off events.

## 3. Room and offline-first data

Room provides SQLite persistence. A DAO defines queries; an Entity defines persisted columns; a Database owns the DAO.

~~~kotlin
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val xp: Int,
    val difficulty: String,
    val completed: Boolean = false,
    val remoteId: Long? = null,
    val pendingCreate: Boolean = true,
    val pendingCompletion: Boolean = false,
    val clientRequestId: String? = null
)

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY id")
    fun observeAll(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(quest: QuestEntity)
}
~~~

Why local persistence?
- Previously loaded data remains available offline.
- Local writes can be durable before a remote request.
- Pending flags support retries after process death.
- Room Flow updates UI when rows change.

An offline queue is not guaranteed delivery by itself. Work must be scheduled, network-constrained, authenticated, idempotent and observable. Conflicts need a policy.

## 4. WorkManager

Use WorkManager for deferrable work that should survive process death and respect OS constraints. It is not an exact-time scheduler.

~~~kotlin
val request = PeriodicWorkRequestBuilder<QuestSyncWorker>(
    15, TimeUnit.MINUTES
).setConstraints(
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
).build()

WorkManager.getInstance(context).enqueueUniquePeriodicWork(
    "lifequest-quest-sync",
    ExistingPeriodicWorkPolicy.KEEP,
    request
)
~~~

LifeQuest schedules both unique periodic work and a network-constrained one-time sync from LifeQuestApplication.onCreate. The foreground ViewModel also attempts sync after local writes. Return retry for transient errors; permanent validation failures should not retry forever. Idempotency keys protect against retries after a remote commit whose response was lost.

## 5. Retrofit and OkHttp

Retrofit maps annotated interfaces to HTTP calls. OkHttp executes requests and interceptors.

~~~kotlin
interface QuestApi {
    @GET("api/quests")
    suspend fun getQuests(): List<QuestResponse>

    @POST("api/quests")
    suspend fun createQuest(
        @Body request: CreateQuestRequest
    ): QuestResponse
}
~~~

The auth interceptor adds Authorization: Bearer token to protected calls and excludes login/register. It reads an in-memory token snapshot populated by DataStore during session startup and updated by login/logout, avoiding a blocking DataStore read on OkHttp threads. Authorization is redacted from logs; production logging does not print request payloads.

## 6. Token lifecycle and security

Expected lifecycle:
- Login succeeds -> persist access token.
- Protected request -> attach bearer token.
- Logout -> clear token and user-specific session state.
- Expired token -> handle 401 and require re-authentication.
- Never print tokens, send them to analytics, or commit them to Git.

TokenManager stores the access token encrypted with AES-GCM using an Android Keystore key; only ciphertext and the IV are stored in Preferences DataStore. The legacy plaintext preference key is migrated on session startup and before background sync. Verify migration and key-loss recovery on physical devices before release.

## 7. Unit and UI tests

Test business rules without Android framework dependencies.

~~~kotlin
@Test
fun preventsDuplicateCompletion() {
    val first = completeQuest(initialState(), questId = 1)
        as QuestResult.Success
    val second = completeQuest(first.state, questId = 1)

    assertEquals(QuestResult.AlreadyCompleted, second)
    assertEquals(50, first.state.totalXP)
}
~~~

Test categories:
- Domain: XP rules, levels, streak rules.
- Repository: local/remote mapping and pending writes.
- ViewModel: loading, success, error and cancellation.
- DAO: query correctness and migrations using in-memory Room.
- Compose: user-visible actions and semantics.
- Instrumentation: Android runtime integration.

Use kotlinx-coroutines-test and runTest to control coroutine scheduling. Use fakes for simple repositories; use MockK or Mockito-Kotlin where interaction verification is useful. Prefer the simplest test double that makes behaviour clear.

A Compose journey test should launch the screen, assert a quest is visible, tap Complete, assert the state changes, then simulate a failure and assert an accessible error is displayed. Prefer semantics over implementation details.

## 8. Build variants

LifeQuest defines staging and production product flavors. Keep API base URLs separate.
- ./gradlew :app:assembleStagingDebug
- ./gradlew :app:assembleProductionRelease

Never ship a staging URL or debug cleartext policy in production. Configure release signing outside source control using CI secrets or a secure local keystore.

## 9. CLI debugging

~~~bash
./gradlew :app:testStagingDebugUnitTest
./gradlew :app:assembleStagingDebug
./gradlew :app:assembleProductionRelease
./gradlew --stacktrace --info :app:assembleStagingDebug
adb devices
adb logcat
adb logcat -s AndroidRuntime
~~~

Debug method: reproduce, capture the exception and first app-owned stack frame, identify the layer, form a falsifiable hypothesis, change one thing, run the smallest test, run regression tests, record root cause and prevention.

## 10. Interview trade-offs

- Room + API: offline UX and server authority, at the cost of conflict resolution and sync complexity.
- StateFlow: current observable state; SharedFlow: transient events.
- WorkManager: persistent deferrable work; not exact scheduling.
- Repository: decouples UI/domain from data sources; avoid needless abstraction.
- DTO vs Entity: protects API and persistence contracts from UI/domain changes.
- Client-side XP preview vs server authority: client may preview; server calculates and persists rewards.
- Optimistic UI vs confirmed UI: faster feedback, but requires rollback/reconciliation on failure.
