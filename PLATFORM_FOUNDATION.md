# BlumTherapy — connected platform foundation 0.3

## Components in this branch
- Android: existing Kotlin + Jetpack Compose APK; the existing offline habit/calendar planner is preserved.
- PWA: English React + TypeScript + Vite app with its own visual identity; Vite PWA plugin generates the install manifest and service worker.
- Backend: FastAPI + SQLAlchemy; country-scoped public resource catalog and a stateless explainable daily-step endpoint.
- Database: SQLite for local development, PostgreSQL in Docker Compose.
- Packaging: web and API Dockerfiles, Nginx reverse proxy and private database network.

## Run the complete local stack
```bash
cp .env.compose.example .env
# Edit .env and set a strong local-only POSTGRES_PASSWORD
docker compose -f compose.platform.yml up --build
```
Open `http://localhost:8081`. The PWA proxies `/api/` and `/health/` to the API. PostgreSQL is not exposed to the host.

## Local development without Docker
Terminal 1:
```bash
cd backend
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
APP_BRAND=blumtherapy uvicorn app.main:app --reload
``
Terminal 2:
```bash
cd web
npm install
npm run dev
``
Local Vite proxies `/api` to port 8000. API documentation is at `http://127.0.0.1:8000/docs`.

## Privacy and safety
Personal habits/history remain local to the device or browser. The stateless planning API receives only the momentary energy, time, focus and language choices; it does not receive or store habit history. User accounts and cloud sync are not enabled. They require explicit consent, authentication, per-user authorization, export/deletion, retention and incident-handling rules before implementation.

The planner is a transparent self-reflection aid, not a diagnostic, clinical decision or predictive model. Country directories are starting points and cannot guarantee access, current availability, eligibility or cost. This branch is not yet a public deployment. Configure HTTPS, unique secrets, strict CORS, backups, migrations and production logging controls before exposing it publicly.
