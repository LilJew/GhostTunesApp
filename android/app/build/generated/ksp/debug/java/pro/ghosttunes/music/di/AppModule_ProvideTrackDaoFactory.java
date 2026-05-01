package pro.ghosttunes.music.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.local.MusicDatabase;
import pro.ghosttunes.music.data.local.TrackDao;

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
public final class AppModule_ProvideTrackDaoFactory implements Factory<TrackDao> {
  private final Provider<MusicDatabase> dbProvider;

  public AppModule_ProvideTrackDaoFactory(Provider<MusicDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public TrackDao get() {
    return provideTrackDao(dbProvider.get());
  }

  public static AppModule_ProvideTrackDaoFactory create(Provider<MusicDatabase> dbProvider) {
    return new AppModule_ProvideTrackDaoFactory(dbProvider);
  }

  public static TrackDao provideTrackDao(MusicDatabase db) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideTrackDao(db));
  }
}
