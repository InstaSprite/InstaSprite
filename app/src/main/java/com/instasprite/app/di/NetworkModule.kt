package com.instasprite.app.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.instasprite.app.data.network.lospec.LospecService
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
    ): OkHttpClient {

        val cache = Cache(File(context.cacheDir, "http_cache"), CACHE_SIZE)

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
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
    fun provideLospecService(@LospecApi retrofit: Retrofit): LospecService =
        retrofit.create(LospecService::class.java)
}