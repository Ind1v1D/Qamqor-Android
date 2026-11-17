# Qamqor 🐾

Qamqor is a small Android app for **lost & found pets in Almaty**: a feed of lost,
found and rehoming announcements, a detail screen, a publish form, favourites and a
profile — all written in **Kotlin + Jetpack Compose**, following **MVVM**
(View → ViewModel → Repository) with a unidirectional data flow.

Built for SIS 3 (screens & styling) and refactored for **SIS 4 (MVVM)**.

## Screens

| | |
|---|---|
| ![Setup](design/01-setup.png) | ![Feed](design/02-feed.png) |
| ![Detail](design/03-detail.png) | ![Create](design/04-create.png) |
| ![Favorites](design/05-favorites.png) | ![Profile](design/06-profile.png) |

## Architecture (MVVM)

The full diagram, the UiState fields, the events and the trace live in
[`design/07-architecture.md`](design/07-architecture.md). The short version:

```
View (stateless)        Route                    ViewModel                Repository
FeedScreen(uiState, …) ─ FeedRoute ────────────► FeedViewModel ─────────► PostRepository (interface)
   events ↑             collectAsStateWithLifecycle  _uiState: MutableStateFlow     │
   state  ↓                                          uiState  : StateFlow           ▼
                                                                              RoomPostRepository
                                                                              (delay + shouldFail)
                                                                              qamqor.db
```

- **View** — `ui/**/…Screen.kt` composables take `uiState` + lambdas and hold **no
  business logic**. They never import a ViewModel.
- **Route** — a tiny `…Route` composable (e.g. `FeedRoute`, `DetailRoute`) is the
  only place that knows the ViewModel: it calls `qamqorViewModel<…>()`,
  `collectAsStateWithLifecycle()` and passes methods as lambdas.
- **ViewModel** — one per screen. State is a private `MutableStateFlow<…UiState>`
  exposed as a read-only `StateFlow`, always updated with `copy()`.
- **Domain** — `GetFeedUseCase`, `ToggleFavoriteUseCase` ("the recipe book").
- **Repository** — `PostRepository` (interface) / `RoomPostRepository`, the single
  owner of the data. It behaves like a server: every read `delay(600)`s, and it can
  throw on demand (`shouldFail`).

### Trace one click (real names)

Tapping the star on a feed card:

```
PostCard.onToggleFavorite
  → FeedScreen's lambda onFavouriteClick(post)          (ui/feed/FeedScreen.kt)
  → FeedViewModel.onFavouriteClick(post)                (ui/feed/FeedViewModel.kt)
  → ToggleFavoriteUseCase(post)                         (domain/ToggleFavoriteUseCase.kt)
  → PostRepository.toggleFavorite(post)                 (data/RoomPostRepository.kt)
  → Room UPDATE posts SET isFavorite …  → re-emits the feed Flow
  → FeedViewModel.render(FeedResult.Content) → _uiState.update { it.copy(posts = …) }
  → FeedRoute.collectAsStateWithLifecycle() → FeedScreen recomposes (star flips)
```

### The four states

`FeedUiState` covers everything the assignment asks for:

| Situation | How it is expressed | Preview |
|---|---|---|
| loading | `isLoading = true` | `FeedPreviews.kt` → "Feed · loading" |
| content | `isContent` (non-empty list) | "Feed · content" |
| empty | `isEmpty` (loaded, no matches) | "Feed · empty" |
| error | `errorMessage != null` (`hasError`) | "Feed · error (dark)" |

Open `ui/feed/FeedPreviews.kt` in Android Studio: all four states render without
running the app, which is the proof the screen no longer depends on the ViewModel.

## Simulating an error

**Profile → Settings → "Demo: simulate error" → On.** The next feed load fails
after the 600 ms delay and the feed shows the error state with a **Retry** button;
switch it Off (or tap Retry) to recover.

## State survives rotation

Search text, filters and favourites live in the ViewModel / Room, so they are kept
when the Activity is recreated.

Logcat while rotating the phone (`adb logcat -s QamqorLifecycle QamqorVM`):

```
D/QamqorLifecycle: onPause
D/QamqorLifecycle: onStop
D/QamqorLifecycle: onDestroy          <- the Activity is thrown away…
D/QamqorLifecycle: onCreate           <- …and rebuilt for the new orientation
D/QamqorLifecycle: onStart
D/QamqorLifecycle: onResume
```

Notice what is **not** there: `FeedViewModel init` and `onCleared` do not appear
again. The Activity was destroyed and recreated, but the ViewModel was not. A
ViewModel lives in a `ViewModelStore` that the framework keeps across a
configuration change (it is handed to the new Activity instead of being rebuilt),
so `FeedViewModel` — and with it the private `MutableStateFlow` holding the search
text and the filters — is the same object on the other side of the rotation.
`onCleared` only runs when the screen is really finished (e.g. leaving it for good),
which is why it is absent here.

## Testing

```
./gradlew test
```

`app/src/test/…/ui/feed/FeedViewModelTest.kt` unit-tests the feed ViewModel against
a hand-written fake `PostRepository` (loading→content, the error state, retry,
search and the empty state) — no emulator needed.

## SIS 5 — Pet facts from the API

In the English mode the «Факты» / «Facts» tab loads five cat facts from
`https://catfact.ninja` and five dog facts from `https://dogapi.dog` with Retrofit.
In the Russian mode the same screen shows a prewritten Russian list from
`LocalPetFacts` (five cat and five dog facts), so no translation API is needed.
Both sources are mapped into one `PetFact` model and shown through three explicit
states: `Loading`, `Success`, `Error`. The error state has a Retry button, so
turning the network off is safe in the English mode.

How it works, file by file: [`SIS5_CHEATSHEET.md`](SIS5_CHEATSHEET.md).

## Where the data is stored

- **Room** database `qamqor.db` (tables `posts`, `profile`) — on device only, at
  `/data/data/com.example.qamqorapp/databases/qamqor.db`. Not in the repo or the APK.
- **SharedPreferences** `settings_prefs` — the theme and language choice.

## Build & run

```
./gradlew assembleDebug     # build
./gradlew installDebug      # install on a connected device/emulator
```

Requires Android Studio / JDK 17+. `minSdk 24`, `targetSdk 37`.

## Origin of the work

This implementation was written with AI assistance; see
[`AI_USAGE.md`](AI_USAGE.md) for what was generated, what was changed, and one place
where the AI put logic in the wrong layer.
