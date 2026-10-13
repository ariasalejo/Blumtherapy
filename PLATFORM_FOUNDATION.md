# BlumTherapy — Platform foundation 0.2

This branch adds the first real web/PWA and API layer alongside the existing Kotlin Android application. It does not replace the Android planner.

## Product architecture

- **Android APK:** the existing Kotlin + Jetpack Compose application remains the native mobile client.
- **PWA/frontend:** `web/` is a responsive React + TypeScript app, installable where the browser supports PWA installation. The core habit record stays in browser-local storage.
- **Backend/API:** `backend/` is a FastAPI service. It serves a reviewed public-resource catalog from a SQLAlchemy database and a deterministic daily-step endpoint.
- **Database:** local development defaults to SQLite. Set `DATABASE_URL` to PostgreSQL in deployed environments. Public catalog rows are seeded when the service starts.
- **Offline behaviour:** the PWA caches its app shell and has local fallback data and a local plan generator. Failure to reach the API does not block the core planner.

## Why the first algorithm is intentionally explainable

The plan endpoint accepts only four fields: energy selected for this moment, time available, the focus selected by the user, and language. It generates small steps from explicit rules. It is not an addiction-risk score, diagnosis, treatment recommendation or prediction. Private habit history is neither sent to the API nor stored by this backend.

## Local development

Terminal 1:
```bash
cd web
cp .env.example .env
npm install
npm run dev
```

Terminal 2:
```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
APP_BRAND=blumtherapy uvicorn app.main:app --reload
```

The Vite development server proxies `/api` to `http://127.0.0.1:8000`. API documentation is available at `/docs`.

## Before public production deployment

Configure HTTPS, PostgreSQL, restrictive `CORS_ORIGINS`, observability that excludes personal content, secret management, backups, dependency scanning, and an explicit resource-verification workflow. This increment intentionally does **not** sync sensitive recovery records to a server and does not include authentication yet. Account sync must not be enabled until consent, data minimisation, deletion/export and access-control rules are implemented and tested.

## Clinical and resource boundaries

Resources are starting points, not guarantees of access, eligibility, current availability or cost. The app does not diagnose, promise recovery, replace professional care or provide emergency response. Review every official link and its coverage before a production release.
