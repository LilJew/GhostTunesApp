"""WebSocket player sync endpoint."""

import json
import logging
from typing import Any

from fastapi import WebSocket, WebSocketDisconnect

from app.db.session import AsyncSessionLocal
from app.services import music_service as svc

logger = logging.getLogger(__name__)


class ConnectionManager:
    def __init__(self):
        self.active: list[WebSocket] = []

    async def connect(self, ws: WebSocket):
        await ws.accept()
        self.active.append(ws)

    def disconnect(self, ws: WebSocket):
        if ws in self.active:
            self.active.remove(ws)

    async def broadcast(self, message: dict, exclude: WebSocket | None = None):
        data = json.dumps(message, default=str)
        for ws in self.active:
            if ws is not exclude:
                try:
                    await ws.send_text(data)
                except Exception:
                    pass


manager = ConnectionManager()


async def websocket_player(websocket: WebSocket):
    await manager.connect(websocket)
    state: dict[str, Any] = {"status": "stopped", "current_track": None, "position": 0.0}
    try:
        while True:
            raw = await websocket.receive_text()
            try:
                msg = json.loads(raw)
            except json.JSONDecodeError:
                await websocket.send_json({"error": "Invalid JSON"})
                continue

            action = msg.get("action")
            track_id = msg.get("track_id")
            position = msg.get("position", 0.0)

            async with AsyncSessionLocal() as db:
                if action == "play" and track_id:
                    track = await svc.get_track(db, track_id)
                    if track:
                        state = {"status": "playing", "current_track": track.model_dump(), "position": position}
                    else:
                        await websocket.send_json({"error": "Track not found"})
                        continue
                elif action == "pause":
                    state["status"] = "paused"
                    state["position"] = position
                elif action == "seek":
                    state["position"] = position
                elif action == "stop":
                    state = {"status": "stopped", "current_track": None, "position": 0.0}
                elif action == "like" and track_id:
                    from app.schemas.schemas import RateRequest
                    new_type = await svc.rate_track(db, RateRequest(track_id=track_id, type=msg.get("type", "like")))
                    event = {"event": "rating_update", "track_id": track_id, "type": new_type}
                    await manager.broadcast(event, exclude=websocket)
                    await websocket.send_json(event)
                    continue

            await websocket.send_json(state)
            await manager.broadcast(state, exclude=websocket)

    except WebSocketDisconnect:
        manager.disconnect(websocket)
    except Exception as e:
        logger.exception("WebSocket error: %s", e)
        manager.disconnect(websocket)
