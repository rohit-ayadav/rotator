Here is a complete Product Requirements Document (PRD) for your Android Rotation Control app, covering the floating overlay, notification controls, rotation modes, permissions, technical architecture, and acceptance criteria. It is structured so you can use it as a development specification for implementation through GitHub Actions without installing Android Studio on Windows.

# Product Requirements Document (PRD)

## Rotator — Floating Screen Rotation Controller

**Version:** 1.0\
**Platform:** Android\
**Application Type:** Native Android application\
**Primary Language:** Kotlin\
**Build System:** Gradle + GitHub Actions\
**Document Status:** Development-ready specification

---

## 1. Product Overview

Rotator is a lightweight Android application that allows users to control screen orientation without repeatedly opening the app.

The application provides a floating control panel that appears over other applications and a persistent notification containing rotation controls. Users can switch between portrait, reverse portrait, landscape left, landscape right, and automatic orientation.

The application also supports pausing orientation enforcement, resuming it, hiding the floating panel, and stopping the rotation controller.

The primary objective is to make screen rotation accessible from anywhere on the device while keeping the interface simple, responsive, and privacy-conscious.

## 2. Problem Statement

Android users may encounter situations where an application does not rotate as desired or where repeatedly navigating to rotation settings is inconvenient.

Examples include:

- Watching videos while lying down.
- Using applications that work better in landscape mode.
- Reading documents in portrait mode.
- Controlling screen orientation without leaving the current application.
- Switching between fixed orientation and automatic rotation quickly.

Rotator aims to solve this problem with persistent, easily accessible controls.

## 3. Product Goals

1. Provide quick access to four fixed orientation modes and automatic orientation.
2. Display a movable floating controller over other applications.
3. Provide essential controls through an Android notification.
4. Support Pause, Resume, Stop, Hide Panel, and Show Panel actions.
5. Minimize storage usage, battery consumption, and unnecessary background activity.
6. Operate without requiring an account or internet connectivity.
7. Provide a straightforward installation process through a downloadable APK.

### Non-goals

The initial release will not include:

- Root access requirements.
- Advertisements or analytics.
- User accounts or cloud synchronization.
- Diagonal screen rotation.
- Guaranteed control over every system screen or application.
- Automatic per-app rules or accessibility-based automation.

## 4. Target Users

- Android users who prefer manually controlled screen orientation.
- Users who watch videos or read while holding their phones in different positions.
- Users who need quick rotation controls while using other applications.
- Users who want a lightweight alternative to repeatedly opening system settings.

## 5. Functional Requirements

### FR-01: Orientation Control

The application must support the following modes:

| Mode             | Expected behaviour                                                                            |
| ---------------- | --------------------------------------------------------------------------------------------- |
| Portrait         | Request upright portrait orientation.                                                         |
| Reverse Portrait | Request upside-down portrait orientation.                                                     |
| Landscape Left   | Request landscape orientation.                                                                |
| Landscape Right  | Request reverse landscape orientation.                                                        |
| Auto-Rotate      | Remove the application's fixed orientation request and allow normal system orientation rules. |

The selected mode must be reflected in the floating panel and notification.

**Platform limitation:** Android applications generally control their own activity orientation. Global orientation enforcement across unrelated applications and system screens must not be assumed to work universally.

### FR-02: Floating Overlay

The application must provide a floating panel that can remain visible while the user interacts with other applications.

Requirements:

- Request the Android `SYSTEM_ALERT_WINDOW` permission through the appropriate system settings screen.
- Display the panel only after the user grants overlay access.
- Allow the panel to be dragged around the screen.
- Prevent the panel from being dragged entirely outside usable screen bounds.
- Provide orientation selection buttons.
- Provide Pause, Resume, Auto, Stop, and Hide controls.
- Provide a compact mode to reduce screen obstruction.
- Keep controls usable on different screen sizes.
- Reopen the panel from the notification when hidden.

If overlay access is revoked, the application must stop attempting to display the panel and provide a clear explanation.

### FR-03: Notification Controls

When the controller is running, the application must display an appropriate ongoing notification.

The notification should include:

- Application name and active status.
- Current orientation mode.
- Pause or Resume action, depending on state.
- Auto-Rotate action.
- Stop action.
- An action or entry point for reopening the floating panel.

Actions must be processed even when the main activity is not visible, subject to Android background-execution rules.

The notification must update when the selected orientation or controller state changes.

### FR-04: Pause Function

Pause temporarily suspends orientation enforcement.

Expected behaviour:

1. Retain the last selected orientation mode.
2. Stop actively reapplying that mode.
3. Keep the controller service running if necessary to support Resume and notification actions.
4. Keep the floating panel available unless the user hides it.
5. Update the interface and notification to show the paused state.

