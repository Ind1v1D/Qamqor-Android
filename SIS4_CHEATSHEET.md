# SIS 4 — План и шпаргалка (MVVM в Qamqor)

> Этот документ — сразу и **план** (что вообще нужно было сделать),
> и **шпаргалка** (что и как уже сделано, простыми словами).
> Он написан так, чтобы его понял человек, который не писал этот код.

---

## 0. Как этим пользоваться

- Раздел 1 — **план**: список всех задач по пунктам.
- Разделы 2–5 — **объяснение**: словарь, шаги, карта файлов, пример потока.
- Раздел 6 — **сценарий защиты**: что показать и что сказать.
- Раздел 7 — **чек-лист**: что осталось сделать вам, студенту.
- Раздел 8 — **частые вопросы**.

---

## 1. План: что требовалось по SIS 4

| # | Пункт | Баллы | Сделано |
|---|-------|:-----:|:-------:|
| A | Архитектурный план в папке `/design`, **закоммичен первым** | 10 | ✅ |
| B1 | Repository владеет данными + `delay()` + имитация ошибки (`shouldFail`) | 15 | ✅ |
| B2 | UiState (только `val`) + приватный `MutableStateFlow` / публичный `StateFlow` + `copy()` + 4 состояния + 3–4 действия | 25 | ✅ |
| B3 | Stateless-экраны + `…Route` с `collectAsStateWithLifecycle()` + `@Preview` на 4 состояния | 20 | ✅ |
| B4 | `Log.d` в жизненном цикле Activity + `init`/`onCleared` у ViewModel + объяснение поворота | 10 | ✅ |
| C | README с архитектурой + `AI_USAGE.md` с «логикой не в том слое» + осмысленные коммиты | 20 | ✅ |
| Бонус | Domain-слой: 2 use case | +10 | ✅ |
| Бонус | `SavedStateHandle` в detail-экране | +10 | ✅ |
| Бонус | Юнит-тест на функцию ViewModel | +10 | ✅ |

### Порядок работ (фазы)

1. **A** — нарисовать архитектуру в `design/` и закоммитить.
2. **B1** — сделать Repository «как сервер»: `delay` и флаг ошибки.
3. **B2** — привести `FeedViewModel` к 4 состояниям через `StateFlow`.
4. **B3** — разбить каждый экран на `…Route` (знает VM) и `…Screen` (чистый UI).
5. **B4** — расставить логи и описать поворот.
6. **C** — README и AI_USAGE.
7. **Бонусы** — domain-слой, `SavedStateHandle`, тест.
8. Собрать, прогнать тесты, закоммитить по фазам.

---

## 2. Словарь простыми словами

Аналогия — **ресторан**:

| Термин | Простыми словами | В нашем проекте |
|---|---|---|
| **View** | Зал и официант: показывает еду, принимает заказ | Compose-экраны `…Screen.kt` |
| **ViewModel** | Повар: помнит заказ и готовит состояние | `FeedViewModel`, `DetailViewModel` и др. |
| **UiState** | Поднос с едой: всё, что видит посетитель | `data class FeedUiState` |
| **Repository** | Холодильник: единственное место, где лежат продукты | `PostRepository` / `RoomPostRepository` |
| **StateFlow** | Табло: показывает «текущее состояние», само обновляется | `val uiState: StateFlow<FeedUiState>` |
| **Route** | Администратор: связывает официанта с поваром | `FeedRoute`, `DetailRoute` |
| **Use case** | Рецепт: одно конкретное действие | `GetFeedUseCase`, `ToggleFavoriteUseCase` |

Два главных правила MVVM:
1. **События — вверх** (экран → ViewModel), **состояние — вниз** (ViewModel → экран).
2. Экран ничего не знает о данных; данными владеет Repository, а состоянием — ViewModel.

---

## 3. Что и как сделано (по шагам)

### Шаг A. Архитектурный план

- **Файл:** `design/07-architecture.md`
- **Зачем:** показать стрелочками, кто чем владеет, до написания кода.
- **Что внутри:** диаграмма (Mermaid), таблица «экран → ViewModel → UiState»,
  список полей состояния, события и «trace одного клика».
