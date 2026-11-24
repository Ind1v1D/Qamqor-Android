	# SIS6 — Dependency Injection with Koin

Branch: **`sis6-koin`**, created from `feature/dummyjson-auth` (that branch holds the SIS5 network code: Retrofit, DummyJSON login, Pet Facts screen with Loading / Success / Error).

> **Update (branch `firebase-auth`):** the DummyJSON login was later replaced by **Firebase email/password login**. Sections 4, 5 and 11 below show the current code. What changed in that task is described in **section 12**. **SIS7 (Firestore, security rules, push notifications, Analytics) is described in section 13** at the end.

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
| `data/AuthClient.kt` | `data/FirebaseAuthClient.kt` | `FirebaseAuth`, `PostRepository` |

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

    // Cat and dog APIs have different base URLs, so each Retrofit gets a name (qualifier)
    single(named("cat")) {
        Retrofit.Builder()
            .baseUrl("https://catfact.ninja/")
            .client(get())                       // Koin gives the OkHttpClient above
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single(named("dog")) { Retrofit.Builder().baseUrl("https://dogapi.dog/") ... .build() }

    // API interfaces
    single<CatFactsApi> { get<Retrofit>(named("cat")).create(CatFactsApi::class.java) }
    single<DogFactsApi> { get<Retrofit>(named("dog")).create(DogFactsApi::class.java) }
}
```
- `get()` means "Koin, give me an object of the type this parameter needs".
- `named("cat")` / `named("dog")`: there are 2 `Retrofit` objects, so the names tell Koin which one we mean. (In SIS6 there was a third, unnamed one for DummyJSON with `single<AuthApi>`. It was removed in the Firebase task, see section 12.)
- Small change from before: the cat and dog APIs share one OkHttpClient. Before, `PetFactsClient` had its own client with no logging. Now cat/dog requests are also logged (BASIC, debug builds only).

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
    single { FirebaseAuth.getInstance() }                       // Firebase task
    single<AuthClient> { FirebaseAuthClient(get(), get()) }     // interface -> impl
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

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun verifyKoinGraph() {
        module { includes(networkModule, appModule) }
            .verify(extraTypes = listOf(
                Context::class, SavedStateHandle::class, Application::class,
                File::class, String::class,
                // added in the Firebase task: types FirebaseAuth needs internally
                FirebaseApp::class, Provider::class, Executor::class, ScheduledExecutorService::class,
            ))
    }
}
```
`verify()` checks every constructor in the modules. If something is missing (for example, someone deletes `single<CatFactsApi>`), the test fails **before** the app runs. `extraTypes` lists the types that are not defined in our modules: types Android or Koin provides at runtime (Context, SavedStateHandle), plain values we pass in (`File`, `String` for `ProfilePhotoStore`), and Firebase's own internal types (Firebase creates `FirebaseAuth` itself through `FirebaseAuth.getInstance()`).

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
5. **What if you forget `androidContext(this)`?** Every definition that calls `androidContext()` (database, SettingsRepository, ProfilePhotoStore) crashes with a Koin error saying the Android context is missing, so the app crashes at startup.
6. **Koin resolves at runtime: downside and alternative?** A missing definition is found only when the app runs and crashes, not when it compiles. Ways to catch it earlier: a `verify()` / `checkModules` test (like our bonus test), or a compile-time DI library such as **Hilt/Dagger** (or Koin Annotations), which reports missing dependencies at build time.

---

## 12. Firebase task: email login instead of DummyJSON (branch `firebase-auth`)

Branch **`firebase-auth`** was created from `sis6-koin`, so the SIS6 pull request stays DI-only. The app is connected to the Firebase project **Qamqor** (`qamqor-8dc8c`), and the DummyJSON login is replaced by **Firebase Authentication (email + password)**. Everything still goes through Koin with constructor injection.

### 12.1 Firebase setup (done in the Firebase console)
1. In the project **Qamqor**, an Android app was added with the package name `com.example.qamqorapp`.
2. The downloaded **`app/google-services.json`** sits next to `app/build.gradle.kts`.
3. **Authentication → Sign-in method → Email/Password** is enabled.

### 12.2 Gradle

