# Changelog 🔄

All notable changes to **Rotator** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- Apache 2.0 `LICENSE` file.
- Open-source governance files: `CONTRIBUTING.md`, `CODE_OF_CONDUCT.md`, and `CHANGELOG.md`.
- GitHub Issue Forms (Bug Report, Feature Request) and Pull Request template.
- Unit test suite (`testDebugUnitTest`) covering `OrientationMode`, `ControllerState`, and `RotatorTileService`.
- Magnetic edge snapping and modern `WindowMetrics` boundary clamping for the floating overlay.
- Haptic feedback when interacting with overlay buttons and mode cycling.
- CI pipeline quality checks running unit tests and lint.

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
