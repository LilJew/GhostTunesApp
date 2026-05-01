"""All REST API routes."""

from fastapi import APIRouter, Depends, File, Form, HTTPException, Query, UploadFile
from fastapi.security import APIKeyHeader
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import get_settings
from app.db.session import get_db
from app.schemas.schemas import (
    AlbumCreate, AlbumOut, AlbumUpdate, FavoriteAdd, PlaylistCreate,
    PlaylistTrackAdd, RateRequest, RateResponse,
    SearchResults, TrackOut, TrackPage, TrackUpdate,
)
from app.services import music_service as svc

settings = get_settings()
router = APIRouter()

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)


async def verify_api_key(key: str = Depends(api_key_header)):
    if key != settings.api_key:
        raise HTTPException(status_code=403, detail="Invalid API key")
    return key


# --- Tracks ---

@router.get("/tracks", response_model=TrackPage, tags=["tracks"])
async def list_tracks(page: int = Query(1, ge=1), size: int = Query(20, ge=1, le=100), db: AsyncSession = Depends(get_db)):
    return await svc.get_tracks(db, page, size)


@router.get("/tracks/{track_id}", response_model=TrackOut, tags=["tracks"])
async def get_track(track_id: str, db: AsyncSession = Depends(get_db)):
    track = await svc.get_track(db, track_id)
    if not track:
        raise HTTPException(status_code=404, detail="Track not found")
    return track


@router.patch("/tracks/{track_id}", response_model=TrackOut, tags=["tracks"])
async def update_track(track_id: str, data: TrackUpdate, db: AsyncSession = Depends(get_db), _: str = Depends(verify_api_key)):
    track = await svc.update_track(db, track_id, data)
    if not track:
        raise HTTPException(status_code=404, detail="Track not found")
    return await svc.get_track(db, track_id)


@router.delete("/tracks/{track_id}", status_code=204, tags=["tracks"])
async def delete_track(track_id: str, db: AsyncSession = Depends(get_db), _: str = Depends(verify_api_key)):
    if not await svc.delete_track(db, track_id):
        raise HTTPException(status_code=404, detail="Track not found")


# --- Albums ---

@router.get("/albums", response_model=list[AlbumOut], tags=["albums"])
async def list_albums(db: AsyncSession = Depends(get_db)):
    return await svc.get_albums(db)


@router.get("/albums/{album_id}/tracks", response_model=list[TrackOut], tags=["albums"])
async def album_tracks(album_id: str, db: AsyncSession = Depends(get_db)):
    if not await svc.get_album(db, album_id):
        raise HTTPException(status_code=404, detail="Album not found")
    return await svc.get_album_tracks(db, album_id)


@router.post("/albums", response_model=AlbumOut, status_code=201, tags=["albums"])
async def create_album(data: AlbumCreate, db: AsyncSession = Depends(get_db), _: str = Depends(verify_api_key)):
    album = await svc.create_album(db, data)
    await db.refresh(album)
    return album


@router.patch("/albums/{album_id}", response_model=AlbumOut, tags=["albums"])
async def update_album(
    album_id: str,
    data: AlbumCreate,
    db: AsyncSession = Depends(get_db),
    _: str = Depends(verify_api_key),
):
    from app.services.music_service import update_album as svc_update_album
    album = await svc_update_album(db, album_id, data)
    if not album:
        raise HTTPException(status_code=404, detail="Album not found")
    await db.refresh(album)
    return album

@router.patch("/albums/{album_id}", response_model=AlbumOut, tags=["albums"])
async def update_album(
    album_id: str,
    data: AlbumUpdate,
    db: AsyncSession = Depends(get_db),
    _: str = Depends(verify_api_key),
):
    result = await db.execute(select(Album).where(Album.id == album_id))
    album = result.scalar_one_or_none()
    if not album:
        raise HTTPException(status_code=404, detail="Album not found")
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(album, field, value)
    await db.refresh(album)
    return album


@router.delete("/albums/{album_id}", status_code=204, tags=["albums"])
async def delete_album(
    album_id: str,
    db: AsyncSession = Depends(get_db),
    _: str = Depends(verify_api_key),
):
    result = await db.execute(select(Album).where(Album.id == album_id))
    album = result.scalar_one_or_none()
    if not album:
        raise HTTPException(status_code=404, detail="Album not found")
    await db.delete(album)

# --- Favorites ---

@router.get("/favorites", response_model=list[TrackOut], tags=["favorites"])
async def get_favorites(db: AsyncSession = Depends(get_db)):
    return await svc.get_favorites(db)


