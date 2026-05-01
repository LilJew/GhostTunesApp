package pro.ghosttunes.music.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import pro.ghosttunes.music.data.remote.MusicApiService;
import retrofit2.Retrofit;

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
public final class AppModule_ProvideMusicApiServiceFactory implements Factory<MusicApiService> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideMusicApiServiceFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public MusicApiService get() {
    return provideMusicApiService(retrofitProvider.get());
  }

  public static AppModule_ProvideMusicApiServiceFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideMusicApiServiceFactory(retrofitProvider);
  }

  public static MusicApiService provideMusicApiService(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMusicApiService(retrofit));
  }
}