Pause must not be implemented by terminating the controller service.

### FR-05: Resume Function

Resume restores enforcement of the previously selected orientation mode.

Expected behaviour:

1. Restore the retained orientation selection.
2. Reapply the orientation request.
3. Update the notification and floating panel.
4. Change the controller state to Running.

### FR-06: Stop Function

Stop terminates the active controller session.

Expected behaviour:

1. Remove the floating panel.
2. Stop the controller service.
3. Remove the ongoing notification.
4. Clear the controller's orientation request where the application has the authority to do so.
5. Release resources and listeners.
6. Return the controller to the Stopped state.

Stopping the controller must not force-close unrelated applications.

### FR-07: Hide and Show Floating Panel

**Hide Panel:**

- Remove the floating panel from the screen.
- Keep the controller and notification active.
- Preserve the current orientation selection.

**Show Panel:**

- Restore the floating panel when overlay permission is available.
- Restore the current orientation and controller state.

### FR-08: Controller State Management

The application must maintain a consistent state across the main activity, overlay, service, and notification.

Supported states:

- Stopped
- Running
- Paused
- Running with panel hidden
- Paused with panel hidden

The selected orientation and controller state must remain synchronized across all interfaces.

### FR-09: Permission Management

The application must explain and request only the permissions required for its features.

Potential permissions and capabilities include:

- Display over other apps: required for the floating panel.
- Foreground service: required where applicable for persistent controller operation.
- Foreground service type: must match applicable Android requirements.
- Notification permission: requested on Android versions that require it.

The application must not request internet access, contacts, location, microphone, camera, or unrelated permissions.

Permission denial must not crash the application. Users must still be able to use supported functionality from the main activity.

## 6. User Interface Requirements

### Main Activity

The main screen must contain:

- Application name and short description.
- Five orientation buttons.
- Current orientation indicator.
- Start Controller button.
- Pause or Resume button.
- Stop Controller button.
- Overlay permission status.
- Notification status where relevant.
- Instructions for enabling required permissions.

### Floating Panel

The floating panel must contain:

- Current orientation indicator.
- Portrait button.
- Reverse Portrait button.
- Landscape Left button.
- Landscape Right button.
- Auto-Rotate button.
- Pause or Resume button.
- Hide Panel button.
- Stop button.

A compact version should prioritize orientation selection and the Pause/Resume control.

### Notification

The notification must show the current mode and controller status. Actions should be prioritized according to platform-supported notification action limits.

If every action cannot fit directly into the notification, the panel-reopening action can provide access to additional controls.

## 7. Technical Architecture

### Technology Stack

| Component               | Technology                                             |
| ----------------------- | ------------------------------------------------------ |
| Language                | Kotlin                                                 |
| UI                      | Native Android Views                                   |
| Build                   | Gradle Kotlin DSL                                      |
| Android SDK             | Compile SDK 35 for the initial implementation          |
| Minimum Android version | Android 6.0 (API 23), subject to compatibility testing |
| Floating interface      | Android WindowManager                                  |
| Background controller   | Foreground service where required                      |
| Notifications           | NotificationManager and notification actions           |
| Build automation        | GitHub Actions                                         |
| Distribution            | Debug APK for testing                                  |

### Proposed Project Structure

