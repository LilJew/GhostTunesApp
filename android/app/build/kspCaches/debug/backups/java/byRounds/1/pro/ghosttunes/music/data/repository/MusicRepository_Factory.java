package pro.ghosttunes.music.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.local.PlaylistDao;
import pro.ghosttunes.music.data.local.TrackDao;
import pro.ghosttunes.music.data.remote.MusicApiService;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class MusicRepository_Factory implements Factory<MusicRepository> {
  private final Provider<MusicApiService> apiProvider;

  private final Provider<TrackDao> trackDaoProvider;

  private final Provider<PlaylistDao> playlistDaoProvider;

  public MusicRepository_Factory(Provider<MusicApiService> apiProvider,
      Provider<TrackDao> trackDaoProvider, Provider<PlaylistDao> playlistDaoProvider) {
    this.apiProvider = apiProvider;
    this.trackDaoProvider = trackDaoProvider;
    this.playlistDaoProvider = playlistDaoProvider;
  }

  @Override
  public MusicRepository get() {
    return newInstance(apiProvider.get(), trackDaoProvider.get(), playlistDaoProvider.get());
  }

  public static MusicRepository_Factory create(Provider<MusicApiService> apiProvider,
      Provider<TrackDao> trackDaoProvider, Provider<PlaylistDao> playlistDaoProvider) {
    return new MusicRepository_Factory(apiProvider, trackDaoProvider, playlistDaoProvider);
  }

  public static MusicRepository newInstance(MusicApiService api, TrackDao trackDao,
      PlaylistDao playlistDao) {
    return new MusicRepository(api, trackDao, playlistDao);
  }
}
