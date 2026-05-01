package pro.ghosttunes.music.domain.model

data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val fileUrl: String,
    val coverUrl: String?,
    val albumId: String?,
    val albumTitle: String?,
    val userRating: String?,
    val isFavorite: Boolean = false,
) {
    val durationFormatted: String
        get() {
            val m = durationSeconds / 60
            val s = durationSeconds % 60
            return "%d:%02d".format(m, s)
        }
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val year: Int?,
    val coverUrl: String?,
)

data class Playlist(
    val id: String,
    val title: String,
    val isPublic: Boolean,
    val trackCount: Int,
)

data class Artist(
    val name: String,
    val tracks: List<Track>,
    val albums: List<Album>,
    val cover: String?,
)