# Qamqor — Lost, Found & Adoption Pet Board for Almaty

> A native Android app that replaces chaotic social-media chat posts with a
> structured, category-based board for lost, found and adoptable pets in Almaty.

| Light theme | Dark theme |
| --- | --- |
| ![Feed (light)](README/screenshots/01_feed_light.png) | ![Feed (dark)](README/screenshots/01_feed_dark.png) |

---

## Table of contents

- [Overview](#overview)
- [Screens](#screens)
- [Features](#features)
- [Tech stack](#tech-stack)
- [Architecture](#architecture)
- [Project structure](#project-structure)
- [Navigation](#navigation)
- [Design system](#design-system)
- [Localization](#localization)
- [Build and run](#build-and-run)
- [Demo script](#demo-script)
- [Known limitations](#known-limitations)
- [Design concept and sketches](#design-concept-and-sketches)
- [AI usage](#ai-usage)

---

## Overview

**Qamqor** («Қамқор» — *care*) is a single-user, offline-first pet listing app.

- **No login.** A local profile is created on the first run.
- **No network.** Everything is stored in a Room database on the device.
- **No photo upload.** Emojis act as photo placeholders (see [limitations](#known-limitations)).

## Screens

| # | Screen | Route | Purpose |
| --- | --- | --- | --- |
| 1 | **Setup** | `setup` | First-run profile (name + phone) |
| 2 | **Feed** | `feed` | All posts, status segments, filters, search |
| 3 | **Detail** | `detail/{postId}` | Full post, info cells, call / WhatsApp / Telegram |
| 4 | **Create** | `create` | New post form (category, status, district, emoji) |
| 5 | **Favorites** | `favorites` | Saved posts |
| 6 | **Profile** | `profile` | Own posts, edit profile, sign out |

Every screen is wrapped in a `Scaffold` with a `TopAppBar`; every screen except
the start screen has a working back button.

## Features

- 🐾 Status segments — **Lost / Found / Adoption** — with a tap counter
- 🔍 Search by title + filters by category and district (chips)
- ⭐ Favorites with an empty state
- 📝 Create-post form with validation and a pinned submit bar
- 📞 Contact actions via Android intents (`Intent.ACTION_DIAL`, WhatsApp, Telegram)
- 🌓 Full **dark mode** support driven by the system setting
- 🔤 Two languages — **English (default)** and **Russian**
- 📜 Optimized lists: `LazyColumn` (posts) and `LazyRow` (chips)
- ♿ Touch targets ≥ 48 dp, title text truncates with an ellipsis

## Tech stack

| Layer | Choice |
| --- | --- |
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (`StateFlow` → `collectAsStateWithLifecycle`) |
| Persistence | Room (DAO, entities, `Flow`) |
| Navigation | Navigation Compose with a `postId` argument |
| DI | Manual (`AppContainer` + ViewModel factory) — no Hilt |
| Build | AGP 9.4.1, built-in Kotlin, version catalog (`libs.versions.toml`) |
| State | `remember { mutableStateOf(...) }` inside screens, hoisted flows in ViewModels |

## Architecture

```
UI (Compose)  ── observes ──▶  ViewModel (StateFlow)
     │                                │
     │ emits events                   │ calls
     ▼                                ▼
   Route  ◀── Navigation ───────  Repository ◀── Room DAO
```

- **Single Activity** (`MainActivity`) hosts `AppShell` → `NavHost`.
- `AppContainer` (in `QamqorApp`) owns the database and repository; ViewModels
  receive it through `QamqorFactory`.
- The database is **seeded on cold start** with demo posts so the app is never
  empty on first launch.

## Project structure

```
app/src/main/java/com/example/qamqorapp/
├── MainActivity.kt          # single Activity, edge-to-edge setup
├── QamqorApp.kt             # Application + manual DI container
├── data/
│   ├── db/                  # Room: entity, DAO, database
│   ├── model/               # Post, PostStatus, PostCategory, District
│   ├── Seed.kt              # demo content (localized strings)
│   └── PostRepository.kt
└── ui/
    ├── navigation/          # AppShell (NavHost), BottomBar, Routes
    ├── components/          # reusable composables + @Preview
    ├── theme/               # Color, Type, Theme, Dimens, Spacing
    ├── feed/ detail/ create/ favorites/ profile/ setup/
    └── QamqorViewModel.kt   # factory / shared state glue
```

## Navigation

```kotlin
NavHost(navController, startDestination = Routes.Setup) {
    composable(Routes.Feed)
    composable(Routes.Favorites)
    composable(Routes.Profile)
    composable(Routes.Create)
    composable(
        route = "detail/{postId}",
        arguments = listOf(navArgument("postId") { type = NavType.LongType })
    ) { backStackEntry ->
        DetailScreen(
            postId = backStackEntry.arguments?.getLong("postId") ?: -1L,
            onBack = { navController.popBackStack() }
        )
    }
}
```

The bottom bar is hidden on `setup`, `create` and `detail` so the pinned
action bars do not overlap it.

## Design system

Ported 1:1 from the HTML design concept (see `/design`).

**Palette**

| Token | Light | Dark | Used for |
| --- | --- | --- | --- |
| `pine` | `#AE4E2A` | `#B65A33` | App bars, primary actions |
| `pine-2` | `#8B3D22` | `#7A3319` | Gradient end, pressed state |
| `amber` | `#D99A3B` | `#DFA84C` | The single bright accent, FAB |
| `bg` | `#F7EFDF` | `#1C140D` | Screen background |
| `panel` | `#FFF9EE` | `#241B11` | Cards, panels |
| `line` | `#E3D7C1` | `#3A2C1E` | Hairlines, borders |

All colours live in `ui/theme/Color.kt` and are read through
`LocalQamqorColors` — **screens contain no hardcoded colours, font sizes or
spacing values**; sizes are in `Dimens`, spacing in `Spacing`, type in `Type.kt`.

**Typography** — *Zilla Slab* for headings, *Inter* for body text.

**Accessibility** — every interactive element is at least **48 × 48 dp**;
images/emojis expose `contentDescription`.

## Localization

| Locale | Folder | Default |
| --- | --- | --- |
| English | `app/src/main/res/values/strings.xml` | ✅ |
| Russian | `app/src/main/res/values-ru/strings.xml` | — |

All user-facing text **and** the seeded demo content come from string
resources, so switching the system language re-seeds readable content.

## Build and run

**Prerequisites:** Android Studio (bundled JDK).

```bash
# Debug build
gradlew assembleDebug

# Install on a connected device / emulator
gradlew installDebug
```

Or just open the project in Android Studio and press **Run ▶**.

## Demo script

1. **Setup** — enter a name and phone number → *Save*.
2. **Feed** — switch the status segments, scroll the list, tap a filter chip.
3. **Detail** — open a post, press *Call*, go **back**.
4. **Create** — fill the form, pick a category/status/district → *Publish*.
5. **Favorites** — star a post, view it in the Favorites tab.
6. **Profile** — see your posts, edit the profile, try the dark theme
   (system → *Dark theme*) — colours switch automatically.

## Known limitations

- **No real photos.** Emojis act as placeholders; adding Coil + a photo picker
  is the next step.
- **No accounts / no backend.** The profile lives on the device only.
- **Seeded data is re-created** after clearing app storage
  (`adb shell pm clear com.example.qamqorapp`).

## Design concept and sketches

- `/design` — labeled layout sketches for all six screens
  (`01-setup.png` … `06-profile.png`).
- The HTML design concept (palette, spacing and component specs) was the
  source of truth for the Compose theme.

## AI usage

AI was used as a **mentor and pair-programmer**, not as a black box: it
explained the architecture, reviewed layout bugs and suggested refactors, while
every decision, edit and commit was made and verified by me.

Full breakdown, prompts and examples: **[`AI_USAGE.md`](AI_USAGE.md)**

## License

Educational project — SIS 3 coursework, 2026.
