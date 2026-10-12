# Architecture

## Current baseline
The application is a small Kotlin + Jetpack Compose starter. Dependencies and permissions are intentionally minimal.

## Target direction
- **Presentation:** Compose screens and explicit UI state; no direct network or platform calls from composables.
- **Domain:** use cases and domain models, preferably pure Kotlin.
- **Data:** repository interfaces and data sources behind clear boundaries.
- **Platform adapters:** Android APIs behind narrow interfaces, with permission checks and graceful unsupported-device states.

Recommended packages as the project grows: presentation, domain, data, and platform. Add Gradle modules only when a real build or ownership boundary justifies them.

## Engineering rules
1. Default to read-only operations.
2. Never log device identifiers or secrets.
3. Represent diagnostic observations with evidence, timestamp, limitations, and next steps.
4. Ask for consent before exporting or sharing diagnostic data.
5. Never claim to modify modem, firmware, carrier registration, or protected settings without a tested implementation.
6. Add tests for domain logic and critical user flows as features are introduced.

Release signing credentials must never be stored in source control.
