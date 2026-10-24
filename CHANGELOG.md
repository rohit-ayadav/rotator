# Changelog 🔄

All notable changes to **Rotator** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.2] - 2026-10-24

### Added
- **Minimalist Floating Bubble:** Streamlined compact mode to a single 44dp circular floating bubble displaying only the active orientation icon with magnetic edge snapping and 1-tap expansion.
- **Auto-Collapse Inactivity Timer:** Full floating panel automatically minimizes back into the floating bubble after 5 seconds of inactivity.
- **Permission Onboarding Rationale:** First-time onboarding dialog explaining why the "Display over other apps" overlay permission is required and safe before navigating to system settings.
- **In-App Help Guide:** Detailed explanation dialog accessible via bottom-right floating action button.
- **GitHub Repository Integration:** Dedicated GitHub button in app header and link in Help guide.
- **Crystal-Clear Orientation Icons:** Added high-contrast directional indicators (`↑`, `↓`, `←`, `→`, dual-arc sensor) for instant recognition.
- **Premium Adaptive Launcher Icon:** Multi-stop linear gradient background (`#1E1B4B` to `#080C14`), 3D glass smartphone frame, and dual-arc rotation arrows.
- **Material You Dynamic Theming:** Android 13+ monochrome icon (`ic_launcher_monochrome`) that adapts to wallpaper palette.
- **Matching Header Logo:** High-definition circular vector badge for in-app header.
- **Release Keystore & R8 Minification:** Configured official release keystore and ProGuard/R8 bytecode minification, eliminating AV false positives (Evo-gen).
- **Official Package ID:** Changed application ID to `io.github.rohit_ayadav.rotator`.
- **Open-Source Governance:** Added Apache 2.0 `LICENSE`, `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, and issue templates.

### Changed
- Unified separate Start and Stop buttons into a single adaptive 52dp toggle button matching the Pause button height.
- Fixed orientation buttons viewport overflow so mode buttons are never cropped on narrow mobile screens.
- Streamlined dashboard by removing redundant Privacy and System Integrity card.

---

## [1.0.1] - 2026-10-22

### Added
- Quick Settings tile (`RotatorTileService`) for 1-tap mode cycling directly from the notification shade.
- Device boot receiver (`BootReceiver`) to optionally restore orientation on reboot.
- Tagged release build automation in GitHub Actions workflow.

### Fixed
- Overlay window inflation crash by wrapping Context in `ContextThemeWrapper(context, R.style.Theme_Rotator)`.
- Android 14+ foreground service type declaration compatibility (`specialUse`).
- Settings fallback navigation when `ACTION_MANAGE_OVERLAY_PERMISSION` is restricted by OEM ROMs.

---

## [1.0.0] - 2026-10-22

### Added
- Initial release of Rotator.
- 5 orientation enforcement modes: Portrait, Reverse Portrait, Landscape Left, Landscape Right, Auto-Rotate.
- Draggable floating overlay controller with expanded and compact pill modes.
- Ongoing notification controls for background orientation management.
- 100% offline, privacy-first architecture with zero analytics or internet permission.