**`gradle/libs.versions.toml`**
```toml
[versions]
googleServices = "4.6.0"
firebaseBom = "34.19.0"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutinesTest" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

**`build.gradle.kts`** (root)
```kotlin
alias(libs.plugins.google.services) apply false
```

**`app/build.gradle.kts`**
```kotlin
plugins {
    alias(libs.plugins.google.services)       // reads google-services.json
}
dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.kotlinx.coroutines.play.services)   // .await() for Firebase Tasks
}
// removed: buildConfigField("String", "API_BASE_URL", "\"https://dummyjson.com/\"")
```
- **Why BoM 34.19.0 and not the newest 35.x:** in 35.x, `firebase-auth` is compiled with Kotlin 2.4, but this project uses Kotlin 2.2.10, so it does not compile. 34.19.0 works without any workaround.
- **`kotlinx-coroutines-play-services`:** Firebase methods return a `Task`. `.await()` turns it into a normal `suspend` call.

### 12.3 Files

| Change | File (`app/src/main/java/com/example/qamqorapp/...`) |
|---|---|
| **Created** | `data/AuthClient.kt`: the login interface |
| **Created** | `data/FirebaseAuthClient.kt`: the Firebase implementation |
| **Deleted** | `data/AuthApi.kt`: `AuthApi` (Retrofit), `LoginRequest/Response`, `UserDto`, `DummyJsonAuthClient` |
| Modified | `di/AppModule.kt`: `FirebaseAuth` + `FirebaseAuthClient` |
| Modified | `di/NetworkModule.kt`: the DummyJSON Retrofit and `single<AuthApi>` are removed (cat/dog stay) |
| Modified | `ui/setup/SetupViewModel.kt`: username → email, new `register()` |
| Modified | `ui/setup/SetupScreen.kt`: Email field, "Create account" link, demo hint removed |
| Modified | `ui/navigation/AppShellViewModel.kt`: Firebase decides whether the user is logged in |
| Modified | `res/values/strings.xml`, `res/values-ru/strings.xml` |
| Modified | `test/.../KoinModulesTest.kt`, `test/.../ui/setup/SetupViewModelTest.kt` |

### 12.4 Interface + implementation

**`data/AuthClient.kt`**: ViewModels depend on this interface only.
```kotlin
interface AuthClient {
    val isLoggedIn: Boolean
    suspend fun login(email: String, password: String)
    suspend fun register(email: String, password: String)
    suspend fun logout()
}
```

**`data/FirebaseAuthClient.kt`**: it receives `FirebaseAuth` in the constructor and builds nothing itself.
```kotlin
class FirebaseAuthClient(
    private val auth: FirebaseAuth,
    private val posts: PostRepository,
) : AuthClient {

    override val isLoggedIn: Boolean
        get() = auth.currentUser != null

    override suspend fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email.trim(), password).await()
        saveProfileOrSignOut(email)
    }

    override suspend fun register(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email.trim(), password).await()
        saveProfileOrSignOut(email)
    }

    override suspend fun logout() {
        auth.signOut()
        posts.clearProfile()
    }

    // If saving the profile fails, undo the Firebase sign-in so we are not half-logged-in.
    private suspend fun saveProfileOrSignOut(email: String) {
        try { saveProfileIfMissing(email) } catch (e: Exception) { auth.signOut(); throw e }
    }

    // Don't overwrite a profile the user has already edited.
    private suspend fun saveProfileIfMissing(email: String) {
        if (posts.currentProfile() == null) {
            posts.saveProfile(name = email.trim().substringBefore('@'), phone = "")
        }
    }
}
```
The profile name comes from the email (`aruzhan@mail.com` → `aruzhan`). It can be changed on the Profile screen.

### 12.5 Koin

**`di/AppModule.kt`**
```kotlin
single { FirebaseAuth.getInstance() }                     // Firebase's own singleton, created once
single<AuthClient> { FirebaseAuthClient(get(), get()) }   // interface -> impl
```
Before: `single<AuthClient> { DummyJsonAuthClient(get(), androidContext(), get()) }`.
`SetupViewModel(get())` and `AppShellViewModel(...)` did not change: they still ask for `AuthClient`. **This is the point of depending on an interface:** login moved from DummyJSON to Firebase without touching the ViewModels' constructors.

### 12.6 Login screen

**`ui/setup/SetupViewModel.kt`**
```kotlin
fun submit()   = authenticate { email, password -> auth.login(email, password) }
fun register() = authenticate { email, password -> auth.register(email, password) }

