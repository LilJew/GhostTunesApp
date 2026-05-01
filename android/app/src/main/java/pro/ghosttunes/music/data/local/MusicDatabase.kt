package pro.ghosttunes.music.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val durationSeconds: Int,
    val fileUrl: String,
    val coverUrl: String?,
    val albumId: String?,
    val albumTitle: String?,
    val albumCoverUrl: String?,
    val userRating: String?,
    val isFavorite: Boolean = false,
    val lyrics: String? = null,
    val cachedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isPublic: Boolean,
    val trackCount: Int,
)

@Entity(
    tableName = "playlist_track_cross",
    primaryKeys = ["playlistId", "trackId"],
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val position: Int,
)

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY cachedAt DESC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE isFavorite = 1")
    fun observeFavorites(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getById(id: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(track: TrackEntity)

    @Query("UPDATE tracks SET userRating = :rating WHERE id = :id")
    suspend fun updateRating(id: String, rating: String?)

    @Query("UPDATE tracks SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: String, isFav: Boolean)

    @Query("DELETE FROM tracks")
    suspend fun clearAll()
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Query("SELECT * FROM playlists WHERE id = :id")
    suspend fun getById(id: String): PlaylistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(playlists: List<PlaylistEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(playlist: PlaylistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<PlaylistTrackCrossRef>)

    @Query("SELECT t.* FROM tracks t INNER JOIN playlist_track_cross ptc ON t.id = ptc.trackId WHERE ptc.playlistId = :playlistId ORDER BY ptc.position")
    fun observePlaylistTracks(playlistId: String): Flow<List<TrackEntity>>

    @Query("DELETE FROM playlist_track_cross WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: String)
}

@Database(
    entities = [TrackEntity::class, PlaylistEntity::class, PlaylistTrackCrossRef::class],
    version = 2,
    exportSchema = false,
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun playlistDao(): PlaylistDao
}