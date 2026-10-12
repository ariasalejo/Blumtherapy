# BlumTherapy-Android

> **Open-source Android utilities, diagnostics guidance, and privacy-first developer tools.**

BlumTherapy-Android is a community-oriented Android project designed to make device information, troubleshooting knowledge, and responsible developer utilities easier to access. This repository starts with a small native Android app and a documented architecture that can grow feature by feature.

**Status:** early scaffold. Features not implemented are explicitly marked as planned.

## Principles
- **Privacy first:** no analytics or personal data collection by default.
- **Least privilege:** the starter app requires no root, Accessibility Service, device-admin, or privileged permission.
- **Honest diagnostics:** never claim to repair a device without a verified implementation.
- **Open development:** documented decisions, reproducible builds, and community contributions.
- **Safe by design:** no bypassing security controls, account protections, carrier restrictions, or device-owner policies.

## Repository map
```text
app/                         Android application
docs/                        Architecture, roadmap, and safe diagnostics
.github/workflows/            Continuous integration
CONTRIBUTING.md               Contribution workflow
SECURITY.md                   Vulnerability reporting
CODE_OF_CONDUCT.md            Community expectations
```

## Current foundation
- Kotlin and Jetpack Compose UI.
- Minimal single-activity Android application.
- Basic GitHub Actions build workflow.
- No network access, tracking SDK, or dangerous permissions in the starter screen.

## Build locally
Requirements: JDK 17, Android SDK, and Gradle 8.11.1 or compatible with the configured Android Gradle Plugin.

```bash
gradle assembleDebug
```

Open the project in Android Studio if preferred. CI will validate the initial configuration. A Gradle wrapper should be added from a trusted local Gradle installation before relying on wrapper-based builds.

## Planned tool areas
- Read-only Android version and device capability information.
- Guided troubleshooting checklists.
- Privacy-safe diagnostic previews with sensitive fields excluded by default.
- ADB learning resources for authorized devices.
- Offline documentation.

These are roadmap items, not current functionality.

## Security and privacy
Read [SECURITY.md](SECURITY.md) and [docs/SAFE_DIAGNOSTICS.md](docs/SAFE_DIAGNOSTICS.md). Never commit IMEIs, serial numbers, access tokens, logs containing personal information, or signing keys.

## Contributing
See [CONTRIBUTING.md](CONTRIBUTING.md).

## License
MIT. See [LICENSE](LICENSE).
