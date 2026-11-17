# SIS 5 — Факты о питомцах

Короткая шпаргалка по вкладке «Факты»: что происходит, где лежит код и как
проверить каждый сценарий.

---

## 1. Что делает экран

На вкладке **«Факты» / “Facts”** показываются 5 фактов о кошках и 5 фактов о
собаках.

- **Английский язык** — факты загружаются из двух бесплатных API:
  - `https://catfact.ninja/facts?limit=5`
  - `https://dogapi.dog/api/v2/facts?limit=5`
- **Русский язык** — факты берутся из заранее подготовленного списка
  `LocalPetFacts`. Второй API перевода не нужен, поэтому русский режим работает
  даже без интернета.

Названия вкладки, заголовок и подзаголовок переводятся через `strings.xml`
и `values-ru/strings.xml`.

---

## 2. Путь данных

```text
Compose Screen
    ↓ события (Retry)
PetFactsViewModel
    ↓ getFacts(language)
PetFactsRepository
    ├── language == "ru" → LocalPetFacts
    └── language == "en" → CatFactsApi + DogFactsApi
                              ↓
                         PetFact
```

`PetFact` — чистая доменная модель:

```kotlin
data class PetFact(
    val text: String,
    val emoji: String, // 🐱 или 🐶
)
```

---

## 3. Файлы

| Файл | Зачем |
|---|---|
| `data/remote/PetFactsApi.kt` | DTO и Retrofit-интерфейсы для двух API |
| `data/remote/PetFactsClient.kt` | Два Retrofit-клиента, общий JSON и таймауты |
| `data/repository/PetFactsRepositoryImpl.kt` | Выбирает локальный или сетевой источник, переводит DTO в `PetFact` |
| `data/local/LocalPetFacts.kt` | Готовый русский список: 5 фактов о кошках и 5 о собаках |
| `domain/model/PetFact.kt` | Модель одного факта |
| `domain/repository/PetFactsRepository.kt` | Интерфейс репозитория |
| `ui/facts/PetFactsViewModel.kt` | Состояния `Loading / Success / Error`, загрузка и Retry |
| `ui/facts/PetFactsScreen.kt` | Route + stateless Screen, карточки фактов |
| `ui/QamqorFactory.kt` | Создаёт `PetFactsViewModel` |
| `AppContainer.kt` | Создаёт `PetFactsRepositoryImpl` |

---

## 4. Состояния ViewModel

```kotlin
sealed interface PetFactsUiState {
    data object Loading : PetFactsUiState
    data class Success(val facts: List<PetFact>) : PetFactsUiState
    data class Error(val message: String) : PetFactsUiState
}
```

Экран использует `when (uiState)`:

- `Loading` → индикатор загрузки;
- `Success` → `LazyColumn` с карточками;
- `Error` → сообщение и кнопка **Retry**.

`Retry` вызывает `loadFacts()` повторно. Ошибка не роняет приложение: она
превращается в состояние `Error`.

---

## 5. Смена языка

`PetFactsViewModel` подписан на `SettingsRepository.language`. Когда пользователь
меняет язык в профиле, ViewModel перезагружает факты:

- выбрали **RU** → показывается русский локальный список;
- выбрали **EN** → идёт запрос к двум API.

Если запрос к API не удался, в английском режиме появится `Error` с Retry.
Русский режим от сети не зависит.

---

## 6. Как проверить

1. Запустить приложение.
2. Профиль → Язык → **RU** → вкладка **«Факты»**.
   - Должны быть 5 фактов о кошках и 5 о собаках на русском.
3. Выключить интернет и снова открыть «Факты».
   - Русский список должен остаться на экране.
4. Профиль → Язык → **EN** → «Факты».
   - Должны загрузиться английские факты из API.
5. Выключить интернет в английском режиме.
   - Появится `Error` и кнопка **Retry**.
6. Включить интернет и нажать **Retry**.
   - Факты загрузятся снова.

---

## 7. Почему русский список локальный

Перевод фактов через отдельный API добавляет второй сетевой сервис, лимиты и
зависимость от него. Для учебного экрана достаточно заранее подготовленного
русского списка: он быстрый, предсказуемый и работает офлайн. При этом
английский режим продолжает демонстрировать полный путь **Retrofit → Repository
→ ViewModel → Screen**.
