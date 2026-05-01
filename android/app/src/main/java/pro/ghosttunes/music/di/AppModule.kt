package pro.ghosttunes.music.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import pro.ghosttunes.music.BuildConfig
import pro.ghosttunes.music.data.local.MusicDatabase
import pro.ghosttunes.music.data.local.PlaylistDao
import pro.ghosttunes.music.data.local.TrackDao
import pro.ghosttunes.music.data.remote.MusicApiService
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG)
                        HttpLoggingInterceptor.Level.BASIC
                    else HttpLoggingInterceptor.Level.NONE
                }
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMusicApiService(retrofit: Retrofit): MusicApiService =
        retrofit.create(MusicApiService::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MusicDatabase =
        Room.databaseBuilder(context, MusicDatabase::class.java, "music.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideTrackDao(db: MusicDatabase): TrackDao = db.trackDao()

    @Provides
    fun providePlaylistDao(db: MusicDatabase): PlaylistDao = db.playlistDao()
}
