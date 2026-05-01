package pro.ghosttunes.music.presentation.player;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.repository.MusicRepository;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class PlayerViewModel_Factory implements Factory<PlayerViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<MusicRepository> repositoryProvider;

  public PlayerViewModel_Factory(Provider<Context> contextProvider,
      Provider<MusicRepository> repositoryProvider) {
    this.contextProvider = contextProvider;
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public PlayerViewModel get() {
    return newInstance(contextProvider.get(), repositoryProvider.get());
  }

  public static PlayerViewModel_Factory create(Provider<Context> contextProvider,
      Provider<MusicRepository> repositoryProvider) {
    return new PlayerViewModel_Factory(contextProvider, repositoryProvider);
  }

  public static PlayerViewModel newInstance(Context context, MusicRepository repository) {
    return new PlayerViewModel(context, repository);
  }
}