private fun authenticate(action: suspend (email: String, password: String) -> Unit) {
    // email must be non-blank and contain '@', password non-blank
    // then: isSaving = true -> action(...) -> done = true, or error = true on any exception
}
```
- **`ui/setup/SetupScreen.kt`:** an Email field (`KeyboardType.Email`, email icon), a **Sign in** button, and a **Create account** link under it. The `emilys / emilyspass` demo hint was removed.
- **Strings (EN / RU):** `login_username` = "Email" / "Эл. почта", `login_username_hint` = "you@example.com", new `setup_register` = "Create account" / "Создать аккаунт". `login_error` now mentions email + a password of at least 6 characters, which is Firebase's minimum. `login_demo_hint` was deleted.

### 12.7 Who counts as logged in

**`ui/navigation/AppShellViewModel.kt`**
```kotlin
// Before: _hasProfile.value = auth.isLoggedIn || repository.currentProfile() != null
// Firebase is the only source of truth; drop any stale local profile.
if (!auth.isLoggedIn && repository.currentProfile() != null) {
    repository.clearProfile()
}
_hasProfile.value = auth.isLoggedIn
```
A phone that still had an old DummyJSON profile saved no longer skips the login screen.

### 12.8 Tests and results
- **`SetupViewModelTest`:** the fake `AuthClient` has `register()`, fields are renamed to email, and there is a new test that `register()` calls the client and sets `done`.
- **`KoinModulesTest`:** has the extra Firebase types (see section 9).
- `./gradlew :app:assembleDebug :app:testDebugUnitTest`: **BUILD SUCCESSFUL**. `processDebugGoogleServices` runs with the real `google-services.json`.
- **Manual check to do on a device:**
  1. Create an account (password of 6+ characters). It should open the feed.
  2. The user appears in Firebase console → Authentication → Users.
  3. Log out, then sign in again.
  4. A wrong password shows the error message.

---

## 13. SIS7: Firebase in the app (Auth + Firestore + Rules + FCM + Analytics)

**The one idea:** everything hangs off the user's **uid**. Auth gives it, Firestore stores data under `users/{uid}`, the rules check it, and the FCM token is saved next to it at `users/{uid}.fcmToken`.

```
FirebaseAuth ──uid──► users/{uid}                 (fcmToken field)
                      users/{uid}/posts/{postId}  (CloudPost documents)
Rules: request.auth.uid == userId
```

The architecture from SIS5/SIS6 stays the same. Every ViewModel exposes a `StateFlow`, every repository sits behind an interface, and Koin builds everything. Firebase objects come from Koin and are passed in through constructors.

### 13.1 Requirements → where they are done

| # | Requirement | Where |
|---|---|---|
| 1 | Firebase connected, BoM, no `-ktx` | `app/google-services.json`, `gradle/libs.versions.toml`, `app/build.gradle.kts` |
| 2 | Sign-up / sign-in, readable errors, loading | `ui/setup/SetupViewModel.kt`, `ui/setup/AuthError.kt`, `ui/setup/SetupScreen.kt` |
| 3 | Session kept, Sign out | `ui/navigation/AppShellViewModel.kt`, `ui/navigation/AppShell.kt` |
| 4 | Data in `users/{uid}/posts`, add / update / delete | `data/CloudPost.kt`, `data/CloudPostsRepository.kt`, `data/FirestorePostsRepository.kt` |
| 5 | Live list: `snapshots()` → `stateIn` | `FirestorePostsRepository.myPosts()`, `ui/profile/MyPostsViewModel.kt` |
| 6 | Rules: only the owner | `firestore.rules` (published in the console) |
| 7 | Push notifications | `push/PushService.kt`, `util/Notifications.kt`, `data/FirestorePushTokenRepository.kt`, `AndroidManifest.xml` |
| 8 | Analytics events + SCREEN_VIEW | `analytics/AnalyticsTracker.kt`, `analytics/FirebaseAnalyticsTracker.kt`, `ui/components/TrackScreen.kt` |
| 9 | Koin provides Firebase | `di/FirebaseModule.kt`, `di/AppModule.kt`, `QamqorApp.kt` |

### 13.2 Files

All paths are under `app/src/main/java/com/example/qamqorapp/` unless shown otherwise.

**Created**
- `firestore.rules` (project root): security rules to paste into the console
- `di/FirebaseModule.kt`: `FirebaseAuth`, `FirebaseFirestore`, `FirebaseAnalytics`, `FirebaseMessaging` as `single`
- `data/CloudPost.kt`: Firestore model + `toPost()` for showing it with the existing UI
- `data/CloudPostsRepository.kt` (interface) and `data/FirestorePostsRepository.kt` (implementation)
- `data/PushTokenRepository.kt` (interface) and `data/FirestorePushTokenRepository.kt` (implementation)
- `analytics/AnalyticsTracker.kt` (interface) and `analytics/FirebaseAnalyticsTracker.kt` (implementation)
- `push/PushService.kt`: `FirebaseMessagingService`
- `util/Notifications.kt`: notification channel + `showNotification()`
- `ui/setup/AuthError.kt`: auth exception → readable message
- `ui/CloudError.kt`: Firestore exception → readable message
- `ui/components/TrackScreen.kt`: logs `SCREEN_VIEW` in `LaunchedEffect(Unit)`
- `ui/profile/MyPostsViewModel.kt`: live "My posts" list (Loading / Success / Error)
- `ui/detail/CloudDetailViewModel.kt`: live detail of one cloud post
- `res/drawable/ic_stat_paw.xml`: white paw icon for notifications
- `app/src/test/.../ui/profile/MyPostsViewModelTest.kt`: tests for the live list

**Modified**
- `gradle/libs.versions.toml`, `app/build.gradle.kts`: Firestore, Messaging, Analytics
- `AndroidManifest.xml`: `POST_NOTIFICATIONS`, `PushService`, default channel and icon
- `QamqorApp.kt`: `firebaseModule` added to `startKoin`, notification channel created
- `di/AppModule.kt`: new repositories and ViewModels; `FirebaseAuth.getInstance()` removed
- `data/FirebaseAuthClient.kt`: clears the FCM token on sign out
- `ui/setup/SetupViewModel.kt`, `SetupScreen.kt`: typed errors, token sync, permission request
- `ui/create/CreateViewModel.kt`, `CreateScreen.kt`: publish to Firestore
- `ui/profile/ProfileViewModel.kt`, `ProfileScreen.kt`: "My posts" from Firestore
- `ui/detail/DetailScreen.kt`, `DetailViewModel.kt`: error state
- `ui/navigation/AppShell.kt`, `AppShellViewModel.kt`, `BottomBar.kt`: `mypost/{postId}` route, sign out waits before navigating
- `ui/feed/FeedScreen.kt`, `favorites/FavoritesScreen.kt`, `facts/PetFactsScreen.kt`: `TrackScreen(...)`
- `res/values/strings.xml`, `res/values-ru/strings.xml`: error messages and notification channel texts (EN / RU)
- `KoinModulesTest.kt`, `SetupViewModelTest.kt`: new types and fakes

### 13.3 Step 1: Connect Firebase

- Firebase project with Google Analytics on. Android app with package **`com.example.qamqorapp`**, the same as `applicationId` in `app/build.gradle.kts`.
- `google-services.json` sits in **`app/`**, not in the project root.

**`gradle/libs.versions.toml`**
```toml
[versions]
googleServices = "4.6.0"
firebaseBom = "34.19.0"

