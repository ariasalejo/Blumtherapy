# Architecture

## Product boundary
BlumTherapy is the English-first international second-chance and recovery-support product. It is separate from BlumHelps, which begins with Spanish-language resources for Colombia.

## Current baseline
A small Kotlin + Jetpack Compose starter with a minimal manifest and no sensitive permissions. It does not yet provide treatment, crisis response, a verified directory, or rewards.

## Target layers
- **Presentation:** Compose screens, accessible components, and explicit loading/empty/error/stale states.
- **Domain:** resource, country, verification, goal, and incentive models; pure Kotlin use cases where practical.
- **Data:** repository interfaces, local resource bundles, and future network sources behind explicit boundaries.
- **Platform:** Android-specific capabilities isolated behind narrow adapters.
- **Evidence governance:** clinical claims have source, population, date, limitations, qualified reviewer, and next-review date.

## Resource model
Each listing needs country and subregion, language, official source, service category, eligibility and cost only when verified, last-checked date, review status, and fallback instructions. A public information site must be distinguished from a treatment provider.

## Privacy and safety rules
1. No analytics or sensitive health data collection by default.
2. No crisis monitoring claims; do not imply the app can dispatch emergency help.
3. Never log substance-use details, device identifiers, or secrets.
4. Keep faith-based content optional and clearly labeled.
5. No unverified benefits, sponsors, grants, jobs, or treatment slots.
6. No reward system that punishes relapse, uses public rankings, or pressures users to disclose health information.
7. Add tests for verification rules, stale data, privacy controls, and critical navigation as features are implemented.

Add Gradle modules only when real build or ownership boundaries justify them. Never store release signing credentials in source control.
