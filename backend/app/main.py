from __future__ import annotations

import os
from contextlib import asynccontextmanager
from typing import Literal

from fastapi import Depends, FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from sqlalchemy import String, create_engine, select
from sqlalchemy.orm import DeclarativeBase, Mapped, Session, mapped_column, sessionmaker

from .catalog import catalog_for

BRAND = os.getenv("APP_BRAND", "blumtherapy").strip().lower()
DATABASE_URL = os.getenv("DATABASE_URL", "sqlite:///./blumhelps.db")
cors_origins = [item.strip() for item in os.getenv(
    "CORS_ORIGINS", "http://localhost:5173,http://127.0.0.1:5173"
).split(",") if item.strip()]
connect_args = {"check_same_thread": False} if DATABASE_URL.startswith("sqlite") else {}
engine = create_engine(DATABASE_URL, connect_args=connect_args, pool_pre_ping=True)
SessionLocal = sessionmaker(bind=engine, autoflush=False, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


class ResourceRow(Base):
    __tablename__ = "public_resources"
    id: Mapped[str] = mapped_column(String(100), primary_key=True)
    brand: Mapped[str] = mapped_column(String(32), index=True)
    title: Mapped[str] = mapped_column(String(180))
    summary: Mapped[str] = mapped_column(String(1000))
    category: Mapped[str] = mapped_column(String(60), index=True)
    country: Mapped[str] = mapped_column(String(12), index=True)
    source_name: Mapped[str] = mapped_column(String(180))
    url: Mapped[str] = mapped_column(String(600))
    reviewed_on: Mapped[str] = mapped_column(String(10))


class ResourceOut(BaseModel):
    id: str
    title: str
    summary: str
    category: str
    country: str
    source_name: str
    url: str
    reviewed_on: str

    model_config = {"from_attributes": True}


def seed_public_catalog() -> None:
    Base.metadata.create_all(engine)
    with SessionLocal() as db:
        existing = db.scalar(select(ResourceRow.id).where(ResourceRow.brand == BRAND).limit(1))
        if existing:
            return
        for item in catalog_for(BRAND):
            db.add(ResourceRow(**item))
        db.commit()


@asynccontextmanager
async def lifespan(_: FastAPI):
    seed_public_catalog()
    yield


app = FastAPI(
    title="BlumTherapy Support API" if BRAND.startswith("blumhelps") else "BlumTherapy Support API",
    version="0.2.0",
    description=(
        "Privacy-first API for reviewed public resources and an explainable, non-clinical daily-step planner. "
        "Personal habit records are not accepted or stored by this API."
    ),
    lifespan=lifespan,
)
app.add_middleware(
    CORSMiddleware,
    allow_origins=cors_origins,
    allow_credentials=False,
    allow_methods=["GET", "POST", "OPTIONS"],
    allow_headers=["Accept", "Content-Type"],
)


def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()


@app.get("/health/live", tags=["health"])
def live():
    return {"status": "ok", "service": BRAND, "version": "0.2.0"}


@app.get("/health/ready", tags=["health"])
def ready(db: Session = Depends(get_db)):
    try:
        db.execute(select(ResourceRow.id).limit(1)).all()
    except Exception as exc:
        raise HTTPException(status_code=503, detail="database_not_ready") from exc
    return {"status": "ready", "service": BRAND}


@app.get("/api/v1/resources", response_model=list[ResourceOut], tags=["resources"])
def get_resources(
    country: str | None = Query(default=None, min_length=2, max_length=12),
    category: str | None = Query(default=None, max_length=60),
    db: Session = Depends(get_db),
):
    statement = select(ResourceRow).where(ResourceRow.brand == BRAND)
    if country:
        normalized = country.strip().upper()
        # GLOBAL resources remain visible in each country filter.
        statement = statement.where(ResourceRow.country.in_([normalized, "GLOBAL"]))
    if category:
        statement = statement.where(ResourceRow.category.ilike(category.strip()))
    rows = db.scalars(statement.order_by(ResourceRow.category, ResourceRow.title)).all()
    return rows


class PlanRequest(BaseModel):
    energy: Literal["low", "steady", "high"] = "steady"
    time_minutes: int = Field(default=10, ge=2, le=60)
    focus: Literal["care", "connection", "learning", "structure"] = "care"
    language: Literal["es", "en"] = "es"


class PlanStep(BaseModel):
    title: str
    detail: str


class PlanResponse(BaseModel):
    title: str
    minutes: int
    steps: list[PlanStep]
    rationale: str
    safety_note: str


def make_plan(request: PlanRequest) -> PlanResponse:
    es = request.language == "es"
    library = {
        "care": {
            "title": "Volver al cuerpo" if es else "Return to your body",
            "low": ("Respira con comodidad y toma un poco de agua." if es else "Breathe comfortably and have some water.", "Elige únicamente lo que se sienta seguro y posible." if es else "Choose only what feels safe and possible."),
            "steady": ("Haz una pausa breve y estira el cuerpo suavemente." if es else "Take a short pause and stretch gently.", "Nota qué te haría el siguiente momento un poco más llevadero." if es else "Notice what could make the next moment a little easier."),
            "high": ("Sal a caminar o prepara algo nutritivo, si puedes." if es else "Take a walk or prepare something nourishing, if you can.", "Deja espacio para descansar después." if es else "Leave room to rest afterward."),
        },
        "connection": {
            "title": "Acercarte sin presión" if es else "Connect without pressure",
            "low": ("Piensa en una persona con quien te sientas a salvo." if es else "Think of someone you feel safe with.", "No tienes que contactar a nadie antes de sentirte listo." if es else "You do not have to contact anyone before you feel ready."),
            "steady": ("Envía un mensaje breve a alguien de confianza." if es else "Send a short message to someone you trust.", "Comparte solo lo que quieras compartir." if es else "Share only what you want to share."),
            "high": ("Propón una llamada o un encuentro sencillo." if es else "Suggest a call or simple get-together.", "Respeta tus límites y los de la otra persona." if es else "Respect your boundaries and the other person's."),
        },
        "learning": {
            "title": "Abrir una ventana nueva" if es else "Open a new window",
            "low": ("Lee un párrafo o escucha algo tranquilo." if es else "Read one paragraph or listen to something calm.", "Parar después de unos minutos también está bien." if es else "Stopping after a few minutes is okay, too."),
            "steady": ("Dedica unos minutos a un tema que te interese." if es else "Spend a few minutes on a topic that interests you.", "Guarda una idea para retomarla cuando quieras." if es else "Save one idea to return to whenever you like."),
            "high": ("Explora una formación o practica una habilidad." if es else "Explore training or practise a skill.", "Elige curiosidad, no perfección." if es else "Choose curiosity, not perfection."),
        },
        "structure": {
            "title": "Hacer espacio para lo importante" if es else "Make room for what matters",
            "low": ("Escoge una sola tarea de dos minutos." if es else "Choose one two-minute task.", "Puedes detenerte cuando lo necesites." if es else "You can stop whenever you need."),
            "steady": ("Ordena un espacio pequeño o prepara algo para mañana." if es else "Tidy one small space or prepare something for tomorrow.", "Divide el resto en pasos pequeños." if es else "Break the rest into small steps."),
            "high": ("Divide una tarea pendiente en tres pasos concretos." if es else "Break one pending task into three clear steps.", "Completar todo no es un requisito." if es else "Finishing everything is not a requirement."),
        },
    }
    selected = library[request.focus]
    first, second = selected[request.energy]
    count = 1 if request.time_minutes < 8 or request.energy == "low" else 2
    steps = [PlanStep(title="Paso posible" if es else "Possible step", detail=first)]
    if count == 2:
        steps.append(PlanStep(title="Cierre amable" if es else "Gentle close", detail=second))
    return PlanResponse(
        title=selected["title"],
        minutes=min(request.time_minutes, 5) if request.energy == "low" else request.time_minutes,
        steps=steps,
        rationale=(
            "La sugerencia usa solo el enfoque, el tiempo y la energía elegidos ahora; no estima diagnósticos ni predice resultados."
            if es else
            "This suggestion uses only the focus, time and energy selected now; it does not estimate diagnoses or predict outcomes."
        ),
        safety_note=(
            "Puedes cambiar o ignorar cualquier paso. Esto no sustituye atención profesional ni servicios de emergencia."
            if es else
            "You can change or ignore any step. This does not replace professional care or emergency services."
        ),
    )


@app.post("/api/v1/plan/suggest", response_model=PlanResponse, tags=["planning"])
def suggest_plan(request: PlanRequest):
    # Deliberately stateless: no identifying information or personal history is stored.
    return make_plan(request)