[libraries]
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }
firebase-firestore = { group = "com.google.firebase", name = "firebase-firestore" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
kotlinx-coroutines-play-services = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-play-services", version.ref = "coroutinesTest" }

[plugins]
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

**`build.gradle.kts`** (project): `alias(libs.plugins.google.services) apply false`

**`app/build.gradle.kts`**
```kotlin
plugins { alias(libs.plugins.google.services) }

implementation(platform(libs.firebase.bom))
implementation(libs.firebase.auth)
implementation(libs.firebase.firestore)
implementation(libs.firebase.messaging)
implementation(libs.firebase.analytics)
implementation(libs.kotlinx.coroutines.play.services)
```
- **BoM (Bill of Materials):** we write the version only once, on the BoM. It picks matching versions for auth, firestore, messaging and analytics.
- **No `-ktx`:** since BoM 34 the Kotlin extensions (`Firebase.auth`, `snapshots()`, ...) live in the main artifacts, and the `-ktx` artifacts were removed.
- **`coroutines-play-services`** gives `.await()`, which turns a Firebase `Task` into a `suspend` call.

### 13.4 Step 2: Koin provides Firebase

**`di/FirebaseModule.kt`**
```kotlin
val firebaseModule = module {
    single { Firebase.auth }
    single { Firebase.firestore }
    single { Firebase.analytics }
    single { Firebase.messaging }
}
```
**`QamqorApp.kt`**
```kotlin
startKoin {
    androidLogger()
    androidContext(this@QamqorApp)
    modules(firebaseModule, networkModule, appModule)
}
createNotificationChannel(this)
```
**`di/AppModule.kt`** (new lines)
```kotlin
single<CloudPostsRepository> { FirestorePostsRepository(get(), get()) }
single<PushTokenRepository> { FirestorePushTokenRepository(get(), get(), get()) }
single<AnalyticsTracker> { FirebaseAnalyticsTracker(get()) }
single<AuthClient> { FirebaseAuthClient(get(), get(), get()) }

viewModel { SetupViewModel(get(), get()) }
viewModel { CreateViewModel(get(), get(), get()) }
viewModel { MyPostsViewModel(get(), get()) }
viewModel { CloudDetailViewModel(get(), get()) }
```
- The old `single { FirebaseAuth.getInstance() }` was removed from `appModule`. All Firebase objects now come from `firebaseModule`.
- **Rule kept:** `Firebase.*` appears **only** in `FirebaseModule.kt`. No repository or ViewModel calls `Firebase.auth` / `Firebase.firestore` or `getInstance()`. Each one gets what it needs in its constructor, which is also why they can be tested with fakes.
- `PushService` is created by Android, not by Koin, so it gets its repository with `by inject()`.

### 13.5 Step 3: Authentication

`AuthClient` / `FirebaseAuthClient` (section 12) already did sign-in and sign-up with `signInWithEmailAndPassword(...).await()` and `createUserWithEmailAndPassword(...).await()`. SIS7 added three things.

**a) Readable errors, never `e.message`**: `ui/setup/AuthError.kt`
```kotlin
enum class AuthError(@param:StringRes val messageRes: Int) {
    WEAK_PASSWORD(R.string.auth_error_weak_password),
    INVALID_CREDENTIALS(R.string.auth_error_invalid_credentials),
    EMAIL_IN_USE(R.string.auth_error_email_in_use),
    NETWORK(R.string.auth_error_network),
    GENERIC(R.string.auth_error_generic),
}

fun Throwable.toAuthError(): AuthError = when (this) {
    is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
    is FirebaseAuthInvalidCredentialsException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
    is FirebaseAuthUserCollisionException -> AuthError.EMAIL_IN_USE
    is FirebaseNetworkException -> AuthError.NETWORK
    else -> AuthError.GENERIC
}
```
- **`WeakPassword` must come first.** `FirebaseAuthWeakPasswordException` is a subclass of `FirebaseAuthInvalidCredentialsException`, and `when` takes the first branch that matches. If it came second, a weak password would show "Wrong email or password".
- `InvalidUserException` (no such account) shows the same message as a wrong password, so the app does not reveal which emails are registered.
- The ViewModel stores an **enum**, not a string. The screen turns it into text with `stringResource(error.messageRes)`, so the message is translated (EN / RU) and the ViewModel stays a plain unit-testable class with no `Context`.

