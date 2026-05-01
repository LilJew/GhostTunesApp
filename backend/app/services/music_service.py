"""Business logic layer."""

import hashlib
import os
import uuid
from math import ceil
from pathlib import Path
from typing import Optional

import aiofiles
from sqlalchemy import func, or_, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.core.config import get_settings
from app.models.models import Album, Favorite, Playlist, PlaylistTrack, Track, TrackRating, RatingType
from app.schemas.schemas import AlbumCreate, PlaylistCreate, RateRequest, TrackCreate, TrackOut, TrackUpdate

settings = get_settings()
USER_ID = 1


def _track_to_out(track: Track, user_rating: Optional[str], is_favorite: bool) -> TrackOut:
    data = TrackOut.model_validate(track)
    data.user_rating = user_rating
    data.is_favorite = is_favorite
    return data


async def _get_user_ratings(db: AsyncSession, track_ids: list[str]) -> dict[str, str]:
    result = await db.execute(
        select(TrackRating).where(TrackRating.user_id == USER_ID, TrackRating.track_id.in_(track_ids))
    )
    return {r.track_id: r.type.value for r in result.scalars()}


async def _get_user_favorites(db: AsyncSession, track_ids: list[str]) -> set[str]:
    result = await db.execute(
        select(Favorite.track_id).where(Favorite.user_id == USER_ID, Favorite.track_id.in_(track_ids))
    )
    return set(result.scalars())


# --- Tracks ---

async def get_tracks(db: AsyncSession, page: int = 1, size: int = 20):
    total = (await db.execute(select(func.count(Track.id)))).scalar_one()
    offset = (page - 1) * size
    result = await db.execute(
        select(Track).options(selectinload(Track.album)).order_by(Track.created_at.desc()).offset(offset).limit(size)
    )
    tracks = result.scalars().all()
    ids = [t.id for t in tracks]
    ratings = await _get_user_ratings(db, ids)
    favs = await _get_user_favorites(db, ids)
    return {
        "items": [_track_to_out(t, ratings.get(t.id), t.id in favs) for t in tracks],
        "total": total, "page": page, "size": size,
        "pages": ceil(total / size) if total else 1,
    }


async def get_track(db: AsyncSession, track_id: str) -> Optional[TrackOut]:
    result = await db.execute(select(Track).options(selectinload(Track.album)).where(Track.id == track_id))
    track = result.scalar_one_or_none()
    if not track:
        return None
    ratings = await _get_user_ratings(db, [track_id])
    favs = await _get_user_favorites(db, [track_id])
    return _track_to_out(track, ratings.get(track_id), track_id in favs)


async def create_track(db: AsyncSession, data: TrackCreate) -> Track:
    dump = data.model_dump()
    track_id = dump.pop("id", None) or str(uuid.uuid4())
    track = Track(id=track_id, **dump)
    db.add(track)
    await db.flush()
    return track


async def update_track(db: AsyncSession, track_id: str, data: TrackUpdate) -> Optional[Track]:
    result = await db.execute(select(Track).where(Track.id == track_id))
    track = result.scalar_one_or_none()
    if not track:
        return None
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(track, field, value)
    return track


async def delete_track(db: AsyncSession, track_id: str) -> bool:
    result = await db.execute(select(Track).where(Track.id == track_id))
    track = result.scalar_one_or_none()
    if not track:
        return False
    await db.delete(track)
    return True


# --- Albums ---

async def get_albums(db: AsyncSession) -> list[Album]:
    result = await db.execute(select(Album).order_by(Album.year.desc()))
    return result.scalars().all()


async def get_album(db: AsyncSession, album_id: str) -> Optional[Album]:
    result = await db.execute(select(Album).where(Album.id == album_id))
    return result.scalar_one_or_none()


