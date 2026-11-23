# SIS6 — Dependency Injection with Koin

Branch: **`sis6-koin`**, created from `feature/dummyjson-auth` (that branch holds the SIS5 network code: Retrofit, DummyJSON login, Pet Facts screen with Loading / Success / Error).

**The idea in one line:** a class asks for what it needs in its constructor. It does not build it. Koin builds every object once, in one place (`di/`), and passes it in.

---

## 1. Before → After (what changed in the app)

| Before (manual DI) | After (Koin) |
|---|---|
| `AppContainer.kt` built every object by hand | **deleted**, replaced by `di/AppModule.kt` |
| `object ApiClient` (OkHttp + Retrofit + `AuthApi`) | **deleted**, replaced by `di/NetworkModule.kt` |
| `object PetFactsClient` (its own OkHttp + 2 Retrofits) | **deleted**, replaced by `di/NetworkModule.kt` |
| `ui/QamqorFactory.kt` (`ViewModelProvider.Factory` + `qamqorViewModel()`) | **deleted**, replaced by `viewModel { }` + `koinViewModel()` |
| `QamqorDatabase.get(context)` (hand-made singleton in a companion object) | **removed**, the database is built by Koin in `di/AppModule.kt` |
| `FeedViewModel` built its use cases itself (default args) | use cases come from Koin (`factory { }`) |
| `QamqorApp` created `AppContainer` | `QamqorApp` calls `startKoin { ... }` |
| Screens: `qamqorViewModel<X>()` | Screens: `koinViewModel<X>()` |
| `MainActivity`: `(application as QamqorApp).container.settings` | `MainActivity`: `val settings: SettingsRepository by inject()` |

The app looks and behaves the same. The Pet Facts screen still shows Loading → Success / Error exactly as in SIS5.

---

## 2. Files

### Created
- `app/src/main/java/com/example/qamqorapp/di/NetworkModule.kt`: network layer
- `app/src/main/java/com/example/qamqorapp/di/AppModule.kt`: data layer + ViewModels
- `app/src/test/java/com/example/qamqorapp/KoinModulesTest.kt`: bonus test that checks the Koin graph

### Deleted
- `app/src/main/java/com/example/qamqorapp/AppContainer.kt`
- `app/src/main/java/com/example/qamqorapp/ui/QamqorFactory.kt`
- `app/src/main/java/com/example/qamqorapp/data/remote/ApiClient.kt`
- `app/src/main/java/com/example/qamqorapp/data/remote/PetFactsClient.kt`

### Modified
- `gradle/libs.versions.toml`: Koin versions and libraries
- `app/build.gradle.kts`: Koin dependencies
- `app/src/main/java/com/example/qamqorapp/QamqorApp.kt`: `startKoin`
- `app/src/main/java/com/example/qamqorapp/MainActivity.kt`: `by inject()` instead of the container
- `app/src/main/java/com/example/qamqorapp/data/QamqorDatabase.kt`: removed the hand-made `get()` / `instance` singleton (only `MIGRATION_1_2` is left in the companion)
- 8 screens, `qamqorViewModel()` → `koinViewModel()`:
  - `ui/create/CreateScreen.kt`
  - `ui/detail/DetailScreen.kt`
  - `ui/facts/PetFactsScreen.kt`
  - `ui/favorites/FavoritesScreen.kt`
  - `ui/feed/FeedScreen.kt`
  - `ui/navigation/AppShell.kt`
  - `ui/profile/ProfileScreen.kt`
  - `ui/setup/SetupScreen.kt`

---

## 3. Step 1: Koin dependencies

**`gradle/libs.versions.toml`**
```toml
[versions]
koin = "4.1.1"

[libraries]
koin-bom = { group = "io.insert-koin", name = "koin-bom", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android" }
koin-androidx-compose = { group = "io.insert-koin", name = "koin-androidx-compose" }
koin-test = { group = "io.insert-koin", name = "koin-test" }
koin-test-junit4 = { group = "io.insert-koin", name = "koin-test-junit4" }
```

**`app/build.gradle.kts`**
```kotlin
implementation(platform(libs.koin.bom))       // BOM picks the same version for all Koin libs
implementation(libs.koin.android)             // startKoin, androidContext, inject()
implementation(libs.koin.androidx.compose)    // koinViewModel()

testImplementation(libs.koin.test)            // verify() for the bonus test
testImplementation(libs.koin.test.junit4)
```

---

## 4. Step 2: Repositories have interfaces (requirement 4 + 5)

These already existed from SIS4/SIS5. Koin now binds each interface to its implementation.

| Interface (what ViewModels ask for) | Implementation (what Koin builds) | Constructor gets |
|---|---|---|
| `domain/repository/PetFactsRepository.kt` | `data/repository/PetFactsRepositoryImpl.kt` | `CatFactsApi`, `DogFactsApi` |
| `data/PostRepository.kt` | `data/RoomPostRepository.kt` | `QamqorDatabase` |
| `data/AuthApi.kt` → `AuthClient` | `DummyJsonAuthClient` (same file) | `AuthApi`, `Context`, `PostRepository` |