| Case | EN | RU |
|---|---|---|
| Weak password | Use at least 6 characters. | Используйте не меньше 6 символов. |
| Wrong email/password | Wrong email or password. | Неверный email или пароль. |
| Email already used | This email already has an account. | Для этого email уже есть аккаунт. |
| No network | No connection. Try again. | Нет соединения. Попробуйте ещё раз. |
| Anything else | Something went wrong. Try again. | Что-то пошло не так. Попробуйте ещё раз. |

**b) Loading state**: `SetupViewModel`
```kotlin
_uiState.update { it.copy(isSaving = true, error = null) }
viewModelScope.launch {
    try {
        action(state.email, state.password)
        pushTokens.syncToken()
        _uiState.update { it.copy(isSaving = false, done = true) }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        _uiState.update { it.copy(isSaving = false, error = e.toAuthError()) }
    }
}
```
While `isSaving` is true, **both** buttons ("Sign in" and "Create account") are disabled, and "Sign in" shows a spinner. That stops double taps from sending two requests. `CancellationException` is rethrown so leaving the screen cancels the coroutine properly instead of showing an error.

**c) Session and Sign out**
- Firebase saves the session on the device by itself. On start, `AppShellViewModel` checks `auth.isLoggedIn` (`auth.currentUser != null`). If true, it opens the feed; if false, the login screen. **Kill the app and reopen it: a signed-in user goes straight to the feed.**
- **Sign out** (Profile screen): `AppShellViewModel.signOut { ... }`. It clears the FCM token, calls `auth.signOut()`, clears the local profile, and only **after** all that has finished does it navigate to the login screen with `popUpTo(0) { inclusive = true }`, so Back cannot return to the signed-in screens.

### 13.6 Step 4: Firestore data and the live list

**Where the data lives:** `users/{uid}/posts/{postId}`. Each user has their own sub-collection of posts.

**Model**: `data/CloudPost.kt`
```kotlin
data class CloudPost(
    @DocumentId val id: String = "",
    val status: String = PostStatus.LOST.name,
    val species: String = Species.DOG.name,
    val name: String = "",
    val breed: String = "",
    val district: String = District.ALMALINSKY.name,
    val address: String = "",
    val marks: String = "",
    val description: String = "",
    val emoji: String = "🐾",
    val phone: String = "",
    val author: String = "",
    val resolved: Boolean = false,
    val favorite: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
)
```
- **A default on every field:** Firestore's `toObject()` first creates an empty object (it needs a no-argument constructor) and then fills in the fields. Kotlin only generates that constructor when every parameter has a default. A document missing a field also won't crash.
- **`@DocumentId`:** the document's id is put into `id` when it is read, and it is **not** written as a field.
- **`@ServerTimestamp`:** `createdAt = null` on write means "the server fills in its own time", so phones with a wrong clock don't break the order.
- Enums are stored as strings (`"LOST"`, `"DOG"`). `CloudPost.toPost()` converts a cloud post to the existing `Post` class **for display only**, so `DetailScreen`, `StatusTag` and the rest of the old UI are reused.

