# 🎵 BeatX

BeatX is a modern, feature-rich, and beautiful Android Music Player built entirely with Kotlin. It streams music directly from a remote API, supports background playback with system notifications, and offers robust offline library management.

## 🛠 Tech Stack & Architecture
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Media Playback**: AndroidX Media3 (`ExoPlayer` & `MediaSessionService`)
- **Local Storage**: Room Database (SQLite) & SharedPreferences
- **Networking**: Retrofit2 + Gson
- **Asynchrony**: Kotlin Coroutines & StateFlow/SharedFlow
- **Image Loading**: Coil
- **UI**: XML Layouts with Material Design 3 Components, Bottom Navigation, ViewBinding, Navigation Component.

---

## ✨ Features Implemented (Journey So Far)

### 1. UI Foundation & Navigation Architecture
- **How it was implemented**: Used Android Jetpack Navigation Component (`nav_graph.xml`) paired with a `BottomNavigationView`.
- **Under the hood**: Built a single-activity architecture (`MainActivity`) that securely hosts multiple fragments (Home, Search, Player, Library, Profile, etc.). Heavily utilized **ViewBinding** to prevent null-pointer crashes.

### 2. Music Streaming & API Integration
- **How it was implemented**: Used **Retrofit2** to connect to an unofficial JioSaavn API.
- **Under the hood**: Designed a unified `Song` domain model. Implemented robust `HomeRepository` and `SearchRepository` to fetch trending songs and process search queries dynamically. Used Kotlin Coroutines (`viewModelScope.launch`) and `StateFlow` to push data seamlessly to the UI.

### 3. Background Playback & Notifications (Media3)
- **How it was implemented**: Migrated from a simple Fragment-scoped player to AndroidX Media3's **`MediaSessionService`**.
- **Under the hood**: Created a `PlaybackService` running continuously in the background. Bound a `MediaController` inside `MainActivity` and `PlayerFragment`. This ensures music keeps playing even when the app is minimized, and provides a native Android Media Notification with lock-screen controls.

### 4. Global Animated Mini Player
- **How it was implemented**: Injected a persistent `layout_mini_player.xml` directly into the `MainActivity`.
- **Under the hood**: The Mini Player listens to the global `MediaController` state. It automatically animates (slides up) when a song starts playing and persists across all screens (Home, Search, Library) without interrupting navigation. Tapping it seamlessly opens the Full Player.

### 5. Advanced Player Controls & Gestures
- **How it was implemented**: Deep integration with native ExoPlayer APIs and Android `GestureDetector`.
- **Under the hood**:
  - **Playback Speed**: Dynamically adjusts speed (0.5x to 2x) via ExoPlayer's `PlaybackParameters`.
  - **Swipe Gestures**: A `GestureDetector` attached to the Album Art allows swiping Left/Right to skip to Next/Previous tracks.
  - **Sleep Timer**: A Kotlin Coroutine `delay()` running on the `lifecycleScope` safely pauses the player after a set duration without blocking the main thread.
  - **Up Next Queue**: A sleek `BottomSheetDialogFragment` pulling upcoming `MediaItem`s directly from the ExoPlayer queue.

### 6. Offline Library Management (Room DB)
- **How it was implemented**: Engineered a highly optimized local database using **Android Room**.
- **Under the hood**:
  - **Favorites**: Storing liked songs in `FavoriteSongEntity`.
  - **Recently Played**: Maintaining a timestamp-sorted history of played tracks (`RecentlyPlayedEntity`).
  - **Playlist Management**: Implemented a 1-to-many SQL relationship using `@Relation`. Users can create custom playlists, add songs via a Bottom Sheet, remove songs, and delete playlists seamlessly.

### 7. Theming & Localization
- **How it was implemented**: Powered by Android 13+ standard `AppCompatDelegate` and `SharedPreferences`.
- **Under the hood**: 
  - **Multi-Language**: Instant toggle between English and Hindi (`values-hi`) using `AppCompatDelegate.setApplicationLocales()`.
  - **Dynamic Themes**: Support for Light, Dark, System Default, and a custom **AMOLED Black** theme that dynamically overrides the window background for maximum OLED battery savings via custom Theme styles.

### 8. Social Sharing
- **How it was implemented**: Android's native `Intent.ACTION_SEND`.
- **Under the hood**: Generates a formatted string with the song title, artist, and audio link, passed seamlessly to WhatsApp, Instagram, or Messages via `Intent.createChooser()`.

---

## 🚀 How to Run
1. Clone the repository.
2. Open in Android Studio.
3. Build and Run on any emulator or physical device running Android 8.0+.
