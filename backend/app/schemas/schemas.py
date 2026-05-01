"""Pydantic v2 schemas."""

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, ConfigDict


class AlbumBase(BaseModel):
    title: str
    artist: str
    year: Optional[int] = None
    cover_url: Optional[str] = None


class AlbumCreate(AlbumBase):
    pass

class AlbumUpdate(BaseModel):
    title: Optional[str] = None
    artist: Optional[str] = None
    year: Optional[int] = None
    cover_url: Optional[str] = None


class AlbumOut(AlbumBase):
    model_config = ConfigDict(from_attributes=True)
    id: str
    created_at: datetime


class TrackBase(BaseModel):
    title: str
    artist: str
    duration_seconds: int = 0
    file_url: str
    cover_url: Optional[str] = None
    album_id: Optional[str] = None


class TrackCreate(TrackBase):
    id: Optional[str] = None
    file_hash: Optional[str] = None


class TrackUpdate(BaseModel):
    title: Optional[str] = None
    artist: Optional[str] = None
    cover_url: Optional[str] = None
    album_id: Optional[str] = None


class TrackOut(TrackBase):
    model_config = ConfigDict(from_attributes=True)
    id: str
    created_at: datetime
    updated_at: datetime
    user_rating: Optional[str] = None
    is_favorite: bool = False
    album: Optional[AlbumOut] = None


class TrackPage(BaseModel):
    items: list[TrackOut]
    total: int
    page: int
    size: int
    pages: int


class PlaylistCreate(BaseModel):
    title: str
    is_public: bool = False


class PlaylistTrackAdd(BaseModel):
    track_id: str
    position: Optional[int] = None


class PlaylistOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)
    id: str
    user_id: int
    title: str
    is_public: bool
    created_at: datetime
    track_count: int = 0


class FavoriteAdd(BaseModel):
    track_id: str


class RateRequest(BaseModel):
    track_id: str
    type: str


class RateResponse(BaseModel):
    track_id: str
    type: Optional[str]


class SearchResults(BaseModel):
    tracks: list[TrackOut]
    albums: list[AlbumOut]