async def get_album_tracks(db: AsyncSession, album_id: str) -> list[TrackOut]:
    result = await db.execute(
        select(Track).options(selectinload(Track.album)).where(Track.album_id == album_id).order_by(Track.created_at)
    )
    tracks = result.scalars().all()
    ids = [t.id for t in tracks]
    ratings = await _get_user_ratings(db, ids)
    favs = await _get_user_favorites(db, ids)
    return [_track_to_out(t, ratings.get(t.id), t.id in favs) for t in tracks]


async def create_album(db: AsyncSession, data: AlbumCreate) -> Album:
    album = Album(**data.model_dump())
    db.add(album)
    await db.flush()
    return album


async def update_album(db: AsyncSession, album_id: str, data) -> Optional[Album]:
    result = await db.execute(select(Album).where(Album.id == album_id))
    album = result.scalar_one_or_none()
    if not album:
        return None
    for field, value in data.model_dump(exclude_none=True).items():
        setattr(album, field, value)
    return album

# --- Favorites ---

async def get_favorites(db: AsyncSession) -> list[TrackOut]:
    result = await db.execute(
        select(Track).options(selectinload(Track.album))
        .join(Favorite, Favorite.track_id == Track.id)
        .where(Favorite.user_id == USER_ID)
        .order_by(Favorite.created_at.desc())
    )
    tracks = result.scalars().all()
    ids = [t.id for t in tracks]
    ratings = await _get_user_ratings(db, ids)
    return [_track_to_out(t, ratings.get(t.id), True) for t in tracks]


async def add_favorite(db: AsyncSession, track_id: str) -> bool:
    existing = (await db.execute(
        select(Favorite).where(Favorite.user_id == USER_ID, Favorite.track_id == track_id)
    )).scalar_one_or_none()
    if existing:
        return False
    db.add(Favorite(user_id=USER_ID, track_id=track_id))
    return True


async def remove_favorite(db: AsyncSession, track_id: str) -> bool:
    fav = (await db.execute(
        select(Favorite).where(Favorite.user_id == USER_ID, Favorite.track_id == track_id)
    )).scalar_one_or_none()
    if not fav:
        return False
    await db.delete(fav)
    return True


# --- Ratings ---

async def rate_track(db: AsyncSession, req: RateRequest) -> Optional[str]:
    existing = (await db.execute(
        select(TrackRating).where(TrackRating.user_id == USER_ID, TrackRating.track_id == req.track_id)
    )).scalar_one_or_none()
    if existing:
        if existing.type.value == req.type:
            await db.delete(existing)
            return None
        existing.type = RatingType[req.type]
        return req.type
    db.add(TrackRating(user_id=USER_ID, track_id=req.track_id, type=RatingType[req.type]))
    return req.type


# --- Playlists ---

async def get_playlists(db: AsyncSession) -> list:
    result = await db.execute(select(Playlist).where(Playlist.user_id == USER_ID).order_by(Playlist.created_at.desc()))
    playlists = result.scalars().all()
    out = []
    for pl in playlists:
        count = (await db.execute(select(func.count(PlaylistTrack.id)).where(PlaylistTrack.playlist_id == pl.id))).scalar_one()
        out.append({**{c.name: getattr(pl, c.name) for c in pl.__table__.columns}, "track_count": count})
    return out


async def get_playlist_detail(db: AsyncSession, playlist_id: str):
    pl = (await db.execute(select(Playlist).where(Playlist.id == playlist_id, Playlist.user_id == USER_ID))).scalar_one_or_none()
    if not pl:
        return None
    pt_result = await db.execute(
        select(PlaylistTrack).options(selectinload(PlaylistTrack.track).selectinload(Track.album))
        .where(PlaylistTrack.playlist_id == playlist_id).order_by(PlaylistTrack.position)
    )
    tracks = [pt.track for pt in pt_result.scalars().all()]
    ids = [t.id for t in tracks]
    ratings = await _get_user_ratings(db, ids)
    favs = await _get_user_favorites(db, ids)
    return {
        **{c.name: getattr(pl, c.name) for c in pl.__table__.columns},
        "track_count": len(tracks),
        "tracks": [_track_to_out(t, ratings.get(t.id), t.id in favs) for t in tracks],
    }