- **Как проверить:** открыть файл на GitHub — диаграмма отрисуется сама.

```text
View (официант) → Route → ViewModel (повар) → Repository (холодильник) → Room
```

---

### Шаг B1. Слой данных (Repository)

- **Файлы:** `data/PostRepository.kt`, `data/RoomPostRepository.kt`,
  `data/PostDao.kt`, `AppContainer.kt`
- **Зачем:** данные должны жить в одном месте, а экраны — не знать про Room.
  Плюс нужно «уметь падать», чтобы показать состояние ошибки.

**1) Repository стал интерфейсом** — чтобы в тестах подменить его фейком:

```kotlin
interface PostRepository {
    fun feed(status: PostStatus?, species: Species?, district: District?, query: String): Flow<List<Post>>
    val shouldFail: StateFlow<Boolean>   // «сеть упала»
    fun setSimulateError(value: Boolean)
    // … остальные методы …
}
```

**2) Настоящая реализация ведёт себя как сервер** — с задержкой и ошибкой:

```kotlin
override fun feed(...): Flow<List<Post>> = flow {
    delay(600)                                    // как будто ждём ответ сети
    if (_shouldFail.value) throw IOException("Simulated network error")  // тестовое падение
    emitAll(posts.observeFeed(status, species, district, query))
}.flowOn(Dispatchers.IO)                           // ожидание не на главном потоке
```

**3) В запрос ленты добавлен поиск** по имени и породе (`PostDao.kt`).

- **Как проверить:** в приложении Профиль → Настройки → «Демо: имитация ошибки»
  → Вкл → лента покажет состояние **ошибки** с кнопкой «Повторить».

---

### Шаг B2. ViewModel и состояние ленты

- **Файл:** `ui/feed/FeedViewModel.kt`
- **Зачем:** всё, что пользователь не хочет потерять при повороте (поиск, фильтры),
  и все 4 состояния (загрузка/контент/пусто/ошибка) живут в одном объекте.

**UiState — один data-class, только `val` (менять нельзя):**

```kotlin
data class FeedUiState(
    val query: String = "",
    val filters: FeedFilters = FeedFilters(),
    val posts: List<Post> = emptyList(),
    val activeCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val hasError: Boolean get() = errorMessage != null
    val isEmpty: Boolean get() = !isLoading && errorMessage == null && posts.isEmpty()
    val isContent: Boolean get() = !isLoading && errorMessage == null && posts.isNotEmpty()
}
```

Четыре состояния: **loading** = `isLoading`, **error** = `hasError`,
**empty** = `isEmpty`, **content** = `isContent`.

**Приватное табло + публичное табло, изменения только через `copy()`:**

```kotlin
private val _uiState = MutableStateFlow(FeedUiState())   // менять может только ViewModel
val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()  // экран только читает

fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }
```

**Действия (события):** `onQueryChange`, `selectStatus`, `toggleSpecies`,
`selectDistrict`, `clearFilters`, `onFavouriteClick`, `onRetry`.

- **Как проверить:** напечатать текст поиска, повернуть телефон — текст остаётся
  (он в ViewModel, а не в `remember`).

---

### Шаг B3. Экраны: разделение на Route и Screen

- **Файлы:** все `ui/**/…Screen.kt`, `ui/components/Chrome.kt`,
  `ui/feed/FeedPreviews.kt`, `ui/navigation/AppShell.kt`
- **Зачем:** экран должен быть «глупым» — получает состояние и лямбды, ничего не решает.
  Тогда его можно показать в Preview и легко протестировать.

**Два уровня одного экрана:**

```kotlin
@Composable
fun FeedRoute(onOpenPost: (Long) -> Unit, onCreatePost: () -> Unit) {
    val viewModel = qamqorViewModel<FeedViewModel>()
    val state by viewModel.uiState.collectAsStateWithLifecycle()  // подписка на жизненный цикл
    FeedScreen(uiState = state, onQueryChange = viewModel::onQueryChange, /* … */)
}

@Composable
fun FeedScreen(uiState: FeedUiState, onQueryChange: (String) -> Unit, /* … */) {
    when {
        uiState.isLoading -> LoadingState()
        uiState.hasError  -> ErrorState(uiState.errorMessage!!, onRetry = …)
        uiState.isEmpty   -> EmptyState(…)
        else              -> LazyColumn { items(uiState.posts) { PostCard(…) } }
    }
}
```