**Repository**: interface `CloudPostsRepository`, implementation `FirestorePostsRepository(db, auth)`
```kotlin
private fun userPosts(): CollectionReference {
    val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not signed in")
    return db.collection("users").document(uid).collection("posts")
}

override fun myPosts(): Flow<List<CloudPost>> = flow {
    emitAll(userPosts().snapshots())
}.map { snapshot ->
    snapshot.toObjects(CloudPost::class.java)
        .sortedByDescending { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE }
}

override suspend fun add(post: CloudPost): String {
    val ref = userPosts().document()
    ref.set(post)
    return ref.id
}
override suspend fun setResolved(id: String, resolved: Boolean) { userPosts().document(id).update("resolved", resolved) }
override suspend fun setFavorite(id: String, favorite: Boolean) { userPosts().document(id).update("favorite", favorite) }
override suspend fun delete(id: String) { userPosts().document(id).delete() }
```
- **`snapshots()`** turns a Firestore real-time listener into a `Flow`. Every change on the server (from the app **or from the Firebase console**) sends a new snapshot, so the list updates without a refresh. When nobody collects the flow, the listener is removed automatically.
- **`flow { emitAll(...) }`** reads the uid lazily, when collection starts, not when the ViewModel is created. If nobody is signed in, the exception happens inside the flow, and `catch` in the ViewModel shows it as an error instead of crashing.
- **Sorting in memory instead of `orderBy("createdAt")`:** `orderBy` hides documents that have no `createdAt`, for example ones added by hand in the console. Sorting in memory shows everything, and new posts whose server time hasn't arrived yet go to the top.
- **Writes are not awaited** (`set` / `update` / `delete` without `.await()`). Firestore writes go to the local cache first and the listener shows them **at once**, but the `Task` finishes only when the server confirms. With `.await()`, publishing with no internet would keep spinning forever. This way the post appears immediately, and Firestore sends it to the server when the network comes back (offline persistence is on by default).

**Where it is used**
- **Add (Create screen)**: `CreateViewModel(repository, cloud, analytics)` builds a `CloudPost` (author and phone come from the local profile), calls `cloud.add(...)` and logs `post_created`. On error it shows a readable `publishError` above the button.
- **Live list (Profile → My posts)**: `MyPostsViewModel`. The "Published" and "Solved" counters are counted from this list.
- **Update**: the check button marks a post solved, and the refresh button makes it active again (`setResolved`). The star on the cloud detail toggles `favorite`.
- **Delete**: the delete button calls `delete(id)`.
- **Detail**: a new route `mypost/{postId}` (String id) → `CloudDetailViewModel`, which watches the single document live with `post(id)` and reuses `DetailScreen`. The old `detail/{postId}` route (Long id, Room) still works for the demo feed.

**ViewModel**: `ui/profile/MyPostsViewModel.kt` (the same pattern as SIS5)
```kotlin
sealed interface MyPostsUiState {
    data object Loading : MyPostsUiState
    data class Success(val posts: List<CloudPost>) : MyPostsUiState
    data class Error(val error: CloudError) : MyPostsUiState
}

val uiState: StateFlow<MyPostsUiState> = repository.myPosts()
    .map<List<CloudPost>, MyPostsUiState> { MyPostsUiState.Success(it) }
    .catch { emit(MyPostsUiState.Error(it.toCloudError())) }
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MyPostsUiState.Loading,
    )
```
- `map`: each new list from Firestore becomes `Success(list)`.
- `catch`: any error in the flow (permission denied, offline, not signed in) becomes `Error(...)`. The app does not crash, and the screen shows a translated message.
- `stateIn(viewModelScope, WhileSubscribed(5_000), Loading)`: this turns the cold `Flow` into a `StateFlow`. It starts with `Loading`. The Firestore listener runs while the screen is visible and stops 5 seconds after the screen goes away. Those 5 seconds mean a screen rotation does not restart the listener.

**Errors**: `ui/CloudError.kt`
```kotlin
fun Throwable.toCloudError(): CloudError = when (this) {
    is FirebaseFirestoreException -> when (code) {
        PERMISSION_DENIED -> CloudError.PERMISSION
        UNAUTHENTICATED -> CloudError.NOT_SIGNED_IN
        UNAVAILABLE, DEADLINE_EXCEEDED -> CloudError.OFFLINE
        else -> CloudError.GENERIC
    }
    is FirebaseNetworkException -> CloudError.OFFLINE
    is IllegalStateException -> CloudError.NOT_SIGNED_IN
    else -> CloudError.GENERIC
}
```
Errors from the list are shown in the list area. Errors from actions (resolve or delete) are shown as a Toast through `actionError`. The cloud detail screen has its own `error` field, so a permission or network problem is no longer shown as "post not found".

### 13.7 Step 5: Security rules

**`firestore.rules`** (paste it into Firestore → Rules → **Publish**)
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```
- `{userId}` takes the id from the path, and `request.auth.uid` is the uid of the user who sent the request. Access is allowed only when they are the same, so **you can only read and write your own data.** Anything not matched is denied by default.
- `{document=**}` is a recursive wildcard. With `rules_version = '2'` it also matches **zero** extra segments, so one rule covers both the `users/{uid}` document (where `fcmToken` lives) and `users/{uid}/posts/...`.
- `request.auth != null` means users who are not signed in get nothing.
- The database must be created in **production mode**, not test mode. Test mode opens everything to everyone for 30 days.
- **Rules Playground check:** a `get` of `/users/<my uid>/posts/x` signed in as my uid → **allowed**; the same path signed in as another uid → **denied**.

### 13.8 Step 6: Push notifications (FCM)

**Manifest**
```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<service android:name=".push.PushService" android:exported="false">
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>

