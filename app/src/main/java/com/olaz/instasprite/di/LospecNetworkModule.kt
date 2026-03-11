package com.olaz.instasprite.di

import com.olaz.instasprite.data.network.lospec.LospecService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object LospecNetworkModule {
    @Provides
    fun provideBaseUrl(): String = "https://lospec.com/"

    @Provides
    @Singleton
    fun provideRetrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun provideLospecService(retrofit: Retrofit): LospecService = retrofit.create(LospecService::class.java)

}