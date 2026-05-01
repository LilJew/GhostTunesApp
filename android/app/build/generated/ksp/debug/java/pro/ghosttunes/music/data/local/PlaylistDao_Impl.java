package pro.ghosttunes.music.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlaylistDao_Impl implements PlaylistDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlaylistEntity> __insertionAdapterOfPlaylistEntity;

  private final EntityInsertionAdapter<PlaylistTrackCrossRef> __insertionAdapterOfPlaylistTrackCrossRef;

  private final SharedSQLiteStatement __preparedStmtOfClearPlaylistTracks;

  public PlaylistDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlaylistEntity = new EntityInsertionAdapter<PlaylistEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `playlists` (`id`,`title`,`isPublic`,`trackCount`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        final int _tmp = entity.isPublic() ? 1 : 0;
        statement.bindLong(3, _tmp);
        statement.bindLong(4, entity.getTrackCount());
      }
    };
    this.__insertionAdapterOfPlaylistTrackCrossRef = new EntityInsertionAdapter<PlaylistTrackCrossRef>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `playlist_track_cross` (`playlistId`,`trackId`,`position`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PlaylistTrackCrossRef entity) {
        statement.bindString(1, entity.getPlaylistId());
        statement.bindString(2, entity.getTrackId());
        statement.bindLong(3, entity.getPosition());
      }
    };
    this.__preparedStmtOfClearPlaylistTracks = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM playlist_track_cross WHERE playlistId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<PlaylistEntity> playlists,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistEntity.insert(playlists);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final PlaylistEntity playlist,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistEntity.insert(playlist);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCrossRefs(final List<PlaylistTrackCrossRef> refs,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPlaylistTrackCrossRef.insert(refs);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearPlaylistTracks(final String playlistId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearPlaylistTracks.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, playlistId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearPlaylistTracks.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PlaylistEntity>> observeAll() {
    final String _sql = "SELECT * FROM playlists";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"playlists"}, new Callable<List<PlaylistEntity>>() {
      @Override
      @NonNull
      public List<PlaylistEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final int _cursorIndexOfTrackCount = CursorUtil.getColumnIndexOrThrow(_cursor, "trackCount");
          final List<PlaylistEntity> _result = new ArrayList<PlaylistEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PlaylistEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            final int _tmpTrackCount;
            _tmpTrackCount = _cursor.getInt(_cursorIndexOfTrackCount);
            _item = new PlaylistEntity(_tmpId,_tmpTitle,_tmpIsPublic,_tmpTrackCount);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getById(final String id, final Continuation<? super PlaylistEntity> $completion) {
    final String _sql = "SELECT * FROM playlists WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PlaylistEntity>() {
      @Override
      @Nullable
      public PlaylistEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfIsPublic = CursorUtil.getColumnIndexOrThrow(_cursor, "isPublic");
          final int _cursorIndexOfTrackCount = CursorUtil.getColumnIndexOrThrow(_cursor, "trackCount");
          final PlaylistEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final boolean _tmpIsPublic;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsPublic);
            _tmpIsPublic = _tmp != 0;
            final int _tmpTrackCount;
            _tmpTrackCount = _cursor.getInt(_cursorIndexOfTrackCount);
            _result = new PlaylistEntity(_tmpId,_tmpTitle,_tmpIsPublic,_tmpTrackCount);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TrackEntity>> observePlaylistTracks(final String playlistId) {
    final String _sql = "SELECT t.* FROM tracks t INNER JOIN playlist_track_cross ptc ON t.id = ptc.trackId WHERE ptc.playlistId = ? ORDER BY ptc.position";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, playlistId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks",
        "playlist_track_cross"}, new Callable<List<TrackEntity>>() {
      @Override
      @NonNull
      public List<TrackEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfArtist = CursorUtil.getColumnIndexOrThrow(_cursor, "artist");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfFileUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "fileUrl");
          final int _cursorIndexOfCoverUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "coverUrl");
          final int _cursorIndexOfAlbumId = CursorUtil.getColumnIndexOrThrow(_cursor, "albumId");
          final int _cursorIndexOfAlbumTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "albumTitle");
          final int _cursorIndexOfAlbumCoverUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "albumCoverUrl");
          final int _cursorIndexOfUserRating = CursorUtil.getColumnIndexOrThrow(_cursor, "userRating");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfLyrics = CursorUtil.getColumnIndexOrThrow(_cursor, "lyrics");
          final int _cursorIndexOfCachedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "cachedAt");
          final List<TrackEntity> _result = new ArrayList<TrackEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final TrackEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpArtist;
            _tmpArtist = _cursor.getString(_cursorIndexOfArtist);
            final int _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getInt(_cursorIndexOfDurationSeconds);
            final String _tmpFileUrl;
            _tmpFileUrl = _cursor.getString(_cursorIndexOfFileUrl);
            final String _tmpCoverUrl;
            if (_cursor.isNull(_cursorIndexOfCoverUrl)) {
              _tmpCoverUrl = null;
            } else {
              _tmpCoverUrl = _cursor.getString(_cursorIndexOfCoverUrl);
            }
            final String _tmpAlbumId;
            if (_cursor.isNull(_cursorIndexOfAlbumId)) {
              _tmpAlbumId = null;
            } else {
              _tmpAlbumId = _cursor.getString(_cursorIndexOfAlbumId);
            }
            final String _tmpAlbumTitle;
            if (_cursor.isNull(_cursorIndexOfAlbumTitle)) {
              _tmpAlbumTitle = null;
            } else {
              _tmpAlbumTitle = _cursor.getString(_cursorIndexOfAlbumTitle);
            }
            final String _tmpAlbumCoverUrl;
            if (_cursor.isNull(_cursorIndexOfAlbumCoverUrl)) {
              _tmpAlbumCoverUrl = null;
            } else {
              _tmpAlbumCoverUrl = _cursor.getString(_cursorIndexOfAlbumCoverUrl);
            }
            final String _tmpUserRating;
            if (_cursor.isNull(_cursorIndexOfUserRating)) {
              _tmpUserRating = null;
            } else {
              _tmpUserRating = _cursor.getString(_cursorIndexOfUserRating);
            }
            final boolean _tmpIsFavorite;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp != 0;
            final String _tmpLyrics;
            if (_cursor.isNull(_cursorIndexOfLyrics)) {
              _tmpLyrics = null;
            } else {
              _tmpLyrics = _cursor.getString(_cursorIndexOfLyrics);
            }
            final long _tmpCachedAt;
            _tmpCachedAt = _cursor.getLong(_cursorIndexOfCachedAt);
            _item = new TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpDurationSeconds,_tmpFileUrl,_tmpCoverUrl,_tmpAlbumId,_tmpAlbumTitle,_tmpAlbumCoverUrl,_tmpUserRating,_tmpIsFavorite,_tmpLyrics,_tmpCachedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