<meta-data android:name="com.google.firebase.messaging.default_notification_channel_id"
    android:value="@string/notification_channel_id" />
<meta-data android:name="com.google.firebase.messaging.default_notification_icon"
    android:resource="@drawable/ic_stat_paw" />
```
`exported="false"` means only Google Play services (through the intent filter) can start the service, not other apps. The two `meta-data` entries make notifications that the **system** shows (app in the background) use the same channel and paw icon as ours.

**Service**: `push/PushService.kt`
```kotlin
class PushService : FirebaseMessagingService() {
    private val pushTokens: PushTokenRepository by inject()

    override fun onNewToken(token: String) {
        pushTokens.saveToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: getString(R.string.app_name)
        val body = message.notification?.body ?: message.data["body"].orEmpty()
        showNotification(this, title, body)
    }
}
```
- **App in the background:** Android/FCM shows a "notification" message by itself, using the channel from the manifest.
- **App open (foreground):** the system shows **nothing**. It calls `onMessageReceived` instead, and we build the notification ourselves with `showNotification(...)`. This is why the test works in both cases.
- `onNewToken` is called when the token changes (first install, reinstall, app data cleared). We save it again.
- **Deprecation:** in firebase-messaging 25.x, `onNewToken` and `getToken()` are marked deprecated in favour of the new `register()` / `onRegistered()`. In the default mode the SDK still calls `onNewToken`, which was confirmed from the library code. The service overrides **both** `onNewToken` and `onRegistered`, and only one of them fires.

**Notification helper (the Lecture 11 pattern)**: `util/Notifications.kt`
```kotlin
const val ALERTS_CHANNEL_ID = "qamqor_alerts"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val channel = NotificationChannel(ALERTS_CHANNEL_ID, context.getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_HIGH)
    context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
}

fun showNotification(context: Context, title: String, body: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    ) return
    val pendingIntent = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    val notification = NotificationCompat.Builder(context, ALERTS_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_paw)
        .setContentTitle(title).setContentText(body)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent).setAutoCancel(true)
        .build()
    NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
}
```
- **Channel:** on Android 8+ every notification needs a channel. It is created once, in `QamqorApp.onCreate()`.
- **`POST_NOTIFICATIONS` check:** on Android 13+ posting needs the runtime permission. Without it we quietly do nothing instead of crashing.
- Tapping the notification opens `MainActivity`. `FLAG_IMMUTABLE` is required on Android 12+.

**Saving the token**: `data/FirestorePushTokenRepository.kt`
```kotlin
override fun syncToken() {
    scope.launch {
        safely {
            val token = messaging.token.await()
            if (BuildConfig.DEBUG) Log.d("FCM", "FCM token: $token")
            store(token)
        }
    }
}

private suspend fun store(token: String) {
    val uid = auth.currentUser?.uid ?: return
    db.collection("users").document(uid)
        .set(mapOf("fcmToken" to token), SetOptions.merge())
        .await()
}

override suspend fun clearToken() {
    val uid = auth.currentUser?.uid ?: return
    withTimeoutOrNull(3_000) {
        db.collection("users").document(uid).update("fcmToken", FieldValue.delete()).await()
    }
}
```
- **When it is saved:** right after a successful sign-in or sign-up (`SetupViewModel` calls `pushTokens.syncToken()`), and again in `onNewToken`.
- **`SetOptions.merge()`** changes only the `fcmToken` field and keeps any other fields in `users/{uid}`. A plain `set()` would replace the whole document.
- It runs on its own `SupervisorJob` scope, so it keeps going after the login screen closes, and any error is caught and logged. **Saving the token can never break login.**
- **On sign out**, `clearToken()` deletes `fcmToken` before `auth.signOut()`. Otherwise pushes meant for the old user would keep coming to this phone after someone else signs in. The 3-second timeout means sign out does not hang without internet.
- The token is printed to Logcat (tag `FCM`) **only in debug builds**, so it can be copied for the console test message.

**Asking for the permission**: `SetupScreen.kt`
```kotlin
val notificationPermission = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestPermission(),
) { onDone() }