```
MyRotator/
├── .github/
│   └── workflows/
│       └── android.yml
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       └── java/com/example/rotator/
│           ├── MainActivity.kt
│           ├── RotationService.kt
│           ├── OverlayController.kt
│           ├── NotificationHelper.kt
│           ├── RotationController.kt
│           ├── ControllerState.kt
│           └── PermissionManager.kt
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

### Component Responsibilities

**MainActivity**

- Displays the dashboard.
- Explains permissions.
- Starts and stops the controller.
- Shows the current controller state.

**RotationService**

- Manages the controller's lifecycle.
- Processes service commands.
- Maintains the notification while active.
- Handles cleanup when stopped.

**OverlayController**

- Creates and removes the floating window.
- Implements drag handling.
- Routes user actions to the controller.
- Handles overlay permission changes.

**NotificationHelper**

- Creates the notification channel.
- Builds the ongoing notification.
- Processes Pause, Resume, Auto, Stop, and Show Panel actions.

**RotationController**

- Maps orientation selections to Android orientation requests.
- Retains the selected mode.
- Applies or suspends orientation enforcement.

**ControllerState**

- Defines controller states and orientation modes.
- Provides a consistent state model for all interfaces.

**PermissionManager**

- Checks overlay and notification permissions.
- Opens the relevant Android settings screens.
- Handles permission denial and recovery.

## 8. Privacy and Security Requirements

1. No internet permission in the Android manifest.
2. No advertising or analytics SDKs.
3. No collection or transmission of personal data.
4. No account creation.
5. No unnecessary sensitive permissions.
6. Clear disclosure of overlay and background-service behaviour.
7. Safe handling of notification actions and service lifecycle.
8. No unnecessary persistence of user data.

The absence of internet permission prevents ordinary application networking through standard Android networking APIs. It does not, by itself, establish that every aspect of the application is secure.

## 9. Non-functional Requirements

### Performance

- Floating panel actions should respond promptly.
- The service must avoid continuous polling.
- No unnecessary wake locks or background loops.
- Resource usage should remain low during normal operation.

### Reliability

- Repeated button presses must not create duplicate services or overlays.
- Notification actions must be idempotent where practical.
- Permission revocation must be handled safely.
- The application must clean up windows and service resources correctly.

### Usability

- Controls must have clear labels.
- The floating panel must not permanently obstruct the screen.
- Touch targets must be large enough for comfortable use.
- Paused and stopped states must be visually distinguishable.

### Compatibility

- Test on multiple Android versions and manufacturers.
- Test behaviour with system auto-rotate enabled and disabled.
- Verify reverse portrait and reverse landscape support on target devices.
- Document device-specific restrictions.

## 10. Acceptance Criteria

The initial release is accepted when all the following tests pass:

1. The application installs and launches successfully.
2. All five orientation options can be selected.
3. Overlay permission can be requested and handled correctly.
4. The floating panel appears over another application.
5. The panel can be moved without disappearing off-screen.
6. Pause stops orientation enforcement without removing the controller.
7. Resume reapplies the selected orientation request.
8. Stop removes the panel and ends the controller session.
9. Hiding the panel preserves the notification and controller state.
10. The notification can reopen the panel.
11. Notification actions work while another app is in the foreground.
12. The controller behaves correctly when overlay permission is revoked.
13. No internet permission appears in the final manifest.
14. GitHub Actions successfully produces a downloadable APK.
15. The app does not crash when the user denies optional permissions.

Acceptance tests must distinguish between an orientation request succeeding and Android actually applying that orientation.

## 11. Build and Release Plan

### Phase 1: Core Rotation

- Implement the five orientation modes.
- Build the basic dashboard.
- Verify orientation requests.

### Phase 2: Floating Overlay

- Add overlay permission handling.
- Implement draggable and compact panel layouts.
- Connect orientation buttons.

### Phase 3: Notification and Service

- Implement the controller service.
- Add notification actions.
- Implement Pause, Resume, Stop, Hide, and Show.

### Phase 4: Reliability

- Test state synchronization.
- Handle permission revocation and service termination.
- Test supported Android versions and manufacturers.

### Phase 5: Cloud Build

- Configure GitHub Actions.
- Compile the debug APK.
- Upload the artifact.
- Install and test on a physical Android device.

### Release Artifact

The initial release will be a debug APK suitable for development and personal testing. A production release will require an appropriate release-signing process.

## 12. Success Metrics

The project will be considered successful when:

- All core orientation controls work as expected on the test device.
- Floating controls remain accessible over supported third-party applications.
- Pause, Resume, and Stop produce consistent state transitions.
- Notification actions work without reopening the main activity.
- No unnecessary permissions or network access are introduced.
- The cloud build produces a valid installable APK.

## 13. Key Technical Risks

**Android orientation restrictions:** Applications cannot universally control orientation across all other applications and system interfaces.

**Background execution:** Android may restrict or terminate services depending on version, battery settings, and manufacturer policies.

**Overlay permission:** The user must explicitly enable the relevant special access.

**Notification behaviour:** Notification visibility and available actions depend on Android version, notification settings, and lock-screen configuration.

**Compatibility:** Reverse orientations may be ignored or restricted by some applications and devices.

These limitations must be documented and tested rather than presented as universally solved.

## 14. Final Product Definition

Rotator is a native Android utility combining a floating rotation panel, a persistent notification, and a lightweight controller service. It offers five orientation modes and provides consistent Pause, Resume, Stop, Hide, and Show behaviour while respecting Android permissions and system restrictions.

The first release prioritizes reliable control within supported Android contexts, privacy, low resource consumption, and a simple GitHub Actions-based APK build process.

### Recommended next step

Use this PRD as the specification for implementation. The most important technical decision is to build and test the foreground service, overlay, and notification actions together, since they must share a consistent controller state. The global orientation behaviour should then be validated on your actual Android phone rather than assumed to work across every app.