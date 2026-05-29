# FanllyMusic UI/UX Redesign Plan

I'm using the writing-plans skill to create the implementation plan.

## Architecture

We are performing a component-first redesign of the existing local music player application to bring it up to Apple Music / Spotify standards of design and premium aesthetics.

### Files to touch:
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/EmptyState.kt`
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/SearchBar.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Type.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/SongCard.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/splash/SplashScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/permission/PermissionScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/library/LibraryScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/search/SearchScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/playlist/PlaylistScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/settings/SettingsScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/GlassNavBar.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/MiniPlayer.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/navigation/FanllyNavGraph.kt`

---

### Task 1: Core Colors & Theme Setup
**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/theme/Type.kt`

- [ ] **Step 1: Write Theme Colors**
- [ ] **Step 2: Update Theme application setup**
- [ ] **Step 3: Setup premium typography**

### Task 2: Create Reusable Components
**Files:**
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/EmptyState.kt`
- Create: `app/src/main/java/com/alfan/fanllymusic/ui/components/SearchBar.kt`

- [ ] **Step 1: Write EmptyState.kt**
- [ ] **Step 2: Write SearchBar.kt**

### Task 3: Premium SongItem Component Redesign
**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/SongCard.kt`

- [ ] **Step 1: Overwrite SongCard style to use premium SongItem UI**

### Task 4: Splash Screen & Onboarding Permissions
**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/splash/SplashScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/permission/PermissionScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/navigation/FanllyNavGraph.kt`

- [ ] **Step 1: Update SplashScreen flow**
- [ ] **Step 2: Redesign PermissionScreen with settings deep links**
- [ ] **Step 3: Route SplashScreen properly**

### Task 5: Redesign Screens (Library, Search, Playlists, Settings)
**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/library/LibraryScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/search/SearchScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/playlist/PlaylistScreen.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/screen/settings/SettingsScreen.kt`

- [ ] **Step 1: Premium LibraryScreen (Home Layout)**
- [ ] **Step 2: Premium SearchScreen**
- [ ] **Step 3: Premium PlaylistScreen**
- [ ] **Step 4: Premium SettingsScreen**

### Task 6: Premium Bottom Nav & MiniPlayer
**Files:**
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/GlassNavBar.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/components/MiniPlayer.kt`
- Modify: `app/src/main/java/com/alfan/fanllymusic/ui/navigation/FanllyNavGraph.kt`

- [ ] **Step 1: Floating pill navigation (GlassNavBar)**
- [ ] **Step 2: Dark glass float MiniPlayer**
