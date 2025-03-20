# Quick Draw

## Game Description

Quick Draw is a turn-based competitive word association game. Each round, players are presented with a category (such as animals) and must provide words that fit within that category (like dog, cat, or giraffe). Players can input their answers either by speaking into the microphone or typing them. The game validates each response against the category list, and players may retry until they provide a valid word or until time runs out. The timer duration can be customized in the settings. When a player fails to provide a valid word before time expires, they lose the round! The game features customizable settings including adjustable timer duration and supports multiple players taking turns.

## Game Preview

![QuickDrawDemoGIF](https://github.com/user-attachments/assets/d6a2c61b-27ea-491a-9015-528efcf27727)

## Figma Design Document

The original Figma design document is provided below, but the UI has certainly changed since then!
https://www.figma.com/design/5URabKeNDSj9pTinuHVedO/Mobile-App-Dev---Quick-Draw?node-id=0-1&t=ICesjPINrfO0QuAa-1

## Features & Technologies

### Android & Jetpack Compose Features:
- Jetpack Compose UI toolkit for declarative UI building
- Kotlin Coroutines and Flow for asynchronous programming
- Android ViewModel and StateFlow for MVVM architecture
- SharedPreferences for settings persistence
- Android Speech Recognition API for voice input
- Navigation Compose for screen navigation
- Material 3 Design components
- Android Permissions API for microphone access

### Third-Party Libraries:
- None required - the app uses built-in Android APIs

## Device Requirements

### Minimum Requirements:
- Android SDK Version: API 24 (Android 7.0 Nougat) or higher
- Target SDK: API 34 (Android 14)
- Permissions: Microphone (RECORD_AUDIO) and Internet access
- Device Features: Microphone hardware for speech recognition
- Internet connection required for speech recognition functionality
- The app works without internet, but speech recognition will be disabled

### Recommended:
- Stable internet connection for reliable speech recognition

## Above and Beyond

Quick Draw was developed with a few features beyond the scope of the class and textbook. Firstly, the app connects turn-based gameplay between players to a dedicated pause menu that allows players to resume or exit the game, allowing for dynamic gameplay and quick restarts when on the go. Additionally, there is a (mostly) working settings page with SharedPreferences to provide customizability for round timers, enhancing the user experience. Finally, my favorite feature is the speech recognition feature, allowing users to play in an intuitive, mobile approach with friends. This speech recognition feature utilizes the android speech recognition API, expanding beyond the work with audio recording we did in class by utilizing intents and listeners.