Разбиты так: лента, детали, избранное, профиль, создание, настройка входа.
В `…Screen` нет ViewModel, нет логики фильтрации — только рисование.

**4 превью для ленты** (`FeedPreviews.kt`): loading, content, empty, error —
открываются прямо в Android Studio **без запуска приложения**.

- **Как проверить:** открыть `FeedPreviews.kt` в Android Studio → панель Preview →
  увидите 4 состояния.

---

### Шаг B4. Логи и объяснение поворота

- **Файлы:** `MainActivity.kt` (логи жизненного цикла), `…ViewModel.kt`
  (`init` / `onCleared`), объяснение в `README.md`
- **Зачем:** доказать, что Activity пересоздаётся, а ViewModel — нет.

```kotlin
override fun onCreate(...) { Log.d(TAG, "onCreate"); … }
override fun onStart()  { super.onStart();  Log.d(TAG, "onStart") }
override fun onResume() { super.onResume(); Log.d(TAG, "onResume") }
override fun onPause()  { super.onPause();  Log.d(TAG, "onPause") }
override fun onStop()   { super.onStop();   Log.d(TAG, "onStop") }
override fun onDestroy(){ super.onDestroy();Log.d(TAG, "onDestroy") }
private companion object { const val TAG = "QamqorLifecycle" }
```

- **Как проверить:** запустить на устройстве, повернуть экран и смотреть
  `adb logcat -s QamqorLifecycle QamqorVM`. Будет видно `onDestroy` → `onCreate`,
  но **не** будет повторного `FeedViewModel: onCleared` — значит, ViewModel выжил.

---

### Шаг C. Документация и коммиты

- **Файлы:** `README.md`, `AI_USAGE.md`
- **README** содержит: описание, диаграмму, trace одного клика, таблицу 4 состояний,
  как включить ошибку, лог поворота, как запустить тесты.
- **AI_USAGE.md** отвечает на вопросы и описывает **случай «логика не в том слое»**:
  раньше `AppShell` читал репозиторий прямо в composable — вынесено в `AppShellViewModel`.

```kotlin
// БЫЛО (неверно): экран лезет в данные
val hasProfile by produceState<Boolean?>(null) { value = repository.currentProfile() != null }

// СТАЛО: данные уходят в ViewModel, экран только читает
class AppShellViewModel(private val repository: PostRepository, …) : ViewModel() {
    private val _hasProfile = MutableStateFlow<Boolean?>(null)
    val hasProfile: StateFlow<Boolean?> = _hasProfile.asStateFlow()
}
```

- **Как проверить:** `git log --oneline` — 9 осмысленных коммитов по фазам.

---

### Бонусы

- **Domain-слой:** `domain/GetFeedUseCase.kt`, `domain/ToggleFavoriteUseCase.kt` —
  ViewModel просит данные у «рецепта», а не у репозитория напрямую.
- **SavedStateHandle:** `ui/detail/DetailViewModel.kt` берёт `postId` из навигации —
  деталь переживает и поворот, и смерть процесса.
- **Тест:** `app/src/test/java/com/example/qamqorapp/ui/feed/FeedViewModelTest.kt` —
  фейковый репозиторий и 5 тестов (загрузка, ошибка, повтор, поиск, пусто).

---

## 4. Карта файлов

