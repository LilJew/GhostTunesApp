"""FastAPI application entrypoint."""

import logging
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI, WebSocket
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from app.api.routes import router
from app.api.websocket import websocket_player
from app.core.config import get_settings
from app.db.session import engine
from app.models.models import Base  # noqa: F401

logging.basicConfig(level=logging.INFO)
settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    for path in [settings.tracks_path, settings.covers_path]:
        Path(path).mkdir(parents=True, exist_ok=True)
    yield
    await engine.dispose()


app = FastAPI(
    title="GhostTunes API",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.origins_list,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

Path(settings.tracks_path).mkdir(parents=True, exist_ok=True)
Path(settings.covers_path).mkdir(parents=True, exist_ok=True)

app.mount("/static/tracks", StaticFiles(directory=settings.tracks_path), name="tracks")
app.mount("/static/covers", StaticFiles(directory=settings.covers_path), name="covers")

app.include_router(router, prefix="/api/v1")


@app.websocket("/ws/player")
async def player_ws(websocket: WebSocket):
    await websocket_player(websocket)


@app.get("/health")
async def health():
    return {"status": "ok"}
