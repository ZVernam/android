# Agent Context

This repository contains the Android application for the Vernam Password Generator.

## Project Overview
- **Name:** Vernam Android Password Generator
- **Description:** An Android app that generates unique, non-encryptable passwords using the Vernam Cipher logic.
- **Key Features:**
    - On-the-fly unique password generation based on a key and a base secret.
    - Encrypted storage using biometrics.
    - URL processing (stripping domain names) to use as keys.
    - Optional salt/suffix for input strings.
    - Material Design UI with Day/Night theme support.

## Tech Stack
- **Language:** Kotlin
- **Build System:** Gradle (Kotlin DSL)
- **Minimum SDK:** 24
- **Target SDK:** 35
- **Libraries:**
    - AndroidX (AppCompat, Biometric, Preference, Security Crypto, Lifecycle)
    - Google Material Components
    - Custom library: `libs.vernam.tools` (from GitHub Packages)

## Key Files
- `app/src/main/java/com/github/zeckson/vernam/`: Main application source code.
- `app/build.gradle.kts`: Application-level build configuration.
- `README.md`: General project information and release notes.

## Development Workflow
- **Releases:** Handled via `release.sh` and `move-tag.sh` scripts.
- **Signing:** Release signing configuration is present but requires external properties.
- **Linting:** Configured in `build.gradle.kts` to ignore errors during release builds.
