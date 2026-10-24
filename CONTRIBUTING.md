# Contributing to Rotator 🔄

Thank you for your interest in contributing to **Rotator**! We welcome bug reports, feature suggestions, localization, and pull requests from the open-source community.

---

## 🧭 Code of Conduct

This project adheres to the Contributor Covenant [Code of Conduct](CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code.

---

## 🛠️ Development Setup

### Prerequisites
- **JDK 17** or newer
- **Android SDK** (API 23 through 35, Build Tools 35.0.0+)
- **Git**

### Getting the Code
```bash
git clone https://github.com/rohit-ayadav/rotator.git
cd rotator
```

### Building the Project
```bash
# On Linux / macOS
./gradlew assembleDebug

# On Windows (PowerShell / Command Prompt)
.\gradlew.bat assembleDebug
```

### Running Tests
```bash
./gradlew testDebugUnitTest
```

### Running Android Lint
```bash
./gradlew lintDebug
```

---

## 💡 How Can You Contribute?

### 1. Reporting Bugs
- Search existing [Issues](https://github.com/rohit-ayadav/rotator/issues) before opening a new one.
- Use our [Bug Report Template](.github/ISSUE_TEMPLATE/bug_report.yml) and include:
  - Device manufacturer, model, and Android OS version.
  - Clear reproduction steps and actual vs. expected behavior.
  - Logcat output if a crash occurred.

### 2. Suggesting Features & Enhancements
- Check the [Product Requirements Document (PRD.md)](PRD.md) and open issues.
- Submit a proposal using the [Feature Request Template](.github/ISSUE_TEMPLATE/feature_request.yml).

### 3. Submitting Code Changes (Pull Requests)
1. **Fork** the repository and create your feature branch:
   ```bash
   git checkout -b feat/your-feature-name
   ```
2. **Follow Coding Standards**:
   - Write clean, idiomatic Kotlin code following official Kotlin code style.
   - Respect Rotator's **privacy-first rule**: **never** add `INTERNET` permission, tracking SDKs, or cloud dependencies.
   - Include unit tests for new logic where appropriate.
3. **Validate Locally**:
   Ensure linting and tests pass before pushing:
   ```bash
   ./gradlew testDebugUnitTest lintDebug
   ```
4. **Commit & Push**:
   Use conventional commit messages (`feat: ...`, `fix: ...`, `docs: ...`, `refactor: ...`).
5. **Open a Pull Request**:
   Fill out the PR template completely with screenshots/videos if UI changes are made.

---

## 🔒 Privacy & Core Principles

Rotator has strict core tenets:
1. **100% Offline & Zero Telemetry**: The app does not request `android.permission.INTERNET`.
2. **Battery & Resource Efficiency**: No busy loops or unnecessary wake locks; enforce orientation using lightweight system window flags.
3. **Decoupled Architecture**: Orientation enforcement must remain decoupled from the floating overlay visibility.

---

## 📄 License
By contributing to Rotator, you agree that your contributions will be licensed under the [Apache License 2.0](LICENSE).
