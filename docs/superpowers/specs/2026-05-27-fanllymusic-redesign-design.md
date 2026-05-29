# FanllyMusic UI/UX Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Redesign FanllyMusic UI/UX to a modern, premium, smooth "Apple Music + Spotify" style, including proper permission onboarding.

**Architecture:** Component-first redesign. Keep existing NavGraph routes, replace layout structures in `ui/screen/` files using new reusable components in `ui/components/`.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Navigation Compose, Accompanist Permissions.

---

### Task 1: Android Manifest & Core Theme Updates

**Files:**
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Type.kt`

- [ ] **Step 1: Update AndroidManifest.xml**
Ensure `READ_MEDIA_AUDIO` is present. Ensure `READ_EXTERNAL_STORAGE` has `android:maxSdkVersion="32"`. Add `android:requestLegacyExternalStorage="true"` if needed.
(Manifest already looks mostly correct, just verify.)

- [ ] **Step 2: Update Color.kt**
```kotlin
package com.alfan.fanllymusic.ui.theme

import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val DarkSurface = Color(0xFF121212)
val DarkSurfaceVariant = Color(0xFF1C1C1E)
val White = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF9A9A9A)
val DividerColor = Color(0xFF262626)
val AccentPink = Color(0xFFFA2D55)

val GlassBackground = Color(0x99151515)
val GlassBorder = Color(0x26FFFFFF)
val OverlayDark = Color(0x8C000000)
```

- [ ] **Step 3: Update Theme.kt**
Use the new colors for the dark color scheme.
```kotlin
private val DarkColorScheme = darkColorScheme(
    primary = AccentPink,
    onPrimary = White,
    secondary = TextSecondary,
    onSecondary = White,
    background = Black,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = White
)
```

- [ ] **Step 4: Update Type.kt**
Add standard Typography weights and sizes to match premium feel.

---

### Task 2: Create Reusable Components (EmptyState, SearchBar)

**Files:**
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/EmptyState.kt`
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/SearchBar.kt`

- [ ] **Step 1: Implement EmptyState.kt**
Use standard Compose icons, centered column, title, subtitle, and an optional action button. Background transparent.

- [ ] **Step 2: Implement SearchBar.kt**
Rounded capsule shape (e.g. 50dp), search icon leading, clear text icon trailing. Transparent background on the textfield, but wrap in a Box with `DarkSurfaceVariant` background.

---

### Task 3: Redesign SongCard to Premium SongItem

**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/SongCard.kt`

- [ ] **Step 1: Replace SongCard with modern SongItem styling**
```kotlin
// Change root Row modifier, shape of album art (14.dp), typography of title (bold) & artist (TextSecondary).
// Add ripple effect (clickable).
// Preserve QualityBadge.
```

---

### Task 4: Refactor Permission & Splash Screen

**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/permission/PermissionScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/splash/SplashScreen.kt`

- [ ] **Step 1: Update SplashScreen.kt**
Add `androidx.compose.animation.AnimatedVisibility` for fade in/out of the logo.

- [ ] **Step 2: Update PermissionScreen.kt**
Redesign to match the "Find Your Local Music" layout specified in the prompt. Make the button use `AccentPink` as background.

---

### Task 5: Redesign LibraryScreen (Home)

**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/library/LibraryScreen.kt`

- [ ] **Step 1: Refactor LibraryScreen.kt**
Add Large TopAppBar "FanllyMusic" + subtitle "Local Lossless Player".
Use `LazyColumn`. Handle empty state with the new `EmptyState` component.

---

### Task 6: Redesign SearchScreen

**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/search/SearchScreen.kt`

- [ ] **Step 1: Refactor SearchScreen.kt**
Use large title "Search", include new `SearchBar` component, and handle empty queries vs no results properly.

---

### Task 7: Redesign Playlist & Settings Screens

**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/playlist/PlaylistScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/settings/SettingsScreen.kt`

- [ ] **Step 1: Refactor PlaylistScreen.kt**
Add `EmptyState` for when there are no playlists.

- [ ] **Step 2: Refactor SettingsScreen.kt**
Implement a list of modern settings cards for Audio Quality, Appearance, Library, and About.

---

### Task 8: Refactor Bottom Navigation & MiniPlayer

**Files:**
- Create/Rename: `app/src/main/java/com/alfan/fanllymusic/ui/components/BottomMusicNav.kt` (replace GlassNavBar.kt)
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/navigation/FanllyNavGraph.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/MiniPlayer.kt`

- [ ] **Step 1: Implement BottomMusicNav.kt**
Floating pill navigation, `#151515` background, rounded 28dp, active capsule highlight.

- [ ] **Step 2: Refactor MiniPlayer.kt**
Dark glass/surface, floating style, artwork 40dp (rounded 8dp), play/pause + next buttons.

- [ ] **Step 3: Update FanllyNavGraph.kt**
Replace `GlassNavBar` with `BottomMusicNav`. Adjust paddings so bottom nav floats correctly over content.
