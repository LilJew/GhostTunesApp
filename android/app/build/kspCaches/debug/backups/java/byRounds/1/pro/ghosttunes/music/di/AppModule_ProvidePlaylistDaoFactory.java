package pro.ghosttunes.music.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.local.MusicDatabase;
import pro.ghosttunes.music.data.local.PlaylistDao;

@ScopeMetadata
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
public final class AppModule_ProvidePlaylistDaoFactory implements Factory<PlaylistDao> {
  private final Provider<MusicDatabase> dbProvider;

  public AppModule_ProvidePlaylistDaoFactory(Provider<MusicDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlaylistDao get() {
    return providePlaylistDao(dbProvider.get());
  }

  public static AppModule_ProvidePlaylistDaoFactory create(Provider<MusicDatabase> dbProvider) {
    return new AppModule_ProvidePlaylistDaoFactory(dbProvider);
  }

  public static PlaylistDao providePlaylistDao(MusicDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.providePlaylistDao(db));
  }
}
