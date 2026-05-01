package pro.ghosttunes.music.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MusicDatabase_Impl extends MusicDatabase {
  private volatile TrackDao _trackDao;

  private volatile PlaylistDao _playlistDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `tracks` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `durationSeconds` INTEGER NOT NULL, `fileUrl` TEXT NOT NULL, `coverUrl` TEXT, `albumId` TEXT, `albumTitle` TEXT, `albumCoverUrl` TEXT, `userRating` TEXT, `isFavorite` INTEGER NOT NULL, `lyrics` TEXT, `cachedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playlists` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `isPublic` INTEGER NOT NULL, `trackCount` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `playlist_track_cross` (`playlistId` TEXT NOT NULL, `trackId` TEXT NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`playlistId`, `trackId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'c8a44fd6864c17f1721913e712aeed64')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `tracks`");
        db.execSQL("DROP TABLE IF EXISTS `playlists`");
        db.execSQL("DROP TABLE IF EXISTS `playlist_track_cross`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsTracks = new HashMap<String, TableInfo.Column>(13);
        _columnsTracks.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("artist", new TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("durationSeconds", new TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("fileUrl", new TableInfo.Column("fileUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("coverUrl", new TableInfo.Column("coverUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumId", new TableInfo.Column("albumId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumTitle", new TableInfo.Column("albumTitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("albumCoverUrl", new TableInfo.Column("albumCoverUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("userRating", new TableInfo.Column("userRating", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("isFavorite", new TableInfo.Column("isFavorite", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("lyrics", new TableInfo.Column("lyrics", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTracks.put("cachedAt", new TableInfo.Column("cachedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTracks = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesTracks = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoTracks = new TableInfo("tracks", _columnsTracks, _foreignKeysTracks, _indicesTracks);
        final TableInfo _existingTracks = TableInfo.read(db, "tracks");
        if (!_infoTracks.equals(_existingTracks)) {
          return new RoomOpenHelper.ValidationResult(false, "tracks(pro.ghosttunes.music.data.local.TrackEntity).\n"
                  + " Expected:\n" + _infoTracks + "\n"
                  + " Found:\n" + _existingTracks);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaylists = new HashMap<String, TableInfo.Column>(4);
        _columnsPlaylists.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("isPublic", new TableInfo.Column("isPublic", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylists.put("trackCount", new TableInfo.Column("trackCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaylists = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaylists = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlaylists = new TableInfo("playlists", _columnsPlaylists, _foreignKeysPlaylists, _indicesPlaylists);
        final TableInfo _existingPlaylists = TableInfo.read(db, "playlists");
        if (!_infoPlaylists.equals(_existingPlaylists)) {
          return new RoomOpenHelper.ValidationResult(false, "playlists(pro.ghosttunes.music.data.local.PlaylistEntity).\n"
                  + " Expected:\n" + _infoPlaylists + "\n"
                  + " Found:\n" + _existingPlaylists);
        }
        final HashMap<String, TableInfo.Column> _columnsPlaylistTrackCross = new HashMap<String, TableInfo.Column>(3);
        _columnsPlaylistTrackCross.put("playlistId", new TableInfo.Column("playlistId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistTrackCross.put("trackId", new TableInfo.Column("trackId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlaylistTrackCross.put("position", new TableInfo.Column("position", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlaylistTrackCross = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlaylistTrackCross = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlaylistTrackCross = new TableInfo("playlist_track_cross", _columnsPlaylistTrackCross, _foreignKeysPlaylistTrackCross, _indicesPlaylistTrackCross);
        final TableInfo _existingPlaylistTrackCross = TableInfo.read(db, "playlist_track_cross");
        if (!_infoPlaylistTrackCross.equals(_existingPlaylistTrackCross)) {
          return new RoomOpenHelper.ValidationResult(false, "playlist_track_cross(pro.ghosttunes.music.data.local.PlaylistTrackCrossRef).\n"
                  + " Expected:\n" + _infoPlaylistTrackCross + "\n"
                  + " Found:\n" + _existingPlaylistTrackCross);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "c8a44fd6864c17f1721913e712aeed64", "75c43a15dfe63046b1ea17a33a53c88f");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "tracks","playlists","playlist_track_cross");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `tracks`");
      _db.execSQL("DELETE FROM `playlists`");
      _db.execSQL("DELETE FROM `playlist_track_cross`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TrackDao.class, TrackDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlaylistDao.class, PlaylistDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public TrackDao trackDao() {
    if (_trackDao != null) {
      return _trackDao;
    } else {
      synchronized(this) {
        if(_trackDao == null) {
          _trackDao = new TrackDao_Impl(this);
        }
        return _trackDao;
      }
    }
  }

  @Override
  public PlaylistDao playlistDao() {
    if (_playlistDao != null) {
      return _playlistDao;
    } else {
      synchronized(this) {
        if(_playlistDao == null) {
          _playlistDao = new PlaylistDao_Impl(this);
        }
        return _playlistDao;
      }
    }
  }
}
