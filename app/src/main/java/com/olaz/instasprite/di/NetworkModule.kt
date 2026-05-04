package com.olaz.instasprite.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.olaz.instasprite.data.network.AuthInterceptor
import com.olaz.instasprite.data.network.api.AlarmApi
import com.olaz.instasprite.data.network.api.AuthApi
import com.olaz.instasprite.data.network.api.CommentApi
import com.olaz.instasprite.data.network.api.FollowApi
import com.olaz.instasprite.data.network.api.MemberPostApi
import com.olaz.instasprite.data.network.api.NotificationApi
import com.olaz.instasprite.data.network.api.PostApi
import com.olaz.instasprite.data.network.api.ProfileApi
import com.olaz.instasprite.data.network.api.SearchApi
import com.olaz.instasprite.data.network.lospec.LospecService
import com.olaz.instasprite.utils.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class LospecApi

@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

    private const val CACHE_SIZE = 10 * 1024 * 1024L
    private const val CONNECT_TIMEOUT_SECONDS = 15L
    private const val READ_TIMEOUT_SECONDS = 30L
    private const val WRITE_TIMEOUT_SECONDS = 30L
    private const val CALL_TIMEOUT_SECONDS = 45L

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {

        val cache = Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE)

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .cache(cache)
            .build()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RetrofitModule {

    val gson: Gson = GsonBuilder().create()

    @Provides
    @Singleton
    @MainApi
    fun provideMainRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @LospecApi
    fun provideLospecRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://lospec.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Provides
    @Singleton
    fun provideAuthApi(@MainApi retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideProfileApi(@MainApi retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun providePostApi(@MainApi retrofit: Retrofit): PostApi =
        retrofit.create(PostApi::class.java)

    @Provides
    @Singleton
    fun provideSearchApi(@MainApi retrofit: Retrofit): SearchApi =
        retrofit.create(SearchApi::class.java)

    @Provides
    @Singleton
    fun provideFollowApi(@MainApi retrofit: Retrofit): FollowApi =
        retrofit.create(FollowApi::class.java)

    @Provides
    @Singleton
    fun provideCommentApi(@MainApi retrofit: Retrofit): CommentApi =
        retrofit.create(CommentApi::class.java)

    @Provides
    @Singleton
    fun provideMemberPostApi(@MainApi retrofit: Retrofit): MemberPostApi =
        retrofit.create(MemberPostApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(@MainApi retrofit: Retrofit): NotificationApi =
        retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideAlarmApi(@MainApi retrofit: Retrofit): AlarmApi =
        retrofit.create(AlarmApi::class.java)


    @Provides
    @Singleton
    fun provideLospecService(@LospecApi retrofit: Retrofit): LospecService =
        retrofit.create(LospecService::class.java)
}