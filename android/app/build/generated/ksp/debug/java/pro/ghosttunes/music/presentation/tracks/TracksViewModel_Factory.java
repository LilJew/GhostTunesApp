package pro.ghosttunes.music.presentation.tracks;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.repository.MusicRepository;

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
public final class TracksViewModel_Factory implements Factory<TracksViewModel> {
  private final Provider<MusicRepository> repositoryProvider;

  public TracksViewModel_Factory(Provider<MusicRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public TracksViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static TracksViewModel_Factory create(Provider<MusicRepository> repositoryProvider) {
    return new TracksViewModel_Factory(repositoryProvider);
  }

  public static TracksViewModel newInstance(MusicRepository repository) {
    return new TracksViewModel(repository);
  }
}