Example: the repository **asks** for the APIs. It doesn't build them:
```kotlin
// data/repository/PetFactsRepositoryImpl.kt
class PetFactsRepositoryImpl(
    private val catApi: CatFactsApi,
    private val dogApi: DogFactsApi,
) : PetFactsRepository { ... }
```

---

## 5. Step 3: Modules (`di/` package)

### `di/NetworkModule.kt`: network layer, everything is `single`
```kotlin
val networkModule = module {

    single { Json { ignoreUnknownKeys = true } }

    // ONE shared OkHttpClient for the whole app
    single {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                            else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()
    }

    // DummyJSON (default Retrofit)
    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(get())                       // Koin gives the OkHttpClient above
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }

    // Cat and dog APIs have other base URLs, so their Retrofits get names (qualifiers)
    single(named("cat")) { Retrofit.Builder().baseUrl("https://catfact.ninja/") ... .build() }
    single(named("dog")) { Retrofit.Builder().baseUrl("https://dogapi.dog/")   ... .build() }

    // API interfaces
    single<AuthApi>     { get<Retrofit>().create(AuthApi::class.java) }
    single<CatFactsApi> { get<Retrofit>(named("cat")).create(CatFactsApi::class.java) }
    single<DogFactsApi> { get<Retrofit>(named("dog")).create(DogFactsApi::class.java) }
}
```
- `get()` means "Koin, give me an object of the type this parameter needs".
- `named("cat")` / `named("dog")`: there are 3 `Retrofit` objects, so the names tell Koin which one we mean.
- Small change from before: the cat and dog APIs now share the same OkHttpClient as DummyJSON. Before, `PetFactsClient` had its own client with no logging. Now there is one client, so cat/dog requests are also logged (BASIC, debug builds only).

### `di/AppModule.kt`: data layer + ViewModels
```kotlin
val appModule = module {

    // Data
    single {
        Room.databaseBuilder(androidContext(), QamqorDatabase::class.java, "qamqor.db")
            .addMigrations(QamqorDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }
    single<PostRepository> { RoomPostRepository(get()) }          // interface -> impl
    single { SettingsRepository(androidContext()) }
    single {
        ProfilePhotoStore(
            dir = File(androidContext().filesDir, "profile"),
            authority = "${androidContext().packageName}.fileprovider",
        )
    }
    single<PhotoDecoder> { AndroidPhotoDecoder() }
    single<AuthClient> { DummyJsonAuthClient(get(), androidContext(), get()) }
    single<PetFactsRepository> { PetFactsRepositoryImpl(get(), get()) }   // interface -> impl

    // Use cases (cheap, so a new one each time)
    factory { GetFeedUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }

    // ViewModels (always viewModel { }, never single/factory)
    viewModel { FeedViewModel(get(), get(), get()) }
    viewModel { PetFactsViewModel(get(), get()) }
    viewModel { FavoritesViewModel(get()) }
    viewModel { ProfileViewModel(get(), get(), get(), get()) }
    viewModel { SetupViewModel(get()) }
    viewModel { CreateViewModel(get()) }
    viewModel { DetailViewModel(get(), get()) } // SavedStateHandle is provided by Koin
    viewModel { AppShellViewModel(get(), get(), get(), get()) }
}
```
- `single<PetFactsRepository> { PetFactsRepositoryImpl(...) }`: the `<PetFactsRepository>` part matters. Without it Koin only knows the type `PetFactsRepositoryImpl` and fails with *"No definition found for type PetFactsRepository"*.
- `androidContext()` is the Application context we gave to Koin in `startKoin`.
- `viewModel { }` is imported from `org.koin.core.module.dsl.viewModel`.

---

## 6. Step 4: Start Koin in the Application class (requirement 1)

**`app/src/main/java/com/example/qamqorapp/QamqorApp.kt`**
```kotlin
class QamqorApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val repository: PostRepository by inject()   // lazy, read after startKoin

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()                       // Koin logs to Logcat
            androidContext(this@QamqorApp)        // so modules can call androidContext()
            modules(networkModule, appModule)
        }
        applicationScope.launch { repository.seedIfEmpty(this@QamqorApp) }
    }
}
```

**`app/src/main/AndroidManifest.xml`**: it was already registered, no change needed:
```xml
<application
    android:name=".QamqorApp"
    ... >
```

**`app/src/main/java/com/example/qamqorapp/MainActivity.kt`**
```kotlin
// before: val settings = (application as QamqorApp).container.settings
val settings: SettingsRepository by inject()
```

---

## 7. Step 5: ViewModels get dependencies in the constructor, screens use `koinViewModel()` (requirement 5 + 6)

