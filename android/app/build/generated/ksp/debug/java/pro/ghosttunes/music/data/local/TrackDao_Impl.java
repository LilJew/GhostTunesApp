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
public final class TrackDao_Impl implements TrackDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TrackEntity> __insertionAdapterOfTrackEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateRating;

  private final SharedSQLiteStatement __preparedStmtOfUpdateFavorite;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public TrackDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTrackEntity = new EntityInsertionAdapter<TrackEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `tracks` (`id`,`title`,`artist`,`durationSeconds`,`fileUrl`,`coverUrl`,`albumId`,`albumTitle`,`albumCoverUrl`,`userRating`,`isFavorite`,`lyrics`,`cachedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TrackEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getTitle());
        statement.bindString(3, entity.getArtist());
        statement.bindLong(4, entity.getDurationSeconds());
        statement.bindString(5, entity.getFileUrl());
        if (entity.getCoverUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCoverUrl());
        }
        if (entity.getAlbumId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getAlbumId());
        }
        if (entity.getAlbumTitle() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAlbumTitle());
        }
        if (entity.getAlbumCoverUrl() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getAlbumCoverUrl());
        }
        if (entity.getUserRating() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getUserRating());
        }
        final int _tmp = entity.isFavorite() ? 1 : 0;
        statement.bindLong(11, _tmp);
        if (entity.getLyrics() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getLyrics());
        }
        statement.bindLong(13, entity.getCachedAt());
      }
    };
    this.__preparedStmtOfUpdateRating = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tracks SET userRating = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateFavorite = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE tracks SET isFavorite = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM tracks";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<TrackEntity> tracks,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrackEntity.insert(tracks);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insert(final TrackEntity track, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTrackEntity.insert(track);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRating(final String id, final String rating,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateRating.acquire();
        int _argIndex = 1;
        if (rating == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, rating);
        }
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateRating.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateFavorite(final String id, final boolean isFav,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateFavorite.acquire();
        int _argIndex = 1;
        final int _tmp = isFav ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateFavorite.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<TrackEntity>> observeAll() {
    final String _sql = "SELECT * FROM tracks ORDER BY cachedAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<TrackEntity>>() {
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

  @Override
  public Flow<List<TrackEntity>> observeFavorites() {
    final String _sql = "SELECT * FROM tracks WHERE isFavorite = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"tracks"}, new Callable<List<TrackEntity>>() {
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

  @Override
  public Object getById(final String id, final Continuation<? super TrackEntity> $completion) {
    final String _sql = "SELECT * FROM tracks WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TrackEntity>() {
      @Override
      @Nullable
      public TrackEntity call() throws Exception {
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
          final TrackEntity _result;
          if (_cursor.moveToFirst()) {
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
            _result = new TrackEntity(_tmpId,_tmpTitle,_tmpArtist,_tmpDurationSeconds,_tmpFileUrl,_tmpCoverUrl,_tmpAlbumId,_tmpAlbumTitle,_tmpAlbumCoverUrl,_tmpUserRating,_tmpIsFavorite,_tmpLyrics,_tmpCachedAt);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
