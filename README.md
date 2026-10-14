# BlumTherapy 🌍

> **A second chance, grounded in evidence: recovery support, education, meaningful work, and human connection.**

BlumTherapy is the English-first, international counterpart to [BlumHelps](https://github.com/ariasalejo/Blumhelps). It is designed to help people affected by substance use explore trustworthy treatment pathways, peer and community support, education, employment resources, healthy activities, and practical ways to rebuild their lives—without stigma or pressure.

**Project status:** early product / MVP in progress. The repository contains an Android app scaffold and a web/PWA experience. The web app includes a local daily-plan fallback, habit tracking, progress views, and a country-aware resource library. This is not a treatment service, crisis-response service, or a complete verified directory. Country-specific resources must be reviewed before being presented as current or available.

## Product principles

1. **Dignity first.** A person's worth is not conditional on abstinence, productivity, or a perfectly linear recovery.
2. **Clear limits.** BlumTherapy is not a diagnostic tool, clinician, emergency service, or substitute for professional care.
3. **Evidence with context.** Health-related claims need traceable sources, plain-language limitations, and periodic review.
4. **Choice without coercion.** Faith-based support may be opt-in; it is never required. Avoid shame, punitive streaks, or financial pressure.
5. **Privacy by default.** Do not collect sensitive health or substance-use data by default. Keep personal planning local where possible; do not send it to sponsors.
6. **No invented promises.** Do not fabricate treatment slots, eligibility, benefits, partners, grants, jobs, rewards, or discount codes.
7. **Local relevance.** Resources, language, costs, crisis contacts, and eligibility depend on country and region.

## Current experience

### Web app / PWA

The web client lives in `web/` and is built with React, TypeScript, and Vite. The interface is organized around five areas:

- **Home:** a gentle starting point for today.
- **My plan:** generate a small, practical plan based on available time, energy, and chosen focus; a local fallback is used if the optional API is unavailable.
- **Library:** browse public resource starting points.
- **Support:** discover pathways to professional and community support.
- **My progress:** track optional habits and milestones without punitive streaks.

The PWA is configured for the GitHub Pages project path `/Blumtherapy/`, including a local hero illustration and a fallback page for direct SPA routes. Offline behavior is limited to the assets and data actually cached by the service worker; do not assume the complete resource directory or API works offline.

### Android

The Android source is under `app/`. Treat it as an early scaffold until device testing confirms navigation, persistence, accessibility, audio/TTS, and offline behavior. A successful build alone does not prove that all product flows are complete.

## Run the web app on Linux

Requirements: Git, Node.js 22 (recommended for the current Pages workflow), and npm.

```bash
git clone https://github.com/ariasalejo/Blumtherapy.git
cd Blumtherapy
git fetch origin
git switch feat/futuristic-wellbeing-habits
cd web
npm install
npm run dev
```

Vite prints the local development URL (normally `http://localhost:5173`). To check the production bundle:

```bash
npm run build
npm run preview
```

The optional API proxy expects a backend at `http://127.0.0.1:8000`. Without a configured API, the local plan fallback should remain available. Do not add real credentials or secrets to the repository; use environment variables and keep local `.env` files untracked.

## GitHub Pages deployment

The Pages workflow is `.github/workflows/nuxtjs.yml` on the `feat/futuristic-wellbeing-habits` branch. It builds `web/`, copies the SPA entry point to `404.html` for direct-route fallback, and deploys `web/dist` to GitHub Pages.

- Public site: https://ariasalejo.github.io/Blumtherapy/
- Workflow runs: https://github.com/ariasalejo/Blumtherapy/actions
- If a deployment fails, inspect the latest Actions run before changing branch protections or merging branches.
- The workflow currently triggers on pushes to `feat/futuristic-wellbeing-habits` and manual dispatch. Confirm the repository's Pages source and workflow status in GitHub before assuming a new commit is live.

## International resource starting points

These are starting points, not endorsements or guarantees of eligibility. Confirm location, availability, cost, privacy terms, and crisis information on the official source before displaying a resource as current.

- **United States:** [FindTreatment.gov](https://www.findtreatment.gov/) — treatment locator.
- **United Kingdom:** [NHS drug addiction support](https://www.nhs.uk/live-well/addiction-support/drug-addiction-getting-help/) — availability differs across UK nations.
- **Canada:** [Health Canada: substance-use treatment](https://www.canada.ca/en/health-canada/services/substance-use/treatment.html).
- **Australia:** [Australian Government Drug Help](https://www.health.gov.au/our-work/drug-help). Reconfirm hotline numbers and hours before displaying them.
- **Global evidence:** [WHO/UNODC International Standards for the Treatment of Drug Use Disorders](https://www.who.int/publications/i/item/international-standards-for-the-treatment-of-drug-use-disorders).

## Design and accessibility direction

The visual identity should feel calm, editorial, warm, and hopeful—not clinical, punitive, or “futuristic” for its own sake. Use the existing cream, plum, terracotta, and sage palette consistently; prefer original, locally bundled artwork to unverified remote images; provide meaningful alt text for informative images and hide decorative images from assistive technology. Test keyboard navigation, visible focus, contrast, reduced motion, narrow screens, enlarged text, and screen readers.

Audio/read-aloud features must use a real supported text-to-speech implementation with clear play/stop controls and language feedback. Keep offline/local speech as the default where supported. Any optional cloud speech provider must be explicitly disclosed, consent-based, and evaluated for service terms and privacy before integration.

## Research and implementation docs

- [International resource framework](docs/INTERNATIONAL_RESOURCE_FRAMEWORK.md)
- [Evidence and ethics](docs/EVIDENCE_AND_ETHICS.md)
- [Responsible incentives](docs/RESPONSIBLE_INCENTIVES.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Security](SECURITY.md)

## Partnerships and incentives

A listing does not imply a partnership. Do not describe an organization as a sponsor, funder, or official partner without documented permission. Any rewards must be voluntary, transparent, privacy-preserving, non-punitive, and reviewed with qualified health professionals and legal counsel where appropriate. Sponsors must not receive personal recovery, health, habit, or support-seeking data by default.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Contributions from people with lived experience, clinicians, researchers, peer-support workers, social workers, educators, employers, accessibility specialists, and developers are welcome. Please include reproducible test results and describe any known limitations.

## License

MIT. See [LICENSE](LICENSE).