ViewModel: asks for the **interface**, builds nothing:
```kotlin
// ui/facts/PetFactsViewModel.kt  (unchanged, it was already written this way)
class PetFactsViewModel(
    private val repository: PetFactsRepository,   // interface, not the Impl
    private val settings: SettingsRepository,
) : ViewModel() {
    // SIS5 StateFlow<PetFactsUiState> with Loading / Success / Error, unchanged
}
```

Screen: gets the ViewModel from Koin:
```kotlin
// ui/facts/PetFactsScreen.kt
import org.koin.androidx.compose.koinViewModel

val viewModel = koinViewModel<PetFactsViewModel>()   // before: qamqorViewModel<PetFactsViewModel>()
```
The same one-line change is in all 8 screens listed in section 2.

There is no `ViewModelProvider.Factory` anywhere in the project now.

---

## 8. Requirement checklist

| # | Requirement | Where |
|---|---|---|
| 1 | Application class with `startKoin`, registered in manifest | `QamqorApp.kt`, `AndroidManifest.xml` (`android:name=".QamqorApp"`) |
| 2 | 2+ modules split by layer in `di/` | `di/NetworkModule.kt` (`networkModule`), `di/AppModule.kt` (`appModule`) |
| 3 | OkHttpClient, Retrofit, API are `single` | `di/NetworkModule.kt` |
| 4 | Repository interface, Koin binds to Impl | `single<PetFactsRepository> { PetFactsRepositoryImpl(get(), get()) }`, `single<PostRepository> { RoomPostRepository(get()) }` |
| 5 | Constructor injection | `PetFactsRepositoryImpl(catApi, dogApi)`, `PetFactsViewModel(repository, settings)`, all other ViewModels |
| 6 | ViewModels from Koin, `koinViewModel()`, no Factory | `viewModel { }` in `AppModule.kt`; 8 screens; `QamqorFactory.kt` deleted |
| 7 | No manual building left | `ApiClient`, `PetFactsClient`, `AppContainer`, `QamqorFactory`, `QamqorDatabase.get()` all removed |
| Bonus | Graph verified in a test | `app/src/test/java/com/example/qamqorapp/KoinModulesTest.kt` |

---

## 9. Bonus: verify the graph in a test (+0.25)

**`app/src/test/java/com/example/qamqorapp/KoinModulesTest.kt`**
```kotlin
class KoinModulesTest {

    // Checks that every dependency in the graph can be resolved.
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyKoinGraph() {
        module { includes(networkModule, appModule) }
            .verify(extraTypes = listOf(
                Context::class, SavedStateHandle::class, Application::class,
                File::class, String::class,
            ))
    }
}
```
`verify()` checks every constructor in the modules. If something is missing (for example, someone deletes `single<AuthApi>`), the test fails **before** the app runs. `extraTypes` lists the types that Android or Koin provides at runtime (Context, SavedStateHandle) or that we pass as plain values (`File`, `String` for `ProfilePhotoStore`).

Run it with: `./gradlew :app:testDebugUnitTest`

---

## 10. Results

- `./gradlew :app:assembleDebug`: **BUILD SUCCESSFUL**
- `./gradlew :app:testDebugUnitTest`: **BUILD SUCCESSFUL** (all unit tests, including `KoinModulesTest`)
- Not checked on a phone/emulator yet. Before the demo, run the app and check: login, feed, detail screen, profile, Pet Facts (Loading → Success, and Error with Wi-Fi off), and rotation (data must not reload).

---

## 11. Defense questions: short answers

1. **What is DI in one sentence?** A class gets the objects it needs from outside (through its constructor) instead of creating them itself.
2. **Why is Retrofit `single` and the ViewModel `viewModel`?** Retrofit/OkHttp are expensive and stateless, so one instance is shared by the whole app. A ViewModel must live exactly as long as its screen and survive rotation. `viewModel { }` connects it to the Android ViewModelStore, which `single` cannot do (one forever) and `factory` cannot do either (a new one every time, so data reloads on rotation).
3. **`single` vs `factory`?** `single` creates one instance and always returns the same one. `factory` creates a new instance every time someone asks (we use it for the cheap use cases).
4. **Why does the ViewModel depend on the interface, not the Impl?** The ViewModel doesn't care where the data comes from. We can swap the Impl (fake for tests, a different API, a cache) by changing one line in the module, and the ViewModel code stays the same. Our `FeedViewModelTest` already uses a fake repository this way.
5. **What if you forget `androidContext(this)`?** Every definition that calls `androidContext()` (database, SettingsRepository, ProfilePhotoStore, DummyJsonAuthClient) crashes with a Koin error saying the Android context is missing, so the app crashes at startup.
6. **Koin resolves at runtime: downside and alternative?** A missing definition is found only when the app runs and crashes, not when it compiles. Ways to catch it earlier: a `verify()` / `checkModules` test (like our bonus test), or a compile-time DI library such as **Hilt/Dagger** (or Koin Annotations), which reports missing dependencies at build time.