@router.post("/favorites", status_code=201, tags=["favorites"])
async def add_favorite(body: FavoriteAdd, db: AsyncSession = Depends(get_db)):
    if not await svc.add_favorite(db, body.track_id):
        raise HTTPException(status_code=409, detail="Already in favorites")
    return {"detail": "Added"}


@router.delete("/favorites/{track_id}", status_code=204, tags=["favorites"])
async def remove_favorite(track_id: str, db: AsyncSession = Depends(get_db)):
    if not await svc.remove_favorite(db, track_id):
        raise HTTPException(status_code=404, detail="Not in favorites")


# --- Ratings ---

@router.post("/rate", response_model=RateResponse, tags=["ratings"])
async def rate_track(req: RateRequest, db: AsyncSession = Depends(get_db)):
    if req.type not in ("like", "dislike"):
        raise HTTPException(status_code=422, detail="type must be 'like' or 'dislike'")
    new_type = await svc.rate_track(db, req)
    return RateResponse(track_id=req.track_id, type=new_type)


# --- Playlists ---

@router.get("/playlists", tags=["playlists"])
async def list_playlists(db: AsyncSession = Depends(get_db)):
    return await svc.get_playlists(db)


@router.get("/playlists/{playlist_id}", tags=["playlists"])
async def get_playlist(playlist_id: str, db: AsyncSession = Depends(get_db)):
    pl = await svc.get_playlist_detail(db, playlist_id)
    if not pl:
        raise HTTPException(status_code=404, detail="Playlist not found")
    return pl


@router.post("/playlists", status_code=201, tags=["playlists"])
async def create_playlist(data: PlaylistCreate, db: AsyncSession = Depends(get_db)):
    pl = await svc.create_playlist(db, data)
    return {"id": pl.id, "title": pl.title}


@router.post("/playlists/{playlist_id}/tracks", status_code=201, tags=["playlists"])
async def add_to_playlist(playlist_id: str, body: PlaylistTrackAdd, db: AsyncSession = Depends(get_db)):
    if not await svc.add_track_to_playlist(db, playlist_id, body.track_id, body.position):
        raise HTTPException(status_code=404, detail="Playlist not found")
    return {"detail": "Added"}


@router.delete("/playlists/{playlist_id}/tracks/{track_id}", status_code=204, tags=["playlists"])
async def remove_from_playlist(playlist_id: str, track_id: str, db: AsyncSession = Depends(get_db)):
    if not await svc.remove_track_from_playlist(db, playlist_id, track_id):
        raise HTTPException(status_code=404, detail="Track not in playlist")


# --- Search ---

@router.get("/search", response_model=SearchResults, tags=["search"])
async def search(q: str = Query(..., min_length=1), db: AsyncSession = Depends(get_db)):
    return await svc.search(db, q)


# --- Admin upload ---

@router.post("/admin/upload", status_code=201, tags=["admin"])
async def upload_track(
    file: UploadFile = File(...),
    title: str = Form(None),
    artist: str = Form(None),
    album_id: str = Form(None),
    db: AsyncSession = Depends(get_db),
    _: str = Depends(verify_api_key),
):
    if not file.filename.lower().endswith(".mp3"):
        raise HTTPException(status_code=422, detail="Only MP3 supported")
    content = await file.read()
    file_hash = svc.compute_file_hash(content)
    from sqlalchemy import select
    from app.models.models import Track
    if (await db.execute(select(Track).where(Track.file_hash == file_hash))).scalar_one_or_none():
        raise HTTPException(status_code=409, detail="File already uploaded")
    track_id, file_url, duration = await svc.save_audio_file(content, file.filename)
    meta = await svc.extract_id3_metadata(content, f"{settings.tracks_path}/{track_id}.mp3")
    cover_url = None
    if meta.get("cover_data"):
        cover_url = await svc.save_cover_image(meta["cover_data"], f"{track_id}.jpg")
    from app.schemas.schemas import TrackCreate
    track = await svc.create_track(db, TrackCreate(
        id=track_id, title=title or meta["title"], artist=artist or meta["artist"],
        duration_seconds=duration, file_url=file_url, cover_url=cover_url,
        album_id=album_id, file_hash=file_hash,
    ))
    return {"id": track.id, "title": track.title, "file_url": track.file_url}


@router.post("/admin/covers", status_code=201, tags=["admin"])
async def upload_cover(file: UploadFile = File(...), _: str = Depends(verify_api_key)):
    content = await file.read()
    url = await svc.save_cover_image(content, file.filename)
    return {"cover_url": url}
