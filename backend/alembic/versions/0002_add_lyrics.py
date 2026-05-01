"""Add lyrics column to tracks table.

Revision ID: 0002_add_lyrics
Revises: 0001_initial
Create Date: 2025-05-01
"""

from alembic import op
import sqlalchemy as sa

revision = "0002_add_lyrics"
down_revision = "0001_initial"
branch_labels = None
depends_on = None


def upgrade():
    op.add_column("tracks", sa.Column("lyrics", sa.Text, nullable=True))


def downgrade():
    op.drop_column("tracks", "lyrics")