```text
design/07-architecture.md          план (A)
README.md, AI_USAGE.md             документация (C)

data/PostRepository.kt             интерфейс репозитория        (B1)
data/RoomPostRepository.kt         delay + имитация ошибки      (B1)
data/PostDao.kt                    поиск в ленте                (B1)
AppContainer.kt                    сборка зависимостей          (B1)

domain/GetFeedUseCase.kt           рецепт «получить ленту»      (бонус)
domain/ToggleFavoriteUseCase.kt    рецепт «переключить избранное» (бонус)

ui/feed/FeedViewModel.kt           4 состояния + StateFlow      (B2)
ui/feed/FeedScreen.kt              Route + статичный экран      (B3)
ui/feed/FeedPreviews.kt            4 превью                     (B3)
ui/components/Chrome.kt            Loading и Error состояния    (B3)
ui/navigation/AppShell.kt          навигация через Route        (B3)
ui/navigation/AppShellViewModel.kt hasProfile в ViewModel        (B3/C)
ui/QamqorFactory.kt                фабрика + SavedStateHandle   (бонус)
ui/detail/DetailScreen.kt          Route + статичный экран      (B3)
ui/detail/DetailViewModel.kt       SavedStateHandle             (бонус)
ui/favorites|profile|create|setup/ Route + статичный экран      (B3)
MainActivity.kt                    6 логов жизненного цикла     (B4)
res/values{,-ru}/strings.xml       тексты и подписи             (B3)
app/src/test/.../FeedViewModelTest.kt  юнит-тесты               (бонус)
gradle/libs.versions.toml, app/build.gradle.kts  тест-зависимость (бонус)
```

---

## 5. Как течёт один клик (trace)

Пользователь нажимает звёздочку на карточке в ленте:

```text
тап по ☆ в PostCard
 → onFavouriteClick(post)            (ui/feed/FeedScreen.kt)
 → FeedViewModel.onFavouriteClick    (ui/feed/FeedViewModel.kt)
 → ToggleFavoriteUseCase(post)       (domain/ToggleFavoriteUseCase.kt)
 → PostRepository.toggleFavorite     (data/RoomPostRepository.kt)
 → Room: UPDATE posts → лента переизлучается
 → FeedViewModel.render(Content) → _uiState.update { it.copy(posts = …) }
 → FeedRoute.collectAsStateWithLifecycle() → FeedScreen перерисовывается (звёздочка)
```

Один и тот же `PostCard` обновится и в избранном, и в деталях — потому что все
экраны смотрят в одну таблицу.

---

## 6. Сценарий защиты (что показать)

1. **Диаграмму** `design/07-architecture.md` — расскажите про 4 слоя.
2. **Ротацию:** напечатайте поиск → поверните телефон → поиск сохранился →
   покажите Logcat (`onDestroy`→`onCreate`, но без `onCleared`).
3. **Ошибку:** Профиль → Настройки → «Демо: имитация ошибки» → Вкл → покажите
   состояние ошибки → «Повторить» → Выкл → восстановление.
4. **Превью:** откройте `FeedPreviews.kt` — 4 состояния без запуска приложения.
5. **Тесты:** `./gradlew test` — 5 зелёных тестов.
6. **Код:** покажите `FeedViewModel` (приватный/публичный `StateFlow`, `copy()`)
   и `FeedRoute` (`collectAsStateWithLifecycle`).

---

## 7. Чек-лист сдачи (что осталось сделать вам)

- [ ] Запустить приложение на устройстве/эмуляторе.
- [ ] Повернуть экран и **вставить свой реальный Logcat** в `README.md`
      (сейчас там показательный пример).
- [ ] Сделать скриншоты 4 состояний и поворота.
- [ ] Проверить `git status` (должно быть чисто) и сделать `git push`.
- [ ] Публичный репозиторий + загрузка в Teams.
- [ ] Если запускаете сборку из терминала — задать `JAVA_HOME` на JDK Android Studio:
      `C:\Program Files\Android\Android Studio\jbr`.

---

## 8. Мини-FAQ

**Почему `data class`, а не `sealed interface`?**
Рубрика просит data-class только с `val`. 4 состояния удобно вывести флагами
(`isLoading`, `hasError`, `isEmpty`, `isContent`). `sealed` — тоже верный вариант,
но не под этот пункт.

**Зачем `delay()` в Repository?**
Чтобы состояния «загрузка» и «ошибка» были настоящими, а не мгновенными.

**Почему после поворота нет `onCleared`?**
ViewModel хранится в `ViewModelStore`, который переживает смену конфигурации,
поэтому объект остаётся тем же, а состояние — сохранённым.

**Что можно и нельзя в `remember`?**
Можно — чисто UI-штуки (открыт ли диалог). Нельзя — поиск, фильтры, данные:
иначе они исчезнут при повороте.
