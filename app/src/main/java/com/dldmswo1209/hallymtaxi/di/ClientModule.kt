package com.dldmswo1209.hallymtaxi.di

import com.dldmswo1209.hallymtaxi.data.remote.KakaoApi
import com.dldmswo1209.hallymtaxi.data.remote.MainServerApi
import com.dldmswo1209.hallymtaxi.util.Keys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ClientModule {
    @Provides
    @Singleton
    fun provideMainServerClient() : MainServerApi =
        Retrofit.Builder()
            .baseUrl(Keys.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MainServerApi::class.java)

    @Provides
    @Singleton
    fun provideKakaoApiClient() : KakaoApi =
         Retrofit.Builder()
            .baseUrl(Keys.KAKAO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(KakaoApi::class.java)
}