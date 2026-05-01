"""SQLAlchemy ORM models."""

import uuid
from datetime import datetime
from enum import Enum as PyEnum

from sqlalchemy import (
    BigInteger, Boolean, DateTime, Enum, ForeignKey,
    Integer, String, Text, UniqueConstraint, func,
)
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.session import Base


def _uuid() -> str:
    return str(uuid.uuid4())


class Album(Base):
    __tablename__ = "albums"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    artist: Mapped[str] = mapped_column(String(255), nullable=False)
    year: Mapped[int | None] = mapped_column(Integer, nullable=True)
    cover_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    tracks: Mapped[list["Track"]] = relationship("Track", back_populates="album")


class Track(Base):
    __tablename__ = "tracks"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    artist: Mapped[str] = mapped_column(String(255), nullable=False)
    duration_seconds: Mapped[int] = mapped_column(Integer, nullable=False, default=0)
    file_url: Mapped[str] = mapped_column(String(512), nullable=False)
    cover_url: Mapped[str | None] = mapped_column(String(512), nullable=True)
    file_hash: Mapped[str | None] = mapped_column(String(64), nullable=True, unique=True)
    album_id: Mapped[str | None] = mapped_column(
        String(36), ForeignKey("albums.id", ondelete="SET NULL"), nullable=True
    )
    lyrics: Mapped[str | None] = mapped_column(Text, nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    album: Mapped[Album | None] = relationship("Album", back_populates="tracks")
    favorites: Mapped[list["Favorite"]] = relationship("Favorite", back_populates="track")
    ratings: Mapped[list["TrackRating"]] = relationship("TrackRating", back_populates="track")
    playlist_tracks: Mapped[list["PlaylistTrack"]] = relationship("PlaylistTrack", back_populates="track")


class Playlist(Base):
    __tablename__ = "playlists"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=_uuid)
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False, default=1)
    title: Mapped[str] = mapped_column(String(255), nullable=False)
    is_public: Mapped[bool] = mapped_column(Boolean, default=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())
    updated_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now(), onupdate=func.now())

    playlist_tracks: Mapped[list["PlaylistTrack"]] = relationship(
        "PlaylistTrack", back_populates="playlist", cascade="all, delete-orphan"
    )


class PlaylistTrack(Base):
    __tablename__ = "playlist_tracks"
    __table_args__ = (UniqueConstraint("playlist_id", "track_id", name="uq_playlist_track"),)

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    playlist_id: Mapped[str] = mapped_column(String(36), ForeignKey("playlists.id", ondelete="CASCADE"))
    track_id: Mapped[str] = mapped_column(String(36), ForeignKey("tracks.id", ondelete="CASCADE"))
    position: Mapped[int] = mapped_column(Integer, nullable=False, default=0)

    playlist: Mapped[Playlist] = relationship("Playlist", back_populates="playlist_tracks")
    track: Mapped[Track] = relationship("Track", back_populates="playlist_tracks")


class Favorite(Base):
    __tablename__ = "favorites"
    __table_args__ = (UniqueConstraint("user_id", "track_id", name="uq_user_track_fav"),)

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False, default=1)
    track_id: Mapped[str] = mapped_column(String(36), ForeignKey("tracks.id", ondelete="CASCADE"))
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    track: Mapped[Track] = relationship("Track", back_populates="favorites")


class RatingType(PyEnum):
    like = "like"
    dislike = "dislike"


class TrackRating(Base):
    __tablename__ = "track_likes_dislikes"
    __table_args__ = (UniqueConstraint("user_id", "track_id", name="uq_user_track_rating"),)

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(BigInteger, nullable=False, default=1)
    track_id: Mapped[str] = mapped_column(String(36), ForeignKey("tracks.id", ondelete="CASCADE"))
    type: Mapped[RatingType] = mapped_column(Enum(RatingType), nullable=False)
    created_at: Mapped[datetime] = mapped_column(DateTime, server_default=func.now())

    track: Mapped[Track] = relationship("Track", back_populates="ratings")
