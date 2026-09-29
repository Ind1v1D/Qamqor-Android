# Qamqor — Local Stray Animal Rescue & Lost Pet Board

Qamqor — нативное Android-приложение для поиска, публикации и пристройства потерянных, найденных и бездомных животных в городах Казахстана. Вместо разрозненных постов в соцсетях — один централизованный, фильтруемый борд с геопривязкой к району и прямым звонком автору объявления.

## Основные возможности

- **Централизованная лента** — хронологический список объявлений: потеряно / найдено / пристройство.
- **Фильтрация и поиск** — по типу животного, статусу, поиску по району.
- **Детальная карточка** — фото, дата и время «видели последний раз», район, приметы, описание, контакт автора.
- **Прямой звонок** — кнопка запускает системный номеронабиратель через Android Intent.
- **Публикация поста** — кличка/описание, категория, район, телефон, фото (камера или галерея).
- **Избранное (Watchlist)** — закладка на любом посте, локально, без сети.
- **Профиль** — статистика пользователя, список своих постов с возможностью отметить «решено» или удалить.
- **Офлайн-хранение** — всё состояние приложения (посты, закладки) живёт в Room Database.

## Структура экранов (Jetpack Compose)

Каждый экран — `Scaffold`, разложенный на `topBar` и `content`. Ниже — разбивка composable-иерархии по фактическим wireframe-скетчам.

### 1. Лента (`FeedScreen`)
```
Scaffold
├─ topBar = TopAppBar
│    ├─ Text("Qamqor")
│    ├─ Text("Алматы · N активных объявлений")
│    └─ lazyRow — сегменты статуса (Все / Найдено / Потеряно / Пристройство)
├─ Row — чипы фильтров (Районы, Тип животного)
├─ content = LazyColumn
│    └─ items(posts) → Card
│         ├─ Box(photo)
│         ├─ Text(тег статуса)
│         ├─ Text(кличка/описание)
│         └─ Row(район, дата)
├─ floatingActionButton = FloatingActionButton("+") → CreatePostScreen
└─ bottomBar = NavigationBar
     └─ Row: Лента · Избранное · Профиль
```

### 2. Карточка питомца (`PetDetailScreen`)
```
Scaffold
├─ topBar = TopAppBar (прозрачный, поверх фото)
│    └─ NavigationIcon (назад)
├─ content = Column
│    ├─ Box(photo, полноширинное)
│    ├─ Text(кличка)
│    ├─ Row — Box(район) + Box(дата/время «видели»), пунктирная обводка
│    ├─ Column
│    │    └─ Row × 2 → Box(порода) + Box(статус), Box(приметы) + Box(автор)
│    └─ Text(description)
└─ bottomBar = Row(Button)
     ├─ OutlinedButton("★ В избранное")
     └─ Button("☎ Позвонить")
```

### 3. Новый пост (`CreatePostScreen`)
```
Scaffold
├─ topBar = TopAppBar
│    ├─ NavigationIcon (назад)
│    └─ Text("Новый пост")
└─ content = Column
     ├─ Row — категория (Потерян / Найден / Пристройство)
     ├─ Box — «Добавить фото» (галерея/камера, Activity Result Contract)
     ├─ OutlinedTextField — кличка/краткое описание
     ├─ OutlinedTextField — район
     ├─ OutlinedTextField — телефон для связи
     ├─ OutlinedTextField — описание
     └─ Button("Опубликовать")
```

### 4. Избранное (`WatchlistScreen`)
```
Scaffold
├─ topBar = TopAppBar
│    ├─ Text("Избранное")
│    └─ Text("N сохранённых объявлений")
├─ content = LazyColumn
│    └─ items(bookmarked) → Card (переиспользует компонент из FeedScreen)
│         ├─ Box(icon/photo)
│         ├─ Column — Text(кличка) + Row(мета)
│         └─ Icon(★, закладка)
└─ bottomBar = NavigationBar (таб «Избранное» активен)
```

### 5. Профиль (`ProfileScreen`)
```
Scaffold
├─ topBar = TopAppBar — Text("Профиль")
├─ content = Column
│    ├─ Avatar + Text(имя) + Text(телефон)
│    ├─ Row — статистика (опубликовано / решено / в избранном)
│    ├─ Text("Мои посты")
│    └─ LazyColumn
│         └─ items(myPosts) → Row(превью, текст, IconButton×2 — решено/удалить)
└─ bottomBar = NavigationBar (таб «Профиль» активен)
```

## Локальное хранение данных

Все посты и закладки персистятся через **Room Database**:

| Entity | Назначение |
|---|---|
| `PostEntity` | объявление: id, категория, кличка, район, телефон, фото, описание, статус, дата |
| `BookmarkEntity` | связь пользователь ↔ пост, для экрана Избранное |

## Технический стек

- Kotlin, Jetpack Compose
- Room Database — офлайн-хранение
- Activity Result Contracts — выбор фото (камера/галерея)
- Android Intent — прямой звонок из карточки поста

## Дизайн-система

- Фон `#F7EFDF`, панели `#FFFCF5`
- Основной цвет — тёплая ржавчина `#AE4E2A` (топбары, primary-кнопки)
- Акцент — золото `#D99A3B` (FAB, submit-кнопки, интерактивные элементы)
- Шрифты: **Zilla Slab** — бренд и заголовки, **Inter** — интерфейс и данные
- Скругления 10–16dp, тонкая обводка 1px вместо теней

## Сборка и запуск

**Требования:** Android Studio

```bash
git clone https://github.com/YOUR_USERNAME/Qamqor-Android.git
```

Открыть Android Studio → **Open** → выбрать склонированную папку `Qamqor`.
