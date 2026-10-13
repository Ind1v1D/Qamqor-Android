# AI usage log

Same questions as SIS 3, plus the new SIS 4 one about a piece of logic that ended up
in the wrong layer.

## 1. Which AI tools did you use, and for what?

- **ChatGPT / Claude (chat)** — for explaining Compose concepts (state hoisting,
  `StateFlow`, `flatMapLatest`, `SavedStateHandle`) and for reviewing snippets.
- **OpenCode (agent in the editor)** — for the larger refactors: splitting the
  screens into `…Route` + stateless `…Screen`, extracting the `PostRepository`
  interface, and writing the first draft of the unit test.

## 2. What prompts did you use?

Roughly, in order:

1. "Explain the difference between `remember`, `rememberSaveable` and a ViewModel,
   with a Compose example."
2. "Refactor this screen so it receives `uiState` and lambdas and does not know the
   ViewModel exists. Keep the visual output identical."
3. "Turn `FeedViewModel` into a `MutableStateFlow` + read-only `StateFlow` with
   loading/content/empty/error."
4. "Wrap the Room flows so the repository simulates a network `delay()` and can be
   forced to fail with a flag."
5. "Write a unit test for `FeedViewModel` using a fake repository."

## 3. What did the AI get wrong, or what did you have to fix?

- It first used `sealed class` for the whole `FeedUiState`; the brief asks for a
  **data class with only `val`s**, so I kept the four states as *derived* properties
  (`isLoading`, `isEmpty`, `hasError`, `isContent`) on one data class.
- It suggested putting a `remember { mutableStateOf(searchText) }` in the feed. That
  state would vanish on rotation, so it moved into the ViewModel.
- It sometimes forgot that `stateIn(WhileSubscribed)` needs an initial value; the
  screens then rendered before the first emission and flashed an empty list. Fixed
  with a proper default `FeedUiState(isLoading = true)`.

## 4. One place where AI put logic in the wrong layer — and how I moved it

The AI's first version of `AppShell` read the repository **inside a composable**:

```kotlin
// WRONG — a View doing data access
val repository = (context.applicationContext as QamqorApp).container.repository
val hasProfile by produceState<Boolean?>(null) {
    value = repository.currentProfile() != null
}
```

That is the Model being touched by the View: it cannot be previewed, it cannot be
tested, and it breaks the "events up, state down" rule. I moved the data access into
a ViewModel:

```kotlin
// ui/navigation/AppShellViewModel.kt
class AppShellViewModel(
    private val repository: PostRepository,
    settings: SettingsRepository,
) : ViewModel() {
    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()

    init { viewModelScope.launch { _hasProfile.value = repository.currentProfile() != null } }
}
```

and the shell now only collects it:

```kotlin
// ui/navigation/AppShell.kt
val viewModel = qamqorViewModel<AppShellViewModel>()
val hasProfile by viewModel.hasProfile.collectAsStateWithLifecycle()
```

A second, smaller case: the feed's subtitle ("Almaty · N active posts") was being
computed inside the composable. Counting is presentation, so it stayed there, but
the **count itself** is business data and now comes from the state
(`FeedUiState.activeCount`), fed by the repository's `observeActiveCount()` — not
from the screen querying Room.
