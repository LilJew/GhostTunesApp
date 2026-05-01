"""Application configuration loaded from environment variables."""

from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # Database
    database_url: str = "mysql+aiomysql://root:root@localhost:3306/music_db"

    # Security
    secret_key: str = "dev-secret-key"
    api_key: str = "dev-api-key"

    # Storage
    music_storage_path: str = "/var/music"
    base_url: str = "http://localhost:8000"

    # Admin
    admin_login: str = "admin"
    admin_password: str = "admin"

    # CORS
    allowed_origins: str = "http://localhost:5173,http://localhost:3000"

    @property
    def origins_list(self) -> list[str]:
        return [o.strip() for o in self.allowed_origins.split(",")]

    @property
    def tracks_path(self) -> str:
        return f"{self.music_storage_path}/tracks"

    @property
    def covers_path(self) -> str:
        return f"{self.music_storage_path}/covers"


@lru_cache
def get_settings() -> Settings:
    return Settings()