async def create_playlist(db: AsyncSession, data: PlaylistCreate) -> Playlist:
    pl = Playlist(user_id=USER_ID, **data.model_dump())
    db.add(pl)
    await db.flush()
    return pl


async def add_track_to_playlist(db: AsyncSession, playlist_id: str, track_id: str, position: Optional[int]) -> bool:
    pl = (await db.execute(select(Playlist).where(Playlist.id == playlist_id, Playlist.user_id == USER_ID))).scalar_one_or_none()
    if not pl:
        return False
    if position is None:
        max_pos = (await db.execute(
            select(func.coalesce(func.max(PlaylistTrack.position), -1)).where(PlaylistTrack.playlist_id == playlist_id)
        )).scalar_one()
        position = max_pos + 1
    db.add(PlaylistTrack(playlist_id=playlist_id, track_id=track_id, position=position))
    return True


async def remove_track_from_playlist(db: AsyncSession, playlist_id: str, track_id: str) -> bool:
    pt = (await db.execute(
        select(PlaylistTrack).where(PlaylistTrack.playlist_id == playlist_id, PlaylistTrack.track_id == track_id)
    )).scalar_one_or_none()
    if not pt:
        return False
    await db.delete(pt)
    return True


# --- Search ---

async def search(db: AsyncSession, q: str) -> dict:
    like = f"%{q}%"
    tracks = (await db.execute(
        select(Track).options(selectinload(Track.album))
        .where(or_(Track.title.ilike(like), Track.artist.ilike(like))).limit(20)
    )).scalars().all()
    ids = [t.id for t in tracks]
    ratings = await _get_user_ratings(db, ids)
    favs = await _get_user_favorites(db, ids)
    albums = (await db.execute(
        select(Album).where(or_(Album.title.ilike(like), Album.artist.ilike(like))).limit(10)
    )).scalars().all()
    return {
        "tracks": [_track_to_out(t, ratings.get(t.id), t.id in favs) for t in tracks],
        "albums": albums,
    }


# --- File helpers ---

async def save_audio_file(content: bytes, original_filename: str) -> tuple[str, str, int]:
    track_id = str(uuid.uuid4())
    os.makedirs(settings.tracks_path, exist_ok=True)
    file_path = Path(settings.tracks_path) / f"{track_id}.mp3"
    async with aiofiles.open(file_path, "wb") as f:
        await f.write(content)
    duration = 0
    try:
        from mutagen import File as MutagenFile
        audio = MutagenFile(str(file_path))
        if audio and audio.info:
            duration = int(audio.info.length)
    except Exception:
        pass
    return track_id, f"{settings.base_url}/static/tracks/{track_id}.mp3", duration


async def extract_id3_metadata(content: bytes, file_path: str) -> dict:
    try:
        from mutagen.id3 import ID3
        tags = ID3(file_path)
        title = str(tags.get("TIT2", "Unknown"))
        artist = str(tags.get("TPE1", "Unknown"))
        cover_data = None
        for tag in tags.values():
            if hasattr(tag, "FrameID") and tag.FrameID == "APIC":
                cover_data = tag.data
                break
        return {"title": title, "artist": artist, "cover_data": cover_data}
    except Exception:
        return {"title": "Unknown", "artist": "Unknown", "cover_data": None}


async def save_cover_image(content: bytes, name: str) -> str:
    os.makedirs(settings.covers_path, exist_ok=True)
    ext = Path(name).suffix or ".jpg"
    cover_id = str(uuid.uuid4())
    path = Path(settings.covers_path) / f"{cover_id}{ext}"
    async with aiofiles.open(path, "wb") as f:
        await f.write(content)
    return f"{settings.base_url}/static/covers/{cover_id}{ext}"


def compute_file_hash(content: bytes) -> str:
    return hashlib.sha256(content).hexdigest()
