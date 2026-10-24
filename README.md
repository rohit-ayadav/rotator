# Rotator 🔄

[![Android CI Build & Release](https://github.com/rohit-ayadav/rotator/actions/workflows/android.yml/badge.svg)](https://github.com/rohit-ayadav/rotator/actions/workflows/android.yml)
![Platform](https://img.shields.io/badge/Platform-Android%206.0%2B%20(API%2023--35)-brightgreen.svg)
![Language](https://img.shields.io/badge/Language-Kotlin-blue.svg)
![Build](https://img.shields.io/badge/Build-Gradle%20Kotlin%20DSL-02303A.svg)
![Privacy](https://img.shields.io/badge/Network%20Access-None%20(100%25%20Offline)-success.svg)

> **A lightweight, privacy-focused Android screen rotation controller with a draggable floating overlay, notification controls, and Quick Settings tile.**

---

## ✨ Features

- **5 Orientation Modes**:
  - 📱 **Portrait**: Standard upright portrait.
  - 🙃 **Reverse Portrait**: Upside-down portrait (ideal for charging or bed reading).
  - 🖥️ **Landscape Left**: Standard landscape mode.
  - 🔄 **Landscape Right**: Inverted landscape mode.
  - 🧭 **Auto-Rotate**: Unlocks rotation to sensor-based system behavior.
- **Floating Overlay Controller**:
  - Draggable anywhere on the screen with boundary-safe clamping.
  - **Full Mode**: Quick access to all 5 orientation modes, Pause/Resume, Hide, and Stop.
  - **Compact Pill Mode**: Minimized floating icon showing active orientation and a 1-tap pause toggle.
- **Quick Settings Tile**:
  - Pull down from your notification shade to view active orientation and 1-tap cycle through modes without opening the app.
- **Ongoing Notification Controls**:
  - Control rotation directly from the notification shade (Pause/Resume, Auto-Rotate, Show/Hide Panel, Stop).
- **Start on Device Boot**:
  - Optional toggle to automatically restore your preferred rotation and controller after your device restarts.
- **Decoupled Orientation Enforcer**:
  - Hiding the floating panel does **not** cancel your active orientation lock.
- **Zero Telemetry & 100% Offline**:
  - Does **not** request Android `INTERNET` permission. No ads, no tracking, no account required.

---

## 📥 Download APK

### 1. Tagged Releases (Recommended)
Download pre-built versioned APKs (`Rotator-vX.Y.Z.apk`) directly from the **[GitHub Releases](https://github.com/rohit-ayadav/rotator/releases)** page.

### 2. Latest Development Builds
Each commit pushed to `main` automatically compiles a fresh APK via GitHub Actions:
1. Navigate to the **[Actions Tab](https://github.com/rohit-ayadav/rotator/actions)**.
2. Click the latest successful run of **Android CI Build & Release**.
3. Under **Artifacts**, download `Rotator-APK` (contains `Rotator-latest-debug.apk`).

---

## 🏗️ Architecture

```
app/src/main/java/com/example/rotator/
├── MainActivity.kt                  # Dashboard, permission onboarding & settings
├── core/
│   ├── ControllerState.kt           # State model & ServiceState enum
│   ├── OrientationEnforcer.kt       # Invisible WindowManager overlay holding screenOrientation
│   ├── OrientationMode.kt           # Orientation enum mapping to ActivityInfo constants
│   └── PreferencesManager.kt        # Persistent coordinates, mode, and boot preferences
├── overlay/
│   ├── OverlayController.kt         # Draggable floating panel & compact pill lifecycle
│   └── DragTouchListener.kt         # Boundary-safe drag/touch handling
├── service/
│   ├── RotationService.kt           # Foreground Service (Android 14+ specialUse FGS type)
│   ├── NotificationHelper.kt        # Notification channel, actions, and ongoing updates
│   └── RotatorTileService.kt        # Quick Settings Tile (1-tap mode cycling)
├── receiver/
│   └── BootReceiver.kt              # Restores controller on device boot
└── util/
    └── PermissionManager.kt         # SYSTEM_ALERT_WINDOW & POST_NOTIFICATIONS helpers
```

---

## 🔒 Permissions & Security

| Permission | Purpose | Required? |
| :--- | :--- | :---: |
| `SYSTEM_ALERT_WINDOW` | Displays the movable floating panel over other apps and applies `screenOrientation`. | **Yes** |
| `FOREGROUND_SERVICE` | Keeps the controller service responsive in the background. | **Yes** |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Mandatory foreground service declaration for Android 14+ (API 34+). | **Yes** |
| `POST_NOTIFICATIONS` | Displays the persistent rotation control notification on Android 13+ (API 33+). | Optional |
| `RECEIVE_BOOT_COMPLETED` | Restores orientation enforcement after phone reboot (if enabled in Settings). | Optional |
| `BIND_QUICK_SETTINGS_TILE` | Binds the Quick Settings tile in the system notification shade. | Optional |
| `INTERNET` | **NOT REQUESTED**. The app has zero networking capabilities. | **No** |

---

## 💻 Building from Source

You do not need Android Studio installed locally. The project can be built via the included Gradle wrapper or via GitHub Actions.

### Prerequisites
- JDK 17 or higher
- Android SDK (or run headlessly via GitHub Actions)

### Build Commands
```bash
# Clone the repository
git clone git@github-personal:rohit-ayadav/rotator.git
cd rotator

# Build debug APK
./gradlew assembleDebug

# Output location:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤝 Contributing
Contributions are always welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for details on setting up your development environment, coding standards, and submitting pull requests. All participants must abide by the [Code of Conduct](CODE_OF_CONDUCT.md).

For release notes and version history, refer to [CHANGELOG.md](CHANGELOG.md).

---

## 📄 License
This project is open-source under the [Apache 2.0 License](LICENSE).