LaunchedEffect(state.done) {
    if (state.done) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !canPostNotifications(context)) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            onDone()
        }
    }
}
```
The permission is requested **after a successful sign-in or registration**, not at app launch. By then the user understands why the app wants to send notifications. Whatever they answer, the app then opens the feed.

### 13.9 Step 7: Analytics

**Wrapper**: `analytics/AnalyticsTracker.kt` (interface) + `FirebaseAnalyticsTracker(analytics: FirebaseAnalytics)`
```kotlin
override fun logScreen(name: String) {
    analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
        putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
        putString(FirebaseAnalytics.Param.SCREEN_CLASS, name)
    })
}
override fun logPostCreated(status: String) = log("post_created", "status", status)
override fun logPostResolved(species: String) = log("post_resolved", "species", species)
override fun logPostDeleted(status: String) = log("post_deleted", "status", status)
```

**Custom events** (each has one parameter and no personal data):

| Event | Parameter | When |
|---|---|---|
| `post_created` | `status` = lost / found / adopt | `CreateViewModel`, after publishing |
| `post_resolved` | `species` = dog / cat / other | `MyPostsViewModel`, when a post is marked solved |
| `post_deleted` | `status` | `MyPostsViewModel`, on delete |

- No email, name, phone, address or description is ever sent. Only categories are.
- Names follow the rules: letters, digits and `_`, start with a letter, at most 40 characters, and no `firebase_` / `google_` / `ga_` prefix.
- Events are logged in **ViewModels** through the interface, so the unit tests check them with a fake tracker.

**SCREEN_VIEW**: `ui/components/TrackScreen.kt`
```kotlin
@Composable
fun TrackScreen(name: String) {
    val tracker = koinInject<AnalyticsTracker>()
    LaunchedEffect(Unit) { tracker.logScreen(name) }
}
```
It is called at the top of every main screen: `login`, `feed`, `favorites`, `facts`, `profile`, `create`, `detail`, `my_post_detail`. `LaunchedEffect(Unit)` runs once when the screen first appears, not on every recomposition.

**Viewing events right away:**
```
adb shell setprop debug.firebase.analytics.app com.example.qamqorapp
```
Then open Analytics → **DebugView**. Normal reports take up to 24 hours. Turn debug mode off with `adb shell setprop debug.firebase.analytics.app .none.`

### 13.10 Design decisions

- **A separate `CloudPostsRepository` next to the Room `PostRepository`.** The demo feed (posts by other people, seeded) stays in Room, because the rules let a user read **only their own** data, so a shared public feed cannot come from `users/{uid}`. The user's **own** posts live in Firestore. Nothing Room-based broke.
- **Result:** a new post appears live in **Profile → My posts** and its detail screen. It does **not** appear in the Feed or Favorites tabs, which are still the local Room demo data. The `favorite` flag of a cloud post is stored in Firestore and only shown on its detail screen. Posts created before SIS7 (stored only in Room) no longer show in Profile.
- Analytics are logged in ViewModels, not in repositories, so they can be unit-tested.
- Writes are not awaited, so the app works offline (see 13.6).

### 13.11 Tests and build

- `./gradlew :app:assembleDebug :app:testDebugUnitTest` → **BUILD SUCCESSFUL**, **23 tests, 0 failures**.
  - `MyPostsViewModelTest` (new, 5 tests): the first value is `Loading`; a new list from the fake repo becomes `Success`; a failing flow becomes `Error`; resolve and delete update the repo and log `post_resolved` / `post_deleted`.
  - `SetupViewModelTest` (6 tests): an error becomes a typed `AuthError`; editing a field clears the error; `syncToken()` is called once after success and never after a failure; an unknown exception maps to `GENERIC`.
  - `KoinModulesTest`: the Koin graph check now knows `FirebaseAuth`, `FirebaseFirestore`, `FirebaseAnalytics` and `FirebaseMessaging` as types that come from outside.
- To build from a terminal: `JAVA_HOME = C:\Program Files\Android\Android Studio\jbr`.
- An independent code review found no blockers. Its fixes are included: no endless spinner offline, the token is cleared on sign out, real errors are shown on the cloud detail screen, the token is logged only in debug, and sign out finishes before navigating.
- Not tested yet on a real device or emulator: live updates, push and Analytics. Use the checklist below.

### 13.12 Things to do in the Firebase console and on a device (defense checklist)

**Part 1 (Steps 1–3)**
1. Authentication → Sign-in method → **Email/Password: Enabled**.
2. Run the app → **Create account** (password of 6+ characters) → the feed opens. The user appears in Authentication → Users.
3. Try a wrong password, an email that is already used, a password of `123`, and airplane mode. Each shows its own readable message.
4. Kill the app and reopen it. It goes straight to the feed. Profile → **Sign out** → login screen.

**Part 2 (Steps 4–7)**
5. Firestore Database → **Create database → Production mode** → a nearby region.
6. Firestore → Rules → paste `firestore.rules` → **Publish**. Check it in the **Rules Playground**: your own uid is allowed, another uid is denied.
7. Create a post in the app. It appears in the console at `users/{uid}/posts/...`. Change `name` or `resolved` in the console, and Profile → My posts updates **without a refresh**. Delete in the app, and it disappears in the console.
8. Sign in on Android 13+ → allow notifications. The console shows `users/{uid}.fcmToken`. Copy the token from Logcat (filter `FCM`).
9. Messaging → New campaign → Notifications → **Send test message** → paste the token. Send it once after pressing Home (background) and once with the app open (foreground). A notification appears both times.
10. Run `adb shell setprop debug.firebase.analytics.app com.example.qamqorapp` → Analytics → **DebugView**. You should see `screen_view`, `post_created`, `post_resolved` and `post_deleted`.
