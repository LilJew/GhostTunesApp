"""Initial schema.

Revision ID: 0001_initial
Revises: 
Create Date: 2024-01-01
"""

from alembic import op
import sqlalchemy as sa

revision = "0001_initial"
down_revision = None
branch_labels = None
depends_on = None


def upgrade():
    op.create_table("albums",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("artist", sa.String(255), nullable=False),
        sa.Column("year", sa.Integer, nullable=True),
        sa.Column("cover_url", sa.String(512), nullable=True),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime, server_default=sa.func.now()),
    )
    op.create_table("tracks",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("artist", sa.String(255), nullable=False),
        sa.Column("duration_seconds", sa.Integer, nullable=False, server_default="0"),
        sa.Column("file_url", sa.String(512), nullable=False),
        sa.Column("cover_url", sa.String(512), nullable=True),
        sa.Column("file_hash", sa.String(64), nullable=True, unique=True),
        sa.Column("album_id", sa.String(36), sa.ForeignKey("albums.id", ondelete="SET NULL"), nullable=True),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime, server_default=sa.func.now()),
    )
    op.create_table("playlists",
        sa.Column("id", sa.String(36), primary_key=True),
        sa.Column("user_id", sa.BigInteger, nullable=False, server_default="1"),
        sa.Column("title", sa.String(255), nullable=False),
        sa.Column("is_public", sa.Boolean, server_default="0"),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
        sa.Column("updated_at", sa.DateTime, server_default=sa.func.now()),
    )
    op.create_table("playlist_tracks",
        sa.Column("id", sa.Integer, primary_key=True, autoincrement=True),
        sa.Column("playlist_id", sa.String(36), sa.ForeignKey("playlists.id", ondelete="CASCADE")),
        sa.Column("track_id", sa.String(36), sa.ForeignKey("tracks.id", ondelete="CASCADE")),
        sa.Column("position", sa.Integer, nullable=False, server_default="0"),
        sa.UniqueConstraint("playlist_id", "track_id", name="uq_playlist_track"),
    )
    op.create_table("favorites",
        sa.Column("id", sa.Integer, primary_key=True, autoincrement=True),
        sa.Column("user_id", sa.BigInteger, nullable=False, server_default="1"),
        sa.Column("track_id", sa.String(36), sa.ForeignKey("tracks.id", ondelete="CASCADE")),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
        sa.UniqueConstraint("user_id", "track_id", name="uq_user_track_fav"),
    )
    op.create_table("track_likes_dislikes",
        sa.Column("id", sa.Integer, primary_key=True, autoincrement=True),
        sa.Column("user_id", sa.BigInteger, nullable=False, server_default="1"),
        sa.Column("track_id", sa.String(36), sa.ForeignKey("tracks.id", ondelete="CASCADE")),
        sa.Column("type", sa.Enum("like", "dislike", name="ratingtype"), nullable=False),
        sa.Column("created_at", sa.DateTime, server_default=sa.func.now()),
        sa.UniqueConstraint("user_id", "track_id", name="uq_user_track_rating"),
    )
    op.create_index("ix_tracks_artist", "tracks", ["artist"])
    op.create_index("ix_tracks_album_id", "tracks", ["album_id"])
    op.create_index("ix_favorites_user_id", "favorites", ["user_id"])


def downgrade():
    op.drop_table("track_likes_dislikes")
    op.drop_table("favorites")
    op.drop_table("playlist_tracks")
    op.drop_table("playlists")
    op.drop_table("tracks")
    op.drop_table("albums")
